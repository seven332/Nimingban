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
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.component.NmbScene

/*
 * Created by Hippo on 6/19/2017.
 */

class ThreadsScene : NmbScene() {

  override fun createLogic(args: Bundle?) = ThreadsSceneLogic(this)

  override fun createUi(inflater: LayoutInflater, container: ViewGroup) =
      ThreadsSceneUi(logic as ThreadsSceneLogic, activity as NmbActivity, inflater, container)
}
