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
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbUi
import com.hippo.nimingban.component.adapter.AlertAdapter
import com.hippo.nimingban.component.adapter.AlertHolder
import com.hippo.nimingban.util.drawable
import com.hippo.nimingban.util.find
import com.hippo.nimingban.util.prettyTime
import com.hippo.nimingban.widget.content.ContentLayout
import com.hippo.nimingban.widget.nmb.NmbReplayMarquee
import com.hippo.nimingban.widget.nmb.NmbThumb

/*
 * Created by Hippo on 6/19/2017.
 */

class ThreadsUi(
    val logic: ThreadsLogic,
    val activity: NmbActivity,
    val inflater: LayoutInflater,
    container: ViewGroup
) : NmbUi(), ContentLayout.Extension {

  override val view: View
  private val context = inflater.context
  private val adapter: ThreadAdapter
  private val layoutManager: LinearLayoutManager
  private val contentLayout: ContentLayout
  private val recyclerView: RecyclerView

  init {
    view = inflater.inflate(R.layout.ui_threads, container, false)

    adapter = ThreadAdapter()
    logic.initializeAdapter(adapter)

    contentLayout = view.find(R.id.content_layout)
    contentLayout.extension = this
    logic.initializeContentLayout(contentLayout)

    layoutManager = LinearLayoutManager(context)

    recyclerView = view.find(R.id.recycler_view)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = layoutManager
    recyclerView.itemAnimator = null

    logic.threadsUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    logic.threadsUi = null
    logic.terminateAdapter(adapter)
    logic.terminateContentLayout(contentLayout)
    recyclerView.adapter = null
    recyclerView.layoutManager = null
  }

  override fun showMessage(message: String) {
    activity.snack(message)
  }

  fun findFirstVisibleItemPosition() = layoutManager.findFirstVisibleItemPosition()


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
      if (forum != null && !forum.isEmpty() && logic.forum?.isVirtual() ?: false) {
        return forum
      } else {
        return displayId
      }
    }

    override fun onBindViewHolder(holder: ThreadHolder, position: Int) {
      val thread = get(position)
      holder.user.text = thread.displayUser
      holder.id.text = thread.idText()
      holder.date.text = thread.date.prettyTime(context)
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
