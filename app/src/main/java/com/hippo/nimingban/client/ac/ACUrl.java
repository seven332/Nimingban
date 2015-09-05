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

public class ACUrl {

    public static final String HOST = "http://h.nimingban.com";

    public static final String API_POST_LIST = HOST + "/Api/showf";

    public static final String API_POST = HOST + "/Api/thread";

    public static final String API_REFERENCE = HOST + "/Home/Forum/ref";

    public static final String API_FEED = HOST + "/Api/feed";

    public static final String API_ADD_FEED = HOST + "/Api/addFeed";

    public static final String API_DEL_FEED = HOST + "/Api/delFeed";

    public static final String API_CREATE_POST = HOST + "/Home/Forum/doPostThread.html";

    public static String getPostListUrl(String forum, int page) {
        return API_POST_LIST + "?id=" + forum + "&page=" + (page + 1);
    }

    public static String getPostUrl(String id, int page) {
        return API_POST + "?id=" + id + "&page=" + (page + 1);
    }

    public static String getReferenceUrl(String id) {
        return API_REFERENCE + "?id=" + id;
    }

    public static String getFeedUrl(String uuid, int page) {
        return API_FEED + "?uuid=" + uuid + "&page=" + (page + 1);
    }

    public static String getAddFeedUrl(String uuid, String tid) {
        return API_ADD_FEED + "?uuid=" + uuid + "&tid=" + tid;
    }

    public static String getDelFeedUrl(String uuid, String tid) {
        return API_DEL_FEED + "?uuid=" + uuid + "&tid=" + tid;
    }
}
