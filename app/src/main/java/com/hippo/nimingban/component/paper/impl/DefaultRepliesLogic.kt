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

package com.hippo.nimingban.component.paper.impl

import android.text.style.ClickableSpan
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.client.REPLY_PAGE_SIZE
import com.hippo.nimingban.client.data.RepliesHtml
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbLogic
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.paper.RepliesLogic
import com.hippo.nimingban.component.scene.galleryScene
import com.hippo.nimingban.util.ceilDiv
import com.hippo.nimingban.widget.content.ContentData
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/20/2017.
 */

abstract class DefaultRepliesLogic(
    private val id: String?,
    private var thread: Thread?,
    private var forum: String?,
    private val scene: NmbScene
) : NmbLogic(), RepliesLogic {

  private val data = RepliesData()

  init {
    data.restore()
  }

  override fun initializeAdapter(adapter: ContentDataAdapter<Reply, *>) {
    adapter.data = data
  }

  override fun terminateAdapter(adapter: ContentDataAdapter<Reply, *>) {
    adapter.data = null
  }

  override fun initializeContentLayout(contentLayout: ContentLayout) {
    data.ui = contentLayout
  }

  override fun terminateContentLayout(contentLayout: ContentLayout) {
    data.ui = null
  }

  override fun onClickThumb(reply: Reply) {
    scene.stage?.pushScene(reply.galleryScene())
  }

  override fun onClickSpan(span: ClickableSpan) {
    //TODO("not implemented")
  }

  override fun onClickReply(reply: Reply) {
    //TODO("not implemented")
  }

  /**
   * Called when the thread updated.
   */
  abstract fun onUpdateThread(thread: Thread)

  /**
   * Called when the forum updated.
   */
  abstract fun onUpdateForum(forum: String)


  private  inner class RepliesData : ContentData<Reply>() {

    private val threadId get() = id ?: thread?.id

    override fun onRequireData(id: Int, page: Int) {
      val threadId = this.threadId
      if (threadId != null) {
        // Try to parse html to get max page
        val htmlSingle = NMB_CLIENT.repliesHtml(threadId, page)
            // If error, max page is Int.MAX_VALUE
            .onErrorReturn { it.printStackTrace(); RepliesHtml("", Int.MAX_VALUE) }
        NMB_CLIENT.replies(threadId, page)
            .zipWith(htmlSingle) { (thread, replies), html -> Triple(thread, replies, html) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (thread, replies, html) ->
              // Calculate page
              val pages =
                  if (thread.replies.size < REPLY_PAGE_SIZE) page + 1
                  else ceilDiv(thread.replyCount, REPLY_PAGE_SIZE)
              // Update thread
              if (this@DefaultRepliesLogic.thread == null) {
                this@DefaultRepliesLogic.thread = thread
                onUpdateThread(thread)
              }
              // Update forum
              if (!html.forum.isNullOrBlank() && forum != html.forum) {
                forum = html.forum
                onUpdateForum(forum!!)
              }
              setData(id, replies, pages)
            }, {
              setError(id, it)
            })
            .register()
      } else {
        // TODO i18n
        schedule { setError(id, Exception("No thread id")) }
      }
    }

    override fun onRestoreData(id: Int) {
      val thread = this@DefaultRepliesLogic.thread
      if (thread != null) {
        setData(id, listOf(thread.toReply()), 1)
      } else {
        setError(id, ContentData.FAILED_TO_RESTORE_EXCEPTION)
      }
    }

    override fun onBackupData(data: List<Reply>) {}
  }
}
