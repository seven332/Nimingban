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

package com.hippo.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hippo.animation.SimpleAnimationListener;
import com.hippo.nimingban.R;
import com.hippo.util.AnimationUtils2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Snackbar {

    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;
    private static final int ANIMATION_DURATION = 250;
    private static final int ANIMATION_FADE_DURATION = 180;

    private static final Handler sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_SHOW:
                    ((Snackbar) message.obj).showView();
                    return true;
                case MSG_DISMISS:
                    ((Snackbar) message.obj).hideView(message.arg1);
                    return true;
            }
            return false;
        }
    });

    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;
    private final ViewGroup mParent;
    private final Context mContext;
    private final SnackbarLayout mView;
    private int mDuration;
    private Callback mCallback;

    private Snackbar(ViewGroup parent) {
        mParent = parent;
        mContext = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = ((SnackbarLayout) inflater.inflate(R.layout.design_layout_snackbar, mParent, false));
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text, int duration) {
        Snackbar snackbar = new Snackbar(findSuitableParent(view));
        snackbar.setText(text);
        snackbar.setDuration(duration);
        return snackbar;
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int resId, int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if ((view instanceof FrameLayout)) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                }
                fallback = (ViewGroup) view;
            }
            if (view != null) {
                ViewParent parent = view.getParent();
                view = (parent instanceof View) ? (View) parent : null;
            }
        } while (view != null);
        return fallback;
    }

    @NonNull
    public Snackbar setAction(@StringRes int resId, View.OnClickListener listener) {
        return setAction(mContext.getText(resId), listener);
    }

    @NonNull
    public Snackbar setAction(CharSequence text, final View.OnClickListener listener) {
        TextView tv = mView.getActionView();
        if ((TextUtils.isEmpty(text)) || (listener == null)) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    dispatchDismiss(Callback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(ColorStateList colors) {
        TextView tv = mView.getActionView();
        tv.setTextColor(colors);
        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(@ColorInt int color) {
        TextView tv = mView.getActionView();
        tv.setTextColor(color);
        return this;
    }

    @NonNull
    public Snackbar setText(@NonNull CharSequence message) {
        TextView tv = mView.getMessageView();
        tv.setText(message);
        return this;
    }

    @NonNull
    public Snackbar setText(@StringRes int resId) {
        return setText(mContext.getText(resId));
    }

    @NonNull
    public Snackbar setDuration(int duration) {
        mDuration = duration;
        return this;
    }

    public int getDuration() {
        return mDuration;
    }

    @NonNull
    public View getView() {
        return mView;
    }

    public void show() {
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }

    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    private void dispatchDismiss(int event) {
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    @NonNull
    public Snackbar setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    public boolean isShown() {
        return mView.isShown();
    }

    private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {

        @Override
        public void show() {
            Snackbar.sHandler.sendMessage(Snackbar.sHandler.obtainMessage(MSG_SHOW, Snackbar.this));
        }

        @Override
        public void dismiss(int event) {
            Snackbar.sHandler.sendMessage(Snackbar.sHandler.obtainMessage(MSG_DISMISS, event, 0, Snackbar.this));
        }
    };

    final void showView() {
        if (mView.getParent() == null) {
            mParent.addView(mView);
        }
        if (ViewCompat.isLaidOut(mView)) {
            animateViewIn();
        } else {
            mView.setOnLayoutChangeListener(new Snackbar.SnackbarLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }
    }

    private void animateViewIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.setTranslationY(mView, mView.getHeight());
            ViewCompat.animate(mView).translationY(0.0F).setInterpolator(AnimationUtils2.FAST_SLOW_INTERPOLATOR).setDuration(ANIMATION_DURATION).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(View view) {
                    mView.animateChildrenIn(70, ANIMATION_FADE_DURATION);
                }

                @Override
                public void onAnimationEnd(View view) {
                    if (mCallback != null) {
                        mCallback.onShown(Snackbar.this);
                    }
                    SnackbarManager.getInstance().onShown(mManagerCallback);
                }
            }).start();
        } else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(mView.getContext(), R.anim.design_snackbar_in);
            anim.setInterpolator(AnimationUtils2.FAST_SLOW_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCallback != null) {
                        mCallback.onShown(Snackbar.this);
                    }
                    SnackbarManager.getInstance().onShown(mManagerCallback);
                }
            });
            mView.startAnimation(anim);
        }
    }

    private void animateViewOut(final int event) {
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(mView).translationY(mView.getHeight()).setInterpolator(AnimationUtils2.SLOW_FAST_INTERPOLATOR).setDuration(ANIMATION_DURATION).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(View view) {
                    mView.animateChildrenOut(0, ANIMATION_FADE_DURATION);
                }

                @Override
                public void onAnimationEnd(View view) {
                    onViewHidden(event);
                }
            }).start();
        } else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(mView.getContext(), R.anim.design_snackbar_out);
            anim.setInterpolator(AnimationUtils2.SLOW_FAST_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewHidden(event);
                }
            });
            mView.startAnimation(anim);
        }
    }

    final void hideView(int event) {
        if ((mView.getVisibility() != View.VISIBLE) || (isBeingDragged())) {
            onViewHidden(event);
        } else {
            animateViewOut(event);
        }
    }

    private void onViewHidden(int event) {
        mParent.removeView(mView);
        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }
        SnackbarManager.getInstance().onDismissed(mManagerCallback);
    }

    private boolean isBeingDragged() {
        return false;
    }

    public static abstract class Callback {
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        public void onDismissed(Snackbar snackbar, int event) {
        }

        public void onShown(Snackbar snackbar) {
        }

        @Retention(RetentionPolicy.SOURCE)
        public static @interface DismissEvent {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public static @interface Duration {
    }

    public static class SnackbarLayout extends LinearLayout {

        private TextView mMessageView;
        private Button mActionView;
        private int mMaxWidth;
        private int mMaxInlineActionWidth;
        private OnLayoutChangeListener mOnLayoutChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }

        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
            mMaxInlineActionWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
            if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, a.getDimensionPixelSize(R.styleable.SnackbarLayout_elevation, 0));
            }
            a.recycle();

            setClickable(true);

            LayoutInflater.from(context).inflate(R.layout.design_layout_snackbar_include, this);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mMessageView = ((TextView) findViewById(R.id.snackbar_text));
            mActionView = ((Button) findViewById(R.id.snackbar_action));
        }

        TextView getMessageView() {
            return mMessageView;
        }

        Button getActionView() {
            return mActionView;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if ((mMaxWidth > 0) && (getMeasuredWidth() > mMaxWidth)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            int multiLineVPadding = getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical_2lines);

            int singleLineVPadding = getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical);

            boolean isMultiLine = mMessageView.getLayout().getLineCount() > 1;

            boolean remeasure = false;
            if ((isMultiLine) && (mMaxInlineActionWidth > 0) && (mActionView.getMeasuredWidth() > mMaxInlineActionWidth)) {
                if (updateViewsWithinLayout(1, multiLineVPadding, multiLineVPadding - singleLineVPadding)) {
                    remeasure = true;
                }
            } else {
                int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
                if (updateViewsWithinLayout(0, messagePadding, messagePadding)) {
                    remeasure = true;
                }
            }
            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        void animateChildrenIn(int delay, int duration) {
            ViewCompat.setAlpha(mMessageView, 0.0F);
            ViewCompat.animate(mMessageView).alpha(1.0F).setDuration(duration).setStartDelay(delay).start();
            if (mActionView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mActionView, 0.0F);
                ViewCompat.animate(mActionView).alpha(1.0F).setDuration(duration).setStartDelay(delay).start();
            }
        }

        void animateChildrenOut(int delay, int duration) {
            ViewCompat.setAlpha(mMessageView, 1.0F);
            ViewCompat.animate(mMessageView).alpha(0.0F).setDuration(duration).setStartDelay(delay).start();
            if (mActionView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(mActionView, 1.0F);
                ViewCompat.animate(mActionView).alpha(0.0F).setDuration(duration).setStartDelay(delay).start();
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if ((changed) && (mOnLayoutChangeListener != null)) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        private boolean updateViewsWithinLayout(int orientation, int messagePadTop, int messagePadBottom) {
            boolean changed = false;
            if (orientation != getOrientation()) {
                setOrientation(orientation);
                changed = true;
            }
            if ((mMessageView.getPaddingTop() != messagePadTop) || (mMessageView.getPaddingBottom() != messagePadBottom)) {
                updateTopBottomPadding(mMessageView, messagePadTop, messagePadBottom);
                changed = true;
            }
            return changed;
        }

        private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view, ViewCompat.getPaddingStart(view), topPadding, ViewCompat.getPaddingEnd(view), bottomPadding);
            } else {
                view.setPadding(view.getPaddingLeft(), topPadding, view.getPaddingRight(), bottomPadding);
            }
        }

        interface OnLayoutChangeListener {
            void onLayoutChange(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
        }
    }
}
