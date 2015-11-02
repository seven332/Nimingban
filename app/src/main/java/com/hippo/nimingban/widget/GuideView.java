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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hippo.drawable.RoundRectDrawable;
import com.hippo.nimingban.R;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ViewUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GuideView extends FrameLayout {

    @IntDef({Gravity.LEFT, Gravity.RIGHT, Gravity.TOP, Gravity.CENTER,
            Gravity.LEFT | Gravity.TOP, Gravity.RIGHT | Gravity.TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MessagePosition {}

    private TextView mMessage;
    private TextView mButton;

    private RoundRectDrawable mMessageBg;
    private ColorDrawable mButtonBg;

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

        LayoutInflater.from(context).inflate(R.layout.widget_guide, this);
        mMessage = (TextView) findViewById(R.id.guide_message);
        mButton = (TextView) findViewById(R.id.guide_button);

        mMessageBg = new RoundRectDrawable(Color.BLACK, LayoutUtils.dp2pix(context, 4));
        mButtonBg = new ColorDrawable(Color.BLACK);
        mMessage.setBackgroundDrawable(mMessageBg);
        mButton.setBackgroundDrawable(mButtonBg);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mMessage.setMaxWidth((widthSize - getPaddingLeft() - getPaddingRight()) * 3 / 4);
        } else {
            mMessage.setMaxWidth(Integer.MAX_VALUE);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMessagePosition(@MessagePosition int gravity) {
        LayoutParams lp = (LayoutParams) mMessage.getLayoutParams();

        if (gravity == Gravity.LEFT) {
            lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        } else if (gravity == Gravity.RIGHT) {
            lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        } else if (gravity == Gravity.TOP) {
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        } else {
            lp.gravity = gravity;
        }

        mMessage.setLayoutParams(lp);
    }

    public void setColor(int color) {
        mMessageBg.setColor(color);
        mButtonBg.setColor(color);
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
}
