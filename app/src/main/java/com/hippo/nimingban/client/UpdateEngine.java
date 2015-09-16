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

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.hippo.nimingban.client.data.UpdateStatus;
import com.hippo.okhttp.GoodRequestBuilder;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public final class UpdateEngine {

    private static final String TAG = UpdateEngine.class.getSimpleName();

    private static final String UPDATE_URL = "http://nimingban.herokuapp.com/update?version_code=";

    public static Call prepareUpdate(OkHttpClient okHttpClient, int versionCode) {
        String url = UPDATE_URL + versionCode;
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static UpdateStatus doUpdate(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();
            return JSON.parseObject(body, UpdateStatus.class);
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }
}
