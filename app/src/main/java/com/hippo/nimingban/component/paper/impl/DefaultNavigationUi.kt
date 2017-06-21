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

import android.support.design.widget.NavigationView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hippo.nimingban.R
import com.hippo.nimingban.component.NmbUi
import com.hippo.nimingban.component.paper.NavigationLogic
import com.hippo.nimingban.component.paper.NavigationUi
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 6/20/2017.
 */

class DefaultNavigationUi(
    val logic: NavigationLogic,
    inflater: LayoutInflater,
    container: ViewGroup
) : NmbUi(), NavigationUi {

  override val view: View

  init {
    view = inflater.inflate(R.layout.ui_navigation, container, false)
    val navigation = view.find<NavigationView>(R.id.navigation)
    val button = view.find<Button>(R.id.button)

    navigation.setNavigationItemSelectedListener { logic.onSelectNavigationItem(it) }
  }
}
