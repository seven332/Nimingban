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

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.drawerlayout.DrawerLayout
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.component.paper.ToolbarUi
import com.hippo.nimingban.component.paper.forumList
import com.hippo.nimingban.component.paper.navigation
import com.hippo.nimingban.component.paper.threads
import com.hippo.nimingban.component.paper.toolbar
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 6/19/2017.
 */

class ThreadsSceneUi(
    private val logic: ThreadsSceneLogic,
    activity: NmbActivity,
    override val inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi() {

  override val view: View
  val drawerLayout: DrawerLayout

  init {
    view = inflater.inflate(R.layout.ui_threads_scene, container, false)
    drawerLayout = view.find(R.id.drawer_layout)
    drawerLayout.setDrawerShadow(R.drawable.drawer_left_shadow, Gravity.LEFT)
    drawerLayout.setDrawerShadow(R.drawable.drawer_right_shadow, Gravity.RIGHT)

    toolbar(logic.threadsToolbarLogic, R.id.drawer_content) {
      threads(logic.threadsLogic, activity, ToolbarUi.CONTAINER_ID)
    }

    navigation(logic.navigationLogic, R.id.left_drawer)

    toolbar(logic.forumListToolbarLogic, R.id.right_drawer) {
      forumList(logic.forumListLogic, ToolbarUi.CONTAINER_ID)
    }

    // Bind the ui to logic
    logic.threadsSceneUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    // Unbind the ui from logic
    logic.threadsSceneUi = null
  }

  fun toggleLeftDrawer() {
    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
      drawerLayout.closeDrawer(Gravity.LEFT)
    } else {
      drawerLayout.openDrawer(Gravity.LEFT)
    }
  }

  fun closeRightDrawer() {
    drawerLayout.closeDrawer(Gravity.RIGHT)
  }
}
