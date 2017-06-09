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

import android.util.Log
import com.hippo.nimingban.GSON
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.exception.PresetException
import com.hippo.nimingban.scene.NmbPresenter
import com.hippo.nimingban.widget.content.ContentData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/5/2017.
 */

class ThreadsPresenter :
    NmbPresenter<ThreadsPresenter, ThreadsView>(),
    ThreadsContract.Presenter {

  companion object {
    const val NO_FORUM = "no_forum"
  }

  private val data = ThreadsData()

  var forum: String = NO_FORUM


  init {

    forum = "4"

    data.goTo(0)
  }

  override fun onCreateView(view: ThreadsView) {
    super.onCreateView(view)
    data.view = view.contentLayout
    view.adapter?.data = data
  }

  override fun onDestroyView(view: ThreadsView) {
    super.onDestroyView(view)
    data.view = null
    view.adapter?.data = null
  }


  inner class ThreadsData : ContentData<Thread>() {

    override fun onRequireData(id: Int, page: Int) {
      if (forum === NO_FORUM) {
        schedule { setError(id, PresetException("No forum", R.string.error_no_forum, R.drawable.emoticon_sad_primary_x64)) }
      } else {
        NMB_CLIENT.threads(forum, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ setData(id, it, Int.MAX_VALUE) }, { setError(id, it) })
      }
    }

    override fun onRestoreData(id: Int) {
      TODO("not implemented")
    }

    override fun onBackupData(data: List<Thread>) {


      Log.d("TAG", GSON.toJson(data[0]))


    }

    override fun isDuplicate(t1: Thread, t2: Thread): Boolean {
      return t1.id == t2.id
    }
  }
}
