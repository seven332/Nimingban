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

package com.hippo.nimingban.network;

import com.hippo.httpclient.Cookie;
import com.hippo.httpclient.HttpClient;

import java.net.HttpCookie;
import java.net.URL;
import java.util.List;

public class NMBHttpClient extends HttpClient {

    private SimpleCookieStore mCookieStore = new SimpleCookieStore();

    @Override
    protected void fillCookie(URL url, Cookie cookie) {
        super.fillCookie(url, cookie);

        List<HttpCookie> httpCookies = mCookieStore.get(url);
        for (HttpCookie httpCookie : httpCookies) {
            cookie.put(httpCookie.getName(), httpCookie.getValue());
        }
    }

    @Override
    protected void storeCookie(URL url, String key, String value) {
        super.storeCookie(url, key, value);

        try {
            List<HttpCookie> httpCookies = HttpCookie.parse(key + ": " + value);
            for (HttpCookie httpCookie : httpCookies) {
                mCookieStore.add(url, httpCookie);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
