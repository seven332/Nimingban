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

package com.hippo.styleable;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class StyleableResources extends Resources {

    private Context mContext;
    private Resources mBase;

    public StyleableResources(Context context, Resources res) {
        super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
        mContext = context;
        mBase = res;
    }

    public boolean isBase(Resources res) {
        return mBase == res;
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return mContext.obtainStyledAttributes(set, attrs);
    }
}
