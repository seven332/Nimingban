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

import com.hippo.nimingban.component.NmbLogic
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.paper.ToolbarUi
import com.hippo.nimingban.util.INVALID_ID

/*
 * Created by Hippo on 6/20/2017.
 */

abstract class DefaultToolbarLogic : NmbLogic(), ToolbarLogic {

  var toolbarUi: ToolbarUi? = null
    set(value) {
      field = value
      if (value != null) {
        value.setTitle(title)
        value.setNavigationIcon(icon)
        value.inflateMenu(menu)
      }
    }

  private var title: CharSequence? = null
  private var icon: Int = INVALID_ID
  private var menu: Int = INVALID_ID

  fun setTitle(title: CharSequence?) {
    this.title = title
    toolbarUi?.setTitle(title)
  }

  fun setNavigationIcon(icon: Int) {
    this.icon = icon
    toolbarUi?.setNavigationIcon(icon)
  }

  fun inflateMenu(menu: Int) {
    this.menu = menu
    toolbarUi?.inflateMenu(menu)
  }
}
