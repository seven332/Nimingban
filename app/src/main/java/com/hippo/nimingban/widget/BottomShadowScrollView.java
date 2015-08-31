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
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.hippo.nimingban.R;
import com.hippo.yorozuya.LayoutUtils;

public class BottomShadowScrollView extends ScrollView {

    private Drawable mShadowDrawable;
    private int mShadowHeight;

    public BottomShadowScrollView(Context context) {
        super(context);
        init(context);
    }

    public BottomShadowScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BottomShadowScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mShadowDrawable = ContextCompat.getDrawable(context, R.drawable.shadow_top);
        mShadowHeight = LayoutUtils.dp2pix(context, 4);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        mShadowDrawable.setBounds(0, 0, getWidth(), mShadowHeight);
        int saved = canvas.save();
        int height = getChildCount() != 0 ? Math.max(getChildAt(0).getHeight(), getHeight()) : getHeight();
        canvas.translate(0, height - mShadowHeight);
        mShadowDrawable.draw(canvas);
        canvas.restoreToCount(saved);
    }
}
