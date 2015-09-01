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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.beerbelly.SimpleDiskCache;
import com.hippo.conaco.Conaco;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.network.HttpCookieDB;
import com.hippo.nimingban.network.NMBHttpClient;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.widget.SimpleDrawableHelper;
import com.hippo.vectorold.content.VectorContext;
import com.hippo.yorozuya.FileUtils;

import java.io.File;
import java.io.IOException;

public class NMBApplication extends Application {

    private SimpleCookieStore mSimpleCookieStore;
    private NMBHttpClient mNMBHttpClient;
    private NMBClient mNMBClient;
    private Conaco mConaco;
    private SimpleDrawableHelper mDrawableHelper;
    private SimpleDiskCache mImageDiskCache;

    @Override
    public void onCreate() {
        super.onCreate();

        // Remove temp file
        FileUtils.deleteContent(NMBAppConfig.getTempDir(this));

        DB.initialize(this);
        HttpCookieDB.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(VectorContext.wrapContext(newBase));
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
            builder.diskCacheMaxSize = 20 * 1024 * 1024;
            builder.httpClient = getNMBHttpClient(context);
            application.mConaco = builder.build();
        }
        return application.mConaco;
    }

    @Nullable
    public static SimpleDrawableHelper getSimpleDrawableHelper(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mDrawableHelper == null) {
            application.mDrawableHelper = new SimpleDrawableHelper(context);
        }
        return application.mDrawableHelper;
    }

    @Nullable
    public static SimpleDiskCache getImageDiskCache(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mImageDiskCache == null) {
            try {
                application.mImageDiskCache = new SimpleDiskCache(new File(context.getCacheDir(), "image"), 20 * 1024 * 1024);
            } catch (IOException e) {
                // Ingore
            }
        }
        return application.mImageDiskCache;
    }
}
