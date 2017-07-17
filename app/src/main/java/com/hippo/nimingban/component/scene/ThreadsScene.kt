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
import android.view.MenuItem
import android.view.View
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.MvpPaper
import com.hippo.nimingban.component.MvpPen
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.paper.DrawerPaper
import com.hippo.nimingban.component.paper.DrawerPen
import com.hippo.nimingban.component.paper.ForumListPen
import com.hippo.nimingban.component.paper.ThreadsPen
import com.hippo.nimingban.component.paper.ToolbarPaper
import com.hippo.nimingban.component.paper.ToolbarPen
import com.hippo.nimingban.component.paper.drawer
import com.hippo.nimingban.component.paper.forumList
import com.hippo.nimingban.component.paper.papers
import com.hippo.nimingban.component.paper.pens
import com.hippo.nimingban.component.paper.threads
import com.hippo.nimingban.component.paper.toolbar
import com.hippo.nimingban.component.refreshForums
import com.hippo.nimingban.widget.nmb.NmbDrawerSide
import com.hippo.stage.Stage

/*
 * Created by Hippo on 2017/7/14.
 */

class ThreadsScene : NmbScene() {

  companion object {
    private const val BACK_PRESSED_INTERVAL = 1000L
  }

  private val drawer: DrawerPen = object : DrawerPen() {

    override fun onCreate(args: Bundle) {
      super.onCreate(args)
      view.setLeftDrawerWidth(R.dimen.threads_scene_left_drawer_width)
      view.setRightDrawerWidth(R.dimen.threads_scene_right_drawer_width)
      view.setLeftDrawerMode(NmbDrawerSide.NONE)
      view.setRightDrawerMode(NmbDrawerSide.ACTION_BAR)
      view.setLeftDrawerShadow(R.drawable.drawer_left_shadow)
      view.setRightDrawerShadow(R.drawable.drawer_right_shadow)
    }

    override fun onOpenRightDrawer() {
      toolbar.view.inflateMenu(R.menu.forum_list)
    }

    override fun onCloseRightDrawer() {
      toolbar.view.inflateMenu(R.menu.threads)
    }
  }

  private val toolbar: ToolbarPen = object : ToolbarPen() {

    override fun onCreate(args: Bundle) {
      super.onCreate(args)
      view.setTitle(R.string.app_name)
      view.setNavigationIcon(R.drawable.menu_white_x24)
      view.inflateMenu(R.menu.threads)
    }

    override fun onClickNavigationIcon() {
      drawer.view.openLeftDrawer()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_sort -> {
          stage?.pushScene(SortForumsScene())
          return true
        }
        else -> return false
      }
    }
  }

  private val threads: ThreadsPen = object : ThreadsPen() {

    override val activity: NmbActivity?
      get() = this@ThreadsScene.activity as? NmbActivity
    override val stage: Stage?
      get() = this@ThreadsScene.stage
  }

  private val forumList: ForumListPen = object : ForumListPen() {

    override fun onSelectForum(forum: Forum?, byUser: Boolean) {
      threads.setForum(forum)

      val title = forum?.displayedName
      if (title != null) {
        toolbar.view.setTitle(title)
      } else {
        toolbar.view.setTitle(R.string.app_name)
      }

      if (byUser) {
        drawer.view.closeDrawers()
      }
    }
  }

  private val pen = pens(drawer, toolbar, threads, forumList)

  // Ui can't container any method with return value
  // But paper can.
  private var drawerPaper: DrawerPaper? = null

  private var lastPressBackTime = 0L

  override fun createPen(): MvpPen<*> = pen

  override fun createPaper(): MvpPaper<*> = papers(pen) {
    drawer(drawer, it) {
      toolbar(toolbar, DrawerPaper.CONTAINER_ID_CONTENT) {
        threads(threads, ToolbarPaper.CONTAINER_ID)
      }
      forumList(forumList, DrawerPaper.CONTAINER_ID_RIGHT)
    }.also { drawerPaper = it }
  }

  override fun onCreate(args: Bundle) {
    super.onCreate(args)
    // Refresh forums every time when ThreadsScene started
    refreshForums()
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    drawerPaper = null
  }

  override fun handleBack(): Boolean {
    if (!super.handleBack()) {
      // Close drawer if it's open
      if (drawerPaper?.isDrawerOpen() ?: false) {
        drawerPaper?.closeDrawers()
        return true
      }
      // Check pressing twice
      val now = System.currentTimeMillis()
      if (lastPressBackTime + BACK_PRESSED_INTERVAL < now) {
        lastPressBackTime = now
        (activity as? NmbActivity)?.snack(R.string.press_twice_exit)
        return true
      }
      return false
    }
    return true
  }
}
