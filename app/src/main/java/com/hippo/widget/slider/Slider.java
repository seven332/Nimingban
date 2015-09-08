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

package com.hippo.widget.slider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsoluteLayout;
import android.widget.PopupWindow;

import com.hippo.nimingban.R;
import com.hippo.util.AnimationUtils;
import com.hippo.util.PathParser;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.SimpleHandler;

public class Slider extends View {

    private static char[] CHARACTERS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private Context mContext;

    private Paint mPaint;

    private PopupWindow mPopup;
    private BubbleView mBubble;

    private int mStart;
    private int mEnd;
    private int mProgress;
    private float mPercent;
    private int mDrawProgress;
    private float mDrawPercent;
    private int mTargetProgress;

    private float mThickness;
    private float mRadius;

    private float mCharWidth;
    private float mCharHeight;

    private int mBubbleWidth;
    private int mBubbleHeight;
    private int mBubbleMinWidth;
    private int mBubbleMinHeight;

    private int mPopupX;
    private int mPopupY;
    private int mPopupWidth;

    private int[] mTemp = new int[2];

    private boolean mReverse = false;

    private boolean mShowBubble;

    private float mDrawBubbleScale = 0f;

    private FloatAnimation mProgressAnimation;
    private FloatAnimation mBubbleScaleAnimation;

    private OnSetProgressListener mListener;

    private CheckForShowBubble mCheckForShowBubble;

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);

        mBubbleMinWidth = LayoutUtils.dp2pix(context, 26);
        mBubbleMinHeight = LayoutUtils.dp2pix(context, 32);

        mBubble = new BubbleView(context, textPaint);
        mBubble.setScaleX(0.0f);
        mBubble.setScaleY(0.0f);
        //noinspection deprecation
        AbsoluteLayout absoluteLayout = new AbsoluteLayout(context);
        absoluteLayout.addView(mBubble);
        //noinspection deprecation
        absoluteLayout.setBackgroundDrawable(null);
        mPopup = new PopupWindow(absoluteLayout);
        mPopup.setBackgroundDrawable(null);
        mPopup.setOutsideTouchable(true);
        mPopup.setTouchable(false);
        mPopup.setFocusable(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Slider);
        textPaint.setColor(a.getColor(R.styleable.Slider_textColor, Color.WHITE));
        textPaint.setTextSize(a.getDimensionPixelSize(R.styleable.Slider_textSize, 12));

        updateTextSize();

        setRange(a.getInteger(R.styleable.Slider_start, 0), a.getInteger(R.styleable.Slider_end, 0));
        setProgress(a.getInteger(R.styleable.Slider_slider_progress, 0));
        mThickness = a.getDimension(R.styleable.Slider_thickness, 2);
        mRadius = a.getDimension(R.styleable.Slider_radius, 6);
        setColor(a.getColor(R.styleable.Slider_color, Color.BLACK));
        a.recycle();

        mProgressAnimation = new FloatAnimation() {
            @Override
            protected void onCalculate(float progress) {
                super.onCalculate(progress);
                mDrawPercent = get();
                mDrawProgress = Math.round(MathUtils.lerp((float) mStart, mEnd, mDrawPercent));
                updateBubblePosition();
                mBubble.setProgress(mDrawProgress);
            }
        };
        mProgressAnimation.setInterpolator(AnimationUtils.FAST_SLOW_INTERPOLATOR);

        mBubbleScaleAnimation = new FloatAnimation() {
            @Override
            protected void onCalculate(float progress) {
                super.onCalculate(progress);
                mDrawBubbleScale = get();
                mBubble.setScaleX(mDrawBubbleScale);
                mBubble.setScaleY(mDrawBubbleScale);
            }
        };
    }

    private void updateTextSize() {
        int length = CHARACTERS.length;
        float[] widths = new float[length];
        mPaint.getTextWidths(CHARACTERS, 0, length, widths);
        mCharWidth = 0.0f;
        for (float f : widths) {
            mCharWidth = Math.max(mCharWidth, f);
        }

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        float charOffsetY = -fm.top;
        mCharHeight = fm.bottom - fm.top;
        mBubble.setCharInfo(charOffsetY);
    }

    private void updateBubbleSize() {
        int oldWidth = mBubbleWidth;
        int oldHeight = mBubbleHeight;
        mBubbleWidth = (int) Math.max(mBubbleMinWidth,
                Integer.toString(mEnd).length() * mCharWidth + LayoutUtils.dp2pix(mContext, 8));
        mBubbleHeight = (int) Math.max(mBubbleMinHeight,
                mCharHeight + LayoutUtils.dp2pix(mContext, 8));

        if (oldWidth != mBubbleWidth && oldHeight != mBubbleHeight) {
            //noinspection deprecation
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) mBubble.getLayoutParams();
            lp.width = mBubbleWidth;
            lp.height = mBubbleHeight;
            mBubble.setLayoutParams(lp);
        }
    }

    private void updatePopup() {
        int width = getWidth();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        getLocationInWindow(mTemp);

        mPopupWidth = (int) (width - mRadius - mRadius + mBubbleWidth);
        int popupHeight = mBubbleHeight;
        mPopupX = (int) (mTemp[0] + mRadius - (mBubbleWidth / 2));
        mPopupY = (int) (mTemp[1] - popupHeight + paddingTop +
                ((getHeight() - paddingTop - paddingBottom) / 2) -
                mRadius -LayoutUtils.dp2pix(mContext, 2));

        mPopup.update(mPopupX, mPopupY, mPopupWidth, popupHeight, false);
    }

    private void updateBubblePosition() {
        float x = ((mPopupWidth - mBubbleWidth) * mDrawPercent);
        mBubble.setX(x);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updatePopup();
        updateBubblePosition();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mPopup.showAtLocation(this, Gravity.TOP|Gravity.LEFT, mPopupX, mPopupY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mPopup.dismiss();
    }

    private void startProgressAnimation(float percent) {
        boolean running = mProgressAnimation.isRunning();
        mProgressAnimation.forceStop();
        mProgressAnimation.setRange(mDrawPercent, percent);
        mProgressAnimation.setDuration(Math.min(500, (long) (20 * getWidth() * Math.abs(mDrawPercent - percent))));
        // Avoid fast swipe to block changing
        long startTime = mProgressAnimation.getLastFrameTime();
        if (running && startTime > 0) {
            mProgressAnimation.startAt(startTime);
        } else {
            mProgressAnimation.startNow();
        }
        invalidate();
    }

    private void startShowBubbleAnimation() {
        mBubbleScaleAnimation.forceStop();
        mBubbleScaleAnimation.setRange(mDrawBubbleScale, 1.0f);
        mBubbleScaleAnimation.setInterpolator(AnimationUtils.FAST_SLOW_INTERPOLATOR);
        mBubbleScaleAnimation.setDuration((long) (300 * Math.abs(mDrawBubbleScale - 1.0f)));
        mBubbleScaleAnimation.start();
        invalidate();
    }

    private void startHideBubbleAnimation() {
        mBubbleScaleAnimation.forceStop();
        mBubbleScaleAnimation.setRange(mDrawBubbleScale, 0.0f);
        mBubbleScaleAnimation.setInterpolator(AnimationUtils.SLOW_FAST_INTERPOLATOR);
        mBubbleScaleAnimation.setDuration((long) (300 * Math.abs(mDrawBubbleScale - 0.0f)));
        mBubbleScaleAnimation.start();
        invalidate();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        mBubble.setColor(color);
        invalidate();
    }

    public void setRange(int start, int end) {
        mStart = start;
        mEnd = end;
        setProgress(mProgress);

        updateBubbleSize();
    }

    public void setProgress(int progress) {
        progress = MathUtils.clamp(progress, mStart, mEnd);
        if (mProgress != progress) {
            int oldProgress = mProgress;
            mProgress = progress;
            mPercent = MathUtils.delerp(mStart, mEnd, mProgress);
            mTargetProgress = progress;

            if (mProgressAnimation == null) {
                // For init
                mDrawPercent = mPercent;
                mDrawProgress = mProgress;
                updateBubblePosition();
                mBubble.setProgress(mDrawProgress);
            } else {
                startProgressAnimation(mPercent);
            }

            if (mListener != null) {
                mListener.onSetProgress(this, progress, oldProgress, false, true);
            }
            invalidate();
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setReverse(boolean reverse) {
        if (mReverse != reverse) {
            mReverse = reverse;
            invalidate();
        }
    }

    public void setOnSetProgressListener(OnSetProgressListener listener) {
        mListener = listener;
    }

    private void update() {
        boolean invalidate;

        long time = SystemClock.uptimeMillis();
        invalidate = mProgressAnimation.calculate(time);
        invalidate |= mBubbleScaleAnimation.calculate(time);

        if (invalidate) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        update();

        int width = getWidth();
        int height = getHeight();
        if (width < LayoutUtils.dp2pix(mContext, 24)) {
            canvas.drawRect(0, 0, width, getHeight(), mPaint);
        } else {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            float thickness = mThickness;
            float radius = mRadius;
            float halfThickness = thickness / 2;

            int saved = canvas.save();

            canvas.translate(0, paddingTop + ((height - paddingTop - paddingBottom) / 2));

            // Draw bar
            canvas.drawRect(paddingLeft + radius, -halfThickness,
                    width - paddingRight - radius, halfThickness, mPaint);

            float currentX = paddingLeft + radius + (width - radius - radius - paddingLeft - paddingRight) *
                    (mReverse ? (1.0f - mDrawPercent) : mDrawPercent);

            float scale = 1.0f - mDrawBubbleScale;
            if (scale != 0.0f) {
                canvas.scale(scale, scale, currentX, 0);
                canvas.drawCircle(currentX, 0, radius, mPaint);
            }

            canvas.restoreToCount(saved);
        }
    }

    private void setShowBubble(boolean showBubble) {
        if (mShowBubble != showBubble) {
            mShowBubble = showBubble;
            if (showBubble) {
                startShowBubbleAnimation();
            } else {
                startHideBubbleAnimation();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int paddingLeft = getPaddingLeft();
                int paddingRight = getPaddingRight();
                float radius = mRadius;
                float x = event.getX();
                int progress = Math.round(MathUtils.lerp((float) mStart, (float) mEnd,
                        MathUtils.clamp((mReverse ? (getWidth() - paddingLeft - radius - x) : (x - radius - paddingLeft)) /
                                (getWidth() - radius - radius - paddingLeft - paddingRight), 0.0f, 1.0f)));
                float percent = MathUtils.delerp(mStart, mEnd, progress);

                // ACTION_CANCEL not changed
                if (action == MotionEvent.ACTION_CANCEL) {
                    progress = mProgress;
                    percent = mPercent;
                }

                if (mTargetProgress != progress) {
                    mTargetProgress = progress;
                    startProgressAnimation(percent);

                    if (mListener != null) {
                        mListener.onSetProgress(this, progress, progress, true, false);
                    }
                }

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    SimpleHandler.getInstance().removeCallbacks(mCheckForShowBubble);
                    setShowBubble(false);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    if (mCheckForShowBubble == null) {
                        mCheckForShowBubble = new CheckForShowBubble();
                    }
                    SimpleHandler.getInstance().postDelayed(mCheckForShowBubble, ViewConfiguration.getTapTimeout());
                }

                if (action == MotionEvent.ACTION_UP) {
                    if (mProgress != progress) {
                        int oldProgress = mProgress;
                        mProgress = progress;
                        mPercent = mDrawPercent;

                        if (mListener != null) {
                            mListener.onSetProgress(this, progress, oldProgress, true, true);
                        }
                    }
                }
                break;
        }

        return true;
    }

    @SuppressLint("ViewConstructor")
    private static class BubbleView extends View {

        private static final float TEXT_CENTER = (float) BubbleDrawable.WIDTH / 2.0f / (float) BubbleDrawable.HEIGHT;

        private Paint mTextPaint;
        private BubbleDrawable mDrawable;

        private String mProgressStr = "";

        private float mCharOffsetY;

        @SuppressWarnings("deprecation")
        public BubbleView(Context context, Paint paint) {
            super(context);
            mDrawable = new BubbleDrawable();
            setBackgroundDrawable(mDrawable);
            mTextPaint = paint;
        }

        public void setColor(int color) {
            mDrawable.setColor(color);
        }

        public void setCharInfo(float offsetY) {
            mCharOffsetY = offsetY;
        }

        public void setProgress(int progress) {
            String str = Integer.toString(progress);
            if (!str.equals(mProgressStr)) {
                mProgressStr = str;
                invalidate();
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            setPivotX(w / 2);
            setPivotY(h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            int x = width / 2;
            int y = (int) (height * TEXT_CENTER + mCharOffsetY);
            canvas.drawText(mProgressStr, x, y, mTextPaint);
        }
    }

    private static class BubbleDrawable extends Drawable {

        private static final String PATH_DATA = "M13,0c7.2,0,13,5.9,13,13.2c0,3.7-2,7.6-7.3,12.9L13,32l-5.8-5.8C2,20.8,0,16.9,0,13.3C0,5.9,5.8,0,13,0z";
        private static final int WIDTH = 26;
        private static final int HEIGHT = 32;

        private Path mPath;
        private Paint mPaint;

        private Matrix mMatrix;
        private Path mRenderPath;

        public BubbleDrawable() {
            mPath = PathParser.createPathFromPathData(PATH_DATA);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMatrix = new Matrix();
            mRenderPath = new Path();
        }

        @Override
        public int getIntrinsicWidth() {
            return WIDTH;
        }

        @Override
        public int getIntrinsicHeight() {
            return HEIGHT;
        }

        public void setColor(int color) {
            mPaint.setColor(color);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);

            mMatrix.reset();
            mMatrix.postScale((float) bounds.width() / (float) WIDTH, (float) bounds.height() / (float) HEIGHT);
            mRenderPath.reset();
            mRenderPath.addPath(mPath, mMatrix);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(mRenderPath, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            // Empty
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            // Empty
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    public interface OnSetProgressListener {

        void onSetProgress(Slider slider, int newProgress, int oldProgress, boolean byUser, boolean confirm);
    }

    private final class CheckForShowBubble implements Runnable {

        @Override
        public void run() {
            setShowBubble(true);
        }
    }
}
