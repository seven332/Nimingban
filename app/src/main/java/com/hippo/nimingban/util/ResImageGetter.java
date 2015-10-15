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

package com.hippo.nimingban.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.hippo.text.Html;
import com.hippo.yorozuya.StringUtils;

public class ResImageGetter implements Html.ImageGetter {

    private static Resources sResources;

    public static void initialize(Context context) {
        sResources = context.getResources();
    }

    @Override
    public Drawable getDrawable(String source) {
        if (StringUtils.isAllDigit(source)) {
            int resId = Integer.parseInt(source);
            Drawable d = sResources.getDrawable(resId);
            if (d != null) {
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                return d;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
