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

package com.hippo.nimingban.component.paper.impl

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.component.SceneUi
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.paper.ToolbarUi
import com.hippo.nimingban.util.INVALID_ID
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 6/19/2017.
 */

class DefaultToolbarUi(
    val logic: ToolbarLogic,
    inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi(), ToolbarUi {

  override val view: View
  private val context = inflater.context
  private var toolbar: Toolbar

  init {
    view = inflater.inflate(R.layout.ui_toolbar, container, false)

    toolbar = view.find(R.id.toolbar)
    toolbar.setNavigationOnClickListener { logic.onClickNavigationIcon() }
    toolbar.setOnMenuItemClickListener { logic.onClickMenuItem(it) }
  }

  fun <T : SceneUi> addChild(creator: (ViewGroup) -> T) =
      inflateChild(creator, R.id.toolbar_content_container, 0)

  override fun setTitle(title: CharSequence?) {
    toolbar.title = title
  }

  override fun setSubtitle(subtitle: CharSequence?) {
    toolbar.subtitle = subtitle
  }

  override fun setNavigationIcon(resId: Int) {
    if (resId != INVALID_ID) {
      toolbar.navigationIcon = AppCompatResources.getDrawable(context, resId)
    }
  }

  override fun inflateMenu(resId: Int) {
    if (resId != INVALID_ID) {
      toolbar.inflateMenu(resId)
    }
  }
}

inline fun defaultToolbarUi(
    logic: ToolbarLogic,
    inflater: LayoutInflater,
    container: ViewGroup,
    init: DefaultToolbarUi.() -> Unit
): DefaultToolbarUi {
  val defaultToolbarUi = DefaultToolbarUi(logic, inflater, container)
  defaultToolbarUi.init()
  return defaultToolbarUi
}
