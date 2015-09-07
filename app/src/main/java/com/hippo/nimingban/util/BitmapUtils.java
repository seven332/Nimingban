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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    /**
     * @return null or the bitmap
     */
    public static Bitmap decodeStream(@NonNull InputStreamPipe isp, int maxWidth, int maxHeight) {
        try {
            isp.obtain();
            InputStream is = isp.open();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            int width = options.outWidth;
            int height = options.outHeight;
            if (width <= 0 || height <= 0) {
                return null;
            }

            options.inJustDecodeBounds = false;
            if (width <= maxWidth && height <= maxHeight) {
                options.inSampleSize = 1;
            } else {
                float scaleW = (float) width / (float) maxWidth;
                float scaleH = (float) height / (float) maxHeight;
                options.inSampleSize = MathUtils.nextPowerOf2((int) Math.max(scaleW, scaleH));
            }

            return BitmapFactory.decodeStream(isp.open(), null, options);
        } catch (IOException e) {
            return null;
        } finally {
            isp.close();
            isp.release();
        }
    }
}
