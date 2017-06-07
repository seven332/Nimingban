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

import android.graphics.RectF

/*
 * Created by Hippo on 6/5/2017.
 */

const val INVALID_ID = -1

inline fun <R> Boolean.select(correct: () -> R, wrong: () -> R) = if (this) correct() else wrong()

fun RectF.centerTo(x: Float, y: Float) { offset(x - centerX(), y - centerY()) }

inline fun <T> Iterable<T>.forEachAny(action: (T) -> Boolean): Boolean {
  var result = false
  for (element in this) {
    result = action(element) || result
  }
  return result
}
