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
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.content.res.AppCompatResources
import com.hippo.html.Html

/*
 * Created by Hippo on 6/5/2017.
 */

const val INVALID_ID = 0

const val INVALID_INDEX = -1

inline fun <T, R> T?.select(nonNullAction: (T) -> R, nullAction: () -> R) =
    if (this != null) nonNullAction(this) else nullAction()

fun RectF.centerTo(x: Float, y: Float) { offset(x - centerX(), y - centerY()) }

inline fun <T> Iterable<T>.forEachAny(action: (T) -> Boolean): Boolean {
  var result = false
  for (element in this) {
    result = action(element) || result
  }
  return result
}

fun String.fromHtml() = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)!!

fun <T> Parcel.readTypedList(creator: Parcelable.Creator<T>): MutableList<T> {
  val list = mutableListOf<T>()
  readTypedList(list, creator)
  return list
}

fun Context.loadDrawable(resId: Int) = AppCompatResources.getDrawable(this, resId)!!

fun StateListDrawable.addState(drawable: Drawable, vararg states: Int) = addState(states, drawable)
