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

package com.hippo.nimingban.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hippo.yorozuya.LayoutUtils;

public class ThicknessPreviewView extends View {

    private static final float TOUCH_TOLERANCE = 4;

    private boolean mInitPath = false;

    private Path mPath;
    private Paint mPaint;

    private float mX, mY;
    private boolean mIsDot;

    public ThicknessPreviewView(Context context) {
        super(context);
        init();
    }

    public ThicknessPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThicknessPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    public void setThickness(int thickness) {
        mPaint.setStrokeWidth(thickness);
        invalidate();
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mIsDot = true;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mIsDot = false;
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (!mInitPath) {
            mInitPath = true;
            int padding = LayoutUtils.dp2pix(getContext(), 16);
            Path path = mPath;
            path.reset();
            path.moveTo(padding, h - padding);
            path.cubicTo(padding, h / 2, w - padding, h / 2, w - padding,  padding);
            mIsDot = false;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIsDot) {
            canvas.drawPoint(mX, mY, mPaint);
        } else {
            canvas.drawPath(mPath, mPaint);
        }
    }
}
