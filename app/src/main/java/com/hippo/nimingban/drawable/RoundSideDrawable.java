/*
 * Copyright 2015 Hippo Seven
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

package com.hippo.nimingban.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class RoundSideDrawable extends Drawable {

    private Paint mPaint;
    private RectF mRectF;

    public RoundSideDrawable(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mRectF = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = getBounds();
        int height = rect.height();
        mRectF.set(rect.left, rect.top, rect.left + height, rect.bottom);
        canvas.drawArc(mRectF, 90, 180, true, mPaint);
        canvas.drawRect(rect.left + height / 2, rect.top, rect.right - height / 2, rect.bottom, mPaint);
        mRectF.set(rect.right - height, rect.top, rect.right, rect.bottom);
        canvas.drawArc(mRectF, -90, 180, true, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        if (mPaint.getColorFilter() != null) {
            return PixelFormat.TRANSLUCENT;
        }

        switch (mPaint.getColor() >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }
}
