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

package com.hippo.nimingban.client.data;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.network.HttpCookieWithId;
import com.hippo.nimingban.network.SimpleCookieStore;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ACSite extends Site {

    private URL mSiteUrl;

    private static ACSite sInstance;

    public static ACSite getInstance() {
        if (sInstance == null) {
            sInstance = new ACSite();
        }
        return sInstance;
    }

    private ACSite() {
        try {
            mSiteUrl = new URL(ACUrl.HOST);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int getId() {
        return Site.AC;
    }

    @Override
    public long getCookieMaxAge(Context context) {
        SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(context);
        HttpCookieWithId cookie = cookieStore.getCookie(mSiteUrl, "userId");
        if (cookie == null) {
            return -2;
        } else {
            return cookie.getMaxAge();
        }
    }

    @Override
    public String getUserId(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();

        String id;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(mac.getBytes());
            id = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            id = String.valueOf(mac.hashCode());
        }

        return id;
    }

    @Override
    public String getPostTitle(Context context, String postId) {
        return "No." + postId;
    }

    /**
     * http://stackoverflow.com/questions/332079
     *
     * @param bytes The bytes to convert.
     * @return A {@link String} converted from the bytes of a hashable key used
     *         to store a filename on the disk, to hex digits.
     */
    private static String bytesToHexString(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}
