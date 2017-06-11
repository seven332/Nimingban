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

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity

/*
 * Created by Hippo on 6/8/2017.
 */

abstract class ToolbarView<V: SceneView<V, P>, P: ScenePresenter<P, V>>(
    scene: NmbScene<P, V>,
    activity: NmbActivity,
    context: Context
) : StatusBarView<V, P>(scene, activity, context) {

  var toolbar: Toolbar? = null

  override fun onCreateStatusBarContent(inflater: LayoutInflater, parent: ViewGroup): View {
    val view = inflater.inflate(R.layout.scene_toolbar, parent, false)

    toolbar = view.findViewById(R.id.toolbar) as Toolbar

    val container = view.findViewById(R.id.toolbar_content_container) as ViewGroup
    val content = onCreateToolbarContent(inflater, container)
    container.addView(content, 0)

    return view
  }

  abstract fun onCreateToolbarContent(inflater: LayoutInflater, parent: ViewGroup): View
}
