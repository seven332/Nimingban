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
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbPen
import io.reactivex.android.schedulers.AndroidSchedulers

/*
 * Created by Hippo on 2017/7/16.
 */

open class SortForumsPen : NmbPen<SortForumsUi>(), SortForumsLogic {

  init {
    SortForumsUiState().also { view = it; state = it }
  }

  override fun onCreate(args: Bundle) {
    super.onCreate(args)

    NMB_DB.liveForums.observable
        .map { it.toMutableList() }
        .observeOn(AndroidSchedulers.mainThread())
        .register({ view.updateForums(it) }, { /* Ignore error */ })
  }

  override fun onUpdateForum(forum: Forum) {
    schedule { NMB_DB.putForum(forum) }
  }

  override fun onReorderForum(fromPosition: Int, toPosition: Int) {
    schedule { NMB_DB.reorderForum(fromPosition, toPosition) }
  }
}
