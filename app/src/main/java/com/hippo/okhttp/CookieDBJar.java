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

package com.hippo.okhttp;

import android.text.TextUtils;

import com.hippo.nimingban.network.SimpleCookieStore;

import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CookieDBJar implements CookieJar {

    private SimpleCookieStore mCookieStore;

    public CookieDBJar(SimpleCookieStore cookieStore) {
        mCookieStore = cookieStore;
    }

    private Cookie httpCookie2Cookie(URL url, HttpCookie httpCookie) {
        String domain = httpCookie.getDomain();
        String path = httpCookie.getPath();
        if (TextUtils.isEmpty(domain)) {
            domain = url.getHost();
        }
        if (TextUtils.isEmpty(path)) {
            path = url.getPath();
        }

        Cookie.Builder builder = new Cookie.Builder()
                .name(httpCookie.getName())
                .value(httpCookie.getValue())
                .expiresAt(System.currentTimeMillis() + (httpCookie.getMaxAge() * 1000))
                .domain(domain)
                .path(path);
        if (httpCookie.getSecure()) {
            builder.secure();
        }
        return builder.build();
    }

    private HttpCookie cookie2HttpCookie(Cookie cookie) {
        HttpCookie httpCookie = new HttpCookie(cookie.name(), cookie.value());
        if (cookie.expiresAt() < System.currentTimeMillis()) {
            httpCookie.setMaxAge(-100L);
        } else {
            httpCookie.setMaxAge((cookie.expiresAt() - System.currentTimeMillis()) / 1000);
        }
        httpCookie.setDomain(cookie.domain());
        httpCookie.setPath(cookie.path());
        httpCookie.setSecure(cookie.secure());
        return httpCookie;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies == null) {
            return;
        }

        URL u = url.url();
        for (Cookie cookie : cookies) {
            mCookieStore.add(u, cookie2HttpCookie(cookie));
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<HttpCookie> httpCookies = mCookieStore.get(url.url());
        List<Cookie> result = new ArrayList<>(httpCookies.size());

        URL u = url.url();
        for (HttpCookie httpCookie : httpCookies) {
            result.add(httpCookie2Cookie(u, httpCookie));
        }

        return result;
    }
}
