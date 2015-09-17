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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.Unikery;
import com.hippo.drawable.TiledBitmapDrawable;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.ProgressView;
import com.hippo.widget.SimpleImageView;
import com.hippo.yorozuya.MathUtils;

import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;

public final class GalleryPage extends FrameLayout implements Unikery, View.OnClickListener {

    private int mTaskId = Unikery.INVAILD_ID;

    private Conaco mConaco;

    private ProgressView mProgressView;
    private SimpleImageView mFailed;
    private PhotoView mPhotoView;

    private String mId;
    private String mUrl;

    private DrawableHolder mHolder;

    public GalleryPage(Context context) {
        super(context);
        init(context);
    }

    public GalleryPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GalleryPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mConaco = NMBApplication.getConaco(context);

        LayoutInflater.from(context).inflate(R.layout.widget_gallery_page, this);

        mProgressView = (ProgressView) findViewById(R.id.progress_view);
        mFailed = (SimpleImageView) findViewById(R.id.failed);
        mPhotoView = (PhotoView) findViewById(R.id.image_view);

        mFailed.setDrawable(VectorDrawable.create(context, R.drawable.ic_empty));
    }

    private void addRetry() {
        setOnClickListener(this);
    }

    private void removeRetry() {
        setOnClickListener(null);
        setClickable(false);
    }

    private void updateMaximumScale() {
        Drawable drawable = mPhotoView.getDrawable();

        if (drawable != null && drawable.getIntrinsicHeight() > 0) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            int height = getHeight();
            int width = getHeight();
            float scaleX = 3.0f;
            float scaleY = 3.0f;

            if (drawableWidth > 0 && width > 0) {
                scaleX = ((float) drawableWidth / (float) width) * 3f;
            }
            if (drawableHeight > 0 && height > 0) {
                scaleY = ((float) drawableHeight / (float) height) * 3f;
            }

            mPhotoView.setMaximumScale(MathUtils.max(3.0f, scaleX, scaleY));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateMaximumScale();
    }

    private void setImageDrawableSafely(Drawable drawable) {
        Drawable oldDrawable = mPhotoView.getDrawable();
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

        mPhotoView.setImageDrawable(drawable);

        updateMaximumScale();
    }

    @Override
    public void onClick(View v) {
        if (mId != null && mUrl != null) {
            load(mId, mUrl);
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
    public void onMiss(Conaco.Source source) {
    }

    public void load(String id, String url) {
        removeRetry();

        if (url == null) {
            return;
        }

        mId = id;
        mUrl = url;

        mProgressView.setVisibility(VISIBLE);
        mProgressView.setIndeterminate(true);
        mFailed.setVisibility(GONE);
        mPhotoView.setVisibility(GONE);
        setImageDrawableSafely(null);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }

        ConacoTask.Builder builder = new ConacoTask.Builder()
                .setUnikery(this)
                .setKey(url)
                .setUrl(url);
        mConaco.load(builder);
    }

    public void unload() {
        mConaco.cancel(this);
        removeRetry();
        mId = null;
        mUrl = null;
        setImageDrawableSafely(null);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    public void onRequest() {
    }

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
        mProgressView.setIndeterminate(false);
        if (totalSize >= 0) {
            mProgressView.setProgress((float) receivedSize / (float) totalSize);
        }
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        // Release
        mId = null;
        mUrl = null;

        DrawableHolder olderHolder = mHolder;
        mHolder = holder;
        holder.obtain();

        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(GONE);
        mPhotoView.setVisibility(VISIBLE);

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
    }

    @Override
    public void onFailure() {
        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(VISIBLE);
        mPhotoView.setVisibility(GONE);
        setImageDrawableSafely(null);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }

        addRetry();
    }

    @Override
    public void onCancel() {
        removeRetry();
        mId = null;
        mUrl = null;
    }

    public void showDrawable(@NonNull Drawable drawable) {
        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(GONE);
        mPhotoView.setVisibility(VISIBLE);

        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).start();
        }

        setImageDrawableSafely(drawable);
    }

    public void showFailedText() {
        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(VISIBLE);
        mPhotoView.setVisibility(GONE);
        setImageDrawableSafely(null);
    }

    public boolean isLoaded() {
        return mPhotoView.getDrawable() != null;
    }
}
