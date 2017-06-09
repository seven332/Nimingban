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

package com.hippo.nimingban.scene

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.android.resource.AttrResources
import com.hippo.nimingban.R
import com.hippo.nimingban.widget.nmb.NmbDrawerContent



/*
 * Created by Hippo on 6/8/2017.
 */

abstract class StatusBarView<V: SceneView<V, P>, P: ScenePresenter<P, V>>(activity: Activity, context: Context) :
    NmbView<V, P>(activity, context), NmbDrawerContent.OnGetWindowPaddingTopListener {

  private var drawerContent: NmbDrawerContent? = null
  private var statusBar: View? = null

  override final fun onCreate(inflater: LayoutInflater, parent: ViewGroup): View {
    val view = inflater.inflate(R.layout.scene_status_bar, parent, false)

    // Update task description color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      val taskDescription = ActivityManager.TaskDescription(
          null, null, AttrResources.getAttrColor(context, R.attr.colorPrimary))
      activity.setTaskDescription(taskDescription)
    }

    val statusBar = view.findViewById(R.id.status_bar)!!
    statusBar.setBackgroundColor(AttrResources.getAttrColor(context, R.attr.colorPrimaryDark))
    this.statusBar = statusBar

    if (parent is NmbDrawerContent) {
      drawerContent = parent
      parent.addOnGetWindowPaddingTopListener(this)
    }

    val container = view.findViewById(R.id.status_bar_content_container) as ViewGroup
    val content = onCreateStatusBarContent(inflater, container)
    container.addView(content)

    return view
  }

  abstract fun onCreateStatusBarContent(inflater: LayoutInflater, parent: ViewGroup): View

  override fun onDestroy() {
    super.onDestroy()
    drawerContent?.removeOnGetWindowPaddingTopListener(this)
  }

  override fun onGetWindowPaddingTop(top: Int) {
    statusBar?.also {
      it.layoutParams.height = top
      it.requestLayout()
    }
  }
}
