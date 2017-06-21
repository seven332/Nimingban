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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.component.paper.impl.DefaultSwipeBackUi

/*
 * Created by Hippo on 6/21/2017.
 */

class RepliesSceneUiImpl(
    logic: RepliesSceneLogic,
    inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi(), RepliesSceneUi {

  override val view: View get() = swipeBackUi.view

  private val swipeBackUi = DefaultSwipeBackUi(logic.getSwipeBackLogic(), inflater, container)
      .also { addChild(it) }

  fun initSwipeBackUi(init: DefaultSwipeBackUi.() -> Unit): DefaultSwipeBackUi {
    swipeBackUi.init()
    return swipeBackUi
  }
}

inline fun repliesSceneUiImpl(
    logic: RepliesSceneLogic,
    inflater: LayoutInflater,
    container: ViewGroup,
    init: RepliesSceneUiImpl.() -> Unit
): RepliesSceneUiImpl {
  val repliesSceneUiImpl = RepliesSceneUiImpl(logic, inflater, container)
  repliesSceneUiImpl.init()
  return repliesSceneUiImpl
}
