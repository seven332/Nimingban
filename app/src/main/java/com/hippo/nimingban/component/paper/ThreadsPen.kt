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

import android.os.Bundle
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbPen
import com.hippo.nimingban.exception.PresetException
import com.hippo.nimingban.widget.content.ContentData
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 2017/7/14.
 */

open class ThreadsPen : NmbPen<ThreadsUi>(), ThreadsLogic {

  init {
    ThreadsUiState().also { view = it; state = it }
  }

  private val data = ThreadsData()

  private var forum: Forum? = null
  private var hasSetForum = false
  private var forumHungOn: Pair<Int, Int>? = null

  var pages: Int = Int.MAX_VALUE

  override fun initAdapter(adapter: ContentDataAdapter<Thread, *>) {
    adapter.data = data
  }

  override fun termAdapter(adapter: ContentDataAdapter<Thread, *>) {}

  override fun initContentLayout(layout: ContentLayout) {
    layout.logic = data
    data.attach(layout)
  }

  override fun termContentLayout(layout: ContentLayout) {
    data.detach()
  }

  override fun showMessage(message: String) {}

  fun refresh() = data.goTo(0)

  override fun isVirtualForum() = forum?.isVirtual() ?: false

  fun isLoading() = data.isLoading()

  override fun onClickThread(thread: Thread) {}

  override fun onClickThumb(reply: Reply) {}

  override fun onCreate(args: Bundle) {
    super.onCreate(args)
    // Call restore first
    // The next request should wait for setForum()
    data.restore()
  }

  fun getForum() = forum

  fun setForum(forum: Forum?) {
    val oldForum = this.forum
    this.forum = forum
    hasSetForum = true

    val forumHungOn = this.forumHungOn
    if (forumHungOn != null) {
      // Continue what's hung on
      this.forumHungOn = null
      data.requestThreads(forumHungOn.first, forumHungOn.second)
    } else if (!data.isRestoring()) {
      // It's not in restoring, just go to first page if it's a different forum
      if (oldForum?.id != forum?.id) {
        data.goTo(0)
      }
    } else {
      // It's in restoring, wait for next requiring
    }
  }


  inner class ThreadsData : ContentData<Thread>() {

    fun requestThreads(id: Int, page: Int) {
      val forum = this@ThreadsPen.forum
      if (forum != null) {
        NMB_CLIENT.threads(forum, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .register({ (list, pages) ->
              // Update pages
              if (pages != Int.MAX_VALUE) {
                this@ThreadsPen.pages = pages
              }
              setData(id, list, this@ThreadsPen.pages)
            }, {
              setError(id, it)
            })
      } else {
        schedule { setError(id, PresetException("No forum", R.string.error_no_forum, 0)) }
      }
    }

    override fun onRequireData(id: Int, page: Int) {
      if (hasSetForum) {
        requestThreads(id, page)
      } else {
        forumHungOn = Pair(id, page)
      }
    }

    override fun onRestoreData(id: Int) {
      // TODO("not implemented")
      setError(id, Exception())
    }

    override fun onBackupData(data: List<Thread>) {
      // TODO("not implemented")
    }
  }
}
