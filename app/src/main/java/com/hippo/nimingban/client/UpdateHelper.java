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

package com.hippo.nimingban.client;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import com.hippo.network.DownloadClient;
import com.hippo.network.DownloadRequest;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.UpdateStatus;
import com.hippo.nimingban.content.UpdateApkProvider;
import com.hippo.nimingban.util.OpenUrlHelper;
import com.hippo.nimingban.util.Settings;
import com.hippo.text.Html;
import com.hippo.unifile.UniFile;
import com.hippo.util.TextUtils2;
import com.hippo.util.Timer;
import com.hippo.yorozuya.FileUtils;
import java.io.File;

public final class UpdateHelper {

    private static boolean sUpdating = false;

    public static void showUpdateDialog(final Activity activity, final UpdateStatus status) {
        if (activity.isFinishing()) {
            return;
        }

        if (status == null) {
            return;
        }

        int versionCode = Settings.getVersionCode();
        if (versionCode >= status.versionCode) {
            return;
        }

        if (status.info == null || status.versionName == null || status.size == 0) {
            return;
        }

        Resources resources = activity.getResources();
        CharSequence message = TextUtils2.combine(
                resources.getString(R.string.version) + ": " + status.versionName + '\n' +
                        resources.getString(R.string.size) + ": " + FileUtils.humanReadableByteCount(status.size, false) + "\n\n",
                Html.fromHtml(status.info));
        new AlertDialog.Builder(activity)
                .setTitle(R.string.download_update)
                .setMessage(message)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSelectDialog(activity, status);
                    }
                })
                .show();
    }

    private static void showSelectDialog(final Activity activity, final UpdateStatus status) {
        final String[] items = new String[1 + status.discUrls.size()];
        int index = 0;
        items[index++] = activity.getString(R.string.direct_download);
        for (String disc : status.discUrls.keySet()) {
            items[index++] = disc;
        }

        new AlertDialog.Builder(activity)
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        downloadApk(activity, status.apkUrl, status.failedUrl,
                            "nimingban-" + status.versionName + ".apk");
                    } else {
                        OpenUrlHelper.openUrl(activity, status.discUrls.get(items[which]), false);
                    }
                }
            })
            .show();
    }

    public static void downloadApk(Context context, String url, String failedUrl, String filename) {
        if (sUpdating) {
            return;
        }

        sUpdating = true;

        context = context.getApplicationContext();
        File dir = NMBAppConfig.getExternalAppDir();
        if (dir == null) {
            Toast.makeText(context, R.string.download_update_failde, Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadRequest request = new DownloadRequest();
        request.setUrl(url);
        request.setFilename(filename);
        request.setDir(UniFile.fromFile(dir));
        request.setOkHttpClient(NMBApplication.getOkHttpClient(context));
        new DownloadApkTask(context, request, Uri.fromFile(new File(dir, filename)), failedUrl).execute();
    }

    private static class DownloadApkTask extends AsyncTask<Void, Void, Boolean> {

        private static final int NOTIFY_ID_DOWNLOADING = -1;

        private Context mContext;
        private DownloadRequest mRequest;
        private Uri mUri;
        private String mFailedUrl;
        private DownloadApkListener mListener;
        private final NotificationManager mNotifyManager;
        private NotificationCompat.Builder mDownloadingBuilder;
        private DownloadTimer mDownloadTimer;

        public DownloadApkTask(Context context, DownloadRequest request, Uri uri, String failedUrl) {
            mContext = context;
            mRequest = request;
            mFailedUrl = failedUrl;
            mListener = new DownloadApkListener();
            mRequest.setListener(mListener);
            mNotifyManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = context.getPackageName()+".UPDATE";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotifyManager.createNotificationChannel(
                        new NotificationChannel(
                                channelId,
                                context.getString(R.string.download_update),
                                NotificationManager.IMPORTANCE_LOW));
            }

            mDownloadingBuilder = new NotificationCompat.Builder(context, channelId);
            mDownloadingBuilder.setContentTitle(context.getString(R.string.downloading_update))
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setProgress(0, 0, true)
                    .setChannelId(channelId);

            mNotifyManager.notify(NOTIFY_ID_DOWNLOADING, mDownloadingBuilder.build());

            mDownloadTimer = new DownloadTimer(2000);
            mDownloadTimer.start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Only use content uri for Android N and above
                mUri = UpdateApkProvider.UPDATE_APK_URI;
                UpdateApkProvider.setUpdateApkFile(uri);
            } else {
                mUri = uri;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return DownloadClient.execute(mRequest);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(mUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, R.string.download_update_failde, Toast.LENGTH_SHORT).show();
                if (mFailedUrl != null) {
                    OpenUrlHelper.openUrl(mContext, mFailedUrl, false);
                }
            }

            sUpdating = false;
            mDownloadTimer.cancel();
            mDownloadTimer = null;
            mListener = null;
            mNotifyManager.cancel(NOTIFY_ID_DOWNLOADING);
            mDownloadingBuilder = null;
            mRequest = null;
            mContext = null;
        }

        private class DownloadTimer extends Timer {

            private long mInterval;

            private long lastReceivedSize = 0;

            public DownloadTimer(long interval) {
                super(interval);
                mInterval = interval;
            }

            @Override
            public void onTick() {
                if (mDownloadingBuilder == null || mNotifyManager == null) {
                    return;
                }

                long receivedSize = mListener.receivedSize;
                String speed = FileUtils.humanReadableByteCount((receivedSize - lastReceivedSize) *
                        1000 / mInterval, false) + "/s";
                int progress = -1;
                long totalSize = mListener.totalSize;
                if (totalSize != -1l) {
                    progress = (int) (receivedSize * 100 / totalSize);
                }
                lastReceivedSize = receivedSize;

                mDownloadingBuilder.setContentText(speed);
                if (progress == -1) {
                    mDownloadingBuilder.setProgress(0, 0, true);
                } else {
                    mDownloadingBuilder.setProgress(100, progress, false);
                }

                mNotifyManager.notify(NOTIFY_ID_DOWNLOADING, mDownloadingBuilder.build());
            }

            @Override
            public void onCancel() {
            }
        }

        private class DownloadApkListener extends DownloadClient.SimpleDownloadListener {

            public long totalSize = -1l;

            public long receivedSize = 0l;

            @Override
            public void onConnect(long totalSize) {
                this.totalSize = totalSize;
            }

            @Override
            public void onDonwlad(long receivedSize, long singleReceivedSize) {
                this.receivedSize = receivedSize;
            }
        }
    }
}
