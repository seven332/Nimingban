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

package com.hippo.nimingban;

import android.content.Context;

import com.hippo.nimingban.util.Settings;
import com.tendcloud.tenddata.TCAgent;

import java.util.HashMap;
import java.util.Map;

public class Analysis {

    private static final Map<String, Object> sMap = new HashMap<>();
    private static final Object sMapLock = new Object();

    public static boolean canAnalysis(Context context) {
        return NMBApplication.hasInitTCAgent(context) && Settings.getAnalysis();
    }

    public static void readPost(Context context, String id) {
        if (!canAnalysis(context)) {
            return;
        }

        synchronized (sMapLock) {
            sMap.put("id", id);
            TCAgent.onEvent(context, "nmb_read_post", "", sMap);
            sMap.clear();
        }
    }

    public static void replyPost(Context context, String id, boolean success) {
        if (!canAnalysis(context)) {
            return;
        }

        synchronized (sMapLock) {
            sMap.put("id", id);
            sMap.put("success", success);
            TCAgent.onEvent(context, "nmb_reply_post", "", sMap);
            sMap.clear();
        }
    }

    public static void createPost(Context context, String forumId, boolean success) {
        if (!canAnalysis(context)) {
            return;
        }

        synchronized (sMapLock) {
            sMap.put("forum_id", forumId);
            sMap.put("success", success);
            TCAgent.onEvent(context, "nmb_create_post", "", sMap);
            sMap.clear();
        }
    }

    public static void getPostList(Context context, String forumId, int page, boolean success) {
        if (!canAnalysis(context)) {
            return;
        }

        synchronized (sMapLock) {
            sMap.put("forum_id", forumId);
            sMap.put("page", page);
            sMap.put("success", success);
            TCAgent.onEvent(context, "nmb_get_post_list", "", sMap);
            sMap.clear();
        }
    }

    public static void action(Context context, String label) {
        if (!canAnalysis(context)) {
            return;
        }

        TCAgent.onEvent(context, "nmb_action", label);
    }

    public static void setting(Context context, String key, Object value) {
        if (!canAnalysis(context)) {
            return;
        }

        synchronized (sMapLock) {
            sMap.put("key", key);
            sMap.put("value", value);
            TCAgent.onEvent(context, "nmb_setting", "", sMap);
            sMap.clear();
        }
    }
}
