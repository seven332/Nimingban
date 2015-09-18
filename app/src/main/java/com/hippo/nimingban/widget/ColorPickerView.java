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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.hippo.nimingban.R;
import com.hippo.widget.ColorView;
import com.hippo.widget.Slider;

public class ColorPickerView extends LinearLayout implements Slider.OnSetProgressListener, View.OnClickListener {

    private ColorView mColorBrick;
    private Slider mRedSlider;
    private Slider mGreenSlider;
    private Slider mBlueSlider;

    private View mRed;
    private View mPink;
    private View mPurple;
    private View mDeepPurple;
    private View mIndigo;
    private View mBlue;
    private View mLightBlue;
    private View mCyan;
    private View mTeal;
    private View mGreen;
    private View mLightGreen;
    private View mLime;
    private View mYellow;
    private View mAmber;
    private View mOrange;
    private View mDeepOrange;

    private int mColor;

    public ColorPickerView(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.widget_color_picker, this);
        mColorBrick = (ColorView) findViewById(R.id.color_brick);
        mRedSlider = (Slider) findViewById(R.id.red_slider);
        mGreenSlider = (Slider) findViewById(R.id.green_slider);
        mBlueSlider = (Slider) findViewById(R.id.blue_slider);
        mRed = findViewById(R.id.red);
        mPink = findViewById(R.id.pink);
        mPurple = findViewById(R.id.purple);
        mDeepPurple = findViewById(R.id.deep_purple);
        mIndigo = findViewById(R.id.indigo);
        mBlue = findViewById(R.id.blue);
        mLightBlue = findViewById(R.id.light_blue);
        mCyan = findViewById(R.id.cyan);
        mTeal = findViewById(R.id.teal);
        mGreen = findViewById(R.id.green);
        mLightGreen = findViewById(R.id.light_green);
        mLime = findViewById(R.id.lime);
        mYellow = findViewById(R.id.yellow);
        mAmber = findViewById(R.id.amber);
        mOrange = findViewById(R.id.orange);
        mDeepOrange = findViewById(R.id.deep_orange);

        mRedSlider.setOnSetProgressListener(this);
        mGreenSlider.setOnSetProgressListener(this);
        mBlueSlider.setOnSetProgressListener(this);

        mRed.setOnClickListener(this);
        mPink.setOnClickListener(this);
        mPurple.setOnClickListener(this);
        mDeepPurple.setOnClickListener(this);
        mIndigo.setOnClickListener(this);
        mBlue.setOnClickListener(this);
        mLightBlue.setOnClickListener(this);
        mCyan.setOnClickListener(this);
        mTeal.setOnClickListener(this);
        mGreen.setOnClickListener(this);
        mLightGreen.setOnClickListener(this);
        mLime.setOnClickListener(this);
        mYellow.setOnClickListener(this);
        mAmber.setOnClickListener(this);
        mOrange.setOnClickListener(this);
        mDeepOrange.setOnClickListener(this);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
            setColor(a.getColor(R.styleable.ColorPickerView_color, Color.BLACK));
            a.recycle();
        } else {
            setColor(Color.BLACK);
        }
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
        mColorBrick.setColor(color);
        mRedSlider.setProgress((color & 0x00ff0000) >> 16);
        mGreenSlider.setProgress((color & 0x0000ff00) >> 8);
        mBlueSlider.setProgress(color & 0x000000ff);
    }

    @Override
    public void onSetProgress(Slider slider, int newProgress, int oldProgress, boolean byUser, boolean confirm) {
        if (!byUser) {
            return;
        }

        int color = 0;
        if (mRedSlider == slider) {
            color = 0xff000000 |
                    (newProgress << 16) |
                    (mGreenSlider.getProgress() << 8) |
                    mBlueSlider.getProgress();
        } else if (mGreenSlider == slider) {
            color = 0xff000000 |
                    (mRedSlider.getProgress() << 16) |
                    (newProgress << 8) |
                    mBlueSlider.getProgress();
        } else if (mBlueSlider == slider) {
            color = 0xff000000 |
                    (mRedSlider.getProgress() << 16) |
                    (mGreenSlider.getProgress() << 8) |
                    newProgress;
        }
        mColor = color;
        mColorBrick.setColor(color);
    }

    @Override
    public void onClick(View v) {
        int colorResId = R.color.red_500;

        if (mRed == v) {
            colorResId = R.color.red_500;
        } else if (mPink == v) {
            colorResId = R.color.pink_500;
        } else if (mPurple == v) {
            colorResId = R.color.purple_500;
        } else if (mDeepPurple == v) {
            colorResId = R.color.deep_purple_500;
        } else if (mIndigo == v) {
            colorResId = R.color.indigo_500;
        } else if (mBlue == v) {
            colorResId = R.color.blue_500;
        } else if (mLightBlue == v) {
            colorResId = R.color.light_blue_500;
        } else if (mCyan == v) {
            colorResId = R.color.cyan_500;
        } else if (mTeal == v) {
            colorResId = R.color.teal_500;
        } else if (mGreen == v) {
            colorResId = R.color.green_500;
        } else if (mLightGreen == v) {
            colorResId = R.color.light_green_500;
        } else if (mLime == v) {
            colorResId = R.color.lime_500;
        } else if (mYellow == v) {
            colorResId = R.color.yellow_500;
        } else if (mAmber == v) {
            colorResId = R.color.amber_500;
        } else if (mOrange == v) {
            colorResId = R.color.orange_500;
        } else if (mDeepOrange == v) {
            colorResId = R.color.deep_orange_500;
        }

        setColor(getContext().getResources().getColor(colorResId));
    }
}
