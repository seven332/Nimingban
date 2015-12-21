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

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.hippo.nimingban.R;
import com.hippo.util.AnimationUtils2;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.ViewUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PostLayout extends FrameLayout {

    @IntDef({STATE_NONE, STATE_SHOW, STATE_HIDE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {}

    public static final int STATE_NONE = 0;
    public static final int STATE_SHOW = 1;
    public static final int STATE_HIDE = 2;

    private ViewDragHelper mDragHelper;

    private Drawable mShadowTop;

    private int mShadowHeight;

    private float mX;
    private float mY;

    private int mThreshold;

    private ValueAnimator mHideTypeSendAnimation;
    private ValueAnimator mShowTypeSendAnimation;

    public PostLayout(Context context) {
        super(context);
        init(context);
    }

    public PostLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PostLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        mShadowTop = context.getResources().getDrawable(R.drawable.shadow_top);
        mShadowHeight = LayoutUtils.dp2pix(context, 8);
        mThreshold = LayoutUtils.dp2pix(context, 48);

        mHideTypeSendAnimation = new ValueAnimator();
        mHideTypeSendAnimation.setDuration(300);
        mHideTypeSendAnimation.setInterpolator(AnimationUtils2.FAST_SLOW_INTERPOLATOR);
        mHideTypeSendAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                View view = getChildAt(1);
                if (view != null) {
                    int value = (Integer) animation.getAnimatedValue();
                    view.offsetTopAndBottom(value - view.getTop());
                    ((LayoutParams) view.getLayoutParams()).offsetY = value;
                    invalidate();
                }
            }
        });

        mShowTypeSendAnimation = new ValueAnimator();
        mShowTypeSendAnimation.setDuration(300);
        mShowTypeSendAnimation.setInterpolator(AnimationUtils2.FAST_SLOW_INTERPOLATOR);
        mShowTypeSendAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                View view = getChildAt(1);
                if (view != null) {
                    int value = (Integer) animation.getAnimatedValue();
                    view.offsetTopAndBottom(value - view.getTop());
                    ((LayoutParams) view.getLayoutParams()).offsetY = value;
                    invalidate();
                }
            }
        });
    }

    @State
    public int getTypeSendState() {
        View typeSendView = getChildAt(1);
        if (typeSendView == null) {
            return STATE_NONE;
        } else {
            LayoutParams lp = (LayoutParams) typeSendView.getLayoutParams();
            return lp.hide ? STATE_HIDE : STATE_SHOW;
        }
    }

    public void onRemoveTypeSend() {
        // Reset post view bottom margin
        View postView = getChildAt(0);
        if (postView != null) {
            LayoutParams lp = (LayoutParams) postView.getLayoutParams();
            lp.bottomMargin = 0;
            postView.setLayoutParams(lp);
        }
    }

    public void hideTypeSend() {
        mHideTypeSendAnimation.cancel();
        mShowTypeSendAnimation.cancel();

        View typeSendView = getChildAt(1);
        if (typeSendView == null) {
            return;
        }

        LayoutParams lp = (LayoutParams) typeSendView.getLayoutParams();
        lp.hide = true;

        int start = typeSendView.getTop();
        int end = getHeight() - typeSendView.findViewById(R.id.toolbar).getHeight();
        if (start != end) {
            mHideTypeSendAnimation.setIntValues(start, end);
            mHideTypeSendAnimation.start();
        }

        // Add post view bottom margin
        View postView = getChildAt(0);
        if (postView != null) {
            lp = (LayoutParams) postView.getLayoutParams();
            lp.bottomMargin = typeSendView.findViewById(R.id.toolbar).getHeight();
            postView.setLayoutParams(lp);
        }
    }

    public void showTypeSend() {
        mHideTypeSendAnimation.cancel();
        mShowTypeSendAnimation.cancel();

        View typeSendView = getChildAt(1);
        if (typeSendView == null) {
            return;
        }

        LayoutParams lp = (LayoutParams) typeSendView.getLayoutParams();
        lp.hide = false;

        int start = typeSendView.getTop();
        int end = 0;
        if (start != end) {
            mShowTypeSendAnimation.setIntValues(start, end);
            mShowTypeSendAnimation.start();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        int top = child.getTop();
        if (1 == indexOfChild(child) && top > 0) {
            mShadowTop.setBounds(0, top - mShadowHeight, child.getRight(), top);
            mShadowTop.draw(canvas);
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            if (GONE == child.getVisibility()) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.offsetLeftAndRight(lp.offsetX);
            child.offsetTopAndBottom(lp.offsetY);
        }
    }

    // ime keyboard may change window size
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        View typeSendView = getChildAt(1);
        if (typeSendView != null) {
            LayoutParams lp = (LayoutParams) typeSendView.getLayoutParams();
            if (lp.hide) {
                mHideTypeSendAnimation.cancel();
                mShowTypeSendAnimation.cancel();

                int top = getHeight() - typeSendView.findViewById(R.id.toolbar).getHeight();
                typeSendView.offsetTopAndBottom(top - typeSendView.getTop());
                lp.offsetY = top;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mX = ev.getX();
        mY = ev.getY();
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        } else {
            return mDragHelper.shouldInterceptTouchEvent(ev);
        }
    }

    private void handleTypeSendState() {
        View view = getChildAt(1);
        if (view == null) {
            return;
        }

        int top = view.getTop();
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.hide) {
            if (top < getHeight() - view.findViewById(R.id.toolbar).getHeight() - mThreshold) {
                showTypeSend();
            } else {
                hideTypeSend();
            }
        } else {
            if (top > mThreshold) {
                hideTypeSend();
            } else {
                showTypeSend();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mX = ev.getX();
        mY = ev.getY();
        mDragHelper.processTouchEvent(ev);

        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            handleTypeSendState();
        }

        return true;
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (child.getId() == R.id.type_send) {
                View view = child.findViewById(R.id.toolbar);
                if (view != null) {
                    return ViewUtils.isViewUnder(view, (int) mX, (int) mY - child.getTop(), 0);
                }
            }
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (child.getId() == R.id.type_send) {
                View view = child.findViewById(R.id.toolbar);
                if (view != null) {
                    return MathUtils.clamp(top, 0, getHeight() - view.getHeight());
                }
            }
            return top;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (child.getId() == R.id.type_send) {
                View view = child.findViewById(R.id.toolbar);
                if (view != null) {
                    return getHeight() - view.getHeight();
                }
            }
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView.getId() == R.id.type_send) {
                LayoutParams lp = (LayoutParams) changedView.getLayoutParams();
                lp.offsetY = top;
            }

            // Hide ime keyboard
            Context context = getContext();
            if (context instanceof Activity) {
                View focusView = ((Activity) context).getCurrentFocus();
                if (focusView != null) {
                    InputMethodManager imm = (InputMethodManager)
                            getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                }
            }

            invalidate();
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        public int offsetX = 0;
        public int offsetY = 0;
        public boolean hide = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }
}
