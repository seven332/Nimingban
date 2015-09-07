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

package com.hippo.nimingban;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;

import com.hippo.conaco.Conaco;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.network.HttpCookieDB;
import com.hippo.nimingban.network.NMBHttpClient;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.Crash;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.SimpleDrawableHelper;
import com.hippo.styleable.StyleableApplication;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.util.NetworkUtils;
import com.hippo.yorozuya.FileUtils;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

public final class NMBApplication extends StyleableApplication
        implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private SimpleCookieStore mSimpleCookieStore;
    private NMBHttpClient mNMBHttpClient;
    private NMBClient mNMBClient;
    private Conaco mConaco;
    private SimpleDrawableHelper mDrawableHelper;

    private boolean mConnectedWifi;

    @Override
    public void onCreate() {
        super.onCreate();

        // Prepare to crash
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        NMBAppConfig.initialize(this);
        Settings.initialize(this);
        DB.initialize(this);
        HttpCookieDB.initialize(this);
        ReadableTime.initialize(this);

        LeakCanary.install(this);

        // Remove temp file
        FileUtils.deleteContent(NMBAppConfig.getTempDir());

        updateNetworkState(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level ==  TRIM_MEMORY_BACKGROUND ) {
            if (mConaco != null) {
                mConaco.clearMemoryCache();
            }
        }
    }

    public static void updateNetworkState(Context context) {
        ((NMBApplication) context.getApplicationContext()).mConnectedWifi =
                NetworkUtils.isConnectedWifi(context);
    }

    public static boolean isConnectedWifi(Context context) {
        return ((NMBApplication) context.getApplicationContext()).mConnectedWifi;
    }

    public static SimpleCookieStore getSimpleCookieStore(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mSimpleCookieStore == null) {
            application.mSimpleCookieStore = new SimpleCookieStore();
        }
        return application.mSimpleCookieStore;
    }

    @NonNull
    public static NMBHttpClient getNMBHttpClient(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mNMBHttpClient == null) {
            application.mNMBHttpClient = new NMBHttpClient(context);
        }
        return application.mNMBHttpClient;
    }

    @NonNull
    public static NMBClient getNMBClient(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mNMBClient == null) {
            application.mNMBClient = new NMBClient(application);
        }
        return application.mNMBClient;
    }

    private static int getMemoryCacheMaxSize(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);
        return Math.min(20 * 1024 * 1024,
                Math.round(0.2f * activityManager.getMemoryClass() * 1024 * 1024));
    }

    @NonNull
    public static Conaco getConaco(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mConaco == null) {
            Conaco.Builder builder = new Conaco.Builder();
            builder.hasMemoryCache = true;
            builder.memoryCacheMaxSize = getMemoryCacheMaxSize(context);
            builder.hasDiskCache = true;
            builder.diskCacheDir = new File(context.getCacheDir(), "thumb");
            builder.diskCacheMaxSize = 80 * 1024 * 1024; // 80MB
            builder.httpClient = getNMBHttpClient(context);
            builder.drawableHelper = getSimpleDrawableHelper(context);
            application.mConaco = builder.build();
        }
        return application.mConaco;
    }

    @NonNull
    public static SimpleDrawableHelper getSimpleDrawableHelper(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mDrawableHelper == null) {
            application.mDrawableHelper = new SimpleDrawableHelper(context);
        }
        return application.mDrawableHelper;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            ex.printStackTrace();
            Crash.saveCrashInfo2File(this, ex);
            return true;
        } catch (Throwable tr) {
            return false;
        }
    }
}
