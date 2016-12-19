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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.Unikery;
import com.hippo.conaco.ValueHolder;
import com.hippo.drawable.ImageDrawable;
import com.hippo.drawable.ImageWrapper;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.widget.ProgressView;

import uk.co.senab.photoview.PhotoView;

public final class GalleryPage extends FrameLayout implements Unikery<ImageWrapper>, View.OnClickListener {

    private int mTaskId = Unikery.INVALID_ID;

    private Conaco<ImageWrapper> mConaco;

    private ProgressView mProgressView;
    private View mFailed;
    private PhotoView mPhotoView;

    private String mKey;
    private String mUrl;
    private DataContainer mContainer;

    private ValueHolder<ImageWrapper> mHolder;

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
        mFailed = findViewById(R.id.failed);
        mPhotoView = (PhotoView) findViewById(R.id.image_view);
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
            int width = getWidth();
            int height = getHeight();
            float scaleX = 1.0f;
            float scaleY = 1.0f;

            if (drawableWidth > 0 && width > 0) {
                scaleX = ((float) width / (float) drawableWidth);
            }
            if (drawableHeight > 0 && height > 0) {
                scaleY = ((float) height / (float) drawableHeight);
            }

            float midScale;
            float maxScale;
            if (scaleX > scaleY) {
                midScale = scaleX / scaleY;
            } else if (scaleX == scaleY) {
                midScale = 1.75f;
            } else {
                midScale = scaleY / scaleX;
            }

            maxScale = midScale * 3;
            mPhotoView.setScaleLevels(1.0f, midScale, maxScale);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateMaximumScale();
    }

    private void removeDrawableAndHolder() {
        // Remove drawable
        Drawable drawable = mPhotoView.getDrawable();
        if (drawable instanceof ImageDrawable) {
            ((ImageDrawable) drawable).recycle();
        }
        mPhotoView.setImageDrawable(null);

        // Remove holder
        if (mHolder != null) {
            mHolder.release(this);

            ImageWrapper imageWrapper = mHolder.getValue();
            if (mHolder.isFree()) {
                // ImageWrapper is free, stop animate
                imageWrapper.stop();
                if (!mHolder.isInMemoryCache()) {
                    // ImageWrapper is not needed any more, recycle it
                    imageWrapper.recycle();
                }
            }

            mHolder = null;
        }
    }

    private void setImageDrawable(Drawable drawable) {
        mPhotoView.setImageDrawable(drawable);
        updateMaximumScale();
    }

    @Override
    public void onClick(View v) {
        if (mUrl != null) {
            load(mKey, mUrl, mContainer);
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

    public void load(String key, String url, DataContainer container) {
        removeRetry();

        if (url == null) {
            return;
        }

        mKey = key;
        mUrl = url;
        mContainer = container;

        mProgressView.setVisibility(VISIBLE);
        mProgressView.setIndeterminate(true);
        mFailed.setVisibility(GONE);
        mPhotoView.setVisibility(GONE);
        removeDrawableAndHolder();

        ConacoTask.Builder<ImageWrapper> builder = new ConacoTask.Builder<ImageWrapper>()
                .setUnikery(this)
                .setKey(key)
                .setUrl(url)
                .setDataContainer(container)
                .setUseMemoryCache(false);
        mConaco.load(builder);
    }

    public void unload() {
        mConaco.cancel(this);
        removeRetry();
        mKey = null;
        mUrl = null;
        mContainer = null;
        removeDrawableAndHolder();
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
    public boolean onGetObject(@NonNull ValueHolder<ImageWrapper> holder, Conaco.Source source) {
        // Release
        mKey = null;
        mUrl = null;
        mContainer = null;

        holder.obtain(this);

        removeDrawableAndHolder();

        mHolder = holder;
        ImageWrapper imageWrapper = holder.getValue();
        Drawable drawable = new ImageDrawable(imageWrapper);
        imageWrapper.start();

        setImageDrawable(drawable);

        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(GONE);
        mPhotoView.setVisibility(VISIBLE);

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
        removeDrawableAndHolder();

        addRetry();
    }

    @Override
    public void onCancel() {
        removeRetry();
        mKey = null;
        mUrl = null;
        mContainer = null;
    }

    public void showDrawable(@NonNull Drawable drawable) {
        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(GONE);
        mPhotoView.setVisibility(VISIBLE);

        removeDrawableAndHolder();

        setImageDrawable(drawable);
    }

    public void showFailedText() {
        mProgressView.setVisibility(GONE);
        mProgressView.setIndeterminate(false);
        mFailed.setVisibility(VISIBLE);
        mPhotoView.setVisibility(GONE);

        removeDrawableAndHolder();
    }

    public boolean isLoaded() {
        return mPhotoView.getDrawable() != null;
    }
}
