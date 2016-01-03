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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.conaco.ObjectHelper;
import com.hippo.conaco.ObjectHolder;
import com.hippo.drawable.ImageWrapper;
import com.hippo.image.Image;
import com.hippo.yorozuya.io.InputStreamPipe;

public class ImageWrapperHelper implements ObjectHelper {

    @Nullable
    @Override
    public Object decode(@NonNull InputStreamPipe isPipe) {
        try {
            isPipe.obtain();
            Image image = Image.decode(isPipe.open(), false);
            if (image != null) {
                return new ImageWrapper(image);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            isPipe.close();
            isPipe.release();
        }
    }

    @Override
    public int sizeOf(@NonNull String key, @NonNull Object value) {
        ImageWrapper imageWrapper = (ImageWrapper) value;
        return imageWrapper.getWidth() * imageWrapper.getHeight() * 4;
    }

    @Override
    public void onRemove(@NonNull String key, @NonNull ObjectHolder oldValue) {
        if (oldValue.isFree()) {
            ((ImageWrapper) oldValue.getObject()).recycle();
        }
    }

    @Override
    public boolean useMemoryCache(@NonNull String key, ObjectHolder holder) {
        return holder == null || !((ImageWrapper) holder.getObject()).isLarge();
    }
}
