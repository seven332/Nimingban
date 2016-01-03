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

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.conaco.DrawableHelper;
import com.hippo.conaco.DrawableHolder;
import com.hippo.drawable.ImageDrawable;
import com.hippo.yorozuya.io.InputStreamPipe;

public class ImageDrawableHelper implements DrawableHelper {

    @Nullable
    @Override
    public Drawable decode(@NonNull InputStreamPipe isPipe) {
        try {
            isPipe.obtain();
            return ImageDrawable.decode(isPipe.open());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            isPipe.close();
            isPipe.release();
        }
    }

    @Override
    public int sizeOf(@NonNull String key, @NonNull Drawable value) {
        if (value instanceof ImageDrawable) {
            return ((ImageDrawable) value).getByteCount();
        } else {
            return 0;
        }
    }

    @Override
    public void onRemove(@NonNull String key, @NonNull DrawableHolder oldValue) {
        if (oldValue.isFree()) {
            Drawable drawable = oldValue.getDrawable();
            if (drawable instanceof ImageDrawable) {
                ((ImageDrawable) drawable).recycle();
            }
        }
    }

    @Override
    public boolean useMemoryCache(@NonNull String key, DrawableHolder holder) {
        if (holder == null) {
            return true;
        } else {
            Drawable drawable = holder.getDrawable();
            if (drawable instanceof ImageDrawable) {
                return !((ImageDrawable) drawable).isLarge();
            } else {
                return false;
            }
        }
    }
}
