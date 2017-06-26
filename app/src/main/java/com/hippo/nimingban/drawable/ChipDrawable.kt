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

package com.hippo.nimingban.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/*
 * Created by Hippo on 6/26/2017.
 */

class ChipDrawable : Drawable {

  private var state: ChipState
  private var mutated: Boolean = false

  private val rect: RectF = RectF()
  private val path: Path = Path()

  var color: Int
    get() = state.paint.color
    set(value) { state.paint.color = value }

  constructor(color: Int) {
    this.state = ChipState(color)
  }

  private constructor(state: ChipState) {
    this.state = state
  }

  override fun draw(canvas: Canvas) {
    val path = path
    val rect = rect
    val bounds = bounds
    val height = bounds.height()

    path.rewind()

    path.moveTo((height / 2).toFloat(), height.toFloat())
    rect.set(bounds.left.toFloat(), bounds.top.toFloat(),
        (bounds.left + height).toFloat(), bounds.bottom.toFloat())
    path.arcTo(rect, 90.0f, 180.0f)

    path.lineTo((bounds.right - height / 2).toFloat(), 0.0f)

    rect.set((bounds.right - height).toFloat(), bounds.top.toFloat(),
        bounds.right.toFloat(), bounds.bottom.toFloat())
    path.arcTo(rect, -90.0f, 180.0f)

    path.close()

    canvas.drawPath(path, state.paint)
  }

  override fun setAlpha(alpha: Int) {
    state.alpha = alpha
    invalidateSelf()
  }

  override fun getOpacity(): Int {
    if (state.paint.colorFilter != null) {
      return PixelFormat.TRANSLUCENT
    }

    return when (state.color ushr 24) {
      255, 0 -> PixelFormat.TRANSPARENT
      else -> PixelFormat.TRANSLUCENT
    }
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    state.paint.colorFilter = colorFilter
    invalidateSelf()
  }

  override fun getConstantState(): ConstantState = state

  override fun mutate(): Drawable {
    if (!mutated && super.mutate() === this) {
      state = ChipState(state)
      mutated = true
    }
    return this
  }


  private class ChipState : ConstantState {

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private var baseColor: Int = 0 // base color, independent of setAlpha()
    private var useColor: Int = 0  // base color modulated by setAlpha()

    var color: Int
      get() = useColor
      set(value) {
        if (baseColor != value || useColor != value) {
          baseColor = value
          useColor = value
          paint.color = useColor
        }
      }

    var alpha: Int
      get() = useColor ushr 24
      set(value) {
        val alpha = value + (value shr 7)   // make it 0..256
        val baseAlpha = baseColor ushr 24
        val useAlpha = baseAlpha * alpha shr 8
        useColor = (baseColor shl 8).ushr(8) or (useAlpha shl 24)
        paint.color = useColor
      }

    constructor(color: Int) {
      this.color = color
    }

    constructor(state: ChipState) {
      this.color = state.paint.color
    }

    override fun newDrawable() = ChipDrawable(this)

    override fun getChangingConfigurations() = 0
  }
}
