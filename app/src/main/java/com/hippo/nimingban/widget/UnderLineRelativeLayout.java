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
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.hippo.nimingban.R;
import com.hippo.yorozuya.ResourcesUtils;

public class UnderLineRelativeLayout extends RelativeLayout {

    private Paint mPaint;
    private int mDividerHeight;

    public UnderLineRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public UnderLineRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UnderLineRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setColor(ResourcesUtils.getAttrColor(context, R.attr.colorDivider));
        mDividerHeight = context.getResources().getDimensionPixelOffset(R.dimen.divider_height);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        int height = getHeight();
        canvas.drawRect(0, height - mDividerHeight, getWidth(), height, mPaint);
    }
}
