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
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Build;

public class StyleableContext extends ContextWrapper {

    private Context mContext;
    private StyleableResources mStyleableResources;

    private StyleableContext(Context context, Context base) {
        super(base);
        mContext = context;
    }

    @Override
    public Resources getResources() {
        final Resources superResources = super.getResources();
        if (mStyleableResources == null || !mStyleableResources.isBase(superResources)) {
            mStyleableResources = new StyleableResources(mContext, superResources);
        }
        return mStyleableResources;
    }

    /**
     * In {@link android.content.ContextWrapper#attachBaseContext(Context)},
     * do <code>super.attachBaseContext(ProxyContext.wrapContext(this, newBase));</code>.
     */
    public static Context wrapContext(Context peak, Context base) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? new StyleableContext(peak, base) : base;
    }
}
