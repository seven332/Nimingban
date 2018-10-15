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

package com.hippo.nimingban.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.hippo.nimingban.R;
import com.hippo.yorozuya.SimpleHandler;

import java.io.IOException;

public class DaDiaoService extends Service {

    public static final String ACTION_DA_DIAO = "com.hippo.nimingban.service.DaDiaoService.ACTION_DA_DIAO";

    public static final int NOTIFICATION_ID = 1;

    private int mOriginalVolume;
    private int mMaxVolume = -1;
    private int mPlaying = 0;

    // Keep volume when play sound
    private boolean mHasPostSetVolumeRunnable = false;
    private Runnable mSetVolumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMaxVolume == -1) {
                mHasPostSetVolumeRunnable = false;
                return;
            }

            final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMaxVolume, 0);

            SimpleHandler.getInstance().postDelayed(this, 500);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !ACTION_DA_DIAO.equals(intent.getAction())) {
            // Remove notification
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);

            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            final MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tnnaii_h_island_c));
            mp.prepare();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlaying--;

                    mp.release();

                    if (mPlaying == 0) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
                        mMaxVolume = -1;

                        // Remove notification
                        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);

                        // Stop service
                        stopSelf();
                    }
                }
            });

            if (mPlaying == 0) {
                mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                String channelId = getPackageName()+".DA_DIAO";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager notifyManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notifyManager.createNotificationChannel(
                            new NotificationChannel(
                                    channelId,
                                    getString(R.string.download_update),
                                    NotificationManager.IMPORTANCE_LOW));
                }

                // startForeground
                Notification notification = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_devil)
                        .setContentTitle(getString(R.string.da_diao_service))
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setChannelId(channelId)
                        .build();
                startForeground(NOTIFICATION_ID, notification);
            }

            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // Sometimes ear protection is off or disabled, make maxVolume to 1/3
            if (mAudioManager.isWiredHeadsetOn()) {
                maxVolume = Math.max(maxVolume / 3, mOriginalVolume);
            }
            // For headset on, can't set max volume directly because of ear protection
            // Just try getting max volume for head set
            for (int i = maxVolume; i > 0; i--) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                if (i == mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                    mMaxVolume = i;
                    break;
                }
                mMaxVolume = -1;
            }

            mPlaying++;

            mp.start();

            if (!mHasPostSetVolumeRunnable) {
                mHasPostSetVolumeRunnable = SimpleHandler.getInstance().postDelayed(mSetVolumeRunnable, 500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new IllegalStateException("No bindService");
    }
}
