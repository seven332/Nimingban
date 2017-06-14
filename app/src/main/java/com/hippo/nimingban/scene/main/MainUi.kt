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

package com.hippo.nimingban.scene.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.scene.NmbUi
import com.hippo.nimingban.scene.navigation.NavigationScene
import com.hippo.nimingban.scene.threads.ThreadsScene

/*
 * Created by Hippo on 6/14/2017.
 */

class MainUi(
    scene: MainScene,
    activity: NmbActivity,
    context: Context
) : NmbUi<MainUi, MainScene>(scene, activity, context) {

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.ui_main, container, false)

    val director = scene.hireChildDirector()

    val content = director.direct(view.findViewById(R.id.drawer_content) as ViewGroup)
    if (content.sceneCount == 0) {
      val threadsScene = ThreadsScene()
      threadsScene.parent = scene
      content.pushScene(threadsScene)
    }

    val leftDrawer = director.direct(view.findViewById(R.id.left_drawer) as ViewGroup)
    if (leftDrawer.sceneCount == 0) {
      val navigationScene = NavigationScene()
      navigationScene.parent = scene
      leftDrawer.pushScene(navigationScene)
    }

    return view
  }
}
