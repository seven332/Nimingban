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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.conaco.BitmapPool;
import com.hippo.conaco.DrawableHelper;
import com.hippo.conaco.DrawableHolder;
import com.hippo.drawable.TiledBitmapDrawable;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;

public class SimpleDrawableHelper implements DrawableHelper {

    private Context mContext;
    private BitmapPool mBitmapPool;

    public SimpleDrawableHelper(Context context) {
        mContext = context.getApplicationContext();
        mBitmapPool = new BitmapPool();
    }

    @Nullable
    @Override
    public Drawable decode(@NonNull InputStreamPipe isPipe) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();

            isPipe.obtain();

            options.inJustDecodeBounds = true;

            InputStream is = isPipe.open();
            BitmapFactory.decodeStream(is, null, options);
            isPipe.close();

            // Check out size
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                isPipe.release();
                return null;
            }

            if ("image/gif".equals(options.outMimeType)) {
                File temp = NMBAppConfig.createTempFile(mContext);
                if (temp == null) {
                    return null;
                }
                FileOutputStream fos = new FileOutputStream(temp);
                is = isPipe.open();
                IOUtils.copy(is, fos);
                isPipe.close();
                isPipe.release();
                return new TempGifDrawable(temp);
            } else if (options.outWidth >= 1024 || options.outHeight >= 1024) { // TODO get the threshold runtime
                return TiledBitmapDrawable.from(
                        isPipe.open(), options.outWidth, options.outHeight, mBitmapPool);
            } else {
                options.inJustDecodeBounds = false;
                options.inMutable = true;
                options.inSampleSize = 1;
                options.inBitmap = mBitmapPool.getInBitmap(options);

                is = isPipe.open();
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                if (bitmap != null) {
                    return new BitmapDrawable(mContext.getResources(), bitmap);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            isPipe.close();
            isPipe.release();
        }
    }

    @Override
    public int sizeOf(@NonNull String key, @NonNull Drawable value) {
        if (value instanceof GifDrawable) {
            return (int) ((GifDrawable) value).getAllocationByteCount();
        } else if (value instanceof TiledBitmapDrawable) {
            return ((TiledBitmapDrawable) value).getByteCount();
        } else if (value instanceof BitmapDrawable) {
            return ((BitmapDrawable) value).getBitmap().getByteCount();
        } else {
            return 0;
        }
    }

    @Override
    public void onRemove(@NonNull String key, @NonNull DrawableHolder oldValue) {
        if (oldValue.isFree()) {
            Drawable drawable = oldValue.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                mBitmapPool.addReusableBitmap(((BitmapDrawable) drawable).getBitmap());
            } else if (drawable instanceof TiledBitmapDrawable) {
                ((TiledBitmapDrawable) drawable).recycle(mBitmapPool);
            } else if (drawable instanceof GifDrawable) {
                ((GifDrawable) drawable).recycle();
            }
        }
    }

    @Override
    public boolean useMemoryCache(@NonNull String key, DrawableHolder holder) {
        if (holder != null && holder.getDrawable() instanceof GifDrawable) {
            return false;
        } else {
            return true;
        }
    }

    private class TempGifDrawable extends GifDrawable {

        private File mFile;

        public TempGifDrawable(@NonNull File file) throws IOException {
            super(file);
            mFile = file;
        }

        @Override
        public void recycle() {
            super.recycle();
            mFile.delete();
        }
    }
}
