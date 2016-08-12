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

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hippo.yorozuya.ObjectUtils;

import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimpleCookieStore {

    /** this map may have null keys! */
    private final Map<URL, List<HttpCookieWithId>> map;

    public SimpleCookieStore() {
        map = HttpCookieDB.getAllCookies();
    }

    /**
     * Returns a non-null path ending in "/".
     */
    private static String matchablePath(String path) {
        if (path == null) {
            return "/";
        } else if (path.endsWith("/")) {
            return path;
        } else {
            return path + "/";
        }
    }

    /**
     * Returns true if {@code cookie} should be sent to or accepted from {@code uri} with respect
     * to the cookie's path. Cookies match by directory prefix: URI "/foo" matches cookies "/foo",
     * "/foo/" and "/foo/bar", but not "/" or "/foobar".
     */
    private static boolean pathMatches(HttpCookie cookie, URL url) {
        String uriPath = matchablePath(url.getPath());
        String cookiePath = matchablePath(cookie.getPath());
        return uriPath.startsWith(cookiePath);
    }

    /**
     * Returns the port to use for {@code scheme} connections will use when
     * {@link URI#getPort} returns {@code specifiedPort}.
     */
    public static int getEffectivePort(URL url) {
        int specifiedPort = url.getPort();
        if (specifiedPort != -1) {
            return specifiedPort;
        }

        String protocol = url.getProtocol();
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        } else if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        } else {
            return -1;
        }
    }

    /**
     * Returns true if {@code cookie} should be sent to {@code uri} with respect to the cookie's
     * port list.
     */
    private static boolean portMatches(HttpCookie cookie, URL url) {
        if (cookie.getPortlist() == null) {
            return true;
        }
        return Arrays.asList(cookie.getPortlist().split(","))
                .contains(Integer.toString(getEffectivePort(url)));
    }

    private static HttpCookieWithId removeCookie(List<HttpCookieWithId> list, HttpCookie cookie) {
        for (int i = 0, n = list.size(); i < n; i++) {
            HttpCookieWithId hcwi = list.get(i);
            if (hcwi.httpCookie.equals(cookie)) {
                list.remove(i);
                return hcwi;
            }
        }

        return null;
    }

    public synchronized void add(URL url, HttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie == null");
        }

        if (cookie.hasExpired()) {
            remove(url, cookie);
            return;
        }

        url = cookiesUrl(url);
        List<HttpCookieWithId> cookies = map.get(url);
        if (cookies == null) {
            cookies = new ArrayList<>();
            map.put(url, cookies);
        } else {
            HttpCookieWithId hcwi = removeCookie(cookies, cookie);
            if (hcwi != null) {
                // Remove cookie in DB
                HttpCookieDB.removeCookie(hcwi.id);
            }
        }

        // Add to DB
        long id = HttpCookieDB.addCookie(cookie, url);
        // Add to list
        cookies.add(new HttpCookieWithId(id, cookie));
    }

    private URL cookiesUrl(URL url) {
        if (url == null) {
            return null;
        }
        try {
            return new URL("http", url.getHost(), -1, "");
        } catch (MalformedURLException e) {
            return url;
        }
    }

    public synchronized List<HttpCookie> get(URL url) {
        if (url == null) {
            throw new NullPointerException("uri == null");
        }

        List<HttpCookie> result = new ArrayList<>();

        // get cookies associated with given URI. If none, returns an empty list
        List<HttpCookieWithId> cookiesForUri = map.get(cookiesUrl(url));

        if (cookiesForUri != null) {
            for (Iterator<HttpCookieWithId> i = cookiesForUri.iterator(); i.hasNext(); ) {
                HttpCookieWithId hcwi = i.next();
                HttpCookie cookie = hcwi.httpCookie;
                if (hcwi.hasExpired()) {
                    i.remove(); // remove expired cookies
                    HttpCookieDB.removeCookie(hcwi.id); // remove from DB
                } else if (pathMatches(cookie, url) && portMatches(cookie, url)) {
                    result.add(cookie);
                }
            }
        }

        // get all cookies that domain matches the URI
        for (Map.Entry<URL, List<HttpCookieWithId>> entry : map.entrySet()) {
            if (url.equals(entry.getKey())) {
                continue; // skip the given URI; we've already handled it
            }

            List<HttpCookieWithId> entryCookies = entry.getValue();
            for (Iterator<HttpCookieWithId> i = entryCookies.iterator(); i.hasNext(); ) {
                HttpCookieWithId hcwi = i.next();
                HttpCookie cookie = hcwi.httpCookie;
                if (!HttpCookie.domainMatches(cookie.getDomain(), url.getHost())) {
                    continue;
                }
                if (hcwi.hasExpired()) {
                    i.remove(); // remove expired cookies
                    HttpCookieDB.removeCookie(hcwi.id); // remove from DB
                } else if (pathMatches(cookie, url) && portMatches(cookie, url) && !result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    public synchronized List<HttpCookie> getCookies() {
        List<HttpCookie> result = new ArrayList<>();
        for (List<HttpCookieWithId> list : map.values()) {
            for (Iterator<HttpCookieWithId> i = list.iterator(); i.hasNext(); ) {
                HttpCookieWithId hcwi = i.next();
                HttpCookie cookie = hcwi.httpCookie;
                if (hcwi.hasExpired()) {
                    i.remove(); // remove expired cookies
                    HttpCookieDB.removeCookie(hcwi.id); // remove from DB
                } else if (!result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<URL> getURLs() {
        List<URL> result = new ArrayList<>(map.keySet());
        result.remove(null); // sigh
        return Collections.unmodifiableList(result);
    }

    public synchronized void remove(URL url) {
        if (url == null) {
            throw new NullPointerException("cookie == null");
        }

        url = cookiesUrl(url);

        map.remove(url);
        HttpCookieDB.removeCookies(url);
    }

    public synchronized void remove(URL url, HttpCookie cookie) {
        if (url == null) {
            throw new NullPointerException("url == null");
        }

        url = cookiesUrl(url);
        List<HttpCookieWithId> cookies = map.get(url);
        if (cookies != null) {
            HttpCookieWithId hcwi = removeCookie(cookies, cookie);
            if (hcwi != null) {
                HttpCookieDB.removeCookie(hcwi.id);
            }
        }
    }

    public synchronized void remove(URL url, String name) {
        if (url == null) {
            throw new NullPointerException("cookie == null");
        }

        url = cookiesUrl(url);
        List<HttpCookieWithId> cookies = map.get(url);
        if (cookies != null) {
            for (Iterator<HttpCookieWithId> i = cookies.iterator(); i.hasNext(); ) {
                HttpCookieWithId hcwi = i.next();
                HttpCookie cookie = hcwi.httpCookie;
                if (hcwi.hasExpired() || (ObjectUtils.equal(name, hcwi.httpCookie.getName()) &&
                        pathMatches(cookie, url) && portMatches(cookie, url))) {
                    i.remove(); // remove expired cookies
                    HttpCookieDB.removeCookie(hcwi.id); // remove from DB
                }
            }
        }
    }

    public synchronized boolean removeAll() {
        boolean result = !map.isEmpty();
        map.clear();
        HttpCookieDB.removeAllCookies();
        return result;
    }

    public synchronized HttpCookieWithId getCookie(@NonNull URL url, String name) {
        List<HttpCookieWithId> cookies = map.get(cookiesUrl(url));
        if (cookies != null) {
            for (Iterator<HttpCookieWithId> i = cookies.iterator(); i.hasNext(); ) {
                HttpCookieWithId hcwi = i.next();
                HttpCookie cookie = hcwi.httpCookie;

                if (hcwi.hasExpired()) {
                    i.remove(); // remove expired cookies
                    HttpCookieDB.removeCookie(hcwi.id); // remove from DB
                } else if (ObjectUtils.equal(name, hcwi.httpCookie.getName()) &&
                        pathMatches(cookie, url) && portMatches(cookie, url)) {
                    return hcwi;
                }
            }
        }

        return null;
    }

    public synchronized boolean contain(@NonNull URL url, String name) {
        return getCookie(url, name) != null;
    }

    public synchronized List<TransportableHttpCookie> getTransportableCookies() {
        List<TransportableHttpCookie> result = new ArrayList<>();
        for (URL url : map.keySet()) {
            List<HttpCookieWithId> list = map.get(url);
            result.addAll(TransportableHttpCookie.from(url, list));
        }
        return result;
    }

    public void fixLostCookiePath() {
        for (URL url : map.keySet()) {
            List<HttpCookieWithId> list = map.get(url);
            for (HttpCookieWithId hcwi : list) {
                HttpCookie cookie = hcwi.httpCookie;
                if (TextUtils.isEmpty(cookie.getPath())) {
                    cookie.setPath("/");
                    HttpCookieDB.updateCookie(hcwi, url);
                }
            }
        }
    }
}
