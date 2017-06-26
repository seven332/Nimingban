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
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbLogic
import com.hippo.nimingban.component.dialog.SelectForumDialog
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/24/2017.
 */

class SendLogic(
    private val scene: Scene,
    private var forum: Forum?
) : NmbLogic() {

  companion object {
    const val KEY_FORUM = "SendLogic:forum"
  }

  var sendUi: SendUi? = null
    set(value) {
      field = value
      value?.onSelectForum(forum)
    }

  fun onSelectForum(forum: Forum) {
    this.forum = forum
    sendUi?.onSelectForum(forum)
  }

  fun onClickForum() {
    val dialog = SelectForumDialog()
    dialog.target = scene
    scene.stage?.pushScene(dialog)
  }

  fun getForum() = forum

  fun getTitle() = sendUi?.getTitle() ?: ""

  fun getName() = sendUi?.getName() ?: ""

  fun getEmail() = sendUi?.getEmail() ?: ""

  fun getContent() = sendUi?.getContent() ?: ""

  override fun onSaveState(outState: Bundle) {
    super.onSaveState(outState)
    outState.putParcelable(KEY_FORUM, forum)
  }

  override fun onRestoreState(savedViewState: Bundle) {
    super.onRestoreState(savedViewState)
    onSelectForum(savedViewState.getParcelable(KEY_FORUM))
  }
}
