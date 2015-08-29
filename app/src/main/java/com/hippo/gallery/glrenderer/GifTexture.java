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

package com.hippo.gallery.glrenderer;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.hippo.gallery.GifDecoder;
import com.hippo.gallery.GifDecoderBuilder;
import com.hippo.yorozuya.InfiniteThreadExecutor;
import com.hippo.yorozuya.PriorityThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class GifTexture extends TiledTexture {

    private static final String TAG = GifTexture.class.getSimpleName();

    private static InfiniteThreadExecutor sThreadExecutor;
    private static PVLock mPVLock = new PVLock(3);

    private volatile GifDecoderBuilder mGifDecoderBuilder;
    private volatile GifDecoder mGifDecoder;
    private boolean mRunning = false;
    private volatile boolean mDecoding = false;
    private volatile boolean mRecycle =false;

    private TiledTexture.Uploader mUploader;

    private GifDecodeTask mGifDecodeTask;

    public static void initialize() {
        if (sThreadExecutor == null) {
            sThreadExecutor = new InfiniteThreadExecutor(3000, new LinkedBlockingQueue<Runnable>(),
                    new PriorityThreadFactory("GifDecode", android.os.Process.THREAD_PRIORITY_BACKGROUND));
        }
    }

    class GifDecodeTask implements Runnable {

        private volatile boolean mRest = false;

        public void setRest() {
            mRest = true;
        }

        @Override
        public void run() {
            long startDecodeTime = SystemClock.uptimeMillis();

            GifDecoder gifDecoder = mGifDecoder;

            if (gifDecoder == null) {
                if (mGifDecoderBuilder == null) {
                    // It is recycled
                    mRunning = false;
                    mGifDecodeTask = null;
                    return;
                } else {
                    mDecoding = true;

                    mPVLock.p();
                    if (!mRecycle) {
                        gifDecoder = mGifDecoderBuilder.build();
                    }
                    mPVLock.v();

                    mGifDecoderBuilder.close();
                    mGifDecoderBuilder = null;

                    // First image is loaded, so go to next
                    if (gifDecoder != null) {
                        gifDecoder.advance();
                    }
                }
            }

            if (mRecycle || gifDecoder == null) {
                // Can't get GifDecoder
                mDecoding = false;
                mRunning = false;
                mGifDecodeTask = null;
                if (gifDecoder != null) {
                    gifDecoder.recycle();
                }
                return;
            } else {
                mGifDecoder = gifDecoder;
            }

            long endDecodeTime = SystemClock.uptimeMillis();
            mDecoding = true;
            long firstDelay = gifDecoder.getNextDelay() - (endDecodeTime - startDecodeTime);
            mDecoding = false;
            if (firstDelay > 0) {
                try {
                    Thread.sleep(firstDelay);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }

            long workTime = 0;
            while (mRunning) {
                mDecoding = true;

                long time1 = SystemClock.uptimeMillis();

                gifDecoder.advance();
                Bitmap bitmap = gifDecoder.getNextFrame();

                if (bitmap != null) {
                    setBitmap(bitmap);
                    mUploader.addTexture(GifTexture.this);
                }

                long time2 = SystemClock.uptimeMillis();

                workTime = ((time2 - time1) + workTime) / 2;

                long delay = gifDecoder.getNextDelay() - workTime;

                mDecoding = false;

                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }

            mRunning = false;
            mDecoding = false;
            mGifDecodeTask = null;

            if (mRest) {
                mRest = false;
                gifDecoder.resetFrameIndex();
            }

            if (mRecycle) {
                gifDecoder.recycle();
            }
        }
    }

    public GifTexture(@NonNull GifDecoderBuilder gifDecoderBuilder, @NonNull Bitmap bitmap, @NonNull TiledTexture.Uploader uploader) {
        super(bitmap);

        mGifDecoderBuilder = gifDecoderBuilder;
        mUploader = uploader;

        //start to buid GifDecode
        mGifDecodeTask = new GifDecodeTask();
        sThreadExecutor.execute(mGifDecodeTask);
    }

    public void start() {
        if (!mRunning) {
            mRunning = true;
            if (mGifDecodeTask == null) {
                mGifDecodeTask = new GifDecodeTask();
                sThreadExecutor.execute(mGifDecodeTask);
            }
        }
    }

    public void stop() {
        if (mRunning) {
            mRunning = false;

            if (!mDecoding && mGifDecoder != null) {
                mGifDecoder.resetFrameIndex();
                mGifDecoder.advance();
                Bitmap bitmap = mGifDecoder.getNextFrame();
                if (bitmap != null) {
                    setBitmap(bitmap);
                    mUploader.addTexture(GifTexture.this);
                }
            } else {
                if (mGifDecodeTask != null) {
                    mGifDecodeTask.setRest();
                }
            }

            mGifDecodeTask = null;
        }
    }

    @Override
    public void recycle() {
        mRecycle = true;
        mRunning = false;

        if (!mDecoding && mGifDecoder != null) {
            mGifDecoder.recycle();
        }

        mGifDecoder = null;
        mGifDecodeTask = null;

        super.recycle();
    }

    static class PVLock {

        private int mCounter;

        public PVLock(int count) {
            mCounter = count;
        }

        /**
         * Obtain
         */
        public synchronized void p() {
            while (true) {
                if (mCounter > 0) {
                    mCounter--;
                    break;
                } else {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
        }

        /**
         * Release
         */
        public synchronized void v() {
            mCounter++;
            if (mCounter > 0) {
                this.notify();
            }
        }
    }
}
