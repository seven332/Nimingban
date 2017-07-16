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

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbPaper
import com.hippo.nimingban.component.adapter.AlertAdapter
import com.hippo.nimingban.component.adapter.AlertHolder
import com.hippo.nimingban.util.drawable
import com.hippo.nimingban.util.find
import com.hippo.nimingban.util.prettyTime
import com.hippo.nimingban.widget.content.ContentLayout
import com.hippo.nimingban.widget.nmb.NmbReplayMarquee
import com.hippo.nimingban.widget.nmb.NmbThumb

/*
 * Created by Hippo on 2017/7/14.
 */

class ThreadsPaper(
    private val logic: ThreadsLogic
) : NmbPaper<ThreadsUi>(logic), ThreadsUi, ContentLayout.Extension {

  private lateinit var contentLayout: ContentLayout
  private lateinit var recyclerView: RecyclerView

  private lateinit var adapter: ThreadAdapter

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)

    view = inflater.inflate(R.layout.paper_threads, container, false)
    contentLayout = view.find(R.id.content_layout)
    recyclerView = view.find(R.id.recycler_view)

    adapter = ThreadAdapter()
    logic.initAdapter(adapter)

    logic.initContentLayout(contentLayout)

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(context)
  }

  override fun onDestroy() {
    super.onDestroy()

    logic.termContentLayout(contentLayout)

    recyclerView.adapter = null
    recyclerView.layoutManager = null
  }

  override fun showMessage(message: String) {
    logic.showMessage(message)
  }


  inner class ThreadHolder(itemView: View) : AlertHolder(itemView) {

    val user = itemView.findViewById(R.id.user) as TextView
    val id = itemView.findViewById(R.id.id) as TextView
    val date = itemView.findViewById(R.id.date) as TextView
    val content = itemView.findViewById(R.id.content) as TextView
    val thumb = itemView.findViewById(R.id.thumb) as NmbThumb
    val replies = itemView.findViewById(R.id.replies) as NmbReplayMarquee
    val replyCount = itemView.findViewById(R.id.reply_count) as TextView
    val bottom = itemView.findViewById(R.id.bottom)!!

    val item: Thread? get() = adapterPosition.takeIf { it in 0 until adapter.size }?.let { adapter[it] }

    init {
      val drawable = context.drawable(R.drawable.comment_multiple_outline_secondary_x16)
      drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
      replyCount.setCompoundDrawables(drawable, null, null, null)

      itemView.setOnClickListener { item?.let { logic.onClickThread(it) } }
      thumb.setOnClickListener { item?.let { logic.onClickThumb(it.toReply()) } }
    }

    override fun onResume() {
      super.onResume()
      replies.start()
      thumb.start()
    }

    override fun onPause() {
      super.onPause()
      replies.stop()
      thumb.stop()
    }
  }


  inner class ThreadAdapter : AlertAdapter<Thread, ThreadHolder>(lifecycle) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        ThreadHolder(inflater.inflate(R.layout.threads_item, parent, false))

    private fun Thread.idText(): String {
      val forum = this.forum
      if (forum != null && !forum.isEmpty() && logic.isVirtualForum()) {
        return forum
      } else {
        return displayId
      }
    }

    override fun onBindViewHolder(holder: ThreadHolder, position: Int) {
      val thread = get(position)
      holder.user.text = thread.displayUser
      holder.id.text = thread.idText()
      holder.date.text = thread.date.prettyTime()
      holder.content.text = thread.displayContent
      holder.thumb.loadThumb(thread.image)
      holder.replies.replies = thread.replies
      holder.replyCount.text = thread.replyCount.toString()

      val showImage = thread.image.isNullOrEmpty().not()
      val showReplies = thread.replies.isNotEmpty()
      val lp = holder.bottom.layoutParams as RelativeLayout.LayoutParams
      if (showImage && !showReplies) {
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.thumb)
        lp.addRule(RelativeLayout.BELOW, 0)
        holder.bottom.requestLayout()
      } else if (showImage && showReplies) {
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0)
        lp.addRule(RelativeLayout.BELOW, R.id.thumb)
        holder.bottom.requestLayout()
      } else {
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0)
        lp.addRule(RelativeLayout.BELOW, R.id.content)
        holder.bottom.requestLayout()
      }

      super.onBindViewHolder(holder, position)
    }
  }
}
