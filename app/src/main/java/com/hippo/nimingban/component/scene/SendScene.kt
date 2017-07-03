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

package com.hippo.nimingban.component.scene

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.dialog.SelectForumDialog

/*
 * Created by Hippo on 6/24/2017.
 */

class SendScene : NmbScene(), SelectForumDialog.OnSelectForumListener {

  companion object {
    internal const val KEY_FORUM = "SendScene:forum"
    internal const val KEY_THREAD = "SendScene:thread"
    internal const val KEY_PRESET_CONTENT = "SendScene:preset_content"
  }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
  }

  override fun createLogic(args: Bundle?) =
      SendSceneLogic(this, args?.getParcelable(KEY_FORUM), args?.getParcelable(KEY_THREAD))

  override fun createUi(inflater: LayoutInflater, container: ViewGroup) =
      SendSceneUi(logic as SendSceneLogic, args?.getString(KEY_PRESET_CONTENT), inflater, container)

  override fun onSelectForum(forum: Forum) {
    val logic = this.logic
    if (logic is SendSceneLogic) {
      logic.onSelectForum(forum)
    }
  }
}
