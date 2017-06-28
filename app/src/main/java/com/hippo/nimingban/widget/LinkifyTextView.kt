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
import android.support.v7.widget.AppCompatTextView
import android.text.Spanned
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent

/*
 * Created by Hippo on 6/16/2017.
 */

class LinkifyTextView : AppCompatTextView {

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

  var currentSpan: ClickableSpan? = null
    get() {
      val span = field
      field = null
      return span
    }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    // Let the parent or grandparent of TextView to handles click action.
    // Otherwise click effect like ripple will not work, and if touch area
    // do not contain a url, the TextView will still get MotionEvent.
    // onTouchEven must be called with MotionEvent.ACTION_DOWN for each touch
    // action on it, so we analyze touched url here.
    if (event.action == MotionEvent.ACTION_DOWN) {
      currentSpan = null

      if (text is Spanned) {
        // Get this code from android.text.method.LinkMovementMethod.
        // Work fine !
        var x = event.x.toInt()
        var y = event.y.toInt()

        x -= totalPaddingLeft
        y -= totalPaddingTop

        x += scrollX
        y += scrollY

        val layout = layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())

        val spans = (text as Spanned).getSpans(off, off, ClickableSpan::class.java)

        if (spans.isNotEmpty()) {
          currentSpan = spans[0]
        }
      }
    }

    return super.onTouchEvent(event)
  }
}
