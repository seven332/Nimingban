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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.hippo.nimingban.R;
import com.hippo.vector.PathParser;

public class GestureView extends View {

    public static final int GESTURE_NONE = -1;
    public static final int GESTURE_SWIPE_LEFT = 0;
    public static final int GESTURE_SWIPE_RIGHT = 1;

    private int mGesture = GESTURE_NONE;

    private int mCircleRadius;
    private int mArrowSize;
    private int mInterval;

    private Paint mPaint;
    private Path mPath;
    private Path mArrowPath;
    private Matrix mMatrix;

    public GestureView(Context context) {
        super(context);
        init(context);
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        Resources resources = context.getResources();
        mCircleRadius = resources.getDimensionPixelOffset(R.dimen.gesture_circle_radius);
        mPath = PathParser.createPathFromPathData(resources.getString(R.string.pd_arrow_left));
        mInterval = resources.getDimensionPixelOffset(R.dimen.gesture_circle_arrow_interval);

        float scale = context.getResources().getDisplayMetrics().density * 2;
        mMatrix = new Matrix();
        mMatrix.setScale(scale, scale);
        mPath.transform(mMatrix);
        mArrowSize = (int) (scale * 24);
        mArrowPath = new Path();
    }

    public void setGesture(int gesture) {
        mGesture = gesture;

        switch (gesture) {
            case GESTURE_SWIPE_LEFT:
                mArrowPath.set(mPath);
                break;
            case GESTURE_SWIPE_RIGHT:
                mMatrix.reset();
                mMatrix.setRotate(180, mArrowSize / 2, mArrowSize / 2);
                mArrowPath.set(mPath);
                mArrowPath.transform(mMatrix);
                break;
        }

        requestLayout();
    }

    public int getGesture() {
        return mGesture;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    private void drawSwipeLeft(Canvas canvas) {
        canvas.drawPath(mArrowPath, mPaint);
        int saved = canvas.save();
        canvas.translate(mArrowSize + mInterval, 0);
        canvas.drawCircle(mCircleRadius, mCircleRadius, mCircleRadius, mPaint);
        canvas.restoreToCount(saved);
    }

    private void drawSwipeRight(Canvas canvas) {
        canvas.drawCircle(mCircleRadius, mCircleRadius, mCircleRadius, mPaint);
        int saved = canvas.save();
        canvas.translate(mCircleRadius * 2 + mInterval, 0);
        canvas.drawPath(mArrowPath, mPaint);
        canvas.restoreToCount(saved);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mGesture) {
            case GESTURE_SWIPE_LEFT:
                drawSwipeLeft(canvas);
                break;
            case GESTURE_SWIPE_RIGHT:
                drawSwipeRight(canvas);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        switch (mGesture) {
            case GESTURE_SWIPE_LEFT:
                setMeasuredDimension(mCircleRadius * 2 + mInterval + mArrowSize, mCircleRadius * 2);
                break;
            case GESTURE_SWIPE_RIGHT:
                setMeasuredDimension(mCircleRadius * 2 + mInterval + mArrowSize, mCircleRadius * 2);
                break;
            default:
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
