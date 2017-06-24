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

package com.hippo.nimingban.component.paper

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.util.INVALID_ID
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 6/19/2017.
 */

class ToolbarUi(
    private val logic: ToolbarLogic,
    override val inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi() {

  companion object {
    const val CONTAINER_ID = R.id.toolbar_content_container
  }

  override val view: View
  private val context = inflater.context
  private var toolbar: Toolbar

  init {
    view = inflater.inflate(R.layout.ui_toolbar, container, false)

    toolbar = view.find(R.id.toolbar)
    toolbar.setNavigationOnClickListener { logic.onClickNavigationIcon() }
    toolbar.setOnMenuItemClickListener { logic.onClickMenuItem(it) }

    // Bind the ui to logic
    logic.toolbarUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    // Unbind the ui from logic
    logic.toolbarUi = null
  }

  fun setTitle(title: CharSequence?) {
    toolbar.title = title
  }

  fun setSubtitle(subtitle: CharSequence?) {
    toolbar.subtitle = subtitle
  }

  fun setNavigationIcon(resId: Int) {
    if (resId != INVALID_ID) {
      toolbar.navigationIcon = AppCompatResources.getDrawable(context, resId)
    }
  }

  fun inflateMenu(resId: Int) {
    if (resId != INVALID_ID) {
      toolbar.inflateMenu(resId)
    }
  }
}
