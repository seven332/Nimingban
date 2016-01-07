/*
 * Copyright 2016 Hippo Seven
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

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hippo.animation.SimpleAnimatorListener;
import com.hippo.nimingban.R;
import com.hippo.util.AnimationUtils2;
import com.hippo.yorozuya.Pool;
import com.hippo.yorozuya.ResourcesUtils;

public class PopupTextView extends FrameLayout {

    private static final int MAX_TEXT_VIEW = 10;

    private Pool<TextView> mFreePool = new Pool<>(MAX_TEXT_VIEW);

    public PopupTextView(Context context) {
        super(context);
        init(context);
    }

    public PopupTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PopupTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        for (int i = 0; i < MAX_TEXT_VIEW; i++) {
            TextView tv = new TextView(context);
            tv.setTextColor(ResourcesUtils.getAttrColor(context, R.attr.colorPureInverse));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setVisibility(INVISIBLE);
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            addView(tv, lp);
            mFreePool.push(tv);
        }
    }

    public boolean popupText(String text) {
        final TextView tv = mFreePool.pop();
        if (tv == null) {
            return false;
        }

        tv.setTranslationY(0);
        tv.setAlpha(1.0f);
        tv.setVisibility(VISIBLE);
        tv.setText(text);

        // Start animate
        tv.animate().y(0.0f).alpha(0.0f).setDuration(1000)
                .setInterpolator(AnimationUtils2.FAST_SLOW_INTERPOLATOR)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        tv.setVisibility(INVISIBLE);
                        mFreePool.push(tv);
                    }
                }).start();
        return true;
    }
}
