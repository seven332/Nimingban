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

package com.hippo.nimingban.component.paper

import android.graphics.Typeface
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbUi
import com.hippo.nimingban.component.adapter.AlertAdapter
import com.hippo.nimingban.component.adapter.AlertHolder
import com.hippo.nimingban.util.attrColor
import com.hippo.nimingban.util.color
import com.hippo.nimingban.util.dp2pix
import com.hippo.nimingban.util.find
import com.hippo.nimingban.util.prettyTime
import com.hippo.nimingban.widget.LinkifyTextView
import com.hippo.nimingban.widget.content.ContentLayout
import com.hippo.nimingban.widget.nmb.NmbThumb
import com.hippo.recyclerview.addons.LinearDividerItemDecoration

/*
 * Created by Hippo on 6/20/2017.
 */

class RepliesUi(
    val logic: RepliesLogic,
    val activity: NmbActivity,
    val inflater: LayoutInflater,
    container: ViewGroup
) : NmbUi(), ContentLayout.Extension {

  override val view: View
  private val context = inflater.context
  private val adapter: RepliesAdapter
  private val layoutManager: LinearLayoutManager
  private val contentLayout: ContentLayout
  private val recyclerView: RecyclerView

  private val defaultUserColor = context.attrColor(android.R.attr.textColorSecondary)

  init {
    view = inflater.inflate(R.layout.ui_replies, container, false)

    adapter = RepliesAdapter()
    logic.initializeAdapter(adapter)

    contentLayout = view.find(R.id.content_layout)
    contentLayout.extension = this
    logic.initializeContentLayout(contentLayout)

    layoutManager = LinearLayoutManager(context)

    recyclerView = view.find(R.id.recycler_view)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = layoutManager
    recyclerView.itemAnimator = null
    val itemDecoration = LinearDividerItemDecoration(
        LinearDividerItemDecoration.VERTICAL,
        context.attrColor(R.attr.dividerColor),
        1.dp2pix(context))
    itemDecoration.setShowLastDivider(true)
    recyclerView.addItemDecoration(itemDecoration)

    logic.repliesUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    logic.repliesUi = null
    logic.terminateAdapter(adapter)
    logic.terminateContentLayout(contentLayout)
    recyclerView.adapter = null
    recyclerView.layoutManager = null
  }

  override fun showMessage(message: String) {
    activity.snack(message)
  }

  fun findFirstVisibleItemPosition() = layoutManager.findFirstVisibleItemPosition()

  fun onUpdateThread(thread: Thread) {
    thread.user?.let {
      logic.registerUserColor(it, context.color(R.color.green_ntr))
    }
  }


  private inner class RepliesHolder(itemView: View) : AlertHolder(itemView) {

    val user = itemView.findViewById(R.id.user) as TextView
    val id = itemView.findViewById(R.id.id) as TextView
    val date = itemView.findViewById(R.id.date) as TextView
    val content = itemView.findViewById(R.id.content) as LinkifyTextView
    val thumb = itemView.findViewById(R.id.thumb) as NmbThumb

    val item: Reply? get() = adapterPosition.takeIf { it in 0 until adapter.size }?.let { adapter[it] }

    init {
      itemView.setOnClickListener {
        val span = content.currentSpan
        if (span != null) {
          logic.onClickSpan(span)
        } else {
          item?.let { logic.onClickReply(it) }
        }
      }
      thumb.setOnClickListener { item?.let { logic.onClickThumb(it) } }
    }

    override fun onResume() {
      super.onResume()
      thumb.start()
    }

    override fun onPause() {
      super.onPause()
      thumb.stop()
    }
  }


  private inner class RepliesAdapter : AlertAdapter<Reply, RepliesHolder>(lifecycle) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        RepliesHolder(inflater.inflate(R.layout.replies_item, parent, false))

    override fun onBindViewHolder(holder: RepliesHolder, position: Int) {
      val thread = get(position)

      val userColor = thread.user?.let { logic.getUserColor(it) }
      if (userColor != null) {
        holder.user.setTextColor(userColor)
        holder.user.setTypeface(null, Typeface.BOLD)
      } else {
        holder.user.setTextColor(defaultUserColor)
        holder.user.setTypeface(null, 0)
      }

      holder.user.text = thread.displayedUser
      holder.id.text = thread.displayedId
      holder.date.text = thread.date.prettyTime(inflater.context)
      holder.content.text = thread.displayedContent
      holder.thumb.loadThumb(thread.image)

      super.onBindViewHolder(holder, position)
    }
  }
}
