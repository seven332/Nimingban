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

package com.hippo.drawable;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.Unikery;

import pl.droidsonroids.gif.GifDrawable;

public class UnikeryDrawable extends WrapDrawable implements Unikery {

    private int mTaskId = Unikery.INVAILD_ID;

    private View mView;

    private DrawableHolder mHolder;

    public UnikeryDrawable(View view) {
        mView = view;
    }

    private void setDrawableSafely(Drawable drawable) {
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

        setDrawable(drawable);
    }

    @Override
    public void setDrawable(Drawable drawable) {
        // Remove old callback
        Drawable oldDrawable = getDrawable();
        if (oldDrawable != null) {
            oldDrawable.setCallback(null);
        }

        super.setDrawable(drawable);

        if (drawable != null) {
            drawable.setCallback(mView);
        }

        updateBounds();
        mView.requestLayout();
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

    @Override
    public void onRequest() {
    }

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        DrawableHolder olderHolder = mHolder;
        mHolder = holder;
        holder.obtain();

        Drawable drawable = holder.getDrawable();
        if (drawable instanceof GifDrawable) {
            ((GifDrawable) drawable).start();
        }

        Log.d("TAG", "onGetDrawable");
        setDrawableSafely(drawable);

        if (olderHolder != null) {
            olderHolder.release();
        }

        return true;
    }

    @Override
    public void onSetDrawable(Drawable drawable) {
        setDrawableSafely(drawable);

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    public void onFailure() {
        // Empty
    }

    @Override
    public void onCancel() {
        // Empty
    }
}
