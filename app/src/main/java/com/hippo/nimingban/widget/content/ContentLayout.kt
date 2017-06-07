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
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.hippo.nimingban.R
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.android.synthetic.main.widget_content_layout.view.*

/*
 * Created by Hippo on 6/5/2017.
 */

class ContentLayout : FrameLayout, ContentContract.View {

  override var presenter: ContentContract.Presenter? = null
  var extension: Extension? = null

  constructor(context: Context) : super(context) { init(context) }
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init(context) }
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

  private fun init(context: Context) {
    LayoutInflater.from(context).inflate(R.layout.widget_content_layout, this)

    val refreshLayout = refresh_layout
    val recyclerView = recycler_view

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
      override fun onHeaderRefresh() { presenter?.onRefreshHeader() }
      override fun onFooterRefresh() { presenter?.onRefreshFooter() }
    })

    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        if (!refreshLayout.isRefreshing && refreshLayout.isAlmostBottom &&
            presenter?.isMaxReached() ?: false) {
          refreshLayout.isFooterRefreshing = true
          presenter?.onRefreshFooter()
        }
      }
    })
  }

  override fun showContent() {
    refresh_layout.visibility = View.GONE
    tip.visibility = View.GONE



  }

  override fun showTip(t: Throwable) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showProgressBar() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showMessage(t: Throwable) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun stopRefreshing() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun setHeaderRefreshing() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun setFooterRefreshing() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun scrollToPosition(position: Int) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun scrollDownALittle() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun notifyDataSetChanged() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  /**
   * Stores tip icon and tip text.
   */
  data class TipInfo(val icon: Drawable?, val text: String?)

  /**
   * `ContentLayout` can't do all UI jobs. It needs a `Extension` to give a hand.
   */
  interface Extension {
    /**
     * Gets tip to represent the `Throwable`.
     *
     * [ContentData.NOT_FOUND_EXCEPTION] for no data.
     *
     * [ContentData.TAP_TO_LOAD_EXCEPTION] for no data but can continue loading.
     */
    fun getTipFromThrowable(e: Throwable): TipInfo

    /**
     * Show a non-interrupting message. Toast? SnackBar? OK.
     */
    fun showMessage(message: String)
  }
}
