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

import com.hippo.network.ResponseCodeException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConvertEngine {

    private static final String TAG = ConvertEngine.class.getSimpleName();

    private static final String URL = "http://opencc.herokuapp.com/opencc";

    public static Call prepareConvert(OkHttpClient okHttpClient, String config, String content) {
        RequestBody formBody = new FormBody.Builder()
                .add("config", config)
                .add("content", content)
                .build();
        String url = URL;
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).post(formBody).build();
        return okHttpClient.newCall(request);
    }

    public static String doConvert(Call call) throws Exception {
        try {
            Response response = call.execute();
            int code = response.code();
            if (code != 200) {
                throw new ResponseCodeException(code);
            }
            return response.body().string();
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }
}
