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

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hippo.nimingban.util.Settings;

public class FontTextView extends TextView {

    static Typeface sTypeface;

    private Typeface mOriginalTypeface;

    public FontTextView(Context context) {
        super(context);
        init(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void ensureTypeface(Context context) {
        if (sTypeface == null) {
            sTypeface = Typeface.createFromAsset(context.getAssets(), "missing_characters.ttf");
        }
    }

    private void init(Context context) {
        mOriginalTypeface = getTypeface();
        ensureTypeface(context);

        if (Settings.getFixEmojiDisplay()) {
            useCustomTypeface();
        }
    }

    public void useCustomTypeface() {
        setTypeface(sTypeface);
    }

    public void useOriginalTypeface() {
        setTypeface(mOriginalTypeface);
    }
}
