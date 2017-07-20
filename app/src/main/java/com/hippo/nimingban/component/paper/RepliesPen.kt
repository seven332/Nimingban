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

import android.text.style.ClickableSpan
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbPen
import com.hippo.nimingban.widget.content.ContentData
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 2017/7/17.
 */

open class RepliesPen : NmbPen<RepliesUi>(), RepliesLogic {

  init {
    RepliesUiState().also { view = it; state = it }
  }

  private val data = RepliesData()

  var threadId: String? = null
    private set
  // The first reply in list
  private var reply: Reply? = null
  private var replyId: String? = null

  private val userColorMap: MutableMap<String, Int> = HashMap()

  fun init(thread: Thread?, replyId: String?, forum: String?) {
    if (thread != null) {
      this.threadId = thread.id
      this.reply = thread.toReply()

      onUpdateThreadId(thread.id)
      if (thread.user != null) {
        view.setThreadUser(thread.user)
      }
    }

    this.replyId = replyId

    if (forum != null) {
      onUpdateForum(forum)
    }

    data.restore()
  }

  override fun initAdapter(adapter: ContentDataAdapter<Reply, *>) {
    adapter.data = data
  }

  override fun termAdapter(adapter: ContentDataAdapter<Reply, *>) {}

  override fun initContentLayout(layout: ContentLayout) {
    layout.logic = data
    data.attach(layout)
  }

  override fun termContentLayout(layout: ContentLayout) {
    data.detach()
  }

  override fun showMessage(message: String) {}

  override fun registerUserColor(user: String, color: Int) {
    userColorMap.put(user, color)
  }

  override fun unregisterUserColor(user: String) {
    userColorMap.remove(user)
  }

  override fun getUserColor(user: String) = userColorMap[user]

  override fun onClickThumb(reply: Reply) {}

  override fun onClickSpan(span: ClickableSpan) {}

  override fun onClickReply(reply: Reply) {}

  open fun onUpdateThreadId(threadId: String) {}

  open fun onUpdateForum(forum: String) {}


  private inner class RepliesData : ContentData<Reply>() {

    override fun onRequireData(id: Int, page: Int) {
      val threadId = this@RepliesPen.threadId
      val replyId = this@RepliesPen.replyId

      if (threadId != null) {
        NMB_CLIENT.replies(threadId, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .register({ (_, replies, forum, pages) ->
              // Update forum
              onUpdateForum(forum)
              // Set data
              setData(id, replies, pages)
            }, { setError(id, it) })

      } else if (replyId != null) {
        NMB_CLIENT.reference(replyId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .register({ reference ->
              // Update thread id
              this@RepliesPen.threadId = reference.threadId
              if (reference.threadId != null) {
                onUpdateThreadId(reference.threadId)
              }
              // Update thread user
              if (reference.user != null) {
                view.setThreadUser(reference.user)
              }
              // Set data
              setData(id, listOf(reference.toReply()), 1)
              // Refresh after get thread id
              goTo(0)
            }, { setError(id, it) })
      }
    }

    override fun onRestoreData(id: Int) {
      val reply = this@RepliesPen.reply
      val replyId = this@RepliesPen.replyId

      if (reply != null) {
        setData(id, listOf(reply), 1)

      } else if (replyId != null) {
        NMB_CLIENT.reference(replyId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .register({ reference ->
              // Update thread id
              this@RepliesPen.threadId = reference.threadId
              if (reference.threadId != null) {
                onUpdateThreadId(reference.threadId)
              }
              // Update thread user
              if (reference.user != null) {
                view.setThreadUser(reference.user)
              }
              // Set data
              setData(id, listOf(reference.toReply()), 1)
            }, { setError(id, ContentData.FAILED_TO_RESTORE_EXCEPTION) })

      } else {
        setError(id, ContentData.FAILED_TO_RESTORE_EXCEPTION)
      }
    }

    override fun onBackupData(data: List<Reply>) {}
  }
}
