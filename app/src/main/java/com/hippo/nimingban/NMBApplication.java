/*
 * Copyright 2016 Hippo Seven
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

/*
 * Created by Hippo on 10/7/2016.
 */

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.hippo.gukize.Gukize;
import com.hippo.nimingban.client.NMBInterceptor;
import com.hippo.yorozuya.OSUtils;

import java.io.File;

import okhttp3.OkHttpClient;

public class NMBApplication extends Application {

    private OkHttpClient mOkHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Gukize
        final Gukize.Builder builder = new Gukize.Builder();
        builder.hasMemoryCache = true;
        builder.memoryCacheMaxSize = Math.min(20 * 1024 * 1024, Math.round(0.2f * OSUtils.getAppMaxMemory()));
        builder.hasDiskCache = true;
        builder.diskCacheDir = new File(getCacheDir(), "thumb");
        builder.diskCacheMaxSize = 80 * 1024 * 1024; // 80MB
        builder.okHttpClient = getOkHttpClient(this);
        builder.debug = false;
        Gukize.init(builder);
    }

    public static OkHttpClient getOkHttpClient(@NonNull Context context) {
        final NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mOkHttpClient == null) {
            application.mOkHttpClient = new OkHttpClient.Builder()
                    // TODO .dns(new NMBDns())
                    // TODO .cookieJar(new CookieDBJar(getSimpleCookieStore(context)))
                    .addInterceptor(new NMBInterceptor())
                    .build();
        }
        return application.mOkHttpClient;
    }
}
