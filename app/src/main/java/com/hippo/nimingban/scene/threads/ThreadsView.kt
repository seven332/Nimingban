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

package com.hippo.nimingban.scene.threads

import android.content.Context
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.hippo.easyrecyclerview.EasyRecyclerView
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.scene.NmbScene
import com.hippo.nimingban.scene.ToolbarView
import com.hippo.nimingban.scene.replies.newRepliesScene
import com.hippo.nimingban.util.debug
import com.hippo.nimingban.util.prettyTime
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import com.hippo.nimingban.widget.nmb.NmbReplayMarquee
import com.hippo.nimingban.widget.nmb.NmbThumb

/*
 * Created by Hippo on 6/5/2017.
 */

class ThreadsView(
    scene: NmbScene<ThreadsPresenter, ThreadsView>,
    activity: NmbActivity,
    context: Context
) : ToolbarView<ThreadsView, ThreadsPresenter>(scene, activity, context),
    ThreadsContract.View, ContentLayout.Extension {

  internal var contentLayout: ContentLayout? = null
  internal var recyclerView: RecyclerView? = null
  internal var adapter: ThreadAdapter? = null

  override fun onCreateToolbarContent(inflater: LayoutInflater, parent: ViewGroup): View {
    val view = inflater.inflate(R.layout.scene_threads, parent, false)!!

    val adapter = ThreadAdapter(inflater)

    val recyclerView = view.findViewById(R.id.recycler_view) as EasyRecyclerView
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(inflater.context)
    recyclerView.setOnItemClickListener { _, holder ->
      pushScene(newRepliesScene(this@ThreadsView.adapter!!.get(holder.adapterPosition)))
    }

    val contentLayout = view.findViewById(R.id.content_layout) as ContentLayout
    contentLayout.extension = this

    this.contentLayout = contentLayout
    this.recyclerView = recyclerView
    this.adapter = adapter

    return view
  }

  override fun onResume() {
    super.onResume()
    adapter?.resume()
  }

  override fun onPause() {
    super.onPause()
    adapter?.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    recyclerView?.adapter = null
    recyclerView?.layoutManager = null
    adapter?.destroy()
  }

  override fun showMessage(message: String) {
    activity.snack(message)
  }


  class ThreadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val user = itemView.findViewById(R.id.user) as TextView
    val id = itemView.findViewById(R.id.id) as TextView
    val date = itemView.findViewById(R.id.date) as TextView
    val content = itemView.findViewById(R.id.content) as TextView
    val thumb = itemView.findViewById(R.id.thumb) as NmbThumb
    val replies = itemView.findViewById(R.id.replies) as NmbReplayMarquee
    val replyCount = itemView.findViewById(R.id.reply_count) as TextView
    val bottom = itemView.findViewById(R.id.bottom)!!

    init {
      val drawable = AppCompatResources.getDrawable(itemView.context, R.drawable.comment_multiple_outline_secondary_x16)!!
      drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
      replyCount.setCompoundDrawables(drawable, null, null, null)
    }

    var isAttached = false
      set(value) {
        if (field == value) return
        field = value
        if (value && isResumed) onResume() else onPause()
      }

    var isResumed = false
      set(value) {
        if (field == value) return
        field = value
        if (value && isAttached) onResume() else onPause()
      }

    private var hasResumed = false

    fun onResume() {
      if (!hasResumed) {
        hasResumed = true
        replies.start()
      }
    }

    fun onPause() {
      if (hasResumed) {
        hasResumed = false
        replies.stop()
      }
    }
  }


  class ThreadAdapter(val inflater: LayoutInflater) : ContentDataAdapter<Thread, ThreadHolder>() {

    private var isResumed = false
    private val holderList = mutableListOf<ThreadHolder>()

    override fun onCreateViewHolder2(parent: ViewGroup, viewType: Int) =
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

      if (isResumed) {
        holder.isResumed = true
      }
      holderList.add(holder)
    }

    override fun onViewAttachedToWindow(holder: ThreadHolder?) {
      super.onViewAttachedToWindow(holder)
      holder?.isAttached = true
    }

    override fun onViewDetachedFromWindow(holder: ThreadHolder?) {
      super.onViewDetachedFromWindow(holder)
      holder?.isAttached = false
    }

    override fun onViewRecycled(holder: ThreadHolder) {
      super.onViewRecycled(holder)
      holderList.remove(holder)
      holder.isAttached = false
      holder.isResumed = false
    }

    fun resume() {
      isResumed = true
      holderList.forEach { it.isResumed = true }
    }

    fun pause() {
      isResumed = false
      holderList.forEach { it.isResumed = false }
    }

    fun destroy() {
      debug(holderList.isEmpty(), { "All ViewHolders should be removed in onViewRecycled()" })
      holderList.clear()
    }
  }
}
