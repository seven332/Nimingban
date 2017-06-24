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
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.component.NmbScene

/*
 * Created by Hippo on 6/21/2017.
 */

class GalleryScene : NmbScene() {

  companion object {
    const val KEY_REPLY = "GalleryScene:reply"
  }

  private var reply: Reply? = null

  init {
    opacity = TRANSLUCENT
  }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
    reply = args?.getParcelable(KEY_REPLY)
  }

  override fun createLogic(args: Bundle?) = GallerySceneLogic(this)

  override fun createUi(inflater: LayoutInflater, container: ViewGroup) =
      GallerySceneUi(logic as GallerySceneLogic, reply, inflater, container)
}
