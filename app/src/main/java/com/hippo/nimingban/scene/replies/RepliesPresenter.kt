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

package com.hippo.nimingban.scene.replies

import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.scene.NmbPresenter
import com.hippo.nimingban.widget.content.ContentData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/11/2017.
 */

class RepliesPresenter(var id: String?, var thread: Thread?) :
    NmbPresenter<RepliesPresenter, RepliesView>(),
    RepliesContract.Presenter {

  private val data = RepliesData()


  init {
    data.restore()
  }


  override fun onCreateView(view: RepliesView) {
    super.onCreateView(view)
    data.view = view.contentLayout
    view.adapter?.data = data
  }

  override fun onDestroyView(view: RepliesView) {
    super.onDestroyView(view)
    data.view = null
    view.adapter?.data = null
  }


  inner class RepliesData : ContentData<Reply>() {

    private val threadId get() = this@RepliesPresenter.id ?: this@RepliesPresenter.thread?.id

    override fun onRequireData(id: Int, page: Int) {
      val threadId = this.threadId
      if (threadId != null) {
        NMB_CLIENT.replies(threadId, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
              if (this@RepliesPresenter.thread == null) {
                this@RepliesPresenter.thread = it.first
              }
              // TODO need a better way to get max page
              setData(id, it.second, Int.MAX_VALUE)
            }, {
              setError(id, it)
            })
      } else {
        // TODO i18n
        schedule { setError(id, Exception("No thread id")) }
      }
    }

    override fun onRestoreData(id: Int) {
      val thread = this@RepliesPresenter.thread
      if (thread != null) {
        setData(id, listOf(thread.toReply()), 1)
      } else {
        setError(id, ContentData.FAILED_TO_RESTORE_EXCEPTION)
      }
    }

    override fun onBackupData(data: List<Reply>) {}
  }
}
