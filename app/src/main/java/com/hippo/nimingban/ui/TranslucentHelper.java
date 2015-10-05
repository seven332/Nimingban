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

package com.hippo.nimingban.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class TranslucentHelper {

    private static final boolean VALID = Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;

    private int mStatusBarColor = Color.BLACK;
    private int mNavigationBarColor = Color.BLACK;

    private boolean mShowStatusBar = true;
    private boolean mShowNavigationBar = true;

    private TranslucentLayout mTranslucentLayout;

    public void handleContentView(Activity activity) {
        if (VALID) {
            ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
            ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
            decor.removeView(decorChild);
            mTranslucentLayout = new TranslucentLayout(activity);
            mTranslucentLayout.setShowStatusBar(mShowStatusBar);
            mTranslucentLayout.setShowNavigationBar(mShowNavigationBar);
            mTranslucentLayout.setStatusBarColor(mStatusBarColor);
            mTranslucentLayout.setNavigationBarColor(mNavigationBarColor);
            mTranslucentLayout.addView(decorChild);
            decor.addView(mTranslucentLayout);
        }
    }

    public void setStatusBarColor(int color) {
        if (VALID) {
            mStatusBarColor = color;
            if (mTranslucentLayout != null) {
                mTranslucentLayout.setStatusBarColor(color);
            }
        }
    }

    public void setNavigationBarColor(int color) {
        if (VALID) {
            mNavigationBarColor = color;
            if (mTranslucentLayout != null) {
                mTranslucentLayout.setNavigationBarColor(color);
            }
        }
    }

    public void setShowStatusBar(boolean showStatusBar) {
        if (VALID) {
            mShowStatusBar = showStatusBar;
            if (mTranslucentLayout != null) {
                mTranslucentLayout.setShowStatusBar(showStatusBar);
            }
        }
    }

    public void setShowNavigationBar(boolean showNavigationBar) {
        if (VALID) {
            mShowNavigationBar = showNavigationBar;
            if (mTranslucentLayout != null) {
                mTranslucentLayout.setShowNavigationBar(showNavigationBar);
            }
        }
    }

    private static class TranslucentLayout extends ViewGroup {

        private Paint mStatusBarPaint;
        private Paint mNavigationBarPaint;

        private boolean mShowStatusBar = true;
        private boolean mShowNavigationBar = true;

        private int mFitPaddingTop;
        private int mFitPaddingBottom;

        public TranslucentLayout(Context context) {
            super(context);
            init();
        }

        public TranslucentLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public TranslucentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            setWillNotDraw(false);
            setFitsSystemWindows(true);

            mStatusBarPaint = new Paint();
            mNavigationBarPaint = new Paint();
        }

        public void setStatusBarColor(int color) {
            mStatusBarPaint.setColor(color);
            if (mShowStatusBar) {
                invalidate(0, 0, 0, mFitPaddingTop);
            }
        }

        public void setNavigationBarColor(int color) {
            mNavigationBarPaint.setColor(color);
            if (mShowNavigationBar) {
                int height = getHeight();
                if (height > 0) {
                    invalidate(0, height - mFitPaddingBottom, 0, height);
                }
            }
        }

        public void setShowStatusBar(boolean showStatusBar) {
            if (mShowStatusBar != showStatusBar) {
                mShowStatusBar = showStatusBar;
                requestLayout();
            }
        }

        public void setShowNavigationBar(boolean showNavigationBar) {
            if (mShowNavigationBar != showNavigationBar) {
                mShowNavigationBar = showNavigationBar;
                requestLayout();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY)
                throw new IllegalArgumentException(
                        "SlidingDrawerLayout must be measured with MeasureSpec.EXACTLY.");

            setMeasuredDimension(widthSize, heightSize);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    widthSize - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    heightSize - getPaddingTop() - getPaddingBottom() -
                            (mShowStatusBar ? mFitPaddingTop : 0) -
                            (mShowNavigationBar ? mFitPaddingBottom : 0),
                    MeasureSpec.EXACTLY);
            for (int i = 0, n = getChildCount(); i < n; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            for (int i = 0, n = getChildCount(); i < n; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                child.layout(getPaddingLeft(), getPaddingTop() + (mShowStatusBar ? mFitPaddingTop : 0),
                        getWidth() - getPaddingRight(), getHeight() - getPaddingBottom() -
                                (mShowNavigationBar ? mFitPaddingBottom : 0));
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mShowStatusBar && mFitPaddingTop != 0) {
                canvas.drawRect(0, 0, getWidth(), mFitPaddingTop, mStatusBarPaint);
            }
            if (mShowNavigationBar && mFitPaddingBottom != 0) {
                int height = getHeight();
                canvas.drawRect(0, height - mFitPaddingBottom, getWidth(), height, mNavigationBarPaint);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected boolean fitSystemWindows(Rect insets) {
            mFitPaddingTop = insets.top;
            mFitPaddingBottom = insets.bottom;
            insets.top = 0;
            insets.bottom = 0;
            return super.fitSystemWindows(insets);
        }
    }
}
