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
import android.widget.ImageView;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.Unikery;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;

public class LoadImageView extends ImageView implements Unikery,
        View.OnClickListener, View.OnLongClickListener {

    private int mTaskId = Unikery.INVAILD_ID;

    private DrawableHolder mHolder;

    private Conaco mConaco;
    private String mKey;
    private String mUrl;

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
        mFailed = false;
        cancelRetryType();

        mKey = key;
        mUrl = url;

        ConacoTask.Builder builder = new ConacoTask.Builder()
                .setUnikery(this).setKey(mKey).setUrl(mUrl);
        mConaco.load(builder);
    }

    public void cancel() {
        mConaco.cancel(this);
        cancelRetryType();

        // release
        mKey = null;
        mUrl = null;
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
        setImageDrawable(null);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
        }
        mHolder = null;
    }

    @Override
    public void onRequest() {
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        mKey = null;
        mUrl = null;

        DrawableHolder olderHolder = mHolder;
        mHolder = holder;

        holder.obtain();

        switch (source) {
            default:
            case MEMORY:
            case DISK:
                setImageDrawable(holder.getDrawable());
                break;

            case NETWORK:
                Drawable[] layers = new Drawable[2];
                layers[0] = new ColorDrawable(Color.TRANSPARENT);
                layers[1] = holder.getDrawable();
                TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                setImageDrawable(transitionDrawable);
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
        setImageDrawable(drawable);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
        }
        mHolder = null;
    }

    @Override
    public void onFailure() {
        mFailed = true;
        setImageResource(R.drawable.ic_failed);
        if (mRetryType == RetryType.CLICK) {
            setOnClickListener(this);
        } else if (mRetryType == RetryType.LONG_CLICK) {
            setOnLongClickListener(this);
        }

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
        }
        mHolder = null;
    }

    @Override
    public void onCancel() {
        // Empty, everything is done in cancel
    }

    @Override
    public void onClick(View v) {
        setOnClickListener(null);
        setClickable(false);

        load(mKey, mUrl);
    }

    @Override
    public boolean onLongClick(View v) {
        setOnLongClickListener(null);
        setLongClickable(false);

        load(mKey, mUrl);

        return true;
    }
}
