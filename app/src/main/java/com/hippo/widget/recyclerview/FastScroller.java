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

package com.hippo.widget.recyclerview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.hippo.animation.SimpleAnimatorListener;
import com.hippo.nimingban.R;
import com.hippo.util.AnimationUtils;
import com.hippo.yorozuya.SimpleHandler;

public class FastScroller extends View {

    private static final int INVALID = -1;

    private static final int SCROLL_BAR_FADE_DURATION = 500;
    private static final int SCROLL_BAR_DELAY = 1000;

    private boolean mDraggable;

    private int mMinHandlerHeight;

    private RecyclerView mRecyclerView;
    private RecyclerView.OnScrollListener mOnScrollChangeListener;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.AdapterDataObserver mAdapterDataObserver;

    private Drawable mHandler;
    private int mHandlerOffset = INVALID;
    private int mHandlerHeight = INVALID;

    private int mLastMotionY = INVALID;

    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mHideAnimator.start();
        }
    };

    public FastScroller(Context context) {
        super(context);
        init(context, null, 0);
    }

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FastScroller, defStyleAttr, 0);
        mHandler = a.getDrawable(R.styleable.FastScroller_handler);
        mDraggable = a.getBoolean(R.styleable.FastScroller_draggable, true);
        a.recycle();

        setAlpha(0.0f);
        setVisibility(INVISIBLE);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mMinHandlerHeight = vc.getScaledScrollBarSize();

        mShowAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f);
        mShowAnimator.setInterpolator(AnimationUtils.FAST_SLOW_INTERPOLATOR);
        mShowAnimator.setDuration(SCROLL_BAR_FADE_DURATION);

        mHideAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.0f);
        mHideAnimator.setInterpolator(AnimationUtils.SLOW_FAST_INTERPOLATOR);
        mHideAnimator.setDuration(SCROLL_BAR_FADE_DURATION);
        mHideAnimator.addListener(new SimpleAnimatorListener() {

            private boolean mCancel;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCancel) {
                    mCancel = false;
                } else {
                    setVisibility(INVISIBLE);
                }
            }
        });
    }

    private void invalidPosition() {
        mHandlerOffset = INVALID;
        mHandlerHeight = INVALID;
    }

    private void updatePosition(boolean show) {
        if (mRecyclerView == null) {
            return;
        }

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int height = getHeight() - paddingTop - paddingBottom;
        int offset = mRecyclerView.computeVerticalScrollOffset();
        int extent = mRecyclerView.computeVerticalScrollExtent();
        int range = mRecyclerView.computeVerticalScrollRange();

        if (height <= 0 || extent >= range || extent <= 0) {
            return;
        }

        int endOffest = height * offset / range;
        int endHeight = height * extent / range;

        endHeight = Math.max(endHeight, mMinHandlerHeight);
        endOffest = Math.min(endOffest, height- endHeight);

        mHandlerOffset = endOffest;
        mHandlerHeight = endHeight;

        if (show) {
            if (mHideAnimator.isRunning()) {
                mHideAnimator.cancel();
                mShowAnimator.start();
            } else if (getVisibility() != VISIBLE && !mShowAnimator.isRunning()) {
                setVisibility(VISIBLE);
                mShowAnimator.start();
            }

            Handler handler = SimpleHandler.getInstance();
            handler.removeCallbacks(mHideRunnable);
            handler.postDelayed(mHideRunnable, SCROLL_BAR_DELAY);
        }
    }

    public void setHandlerDrawable(Drawable drawable) {
        mHandler = drawable;
        invalidate();
    }

    public boolean isDraggable() {
        return mDraggable;
    }

    public void setDraggable(boolean draggable) {
        mDraggable = draggable;
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }

        if (mRecyclerView != null) {
            throw new IllegalStateException("The FastScroller is already attached to a RecyclerView, " +
                    "call detachedFromRecyclerView first");
        }

        mRecyclerView = recyclerView;
        mOnScrollChangeListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){}

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                updatePosition(true);
                invalidate();
            }
        };

        recyclerView.addOnScrollListener(mOnScrollChangeListener);

        mAdapter = recyclerView.getAdapter();
        if (mAdapter != null) {
            mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    updatePosition(false);
                    invalidate();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                    super.onItemRangeChanged(positionStart, itemCount, payload);
                    updatePosition(false);
                    invalidate();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    updatePosition(false);
                    invalidate();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    updatePosition(false);
                    invalidate();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    updatePosition(false);
                    invalidate();
                }
            };
            mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        }
    }

    public void detachedFromRecyclerView() {
        if (mRecyclerView != null && mOnScrollChangeListener != null ) {
            mRecyclerView.removeOnScrollListener(mOnScrollChangeListener);
        }
        mRecyclerView = null;
        mOnScrollChangeListener = null;

        if (mAdapter != null && mAdapterDataObserver != null) {
            mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        }
        mAdapter = null;
        mAdapterDataObserver = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRecyclerView == null || mHandler == null) {
            return;
        }
        if (mHandlerHeight == INVALID) {
            updatePosition(false);
        }
        if (mHandlerHeight == INVALID) {
            return;
        }

        int paddingLeft = getPaddingLeft();
        int saved = canvas.save();
        canvas.translate(paddingLeft, getPaddingTop() + mHandlerOffset);
        mHandler.setBounds(0, 0, getWidth() - paddingLeft - getPaddingRight(), mHandlerHeight);
        mHandler.draw(canvas);
        canvas.restoreToCount(saved);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDraggable || getVisibility() != VISIBLE || mRecyclerView == null || mHandlerHeight == INVALID) {
            return false;
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                int y = (int) event.getY();
                if (y < mHandlerOffset || y >= mHandlerOffset + mHandlerHeight) {
                    // the point out of handler, make the point in handler center
                    int range = mRecyclerView.computeVerticalScrollRange();
                    if (range > 0) {
                        int scroll = range * (y - (mHandlerOffset + mHandlerHeight / 2)) / (getHeight() - getPaddingTop() - getPaddingBottom());
                        mRecyclerView.scrollBy(0, scroll);
                    }
                }
                mLastMotionY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                int range = mRecyclerView.computeVerticalScrollRange();
                if (range <= 0) {
                    break;
                }
                int y = (int) event.getY();
                int scroll = range * (y - mLastMotionY) / (getHeight() - getPaddingTop() - getPaddingBottom());
                mRecyclerView.scrollBy(0, scroll);
                mLastMotionY = y;
                break;
            }
        }

        return true;
    }
}
