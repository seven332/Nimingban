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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.hippo.nimingban.R;

public class ColorView extends View {

    private int mColor;

    public ColorView(Context context) {
        super(context);
        init(context, null);
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorView);
            setColor(a.getColor(R.styleable.ColorView_color, Color.BLACK));
            a.recycle();
        } else {
            mColor = Color.BLACK;
        }
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        if (color != mColor) {
            mColor = color;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int saved = canvas.save();
        canvas.clipRect(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        canvas.drawColor(mColor);
        canvas.restoreToCount(saved);
    }
}
