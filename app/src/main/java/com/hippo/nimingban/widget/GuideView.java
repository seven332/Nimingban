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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.nimingban.R;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ViewUtils;

public class GuideView extends ViewGroup {

    private GestureView mGestureView;
    private TextView mMessage;
    private TextView mButton;

    private int mGestureViewLeft;
    private int mGestureViewTop;
    private int mMessageLeft;
    private int mMessageTop;

    private int mGravity = Gravity.NO_GRAVITY;
    private int mX;
    private int mY;

    public GuideView(Context context) {
        super(context);
        init(context);
    }

    public GuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClickable(true);
        setSoundEffectsEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(context);
        mGestureView = new GestureView(context);
        mMessage = (TextView) inflater.inflate(R.layout.guide_message, null);
        mButton = (TextView) inflater.inflate(R.layout.guide_button, null);

        addView(mGestureView);
        addView(mMessage);
        addView(mButton);
    }

    public void setGesturePosition(int gravity) {
        mGravity = gravity;
    }

    public void setGesturePosition(int x, int y) {
        mX = x;
        mY = y;
    }

    public void setGesture(int gesture) {
        mGestureView.setGesture(gesture);
    }

    public void setColor(int color) {
        mGestureView.setColor(color);
        mMessage.setBackgroundColor(color);
        mButton.setBackgroundColor(color);
    }

    public void setMessage(CharSequence text) {
        mMessage.setText(text);
    }

    public void setButton(CharSequence text) {
        mButton.setText(text);
    }

    public void setOnDissmisListener(final View.OnClickListener listener) {
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.removeFromParent(GuideView.this);
                if (listener != null) {
                    listener.onClick(GuideView.this);
                }
            }
        });
    }

    private void calcGesturePosition(int widthMeasureSpec, int heightMeasureSpec) {
        int gestureWidth = mGestureView.getMeasuredWidth();
        int gestureHeight = mGestureView.getMeasuredHeight();

        if (mGravity == Gravity.NO_GRAVITY) {
            mGestureViewLeft = mX - (gestureWidth / 2);
            mGestureViewTop = mY - (gestureHeight / 2);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            if ((mGravity & Gravity.LEFT) == Gravity.LEFT) {
                mGestureViewLeft = paddingLeft;
            } else if ((mGravity & Gravity.RIGHT) == Gravity.RIGHT) {
                mGestureViewLeft = width - paddingRight - gestureWidth;
            } else {
                mGestureViewLeft = ((width - paddingLeft - paddingRight) / 2) + paddingLeft - (gestureWidth / 2);
            }

            int height = MeasureSpec.getSize(heightMeasureSpec);
            int paddingTop = getPaddingTop();
            int paddingBottom = getPaddingBottom();
            if ((mGravity & Gravity.TOP) == Gravity.TOP) {
                mGestureViewTop = paddingTop;
            } else if ((mGravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
                mGestureViewTop = height - paddingBottom - gestureHeight;
            } else {
                mGestureViewTop = ((height - paddingTop - paddingBottom) / 2) + paddingTop - (gestureHeight / 2);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mGestureView.measure(widthMeasureSpec, heightMeasureSpec);
        calcGesturePosition(widthMeasureSpec, heightMeasureSpec);

        // TODO message position
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int messageWidthSpec = 0;
        if (mGestureView.getGesture() == GestureView.GESTURE_SWIPE_RIGHT) {
            mMessageLeft = mGestureViewLeft + mGestureView.getMeasuredWidth() +
                    LayoutUtils.dp2pix(getContext(), 24);
            messageWidthSpec = MeasureSpec.makeMeasureSpec(
                    width - mMessageLeft - paddingRight, MeasureSpec.EXACTLY);
        } else if (mGestureView.getGesture() == GestureView.GESTURE_SWIPE_LEFT) {
            mMessageLeft = paddingLeft;
            messageWidthSpec = MeasureSpec.makeMeasureSpec(
                    width - paddingLeft - paddingRight - mGestureView.getMeasuredWidth() -
                            LayoutUtils.dp2pix(getContext(), 24), MeasureSpec.EXACTLY);
        }
        int messageHeightSpec = MeasureSpec.makeMeasureSpec(
                height - paddingTop - paddingBottom, MeasureSpec.AT_MOST);
        mMessage.measure(messageWidthSpec, messageHeightSpec);
        mMessageTop = ((height - paddingTop - paddingBottom) / 2) + paddingTop - (mMessage.getMeasuredHeight() / 2);

        mButton.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mGestureView.layout(
                mGestureViewLeft, mGestureViewTop,
                mGestureViewLeft + mGestureView.getMeasuredWidth(),
                mGestureViewTop + mGestureView.getMeasuredHeight());

        mMessage.layout(mMessageLeft, mMessageTop,
                mMessageLeft + mMessage.getMeasuredWidth(),
                mMessageTop + mMessage.getMeasuredHeight());

        int width = getWidth();
        int height = getHeight();
        int buttonWidth = mButton.getMeasuredWidth();
        int buttonHeight = mButton.getMeasuredHeight();
        int padding = LayoutUtils.dp2pix(getContext(), 24);
        int buttonRight = width - padding;
        int buttonBottom = height - padding;
        mButton.layout(buttonRight - buttonWidth, buttonBottom - buttonHeight,
                buttonRight, buttonBottom);
    }
}
