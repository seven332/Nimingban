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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.Unikery;
import com.hippo.drawable.TiledBitmapDrawable;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.widget.FixedAspectImageView;

import pl.droidsonroids.gif.GifDrawable;

public class LoadImageView extends FixedAspectImageView implements Unikery,
        View.OnClickListener, View.OnLongClickListener {

    private int mTaskId = Unikery.INVAILD_ID;

    private Conaco mConaco;

    private String mKey;
    private String mUrl;
    private DataContainer mContainer;

    private DrawableHolder mHolder;

    private boolean mFailed;

    private RetryType mRetryType = RetryType.NONE;

    private static final RetryType[] sRetryTypeArray = {
            RetryType.NONE,
            RetryType.CLICK,
            RetryType.LONG_CLICK
    };

    public enum RetryType {
        NONE      (0),
        CLICK      (1),
        LONG_CLICK   (2);

        RetryType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }

    public LoadImageView(Context context) {
        super(context);
    }

    public LoadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadImageView);
        final int index = a.getInt(R.styleable.LoadImageView_retryType, -1);
        if (index >= 0) {
            setRetryType(sRetryTypeArray[index]);
        }
        a.recycle();

        mConaco = NMBApplication.getConaco(context);
    }

    public void setRetryType(RetryType retryType) {
        if (mRetryType != retryType) {
            RetryType oldRetryType = mRetryType;
            mRetryType = retryType;

            if (mFailed) {
                if (oldRetryType == RetryType.CLICK) {
                    setOnClickListener(null);
                    setClickable(false);
                } else if (oldRetryType == RetryType.LONG_CLICK) {
                    setOnLongClickListener(null);
                    setLongClickable(false);
                }

                if (retryType == RetryType.CLICK) {
                    setOnClickListener(this);
                } else if (retryType == RetryType.LONG_CLICK) {
                    setOnLongClickListener(this);
                }
            }
        }
    }

    private void cancelRetryType() {
        if (mRetryType == RetryType.CLICK) {
            setOnClickListener(null);
            setClickable(false);
        } else if (mRetryType == RetryType.LONG_CLICK) {
            setOnLongClickListener(null);
            setLongClickable(false);
        }
    }

    public void load(String key, String url) {
        load(key, url, null);
    }

    public void load(String key, String url, DataContainer container) {
        mFailed = false;
        cancelRetryType();

        mKey = key;
        mUrl = url;
        mContainer = container;

        ConacoTask.Builder builder = new ConacoTask.Builder()
                .setUnikery(this)
                .setKey(key)
                .setUrl(url)
                .setDataContainer(container);
        mConaco.load(builder);
    }

    public void unload() {
        mConaco.cancel(this);
        mKey = null;
        mUrl = null;
        mContainer = null;
        setImageDrawableSafely(null);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    public void cancel() {
        mConaco.cancel(this);
    }

    @Override
    public void setTaskId(int id) {
        mTaskId = id;
    }

    @Override
    public int getTaskId() {
        return mTaskId;
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
    public void onStart() {
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
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        // Release
        mKey = null;
        mUrl = null;
        mContainer = null;

        DrawableHolder olderHolder = mHolder;
        mHolder = holder;
        holder.obtain();

        Drawable drawable = holder.getDrawable();
        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).start();
        }

        switch (source) {
            default:
            case MEMORY:
            case DISK:
                setImageDrawableSafely(drawable);
                break;

            case NETWORK:
                Drawable[] layers = new Drawable[2];
                layers[0] = new ColorDrawable(Color.TRANSPARENT);
                layers[1] = drawable;
                TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                setImageDrawableSafely(transitionDrawable);
                transitionDrawable.startTransition(300);
                break;
        }

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
        mFailed = true;
        setImageDrawableSafely(getContext().getResources().getDrawable(R.drawable.image_failed));
        if (mRetryType == RetryType.CLICK) {
            setOnClickListener(this);
        } else if (mRetryType == RetryType.LONG_CLICK) {
            setOnLongClickListener(this);
        } else {
            // Can't retry, so release
            mKey = null;
            mUrl = null;
            mContainer = null;
        }

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
        }
        mHolder = null;
    }

    @Override
    public void onCancel() {
        mFailed = false;
        cancelRetryType();
        // release
        mKey = null;
        mUrl = null;
        mContainer = null;
    }

    @Override
    public void onClick(View v) {
        load(mKey, mUrl, mContainer);
    }

    @Override
    public boolean onLongClick(View v) {
        load(mKey, mUrl, mContainer);
        return true;
    }
}
