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

import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupPaper
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 2017/7/18.
 */

class BottomToolPaper(
    private val logic: BottomToolLogic
) : GroupPaper<BottomToolPaper>(logic), BottomToolUi {

  companion object {
    const val CONTAINER_ID = R.id.bottom_tool_container
  }

  private lateinit var toolbar: Toolbar

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)

    view = inflater.inflate(R.layout.paper_bottom_tool, container, false)
    toolbar = view.find(R.id.bottom_tool)
    toolbar.setOnMenuItemClickListener { logic.onClickMenuItem(it) }
  }

  override fun inflateMenu(menu: Int) {
    toolbar.menu.clear()
    toolbar.inflateMenu(menu)
  }
}
