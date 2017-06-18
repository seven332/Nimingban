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

package com.hippo.nimingban.scene.threads

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.drawerlayout.DrawerLayout
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.scene.ui.ForumListUi
import com.hippo.nimingban.scene.ui.GroupUi
import com.hippo.nimingban.scene.ui.NavigationUi
import com.hippo.nimingban.scene.ui.ThreadsUi
import com.hippo.nimingban.scene.ui.ToolbarUi
import com.hippo.nimingban.scene.ui.wrapInToolbar

/*
 * Created by Hippo on 6/14/2017.
 */

class ThreadsSceneUi(
    val logic: ThreadsSceneLogic,
    context: Context,
    activity: NmbActivity
) : GroupUi(context, activity) {

  private var drawerLayout: DrawerLayout? = null

  private var toolbarUi: ToolbarUi? = null

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.ui_threads_scene, container, false)

    drawerLayout = view.findViewById(R.id.drawer_layout) as DrawerLayout

    val drawerContentContainer = view.findViewById(R.id.drawer_content) as ViewGroup
    val drawerContentUi = ThreadsUi(logic, context, activity).wrapInToolbar(logic)
    val drawerContentView = drawerContentUi.create(inflater, drawerContentContainer)
    drawerContentContainer.addView(drawerContentView)
    addChild(drawerContentUi)

    val leftDrawerContainer = view.findViewById(R.id.left_drawer) as ViewGroup
    val leftDrawerUi = NavigationUi(logic, context, activity)
    val leftDrawerView = leftDrawerUi.create(inflater, leftDrawerContainer)
    leftDrawerContainer.addView(leftDrawerView)
    addChild(leftDrawerUi)

    val rightDrawerContainer = view.findViewById(R.id.right_drawer) as ViewGroup
    val rightDrawerUi = ForumListUi(logic, context, activity)
    val rightDrawerView = rightDrawerUi.create(inflater, rightDrawerContainer)
    rightDrawerContainer.addView(rightDrawerView)
    addChild(rightDrawerUi)

    toolbarUi = drawerContentUi

    return view
  }

  fun closeDrawers() {
    drawerLayout?.closeDrawers()
  }

  fun setTitle(title: CharSequence?) {
    toolbarUi?.setTitle(title)
  }

  fun setNavigationIcon(icon: Drawable?) {
    toolbarUi?.setNavigationIcon(icon)
  }

  fun setNavigationOnClickListener(listener: (View) -> Unit) {
    toolbarUi?.setNavigationOnClickListener(listener)
  }

  fun toogleLeftDrawer() {
    drawerLayout?.let { drawerLayout ->
      if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
        drawerLayout.openDrawer(Gravity.LEFT)
      }
    }
  }
}
