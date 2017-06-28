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

package com.hippo.nimingban.widget

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager

/*
 * Created by Hippo on 6/28/2017.
 */

/**
 * Shows soft input when getting focus, hides soft input when losing focus.
 */
class AutoEditText : AppCompatEditText {

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

  init {
    setOnFocusChangeListener { _, hasFocus ->
      val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      if (hasFocus) {
        manager.showSoftInput(this, 0)
      } else if (manager.isActive(this)) {
        manager.hideSoftInputFromWindow(windowToken, 0)
      }
    }
  }
}
