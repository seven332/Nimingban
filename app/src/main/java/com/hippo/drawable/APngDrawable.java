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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.hippo.yorozuya.SimpleHandler;

import java.io.InputStream;

public class APngDrawable extends Drawable implements Runnable, Animatable {

    private long mNativePtr;
    private final int mWidth;
    private final int mHeight;
    private final int mFrameCount;
    private Bitmap mBitmap;
    private Paint mPaint;

    private boolean mRunning = false;

    private APngDrawable(long nativePtr, int width, int height, int frameCount) {
        mNativePtr = nativePtr;
        mWidth = width;
        mHeight = height;
        mFrameCount = frameCount;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        next();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public void start() {
        if (mFrameCount <= 1 || mNativePtr == 0 || mRunning) {
            return;
        }

        mRunning = true;

        SimpleHandler.getInstance().postDelayed(this, getCurrentDelay());
    }

    @Override
    public void stop() {
        mRunning = false;
        SimpleHandler.getInstance().removeCallbacks(this);
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void run() {
        // Check released
        if (mNativePtr == 0) {
            mRunning = false;
            return;
        }

        next();
        invalidateSelf();

        if (mRunning) {
            SimpleHandler.getInstance().postDelayed(this, getCurrentDelay());
        }
    }

    public void next() {
        nativeNext(mNativePtr, mBitmap);
    }

    private int getCurrentDelay() {
        return nativeCurrentDelay(mNativePtr);
    }

    public void recycle() {
        nativeRecycle(mNativePtr);
        mNativePtr = 0;
        mBitmap.recycle();
        mBitmap = null;
    }

    public static APngDrawable decode(InputStream is) {
        return nativeDecode(is);
    }

    @Override
    protected void finalize() throws Throwable {
        if (mNativePtr != 0) {
            recycle();
        }
        super.finalize();
    }

    private static native APngDrawable nativeDecode(InputStream is);

    private static native void nativeNext(long nativePtr, Bitmap bitmap);

    private static native int nativeCurrentDelay(long nativePtr);

    private static native void nativeRecycle(long nativePtr);

    static {
        System.loadLibrary("apng");
    }
}
