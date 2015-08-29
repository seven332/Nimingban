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

package com.hippo.gallery;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.conaco.BitmapPool;
import com.hippo.yorozuya.IOUtils;

import java.io.InputStream;

public class GifDecoderBuilder {

    private InputStream mInputStream;
    private BitmapPool mBitmapPool;

    public GifDecoderBuilder(@NonNull InputStream is) {
        mInputStream = is;
    }

    public GifDecoderBuilder setBitmapPool(BitmapPool bitmapPool) {
        mBitmapPool = bitmapPool;
        return this;
    }

    public void close() {
        IOUtils.closeQuietly(mInputStream);
        mInputStream = null;
        mBitmapPool = null;
    }

    public @Nullable GifDecoder build() {
        if (mInputStream != null && mBitmapPool != null) {
            GifDecoder gifDecoder = GifDecoder.decodeStream(mInputStream);
            gifDecoder.setBitmapPool(mBitmapPool);
            IOUtils.closeQuietly(mInputStream);
            mInputStream = null;
            mBitmapPool = null;
            return gifDecoder;
        } else {
            mInputStream = null;
            mBitmapPool = null;
            return null;
        }
    }
}
