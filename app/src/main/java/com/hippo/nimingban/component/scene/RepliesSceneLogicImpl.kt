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

import android.view.MenuItem
import com.hippo.nimingban.NMB_APP
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.GroupLogic
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.paper.RepliesLogic
import com.hippo.nimingban.component.paper.RepliesUi
import com.hippo.nimingban.component.paper.SwipeBackLogic
import com.hippo.nimingban.component.paper.SwipeBackUi
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.paper.ToolbarUi
import com.hippo.nimingban.component.paper.impl.DefaultRepliesLogic
import com.hippo.nimingban.component.paper.impl.DefaultSwipeBackLogic
import com.hippo.nimingban.component.paper.impl.DefaultToolbarLogic
import com.hippo.swipeback.SwipeBackLayout

/*
 * Created by Hippo on 6/20/2017.
 */

class RepliesSceneLogicImpl(
    id: String?,
    thread: Thread?,
    forum: String?,
    val scene: NmbScene
) : GroupLogic(), RepliesSceneLogic {

  override var repliesSceneUi: RepliesSceneUi? = null

  private val repliesLogic = RepliesLogicImpl(id, thread, forum, scene).also { addChild(it) }
  override var repliesUi: RepliesUi? = null

  private val toolbarLogic = ToolbarLogicImpl(thread, forum).also { addChild(it) }
  override var toolbarUi: ToolbarUi? = null
    set(value) {
      field = value
      toolbarLogic.toolbarUi = value
    }

  private val swipeBackLogic = DefaultSwipeBackLogic(scene).also { addChild(it) }
  override var swipeBackUi: SwipeBackUi? = null
    set(value) {
      field = value
      swipeBackLogic.swipeBackUi = value
    }

  init {
    swipeBackLogic.setSwipeEdge(SwipeBackLayout.EDGE_LEFT or SwipeBackLayout.EDGE_RIGHT)
  }

  override fun getRepliesLogic(): RepliesLogic = repliesLogic

  override fun getToolbarLogic(): ToolbarLogic = toolbarLogic

  override fun getSwipeBackLogic(): SwipeBackLogic = swipeBackLogic


  private inner class ToolbarLogicImpl(thread: Thread?, forum: String?) : DefaultToolbarLogic() {

    init {
      setTitle(thread)
      if (!forum.isNullOrBlank()) setSubtitle(forum)
      setNavigationIcon(R.drawable.arrow_left_white_x24)
    }

    fun setTitle(thread: Thread?) {
      setTitle(thread?.displayId ?: NMB_APP.getString(R.string.app_name))
    }

    override fun onClickNavigationIcon() {
      scene.pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean { return false }
  }


  private inner class RepliesLogicImpl(
      id: String?,
      thread: Thread?,
      forum: String?,
      scene: NmbScene
  ) : DefaultRepliesLogic(id, thread, forum, scene) {

    override fun onUpdateThread(thread: Thread) {
      toolbarLogic.setTitle(thread)
    }

    override fun onUpdateForum(forum: String) {
      toolbarLogic.setSubtitle(forum)
    }
  }
}
