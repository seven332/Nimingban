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
import android.support.annotation.NonNull;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.ac.data.ACCdnPath;
import com.hippo.nimingban.network.HttpCookieWithId;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.Settings;
import com.hippo.util.UrlUtils;
import com.hippo.yorozuya.MathUtils;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.HttpUrl;

public class ACSite extends Site {

    private static final String TAG = ACSite.class.getSimpleName();

    private static final String DEFAULT_PICTURE_PREFIX = ACUrl.getHost() + "/Public/Upload/";

    private static final List<ACCdnPath> DEFAULT_AC_CDN_PATH_LIST = Collections.singletonList(new ACCdnPath());
    private static final String[] DEFAULT_AC_CDN_HOST_LIST = { ACCdnPath.DEFAULT_CDN_HOST };

    private URL mSiteUrl;

    private List<ACCdnPath> mCdnPathList = DEFAULT_AC_CDN_PATH_LIST;
    private float mRateSum;

    private boolean mCdnHostsDirty;
    private String[] mCdnHosts = DEFAULT_AC_CDN_HOST_LIST;

    private static ACSite sInstance;

    public static ACSite getInstance() {
        if (sInstance == null) {
            sInstance = new ACSite();
        }
        return sInstance;
    }

    private ACSite() {
        try {
            mSiteUrl = new URL(ACUrl.getHost());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public URL getSiteUrl() {
        return mSiteUrl;
    }

    @Override
    public int getId() {
        return Site.AC;
    }

    @Override
    public String getReadableName(Context context) {
        return "ac";
    }

    @Override
    public long getCookieMaxAge(Context context) {
        SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(context);
        HttpCookieWithId cookie = cookieStore.getCookie(mSiteUrl, "userhash");
        if (cookie == null) {
            return -2;
        } else {
            return cookie.getMaxAge();
        }
    }

    @Override
    public void setCookieMaxAge(Context context, long maxAge) {
        SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(context);
        HttpCookieWithId cookie = cookieStore.getCookie(mSiteUrl, "userhash");
        if (cookie != null) {
            // Remove it
            cookieStore.remove(mSiteUrl, "userhash");

            // Update it
            HttpCookie httpCookie = cookie.httpCookie;
            httpCookie.setMaxAge(maxAge);
            cookieStore.add(mSiteUrl, httpCookie);
        }
    }

    @Override
    public String getUserId(Context context) {
        return Settings.getFeedId();
    }

    @Override
    public String getPostTitle(Context context, String postId) {
        return "No." + postId;
    }

    @Override
    public String getReportForumId() {
        return "18"; // TODO how to get it ?
    }

    public synchronized void setCdnPath(List<ACCdnPath> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // Remove invalid cdn path
        List<ACCdnPath> paths = new ArrayList<>(list.size());
        for (ACCdnPath path : list) {
            if (path.url != null && HttpUrl.parse(path.url) != null && path.rate > 0) {
                paths.add(path);
            }
        }
        if (paths.isEmpty()) {
            return;
        }

        mCdnPathList = paths;

        mRateSum = 0.0f;
        for (int i = 0, size = list.size(); i < size; i++) {
            mRateSum += list.get(i).rate;
        }

        // Set cdn hosts dirty
        mCdnHostsDirty = true;
    }

    private ACCdnPath getCdnPath() {
        final float r = MathUtils.random(mRateSum);
        float sum = 0.0f;
        List<ACCdnPath> list = mCdnPathList;
        ACCdnPath cdnPath = null;
        for (int i = 0, size = list.size(); i < size; i++) {
            cdnPath = list.get(i);
            sum += cdnPath.rate;
            if (r <= sum) {
                return cdnPath;
            }
        }
        return cdnPath;
    }

    @NonNull
    public String[] getCdnHosts() {
        if (mCdnHostsDirty || mCdnHosts == null) {
            if (mCdnPathList == null) {
                mCdnHosts = new String[0];
            } else {
                List<String> hosts = new ArrayList<>();
                for (ACCdnPath cdn : mCdnPathList) {
                    if (cdn.url == null) {
                        continue;
                    }
                    HttpUrl url = HttpUrl.parse(cdn.url);
                    if (url != null) {
                        hosts.add(url.host());
                    }
                }
                mCdnHosts = hosts.toArray(new String[hosts.size()]);
            }
        }
        return mCdnHosts;
    }

    public synchronized String getPictureUrl(String key) {
        String url;
        ACCdnPath cdnPath;

        if (mCdnPathList != null && (cdnPath = getCdnPath()) != null) {
            url = UrlUtils.join(cdnPath.url, key);
        } else {
            url = DEFAULT_PICTURE_PREFIX + key;
        }

        return url;
    }
}
