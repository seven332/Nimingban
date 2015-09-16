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

import com.hippo.network.Cookies;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.squareup.okhttp.Request;

import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class GoodRequestBuilder extends Request.Builder {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/43.0.2357.132 Safari/537.36";
    private static final String sUserAgent = System.getProperty("http.agent", DEFAULT_USER_AGENT);

    private static SimpleCookieStore sCookieStore;


    public static void initialize(Context context) {
        sCookieStore = NMBApplication.getSimpleCookieStore(context);
    }

    public GoodRequestBuilder(String url) {
        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }

        url(u);

        addHeader("User-Agent", sUserAgent);

        Cookies cookies = new Cookies();
        List<HttpCookie> httpCookies = sCookieStore.get(u);
        for (HttpCookie httpCookie : httpCookies) {
            cookies.put(httpCookie.getName(), httpCookie.getValue());
        }
        addHeader("Cookie", cookies.toString());
    }
}
