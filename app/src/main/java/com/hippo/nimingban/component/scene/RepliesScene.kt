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
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.SceneLogic
import com.hippo.nimingban.component.SceneUi
import com.hippo.nimingban.component.paper.impl.DefaultRepliesUi
import com.hippo.nimingban.component.paper.impl.defaultToolbarUi

/*
 * Created by Hippo on 6/20/2017.
 */

class RepliesScene : NmbScene() {

  companion object {
    const val KEY_ID = "RepliesScene:id"
    const val KEY_THREAD = "RepliesScene:thread"
    const val KEY_FORUM = "RepliesScene:forum"
  }

  init {
    opacity = TRANSLUCENT
  }

  override fun createLogic(args: Bundle?): SceneLogic {
    var id: String? = null
    var thread: Thread? = null
    var forum: String? = null
    if (args != null) {
      id = args.getString(KEY_ID)
      thread = args.getParcelable(KEY_THREAD)
      forum = args.getString(KEY_FORUM)
    }
    return RepliesSceneLogicImpl(id, thread, forum, this)
  }

  override fun createUi(inflater: LayoutInflater, container: ViewGroup): SceneUi {
    val logic = this.logic as RepliesSceneLogicImpl
    val activity = this.activity as NmbActivity

    return repliesSceneUiImpl(logic, inflater, container) {
      initSwipeBackUi {
        addChild { container->
          defaultToolbarUi(logic.getToolbarLogic(), inflater, container) {
            addChild { container->
              DefaultRepliesUi(logic.getRepliesLogic(), activity, inflater, container)
                  .also { logic.repliesUi = it }
            }
          }.also { logic.toolbarUi = it }
        }
      }.also { logic.swipeBackUi = it }
    }.also { logic.repliesSceneUi = it }
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    val logic = this.logic as RepliesSceneLogicImpl
    logic.repliesSceneUi = null
    logic.swipeBackUi = null
    logic.toolbarUi = null
    logic.repliesUi = null
  }
}

fun Thread.repliesScene(forum: String?): RepliesScene {
  val args = Bundle()
  args.putParcelable(RepliesScene.KEY_THREAD, this)
  args.putString(RepliesScene.KEY_FORUM, forum)
  val scene = RepliesScene()
  scene.args = args
  return scene
}
