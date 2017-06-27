/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.nimingban.widget.content

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.hippo.nimingban.R
import com.hippo.nimingban.util.dp2pix
import com.hippo.nimingban.util.explain
import com.hippo.nimingban.util.explainVividly
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.android.synthetic.main.widget_content_layout.view.*

/*
 * Created by Hippo on 6/5/2017.
 */

class ContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ContentUi {

  override var logic: ContentLogic? = null
  var extension: Extension? = null

  val refreshLayout by lazy { refresh_layout!! }
  val recyclerView by lazy { recycler_view!! }
  val tipView by lazy { tip_view!! }
  val progressView by lazy { progress_view!! }

  val aLittleDistance: Int

  init {
    LayoutInflater.from(context).inflate(R.layout.widget_content_layout, this)

    refreshLayout.setHeaderColorSchemeResources(
        R.color.color_scheme_1,
        R.color.color_scheme_2,
        R.color.color_scheme_3,
        R.color.color_scheme_4,
        R.color.color_scheme_5,
        R.color.color_scheme_6
    )
    refreshLayout.setFooterColorSchemeResources(
        R.color.color_scheme_1,
        R.color.color_scheme_2,
        R.color.color_scheme_3,
        R.color.color_scheme_4,
        R.color.color_scheme_5,
        R.color.color_scheme_6
    )
    refreshLayout.setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
      override fun onHeaderRefresh() { logic?.onRefreshHeader() }
      override fun onFooterRefresh() { logic?.onRefreshFooter() }
    })

    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        val logic = this@ContentLayout.logic ?: return
        if (!refreshLayout.isRefreshing && refreshLayout.isAlmostBottom &&
            !logic.isMaxReached()) {
          refreshLayout.isFooterRefreshing = true
          logic.onRefreshFooter()
        }
      }
    })

    // TODO throttleFirst
    tipView.setOnClickListener { logic?.onClickTip() }

    aLittleDistance = 48.dp2pix(context)
  }

  override fun showContent() {
    refreshLayout.visibility = View.VISIBLE
    tipView.visibility = View.GONE
    progressView.visibility = View.GONE
  }

  override fun showTip(t: Throwable) {
    refreshLayout.visibility = View.GONE
    tipView.visibility = View.VISIBLE
    progressView.visibility = View.GONE

    val drawable = explainVividly(context, t)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    tipView.setCompoundDrawables(null, drawable, null, null)
    tipView.text = explain(t)
  }

  override fun showProgressBar() {
    refreshLayout.visibility = View.GONE
    tipView.visibility = View.GONE
    progressView.visibility = View.VISIBLE
  }

  override fun showMessage(t: Throwable) {
    extension?.let { it.showMessage(explain(t)) }
  }

  override fun stopRefreshing() {
    refreshLayout.isHeaderRefreshing = false
    refreshLayout.isFooterRefreshing = false
  }

  override fun setHeaderRefreshing() {
    refreshLayout.isHeaderRefreshing = true
  }

  override fun setFooterRefreshing() {
    refreshLayout.isFooterRefreshing = true
  }

  override fun scrollToPosition(position: Int) {
    recyclerView.scrollToPosition(position)
  }

  override fun scrollDownALittle() {
    if (refreshLayout.isAlmostBottom) {
      recyclerView.smoothScrollBy(0, aLittleDistance)
    }
  }

  override fun notifyDataSetChanged() {
    recyclerView.adapter?.notifyDataSetChanged()
  }

  override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
    recyclerView.adapter?.notifyItemRangeInserted(positionStart, itemCount)
  }

  override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
    recyclerView.adapter?.notifyItemRangeRemoved(positionStart, itemCount)
  }

  override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
    recyclerView.adapter?.notifyItemRangeChanged(positionStart, itemCount)
  }

  /**
   * `ContentLayout` can't do all UI jobs. It needs a `Extension` to give a hand.
   */
  interface Extension {

    /**
     * Show a non-interrupting message. Toast? SnackBar? OK.
     */
    fun showMessage(message: String)
  }
}
