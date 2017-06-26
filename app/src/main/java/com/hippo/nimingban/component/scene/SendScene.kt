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
import com.hippo.nimingban.component.NmbScene

/*
 * Created by Hippo on 6/24/2017.
 */

class SendScene : NmbScene() {

  companion object {
    const val KEY_FORUM = "SendScene:forum"
    const val KEY_ID = "SendScene:id"
  }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
  }

  override fun createLogic(args: Bundle?) = SendSceneLogic(this, args?.getParcelable(KEY_FORUM))

  override fun createUi(inflater: LayoutInflater, container: ViewGroup) =
      SendSceneUi(logic as SendSceneLogic, inflater, container)
}
