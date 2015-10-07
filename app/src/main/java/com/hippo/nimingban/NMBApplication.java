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
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.hippo.conaco.Conaco;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.network.HttpCookieDB;
import com.hippo.nimingban.network.HttpCookieWithId;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.nimingban.util.Crash;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.SimpleDrawableHelper;
import com.hippo.okhttp.GoodHttpClient;
import com.hippo.okhttp.GoodRequestBuilder;
import com.hippo.okhttp.ResponseUtils;
import com.hippo.util.NetworkUtils;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.Say;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;
import com.tendcloud.tenddata.TCAgent;

import java.io.File;
import java.net.HttpCookie;
import java.net.URL;

public final class NMBApplication extends Application
        implements Thread.UncaughtExceptionHandler, Messenger.Receiver {

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private SimpleCookieStore mSimpleCookieStore;
    private NMBClient mNMBClient;
    private Conaco mConaco;
    private SimpleDrawableHelper mDrawableHelper;
    private OkHttpClient mOkHttpClient;

    private boolean mConnectedWifi;

    private boolean mHasInitTCAgent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Prepare to crash
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        NMBAppConfig.initialize(this);
        File logFile = NMBAppConfig.getFileInAppDir("nimingban.log");
        if (logFile != null) {
            Say.initSayFile(logFile);
        }
        Settings.initialize(this);
        DB.initialize(this);
        HttpCookieDB.initialize(this);
        ReadableTime.initialize(this);
        GoodRequestBuilder.initialize(this);
        ResponseUtils.initialize(this);
        BitmapUtils.initialize(this);

        LeakCanary.install(this);

        // Remove temp file
        FileUtils.deleteContent(NMBAppConfig.getTempDir());

        updateNetworkState(this);

        // Theme
        setTheme(Settings.getDarkTheme() ? R.style.AppTheme_Dark : R.style.AppTheme);

        Messenger.getInstance().register(Constants.MESSENGER_ID_CHANGE_THEME, this);

        try {
            update();
        } catch (PackageManager.NameNotFoundException e) {
            // Ignore
        }

        // TCAgent
        if (Settings.getAnalysis()) {
            mHasInitTCAgent = true;
            TCAgent.init(this);
        } else {
            mHasInitTCAgent = false;
        }
    }

    private void update() throws PackageManager.NameNotFoundException {
        int oldVersionCode = Settings.getVersionCode();
        PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        Settings.putVersionCode(pi.versionCode);

        if (oldVersionCode < 6) {
            updateCookies(this);
        }

        if (oldVersionCode < 14) {
            Settings.putGuideListActivity(true);
        }

        if (oldVersionCode < 20) {
            Settings.putSetAnalysis(false);
            Settings.putAnalysis(false);
        }
    }

    public static void updateCookies(Context context) {
        SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(context);

        URL url = ACSite.getInstance().getSiteUrl();
        HttpCookieWithId hcwi = cookieStore.getCookie(url, "userId");
        if (hcwi != null) {
            HttpCookie oldCookie = hcwi.httpCookie;
            cookieStore.remove(url, oldCookie);

            HttpCookie newCookie = new HttpCookie("userhash", oldCookie.getValue());
            newCookie.setComment(oldCookie.getComment());
            newCookie.setCommentURL(oldCookie.getCommentURL());
            newCookie.setDiscard(oldCookie.getDiscard());
            newCookie.setDomain(oldCookie.getDomain());
            newCookie.setMaxAge(oldCookie.getMaxAge());
            newCookie.setPath(oldCookie.getPath());
            newCookie.setPortlist(oldCookie.getPortlist());
            newCookie.setSecure(oldCookie.getSecure());
            newCookie.setVersion(oldCookie.getVersion());

            cookieStore.add(url, newCookie);
        }
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

    public static boolean hasInitTCAgent(Context context) {
        return ((NMBApplication) context.getApplicationContext()).mHasInitTCAgent;
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
            builder.okHttpClient = getOkHttpClient(context);
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

    public static OkHttpClient getOkHttpClient(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mOkHttpClient == null) {
            application.mOkHttpClient = new GoodHttpClient();
        }
        return application.mOkHttpClient;
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

    @Override
    public void onReceive(int id, Object obj) {
        setTheme((Boolean) obj ? R.style.AppTheme_Dark : R.style.AppTheme);
    }
}
