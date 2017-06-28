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

package com.hippo.nimingban.widget.nmb

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.hippo.nimingban.R

/*
 * Created by Hippo on 6/14/2017.
 */

class NmbLeftDrawer : LinearLayout {

  private val button: View
  private val navigation: View

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

  init {
    LayoutInflater.from(context).inflate(R.layout.widget_nmb_left_drawer, this)
    button = findViewById(R.id.button)
    navigation = findViewById(R.id.navigation)

    if (navigation is ViewGroup && navigation.childCount > 0) {
      navigation.getChildAt(0).overScrollMode = OVER_SCROLL_NEVER
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    // Make sure the button's width is not larger than the navigation's width
    // Reset LayoutParams
    button.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val buttonWidth = button.measuredWidth
    val navigationWidth = navigation.measuredWidth
    if (buttonWidth > navigationWidth) {
      // Change LayoutParams to make button fit parent
      button.layoutParams.width = navigationWidth
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
  }
}
