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
import android.graphics.drawable.Drawable
import com.hippo.nimingban.R
import com.hippo.nimingban.exception.GeneralException
import com.hippo.nimingban.exception.PresetException
import com.hippo.nimingban.string

/*
 * Created by Hippo on 6/8/2017.
 */

/**
 * Explains the exception with a String.
 */
fun explain(e: Throwable): String {
  return when (e) {
    is PresetException -> string(e.text)
    is GeneralException -> e.message
    else -> string(R.string.error_unknown)
  }
}

/**
 * Explains the exception with a Drawable.
 */
fun explainVividly(context: Context, e: Throwable): Drawable {
  val drawable: Int = when (e) {
    is PresetException -> if (e.icon != 0) e.icon else R.drawable.emoticon_sad_primary_x64
    else -> R.drawable.emoticon_sad_primary_x64
  }
  return context.drawable(drawable)
}
