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
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.GroupLogic
import com.hippo.nimingban.component.paper.ForumListLogic
import com.hippo.nimingban.component.paper.NavigationLogic
import com.hippo.nimingban.component.paper.ThreadsLogic
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/19/2017.
 */

class ThreadsSceneLogic(
    val scene: Scene
) : GroupLogic() {

  var threadsSceneUi: ThreadsSceneUi? = null

  val threadsToolbarLogic: ToolbarLogic = ThreadsToolbarLogic().also { addChild(it) }
  val threadsLogic: ThreadsLogic = ThreadsLogic(scene).also { addChild(it) }
  val forumListToolbarLogic: ToolbarLogic = ForumListToolbarLogic().also { addChild(it) }
  val forumListLogic: ForumListLogic = ForumListLogicImpl().also { addChild(it) }
  val navigationLogic: NavigationLogic = NavigationLogicImpl().also { addChild(it) }


  private inner class ThreadsToolbarLogic : ToolbarLogic() {

    init {
      setNavigationIcon(R.drawable.menu_white_x24)
    }

    fun onSetForum(forum: Forum?) {
      setTitle(forum?.name ?: NMB_APP.getString(R.string.app_name))
    }

    override fun onClickNavigationIcon() {
      threadsSceneUi?.toggleLeftDrawer()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      // TODO
      return false
    }
  }


  private inner class ForumListToolbarLogic : ToolbarLogic() {

    init {
      inflateMenu(R.menu.forum_list)
    }

    override fun onClickNavigationIcon() {}

    override fun onClickMenuItem(item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_sort -> {
          scene.stage?.pushScene(ForumsScene())
          return true
        }
        else -> return false
      }
    }
  }


  private inner class ForumListLogicImpl : ForumListLogic() {

    override fun onSelectForum(forum: Forum?, byUser: Boolean) {
      threadsLogic.forum = forum
      (threadsToolbarLogic as ThreadsToolbarLogic).onSetForum(forum)
      if (byUser) {
        threadsSceneUi?.closeRightDrawer()
      }
    }
  }


  private inner class NavigationLogicImpl : NavigationLogic() {

    override fun onSelectNavigationItem(item: MenuItem): Boolean {
      // TODO
      return false
    }
  }
}
