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
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.component.SceneUi
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 6/19/2017.
 */

class ThreadsSceneUiImpl(
    inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi(), ThreadsSceneUi {

  override val view: View
  val drawerLayout: DrawerLayout

  init {
    view = inflater.inflate(R.layout.ui_threads_scene, container, false)
    drawerLayout = view.find(R.id.drawer_layout)
  }

  fun <T : SceneUi> setContentUi(creator: (ViewGroup) -> T) =
      inflateChild(creator, R.id.drawer_content, -1)

  fun <T : SceneUi> setLeftUi(creator: (ViewGroup) -> T) =
      inflateChild(creator, R.id.left_drawer, -1)

  fun <T : SceneUi> setRightUi(creator: (ViewGroup) -> T) =
      inflateChild(creator, R.id.right_drawer, -1)

  override fun toggleLeftDrawer() {
    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
      drawerLayout.closeDrawer(Gravity.LEFT)
    } else {
      drawerLayout.openDrawer(Gravity.LEFT)
    }
  }

  override fun closeRightDrawer() {
    drawerLayout.closeDrawer(Gravity.RIGHT)
  }
}

inline fun threadsSceneUiImpl(
    inflater: LayoutInflater,
    container: ViewGroup,
    init: ThreadsSceneUiImpl.() -> Unit
): ThreadsSceneUiImpl {
  val threadsSceneUiImpl = ThreadsSceneUiImpl(inflater, container)
  threadsSceneUiImpl.init()
  return threadsSceneUiImpl
}
