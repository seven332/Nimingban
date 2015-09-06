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
import android.graphics.drawable.TransitionDrawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.ProgressNotify;
import com.hippo.conaco.Unikery;
import com.hippo.drawable.TiledBitmapDrawable;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.widget.FixedAspectImageView;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import pl.droidsonroids.gif.GifDrawable;

public final class HeaderImageView extends FixedAspectImageView
        implements Unikery, View.OnClickListener, View.OnLongClickListener {

    private int mTaskId = Unikery.INVAILD_ID;

    private Conaco mConaco;

    private final long[] mHits = new long[8];

    private File mImageFile;
    private TempDataContainer mContainer;
    private DrawableHolder mHolder;

    private OnLongClickImageListener mOnLongClickImageListener;

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
        setScaleType(ScaleType.CENTER_CROP);
        setSoundEffectsEnabled(false);
        setOnClickListener(this);
        setOnLongClickListener(this);
        load();
    }

    public void setOnLongClickImageListener(OnLongClickImageListener listener) {
        mOnLongClickImageListener = listener;
    }

    @Override
    public void onClick(View v) {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length-1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 3000)) {
            Arrays.fill(mHits, 0);
            Toast.makeText(getContext(), "（<ゝω・）☆ Kira", Toast.LENGTH_SHORT).show();
            load();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mImageFile != null && mOnLongClickImageListener != null) {
            return mOnLongClickImageListener.onLongClickImage(mImageFile);
        } else {
            return false;
        }
    }

    private void load() {
        mContainer = new TempDataContainer();
        ConacoTask.Builder builder = new ConacoTask.Builder()
                .setUnikery(this)
                .setKey(null)
                .setUrl("http://cover.acfunwiki.org/cover.php")
                .setDataContainer(mContainer);
        mConaco.load(builder);
    }

    public void unload() {
        mConaco.cancel(this);
        setImageDrawableSafely(null);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    public void setTaskId(int id) {
        mTaskId = id;
    }

    @Override
    public int getTaskId() {
        return mTaskId;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onRequest() {
    }

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
    }

    private void setImageDrawableSafely(Drawable drawable) {
        Drawable oldDrawable = getDrawable();
        if (oldDrawable instanceof TransitionDrawable) {
            TransitionDrawable tDrawable = (TransitionDrawable) oldDrawable;
            int number = tDrawable.getNumberOfLayers();
            if (number > 0) {
                oldDrawable = tDrawable.getDrawable(number - 1);
            }
        }

        if (oldDrawable instanceof GifDrawable) {
            ((GifDrawable) oldDrawable).recycle();
        }

        if (oldDrawable instanceof TiledBitmapDrawable) {
            ((TiledBitmapDrawable) oldDrawable).recycle(null);
        }

        setImageDrawable(drawable);
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        // Update image file
        FileUtils.delete(mImageFile);
        if (mContainer != null) {
            mImageFile = mContainer.mTempFile;
            mContainer = null;
        }

        DrawableHolder olderHolder = mHolder;
        mHolder = holder;
        holder.obtain();

        Drawable drawable = holder.getDrawable();
        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).start();
        }

        setImageDrawableSafely(drawable);

        if (olderHolder != null) {
            olderHolder.release();
        }

        return true;
    }

    @Override
    public void onSetDrawable(Drawable drawable) {
        setImageDrawableSafely(drawable);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    public void onFailure() {
        mContainer = null;
    }

    @Override
    public void onCancel() {
        mContainer = null;
    }

    public interface OnLongClickImageListener {

        boolean onLongClickImage(File imageFile);
    }

    private static class TempDataContainer implements DataContainer {

        private File mTempFile;

        @Override
        public boolean save(InputStream is, ProgressNotify notify) {
            FileOutputStream os = null;
            try {
                mTempFile = NMBAppConfig.createTempFile();
                if (mTempFile == null) {
                    return false;
                }
                os = new FileOutputStream(mTempFile);
                IOUtils.copy(is, os);
                return true;
            } catch (IOException e) {
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

        @Override
        public void remove() {
            FileUtils.delete(mTempFile);
        }
    }
}
