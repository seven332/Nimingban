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

import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.network.HttpCookieWithId;
import com.hippo.nimingban.network.SimpleCookieStore;

import java.net.MalformedURLException;
import java.net.URL;

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
}
