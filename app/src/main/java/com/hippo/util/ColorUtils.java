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

package com.hippo.util;

import android.graphics.Color;

public class ColorUtils {

    /**
     * windowTranslucentStatus make status bar translucent, so the status bar background looks draker.
     * Lighter color is needed for status bar.
     * It work fine in AOSP.
     */
    public static int getColorForStatusBar(int color) {
        return Color.argb(
                Color.alpha(color),
                Math.min(Color.red(color) * 5 / 3, 0xff),
                Math.min(Color.green(color) * 5 / 3, 0xff),
                Math.min(Color.blue(color) * 5 / 3, 0xff));
    }
}
