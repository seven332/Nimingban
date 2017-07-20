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

import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.support.v4.graphics.drawable.DrawableCompat

/*
 * Created by Hippo on 2017/7/18.
 */

fun Drawable.wrap(): Drawable = DrawableCompat.wrap(this)

fun StateListDrawable.addState(drawable: Drawable, vararg states: Int): Unit = addState(states, drawable)
