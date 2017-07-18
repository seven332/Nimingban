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
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.annotation.BoolRes
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.IntegerRes
import android.support.annotation.PluralsRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import com.hippo.android.resource.AttrResources

/*
 * Created by Hippo on 2017/7/18.
 */

fun Context.boolean(@BoolRes id: Int): Boolean = resources.getBoolean(id)

fun Context.integer(@IntegerRes id: Int): Int = resources.getInteger(id)

fun Context.dimension(@DimenRes id: Int): Float = resources.getDimension(id)

fun Context.dimensionPixelOffset(@DimenRes id: Int): Int = resources.getDimensionPixelOffset(id)

fun Context.dimensionPixelSize(@DimenRes id: Int): Int = resources.getDimensionPixelSize(id)

fun Context.color(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Context.colorStateList(@ColorRes id: Int): ColorStateList = AppCompatResources.getColorStateList(this, id)

fun Context.drawable(@DrawableRes id: Int): Drawable = AppCompatResources.getDrawable(this, id)!!

fun Context.string(@StringRes id: Int): String = resources.getString(id)

fun Context.string(@StringRes id: Int, vararg formatArgs: Any): String = resources.getString(id, *formatArgs)

fun Context.quantityString(@PluralsRes id: Int, quantity: Int): String = resources.getQuantityString(id, quantity)

fun Context.quantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String = resources.getQuantityString(id, quantity, *formatArgs)

fun Context.attrBoolean(@AttrRes id: Int): Boolean = AttrResources.getAttrBoolean(this, id)

fun Context.attrInteger(@AttrRes id: Int): Int = AttrResources.getAttrInteger(this, id)

fun Context.attrDimension(@AttrRes id: Int): Float = AttrResources.getAttrDimension(this, id)

fun Context.attrDimensionPixelOffset(@AttrRes id: Int): Int = AttrResources.getAttrDimensionPixelOffset(this, id)

fun Context.attrDimensionPixelSize(@AttrRes id: Int): Int = AttrResources.getAttrDimensionPixelSize(this, id)

fun Context.attrColor(@AttrRes id: Int): Int = AttrResources.getAttrColor(this, id)

fun Context.attrColorStateList(@AttrRes id: Int): ColorStateList = AttrResources.getAttrColorStateList(this, id)

fun Context.attrDrawable(@AttrRes id: Int): Drawable = AttrResources.getAttrDrawable(this, id)
