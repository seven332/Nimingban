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
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.FloatRange
import android.text.TextUtils

/*
 * Created by Hippo on 6/10/2017.
 */

class TextDrawable : Drawable {

  private var state: TextState

  private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

  private var x: Float = 0.0f
  private var y: Float = 0.0f
  private var textSizeDirty = true

  private var mutated: Boolean = false


  var textColor: Int
    get() = state.textColor
    set(color) {
      if (state.textColor != color) {
        state.textColor = color
        invalidateSelf()
      }
    }

  var backgroundColor: Int
    get() = state.backgroundColor
    set(color) {
      if (state.backgroundColor != color) {
        state.backgroundColor = color
        invalidateSelf()
      }
    }


  constructor(text: String?, @FloatRange(from = 0.0, to = 1.0) contentPercent: Float) {
    state = TextState(text ?: "", contentPercent)
  }

  private constructor(state: TextState) {
    this.state = state
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    backgroundPaint.colorFilter = colorFilter
    textPaint.colorFilter = colorFilter
    invalidateSelf()
  }

  override fun getConstantState(): ConstantState {
    return state
  }

  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun setAlpha(alpha: Int) {}

  override fun mutate(): Drawable {
    if (!mutated && super.mutate() === this) {
      state = TextState(state)
      mutated = true
    }
    return this
  }

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    textSizeDirty = true
  }

  private fun updateTextSizeIfDirty() {
    if (!textSizeDirty) return
    textSizeDirty = false

    val bounds = bounds
    val contentWidth = (bounds.width() * state.contentPercent).toInt()
    val contentHeight = (bounds.height() * state.contentPercent).toInt()
    val widthRatio = contentWidth.toFloat() / state.textBounds.width()
    val heightRatio = contentHeight.toFloat() / state.textBounds.height()
    val ratio = Math.min(widthRatio, heightRatio)
    val textSize = TextState.STANDARD_TEXT_SIZE * ratio
    textPaint.textSize = textSize
    x = (bounds.width() - state.textBounds.width() * ratio) / 2 - state.textBounds.left * ratio
    y = (bounds.height() - state.textBounds.height() * ratio) / 2 - state.textBounds.top * ratio
  }

  override fun draw(canvas: Canvas) {
    if (!bounds.isEmpty) {
      // Draw background
      backgroundPaint.color = state.backgroundColor
      canvas.drawRect(bounds, backgroundPaint)

      if (!TextUtils.isEmpty(state.text)) {
        // Draw text
        updateTextSizeIfDirty()
        textPaint.color = state.textColor
        canvas.drawText(state.text, x, y, textPaint)
      }
    }
  }


  private class TextState : ConstantState {

    companion object {
      internal const val STANDARD_TEXT_SIZE = 1000.0f
      private val STANDARD_PAINT: Paint = Paint()

      init {
        STANDARD_PAINT.textSize = STANDARD_TEXT_SIZE
      }
    }

    internal var textColor: Int = 0
    internal var backgroundColor: Int = 0
    internal var contentPercent: Float = 0.toFloat()
    internal var text: String
    internal var textBounds: Rect

    constructor(text: String, contentPercent: Float) {
      this.text = text
      this.contentPercent = contentPercent
      this.textBounds = Rect()
      STANDARD_PAINT.getTextBounds(text, 0, text.length, this.textBounds)
    }

    constructor(state: TextState) {
      textColor = state.textColor
      backgroundColor = state.backgroundColor
      contentPercent = state.contentPercent
      text = state.text
      textBounds = state.textBounds
    }

    override fun newDrawable(): Drawable = TextDrawable(this)

    override fun getChangingConfigurations() = 0
  }
}
