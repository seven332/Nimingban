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

package com.hippo.nimingban.client.ac;

import com.hippo.nimingban.util.Settings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ACUrl {

    public static final String APP_ID = "nimingban";

    public static final String DOMAIN = "adnmb1.com";

    public static final String API_POST_LIST = "/Api/showf?appid=" + APP_ID;

    public static final String API_TIME_LINE = "/Api/timeline?appid=" + APP_ID;

    public static final String API_POST = "/Api/thread?appid=" + APP_ID;

    public static final String API_REFERENCE = "/Home/Forum/ref?appid=" + APP_ID;

    public static final String API_FEED = "/Api/feed?appid=" + APP_ID;

    public static final String API_ADD_FEED = "/Api/addFeed?appid=" + APP_ID;

    public static final String API_DEL_FEED = "/Api/delFeed?appid=" + APP_ID;

    public static final String API_CREATE_POST = "/Home/Forum/doPostThread.html?appid=" + APP_ID;

    public static final String API_GET_COOKIE = "/Api/getCookie?appid=" + APP_ID;

    public static final String API_GET_CDN_PATH = "/Api/getCdnPath?appid=" + APP_ID;

    public static final String API_GET_FORUM_LIST = "/Api/getForumList?appid=" + APP_ID;

    public static final String API_REPLY = "/Home/Forum/doReplyThread.html?appid=" + APP_ID;

    public static final String API_COMMON_POSTS = "http://nimingban.herokuapp.com/common_posts";

    public static final String API_SEARCH = "/Api/search?appid=" + APP_ID;

    public static final String FORUM_ID_TIME_LINE = "-1";

    public static String getHost() {
        if (Settings.getEnableCustomizedAcHost()) {
            return Settings.getCustomizedAcHost();
        } else {
            return Settings.getAcHost();
        }
    }

    public static String getPostListUrl(String forum, int page) {
        if (FORUM_ID_TIME_LINE.equals(forum)) {
            return getTimeLineUrl(page);
        } else {
            return getHost() + API_POST_LIST + "&id=" + forum + "&page=" + (page + 1);
        }
    }

    public static String getTimeLineUrl(int page) {
        return getHost() + API_TIME_LINE + "&page=" + (page + 1);
    }

    public static String getPostUrl(String id, int page) {
        return getHost() + API_POST + "&id=" + id + "&page=" + (page + 1);
    }

    public static String getReferenceUrl(String id) {
        return getHost() + API_REFERENCE + "&id=" + id;
    }

    public static String getFeedUrl(String uuid, int page) {
        return getHost() + API_FEED + "&uuid=" + uuid + "&page=" + (page + 1);
    }

    public static String getAddFeedUrl(String uuid, String tid) {
        return getHost() + API_ADD_FEED + "&uuid=" + uuid + "&tid=" + tid;
    }

    public static String getDelFeedUrl(String uuid, String tid) {
        return getHost() + API_DEL_FEED + "&uuid=" + uuid + "&tid=" + tid;
    }

    public static String getBrowsablePostUrl(String id, int page) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHost()).append("/t/").append(id);
        if (page != 0) {
            sb.append("&page=").append(page + 1);
        }
        return sb.toString();
    }

    public static String getBingSearchUrl(String keyword, int page) throws UnsupportedEncodingException {
        return "http://www.bing.com/search?q=" + URLEncoder.encode(keyword, "UTF-8") + "+site%3ah.nimingban.com&first=" + (page * 10 + 1);
    }

    public static String getSearchUrl(String keyword, int page) throws UnsupportedEncodingException {
        return getHost() + API_SEARCH + "&q=" + URLEncoder.encode(keyword, "UTF-8") + "&pageNo=" + (page + 1);
    }
}
