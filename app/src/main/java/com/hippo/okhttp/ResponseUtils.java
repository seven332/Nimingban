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

package com.hippo.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.squareup.okhttp.Response;

import java.net.HttpCookie;
import java.net.URL;
import java.util.List;

public class ResponseUtils {

    private static SimpleCookieStore sCookieStore;

    public static void initialize(Context context) {
        sCookieStore = NMBApplication.getSimpleCookieStore(context);
    }

    private static void storeCookies(URL url, String key, String value) {
        try {
            List<HttpCookie> httpCookies = HttpCookie.parse(key + ": " + value);
            for (HttpCookie httpCookie : httpCookies) {
                sCookieStore.add(url, httpCookie);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    public static void storeCookies(@NonNull Response response) {
        URL url = response.request().url();
        String cookies = response.header("Set-Cookie");
        if (cookies != null) {
            storeCookies(url, "Set-Cookie", cookies);
        }
        String cookies2 = response.header("Set-Cookie2");
        if (cookies2 != null) {
            storeCookies(url, "Set-Cookie2", cookies2);
        }
    }
}
