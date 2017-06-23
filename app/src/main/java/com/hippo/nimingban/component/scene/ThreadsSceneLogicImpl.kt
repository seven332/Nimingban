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
import com.hippo.nimingban.component.NmbLogic
import com.hippo.nimingban.component.paper.ForumListLogic
import com.hippo.nimingban.component.paper.ForumListUi
import com.hippo.nimingban.component.paper.NavigationLogic
import com.hippo.nimingban.component.paper.NavigationUi
import com.hippo.nimingban.component.paper.ThreadsLogic
import com.hippo.nimingban.component.paper.ThreadsUi
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.paper.ToolbarUi
import com.hippo.nimingban.component.paper.impl.DefaultForumListLogic
import com.hippo.nimingban.component.paper.impl.DefaultThreadsLogic
import com.hippo.nimingban.component.paper.impl.DefaultToolbarLogic
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/20/2017.
 */

class ThreadsSceneLogicImpl(
    val scene: Scene
) : GroupLogic(), ThreadsSceneLogic {

  override var threadsSceneUi: ThreadsSceneUi? = null

  private val threadsLogic = DefaultThreadsLogic(scene).also { addChild(it) }
  override var threadsUi: ThreadsUi? = null

  private val threadsToolbarLogic = ThreadsToolbarLogicImpl().also { addChild(it) }
  override var threadsToolbarUi: ToolbarUi? = null
    set(value) {
      field = value
      threadsToolbarLogic.toolbarUi = value
    }

  private val forumListLogic = ForumListLogicImpl().also { addChild(it) }
  override var forumListUi: ForumListUi? = null
    set(value) {
      field = value
      forumListLogic.forumListUi = value
    }

  private val forumListToolbarLogic = ForumListToolbarLogicImpl().also { addChild(it) }
  override var forumListToolbarUi: ToolbarUi? = null
    set(value) {
      field = value
      forumListToolbarLogic.toolbarUi = value
    }

  private val navigationLogic = NavigationLogicImpl().also { addChild(it) }
  override var navigationUi: NavigationUi? = null

  override fun getThreadsLogic(): ThreadsLogic = threadsLogic

  override fun getThreadsToolbarUiLogic(): ToolbarLogic = threadsToolbarLogic

  override fun getForumListLogic(): ForumListLogic = forumListLogic

  override fun getForumListToolbarLogic(): ToolbarLogic = forumListToolbarLogic

  override fun getNavigationLogic(): NavigationLogic = navigationLogic


  private inner class ThreadsToolbarLogicImpl : DefaultToolbarLogic() {

    init {
      setNavigationIcon(R.drawable.menu_white_x24)
    }

    fun onSetForum(forum: Forum?) {
      setTitle(forum?.name ?: NMB_APP.getString(R.string.app_name))
    }

    override fun onClickNavigationIcon() {
      threadsSceneUi?.toggleLeftDrawer()
    }

    override fun onClickMenuItem(item: MenuItem) = false
  }


  private inner class ForumListLogicImpl : DefaultForumListLogic() {

    override fun onSelectForum(forum: Forum?, byUser: Boolean) {
      threadsLogic.forum = forum
      threadsToolbarLogic.onSetForum(forum)
      if (byUser) {
        threadsSceneUi?.closeRightDrawer()
      }
    }
  }


  private inner class ForumListToolbarLogicImpl : DefaultToolbarLogic() {

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


  private inner class NavigationLogicImpl : NmbLogic(), NavigationLogic {
    override fun onSelectNavigationItem(item: MenuItem) = false
  }
}
