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
import com.hippo.nimingban.widget.content.ContentData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/4/2017.
 */

class ThreadsScene: NmbScene<ThreadsScene, ThreadsUi>() {

  companion object {
    private const val INIT_FORUM = "ThreadsData:init_forum"
    /** Assigning it to [forum] means no forum available **/
    const val NO_FORUM = "ThreadsData:no_forum"
  }


  internal val data = ThreadsData()


  /** Forum id, null for no forum **/
  var forum: String = INIT_FORUM
    set(value) {
      if (field != value) {
        field = value
        data.goTo(0)
      }
    }


  override fun createUi(): ThreadsUi {
    return ThreadsUi(this, activity as NmbActivity, context!!)
  }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)

    // TODO get forum list
    forum = "4"
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
