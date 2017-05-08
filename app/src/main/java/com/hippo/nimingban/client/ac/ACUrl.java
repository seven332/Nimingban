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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ACUrl {

    public static final String APP_ID = "nimingban";

    public static final String DOMAIN = "h.nimingban.com";

    public static final String HOST = "https://" + DOMAIN;

    public static final String API_POST_LIST = HOST + "/Api/showf?appid=" + APP_ID;

    public static final String API_TIME_LINE = HOST + "/Api/timeline?appid=" + APP_ID;

    public static final String API_POST = HOST + "/Api/thread?appid=" + APP_ID;

    public static final String API_REFERENCE = HOST + "/Home/Forum/ref?appid=" + APP_ID;

    public static final String API_FEED = HOST + "/Api/feed?appid=" + APP_ID;

    public static final String API_ADD_FEED = HOST + "/Api/addFeed?appid=" + APP_ID;

    public static final String API_DEL_FEED = HOST + "/Api/delFeed?appid=" + APP_ID;

    public static final String API_CREATE_POST = HOST + "/Home/Forum/doPostThread.html?appid=" + APP_ID;

    public static final String API_GET_COOKIE = HOST + "/Api/getCookie?appid=" + APP_ID;

    //public static final String API_GET_CDN_PATH = HOST + "/Api/getCdnPath?appid=" + APP_ID;
    public static final String API_GET_CDN_PATH = "http://nimingban.herokuapp.com/get_image_cdn_path";

    public static final String API_GET_FORUM_LIST = HOST + "/Api/getForumList?appid=" + APP_ID;

    public static final String API_REPLY = HOST + "/Home/Forum/doReplyThread.html?appid=" + APP_ID;

    public static final String API_COMMON_POSTS = "http://nimingban.herokuapp.com/common_posts";

    public static final String API_SEARCH = HOST + "/Api/search?appid=" + APP_ID;

    public static final String FORUM_ID_TIME_LINE = "-1";

    public static String getPostListUrl(String forum, int page) {
        if (FORUM_ID_TIME_LINE.equals(forum)) {
            return getTimeLineUrl(page);
        } else {
            return API_POST_LIST + "&id=" + forum + "&page=" + (page + 1);
        }
    }

    public static String getTimeLineUrl(int page) {
        return API_TIME_LINE + "&page=" + (page + 1);
    }

    public static String getPostUrl(String id, int page) {
        return API_POST + "&id=" + id + "&page=" + (page + 1);
    }

    public static String getReferenceUrl(String id) {
        return API_REFERENCE + "&id=" + id;
    }

    public static String getFeedUrl(String uuid, int page) {
        return API_FEED + "&uuid=" + uuid + "&page=" + (page + 1);
    }

    public static String getAddFeedUrl(String uuid, String tid) {
        return API_ADD_FEED + "&uuid=" + uuid + "&tid=" + tid;
    }

    public static String getDelFeedUrl(String uuid, String tid) {
        return API_DEL_FEED + "&uuid=" + uuid + "&tid=" + tid;
    }

    public static String getBrowsablePostUrl(String id, int page) {
        StringBuilder sb = new StringBuilder();
        sb.append(HOST).append("/t/").append(id);
        if (page != 0) {
            sb.append("&page=").append(page + 1);
        }
        return sb.toString();
    }

    public static String getBingSearchUrl(String keyword, int page) throws UnsupportedEncodingException {
        return "http://www.bing.com/search?q=" + URLEncoder.encode(keyword, "UTF-8") + "+site%3ah.nimingban.com&first=" + (page * 10 + 1);
    }

    public static String getSearchUrl(String keyword, int page) throws UnsupportedEncodingException {
        return API_SEARCH + "&q=" + URLEncoder.encode(keyword, "UTF-8") + "&pageNo=" + (page + 1);
    }
}
