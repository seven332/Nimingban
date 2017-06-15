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

import android.os.Bundle
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.exception.PresetException
import com.hippo.nimingban.scene.NmbScene
import com.hippo.nimingban.scene.replies.repliesScene
import com.hippo.nimingban.scene.ui.SceneUi
import com.hippo.nimingban.widget.content.ContentData
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/14/2017.
 */

class MainScene : NmbScene(), MainSceneLogic {

  companion object {
    private const val INIT_FORUM = "ThreadsData:init_forum"
    /** Assigning it to [forum] means no forum available **/
    private const val NO_FORUM = "ThreadsData:no_forum"
  }

  private val data = ThreadsData()

  /** Forum id, null for no forum **/
  private var forum: String = INIT_FORUM
    set(value) {
      if (field != value) {
        field = value
        data.goTo(0)
      }
    }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)

    forum = "4"
  }

  override fun createUi(): SceneUi {
    return MainSceneUi(this, context!!, activity as NmbActivity)
  }

  override fun initializeAdapter(adapter: ContentDataAdapter<Thread, *>) { adapter.data = data }

  override fun terminateAdapter(adapter: ContentDataAdapter<Thread, *>) { adapter.data = null }

  override fun initializeContentLayout(contentLayout: ContentLayout) { data.view = contentLayout }

  override fun terminateContentLayout(contentLayout: ContentLayout) { data.view = null }

  override fun onClickThread(thread: Thread) {
    stage?.pushScene(thread.repliesScene())
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
      // TODO("not implemented")
    }

    override fun onBackupData(data: List<Thread>) {
      // TODO("not implemented")
    }

    override fun isDuplicate(t1: Thread, t2: Thread): Boolean {
      return t1.id == t2.id
    }
  }
}
