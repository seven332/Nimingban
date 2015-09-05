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

package com.hippo.util;

import android.os.Handler;

import com.hippo.yorozuya.SimpleHandler;

public abstract class Timer {

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mInterval;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = false;

    private final Handler mHandler;

    public Timer(long interval) {
        mInterval = interval;
        mHandler = SimpleHandler.getInstance();
    }

    /**
     * Cancel the countdown.
     */
    public synchronized final void cancel() {
        mHandler.removeCallbacks(mRunnable);
        mCancelled = true;
        mHandler.post(mRunnable);
    }

    /**
     * Start the countdown.
     */
    public synchronized final Timer start() {
        mCancelled = false;
        mHandler.post(mRunnable);
        return this;
    }

    /**
     * Callback fired on regular interval.
     */
    public abstract void onTick();

    public abstract void onCancel();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (Timer.this) {
                if (mCancelled) {
                    onCancel();
                } else {
                    onTick();
                    mHandler.postDelayed(mRunnable, mInterval);
                }
            }
        }
    };
}
