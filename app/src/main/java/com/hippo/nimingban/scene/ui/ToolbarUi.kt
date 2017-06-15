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

package com.hippo.nimingban.scene.ui

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity

/*
 * Created by Hippo on 6/14/2017.
 */

class ToolbarUi(
    val child: SceneUi,
    val logic: ToolbarLogic,
    context: Context,
    activity: NmbActivity
) : GroupUi(context, activity) {

  private var toolbar: Toolbar? = null

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.ui_toolbar, container, false)
    toolbar = view.findViewById(R.id.toolbar) as Toolbar

    val childContainer = view.findViewById(R.id.toolbar_content_container) as ViewGroup
    val childView = child.create(inflater, childContainer)
    childContainer.addView(childView, 0)
    addChild(child)

    return view
  }

  fun setTitle(title: CharSequence) {
    toolbar?.title = title
  }
}

/** Wrap a ui in a ToolbarUi **/
fun SceneUi.wrapInToolbar(logic: ToolbarLogic, context: Context, activity: NmbActivity) =
    ToolbarUi(this, logic, context, activity)

/** Wrap a ui in a ToolbarUi **/
fun NmbUi.wrapInToolbar(logic: ToolbarLogic) = ToolbarUi(this, logic, this.context, this.activity)
