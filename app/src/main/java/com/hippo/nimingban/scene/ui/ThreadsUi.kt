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

package com.hippo.nimingban.scene.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.hippo.easyrecyclerview.EasyRecyclerView
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.AlertAdapter
import com.hippo.nimingban.component.AlertHolder
import com.hippo.nimingban.util.prettyTime
import com.hippo.nimingban.widget.content.ContentLayout
import com.hippo.nimingban.widget.nmb.NmbReplayMarquee
import com.hippo.nimingban.widget.nmb.NmbThumb

/*
 * Created by Hippo on 6/12/2017.
 */

class ThreadsUi(
    val logic: ThreadsLogic,
    context: android.content.Context,
    activity: NmbActivity
) : NmbUi(context, activity), ContentLayout.Extension {

  private var adapter: ThreadAdapter? = null
  private var contentLayout: ContentLayout? = null
  private var recyclerView: EasyRecyclerView? = null

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): android.view.View {
    val view = inflater.inflate(R.layout.ui_threads, container, false)

    val adapter = ThreadAdapter(inflater, lifecycle)
    logic.initializeAdapter(adapter)

    val contentLayout = view.findViewById(R.id.content_layout) as ContentLayout
    contentLayout.extension = this
    logic.initializeContentLayout(contentLayout)

    val recyclerView = view.findViewById(R.id.recycler_view) as EasyRecyclerView
    recyclerView.adapter = adapter
    recyclerView.layoutManager = android.support.v7.widget.LinearLayoutManager(context)
    recyclerView.setOnItemClickListener { _, holder ->
      logic.onClickThread(this@ThreadsUi.adapter!!.get(holder.adapterPosition))
    }

    this.adapter = adapter
    this.contentLayout = contentLayout
    this.recyclerView = recyclerView

    return view
  }

  override fun onDestroy() {
    super.onDestroy()
    adapter?.run { logic.terminateAdapter(this) }
    contentLayout?.run { logic.terminateContentLayout(this) }
    recyclerView?.adapter = null
    recyclerView?.layoutManager = null
    recyclerView?.setOnItemClickListener(null)
  }

  override fun showMessage(message: String) {
    activity.snack(message)
  }


  class ThreadHolder(itemView: android.view.View) : AlertHolder(itemView) {
    val user = itemView.findViewById(R.id.user) as TextView
    val id = itemView.findViewById(R.id.id) as TextView
    val date = itemView.findViewById(R.id.date) as TextView
    val content = itemView.findViewById(R.id.content) as TextView
    val thumb = itemView.findViewById(R.id.thumb) as NmbThumb
    val replies = itemView.findViewById(R.id.replies) as NmbReplayMarquee
    val replyCount = itemView.findViewById(R.id.reply_count) as TextView
    val bottom = itemView.findViewById(R.id.bottom)!!

    init {
      val drawable = android.support.v7.content.res.AppCompatResources.getDrawable(itemView.context, R.drawable.comment_multiple_outline_secondary_x16)!!
      drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
      replyCount.setCompoundDrawables(drawable, null, null, null)
    }

    override fun onResume() {
      super.onResume()
      replies.start()
    }

    override fun onPause() {
      super.onPause()
      replies.stop()
    }
  }


  class ThreadAdapter(val inflater: android.view.LayoutInflater, lifecycle: io.reactivex.Observable<Int>)
    : AlertAdapter<Thread, ThreadHolder>(lifecycle) {

    override fun onCreateViewHolder2(parent: android.view.ViewGroup, viewType: Int) =
        ThreadHolder(inflater.inflate(R.layout.threads_item, parent, false))

    override fun onBindViewHolder(holder: ThreadHolder, position: Int) {
      val thread = get(position)
      holder.user.text = thread.displayUser
      holder.id.text = thread.displayId
      holder.date.text = thread.date.prettyTime(inflater.context)
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
