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

package com.hippo.nimingban.util

import android.content.Context
import android.view.View

/*
 * Created by Hippo on 6/7/2017.
 */

fun Int.dp2pix(context: Context) = (this * context.resources.displayMetrics.density + 0.5f).toInt()

/**
 * Utility to return a default size. Uses the supplied size if the
 * MeasureSpec imposed no constraints. Will get suitable if allowed
 * by the MeasureSpec.

 * @param size Default size for this view
 * *
 * @param spec Constraints imposed by the parent
 * *
 * @return The size this view should be.
 */
fun getSuitableSize(size: Int, spec: Int): Int {
  var result = size
  val specMode = View.MeasureSpec.getMode(spec)
  val specSize = View.MeasureSpec.getSize(spec)

  when (specMode) {
    View.MeasureSpec.UNSPECIFIED -> result = size
    View.MeasureSpec.EXACTLY -> result = specSize
    View.MeasureSpec.AT_MOST -> result = if (size == 0) specSize else Math.min(size, specSize)
  }
  return result
}

inline fun <reified T : View> View.find(id: Int): T = findViewById(id) as T

/**
 * Whether the view is under the point.
 */
fun View.isUnder(localX: Float, localY: Float, slop: Float = 0.0f) =
    localX >= (left -slop)
        && localY >= (top -slop)
        && localX < (right + slop)
        && localY < (bottom + slop)
