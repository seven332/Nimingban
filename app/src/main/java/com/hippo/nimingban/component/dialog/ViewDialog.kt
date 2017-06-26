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

package com.hippo.nimingban.component.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.android.dialog.base.DialogView
import com.hippo.stage.dialog.DialogScene

/*
 * Created by Hippo on 6/26/2017.
 */

abstract class ViewDialog : DialogScene() {

  override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View? {
    val view = onCreateDialogView(inflater, container)
    view.dialog = this
    return view
  }

  /**
   * Create a [DialogView] for this dialog.
   */
  abstract fun onCreateDialogView(inflater: LayoutInflater, container: ViewGroup): DialogView
}
