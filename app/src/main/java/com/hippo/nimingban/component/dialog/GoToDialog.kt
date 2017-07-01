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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hippo.android.dialog.base.DialogView
import com.hippo.android.dialog.base.DialogViewBuilder
import com.hippo.nimingban.R
import com.hippo.nimingban.util.find
import com.hippo.nimingban.util.inflate
import com.hippo.nimingban.widget.Slider

/*
 * Created by Hippo on 6/30/2017.
 */

class GoToDialog : NmbDialog() {

  companion object {
    const val KEY_MIN = "GoToDialog:min"
    const val KEY_MAX = "GoToDialog:max"
    const val KEY_PROGRESS = "GoToDialog:progress"
  }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
  }

  override fun onCreateDialogView(inflater: LayoutInflater, container: ViewGroup): DialogView {
    val content = inflater.inflate(R.layout.dialog_go_to)
    val slider = content.find<Slider>(R.id.slider)
    val minView = content.find<TextView>(R.id.min)
    val maxView = content.find<TextView>(R.id.max)

    val args = this.args
    if (args != null) {
      val min = args.getInt(KEY_MIN) + 1
      val max = args.getInt(KEY_MAX)
      slider.setRange(min, max)
      slider.setProgress(args.getInt(KEY_PROGRESS), false)

      minView.text = min.toString()
      maxView.text = max.toString()
    }

    return DialogViewBuilder()
        .title(R.string.go_to_title)
        .customContent(content, false)
        .positiveButton(android.R.string.ok) { dialog, _ ->
          val target = this.target
          if (target is OnGoToListener) {
            target.onGoTo(slider.progress - 1)
          }
          dialog.dismiss()
        }
        .build(inflater, container)
  }


  interface OnGoToListener {
    fun onGoTo(page: Int)
  }
}
