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

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.network.NMBHttpClient;

public class NMBApplication extends Application {

    private NMBHttpClient mNMBHttpClient;
    private NMBClient mNMBClient;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @NonNull
    public static NMBHttpClient getNMBHttpClient(@NonNull Context context) {
        NMBApplication application = ((NMBApplication) context.getApplicationContext());
        if (application.mNMBHttpClient == null) {
            application.mNMBHttpClient = new NMBHttpClient();
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
}
