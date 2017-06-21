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
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.SceneUi
import com.hippo.nimingban.component.paper.impl.DefaultForumListUi
import com.hippo.nimingban.component.paper.impl.DefaultNavigationUi
import com.hippo.nimingban.component.paper.impl.DefaultThreadsUi
import com.hippo.nimingban.component.paper.impl.defaultToolbarUi

/*
 * Created by Hippo on 6/19/2017.
 */

class ThreadsScene : NmbScene() {

  override fun createLogic(args: Bundle?) = ThreadsSceneLogicImpl(this)

  override fun createUi(inflater: LayoutInflater, container: ViewGroup): SceneUi {
    val logic = this.logic as ThreadsSceneLogicImpl
    val activity = this.activity as NmbActivity

    return threadsSceneUiImpl(inflater, container) {
      setContentUi { container ->
        defaultToolbarUi(logic.getThreadsToolbarUiLogic(), inflater, container) {
          addChild { container ->
            DefaultThreadsUi(logic.getThreadsLogic(), activity, inflater, container)
                .also { logic.threadsUi = it }
          }
        }.also { logic.threadsToolbarUi = it }
      }

      setLeftUi { container ->
        DefaultNavigationUi(logic.getNavigationLogic(), inflater, container)
            .also { logic.navigationUi = it }
      }

      setRightUi { container ->
        defaultToolbarUi(logic.getForumListToolbarLogic(), inflater, container) {
          addChild { container ->
            DefaultForumListUi(logic.getForumListLogic(), inflater, container)
                .also { logic.forumListUi = it }
          }
        }.also { logic.forumListToolbarUi = it }
      }
    }.also { logic.threadsSceneUi = it }
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    val logic = this.logic as ThreadsSceneLogicImpl
    logic.threadsSceneUi = null
    logic.threadsToolbarUi = null
    logic.threadsUi = null
    logic.navigationUi = null
    logic.forumListToolbarUi = null
    logic.forumListUi = null
  }
}
