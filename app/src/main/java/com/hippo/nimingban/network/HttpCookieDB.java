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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hippo.network.dao.DaoMaster;
import com.hippo.network.dao.DaoSession;
import com.hippo.network.dao.HttpCookieDao;
import com.hippo.network.dao.HttpCookieRaw;

import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpCookieDB {

    private static DaoSession sDaoSession;

    public static class DBOpenHelper extends DaoMaster.OpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public static void initialize(Context context) {
        DBOpenHelper helper = new DBOpenHelper(
                context.getApplicationContext(), "httpcookie", null);

        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);

        sDaoSession = daoMaster.newSession();
    }


    public static Map<URL, List<HttpCookieWithId>> getAllCookies() {
        HttpCookieDao dao = sDaoSession.getHttpCookieDao();
        List<HttpCookieRaw> list = dao.queryBuilder().list();

        Map<URL, List<HttpCookieWithId>> result = new HashMap<>();
        for (HttpCookieRaw httpCookieRaw : list) {

            URL url;
            try {
                url = new URL(httpCookieRaw.getUrl());
            } catch (MalformedURLException e) {
                // Can not be recognized, remove it
                dao.delete(httpCookieRaw);
                continue;
            }

            // hasExpired
            long maxAgeNow;
            long maxAge = httpCookieRaw.getMaxAge();
            if (maxAge != -1) {
                maxAgeNow = maxAge - ((System.currentTimeMillis() - httpCookieRaw.getWhenCreated()) / 1000);
                if (maxAgeNow <= 0) {
                    // It has expired, remove it
                    dao.delete(httpCookieRaw);
                    continue;
                }
            } else {
                maxAgeNow = -1;
            }

            HttpCookie httpCookie = new HttpCookie(httpCookieRaw.getName(), httpCookieRaw.getValue());
            httpCookie.setComment(httpCookieRaw.getComment());
            httpCookie.setCommentURL(httpCookieRaw.getCommentURL());
            httpCookie.setDiscard(httpCookieRaw.getDiscard());
            httpCookie.setDomain(httpCookieRaw.getDomain());
            httpCookie.setMaxAge(maxAgeNow);
            httpCookie.setPath(httpCookieRaw.getPath());
            httpCookie.setPortlist(httpCookieRaw.getPortList());
            httpCookie.setSecure(httpCookieRaw.getSecure());
            httpCookie.setVersion(httpCookieRaw.getVersion());

            List<HttpCookieWithId> cookies = result.get(url);
            if (cookies == null) {
                cookies = new ArrayList<>();
                result.put(url, cookies);
            }

            cookies.add(new HttpCookieWithId(httpCookieRaw.getId(), httpCookie));
        }

        return result;
    }

    /*
    @Nullable
    public static List<HttpCookieWithId> getCookies(URI uri) {
        HttpCookieDao dao = sDaoSession.getHttpCookieDao();

        List<HttpCookieRaw> list = dao.queryBuilder().where(HttpCookieDao.Properties.Uri.eq(uri.toString())).list();

        if (list == null || list.isEmpty()) {
            return null;
        }

        List<HttpCookieWithId> result = new ArrayList<>();
        for (HttpCookieRaw httpCookieRaw : list) {
            HttpCookie httpCookie = new HttpCookie(httpCookieRaw.getName(), httpCookieRaw.getValue());
            httpCookie.setComment(httpCookieRaw.getComment());
            httpCookie.setCommentURL(httpCookieRaw.getCommentURL());
            httpCookie.setDiscard(httpCookieRaw.getDiscard());
            httpCookie.setDomain(httpCookieRaw.getDomain());
            httpCookie.setMaxAge(httpCookieRaw.getMaxAge() -
                    (System.currentTimeMillis() - httpCookieRaw.getWhenCreated())); // Fix maxAge
            httpCookie.setPath(httpCookieRaw.getPath());
            httpCookie.setPortlist(httpCookieRaw.getPortList());
            httpCookie.setSecure(httpCookieRaw.getSecure());
            httpCookie.setVersion(httpCookieRaw.getVersion());

            result.add(new HttpCookieWithId(httpCookieRaw.getId(), httpCookie));
        }

        return result;
    }
    */

    public static long addCookie(HttpCookie cookie, URL url) {
        HttpCookieDao dao = sDaoSession.getHttpCookieDao();

        HttpCookieRaw raw = new HttpCookieRaw();
        raw.setName(cookie.getName());
        raw.setValue(cookie.getValue());
        raw.setComment(cookie.getComment());
        raw.setCommentURL(cookie.getCommentURL());
        raw.setDiscard(cookie.getDiscard());
        raw.setDomain(cookie.getDomain());
        raw.setMaxAge(cookie.getMaxAge());
        raw.setPath(cookie.getPath());
        raw.setPortList(cookie.getPortlist());
        raw.setSecure(cookie.getSecure());
        raw.setVersion(cookie.getVersion());
        raw.setUrl(url.toString());
        raw.setWhenCreated(System.currentTimeMillis());

        return dao.insert(raw);
    }

    public static void updateCookie(HttpCookieWithId hcw, URL url) {
        HttpCookieDao dao = sDaoSession.getHttpCookieDao();

        HttpCookieRaw raw = dao.load(hcw.id);
        if (raw == null) {
            return;
        }
        HttpCookie cookie = hcw.httpCookie;
        raw.setName(cookie.getName());
        raw.setValue(cookie.getValue());
        raw.setComment(cookie.getComment());
        raw.setCommentURL(cookie.getCommentURL());
        raw.setDiscard(cookie.getDiscard());
        raw.setDomain(cookie.getDomain());
        raw.setMaxAge(cookie.getMaxAge());
        raw.setPath(cookie.getPath());
        raw.setPortList(cookie.getPortlist());
        raw.setSecure(cookie.getSecure());
        raw.setVersion(cookie.getVersion());
        raw.setUrl(url.toString());
        raw.setWhenCreated(System.currentTimeMillis());

        dao.update(raw);
    }

    public static void removeCookie(long id) {
        sDaoSession.getHttpCookieDao().deleteByKey(id);
    }

    public static void removeCookies(URL url) {
        sDaoSession.getHttpCookieDao().queryBuilder()
                .where(HttpCookieDao.Properties.Url.eq(url.toString()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public static void removeAllCookies() {
        sDaoSession.getHttpCookieDao().deleteAll();
    }
}
