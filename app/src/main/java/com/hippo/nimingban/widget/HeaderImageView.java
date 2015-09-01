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
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.DrawableHelper;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.Unikery;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.widget.FixedAspectImageView;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import pl.droidsonroids.gif.GifDrawable;

public final class HeaderImageView extends FixedAspectImageView implements Unikery, View.OnClickListener {

    private int mId = Unikery.INVAILD_ID;;
    private Conaco mConaco;
    private DrawableHelper mDrawableHelper;
    private DrawableHolder mHolder;

    private final long[] mHits = new long[8];

    public HeaderImageView(Context context) {
        super(context);
        init(context);
    }

    public HeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeaderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mConaco = NMBApplication.getConaco(context);
        mDrawableHelper = NMBApplication.getSimpleDrawableHelper(context);
        setSoundEffectsEnabled(false);
        setOnClickListener(this);
        load();
    }

    @Override
    public void onClick(View v) {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
        mHits[mHits.length-1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 3000)) {
            Arrays.fill(mHits, 0);
            Toast.makeText(getContext(), "（<ゝω・）☆ Kira", Toast.LENGTH_SHORT).show();
            load();
        }
    }

    private void load() {
        setScaleType(ScaleType.CENTER_CROP);
        ConacoTask.Builder builder = new ConacoTask.Builder()
                .setUnikery(this)
                .setUrl("http://cover.acfunwiki.org/cover.php")
                .setDataContainer(new TempDataContainer())
                .setHelper(mDrawableHelper);
        mConaco.load(builder);
    }

    @Override
    public void setTaskId(int id) {
        mId = id;
    }

    @Override
    public int getTaskId() {
        return mId;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onRequest() {
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        if (holder.getDrawable() instanceof GifDrawable && !holder.isFree()) {
            return false;
        }

        DrawableHolder olderHolder = mHolder;
        mHolder = holder;
        holder.obtain();

        // Refresh old gif drawable
        Drawable oldDrawable = getDrawable();
        if (oldDrawable instanceof GifDrawable) {
            ((GifDrawable) oldDrawable).recycle();
        }

        Drawable drawable = holder.getDrawable();
        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).start();
        }
        setImageDrawable(drawable);

        // Release old holder
        if (olderHolder != null) {
            olderHolder.release();
        }

        return true;
    }

    @Override
    public void onSetDrawable(Drawable drawable) {
        // Refresh old gif drawable
        Drawable oldDrawable = getDrawable();
        if (oldDrawable instanceof GifDrawable) {
            ((GifDrawable) oldDrawable).recycle();
        }

        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).start();
        }
        setImageDrawable(drawable);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
        }
        mHolder = null;
    }

    @Override
    public void onFailure() {
    }

    @Override
    public void onCancel() {
    }

    private class TempDataContainer implements DataContainer {

        private File mTempFile;

        @Override
        public boolean save(InputStream is) {
            FileOutputStream os = null;
            try {
                mTempFile = NMBAppConfig.createTempFile(getContext());
                if (mTempFile == null) {
                    return false;
                }
                os = new FileOutputStream(mTempFile);
                IOUtils.copy(is, os);
                return true;
            } catch (java.io.IOException e) {
                return false;
            } finally {
                IOUtils.closeQuietly(os);
            }
        }

        @Override
        public InputStreamPipe get() {
            if (mTempFile == null) {
                return null;
            } else {
                return new FileInputStreamPipe(mTempFile);
            }
        }
    }
}
