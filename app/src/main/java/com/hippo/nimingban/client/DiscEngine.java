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

package com.hippo.nimingban.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscEngine {

    private static final String SITE_WEIYUN = "weiyun";
    private static final String SITE_PGYER = "pgyer";

    private static final Pattern PATTERN_WEIYUN_1 = Pattern.compile("outlink_mod.render\\((.+?)\\);");
    private static final Pattern PATTERN_WEIYUN_2 = Pattern.compile("\"(http.+?)\"");

    private static final String URL_WEIYUN_OUTLINK = "http://user.weiyun.com/newcgi/outlink.fcg";

    private static final String JSON_STR_WEIYUN = "{\n" +
            "  \"req_header\": {\n" +
            "    \"cmd\": 12023,\n" +
            "    \"appid\": 30013,\n" +
            "    \"version\": 2,\n" +
            "    \"major_version\": 2\n" +
            "  },\n" +
            "  \"req_body\": {\n" +
            "    \"ReqMsg_body\": {\n" +
            "      \"weiyun.WeiyunSharePartDownloadMsgReq_body\": {\n" +
            "        \"share_key\": \"32fbe566342efb06aac1f1703694fa44\",\n" +
            "        \"pwd\": \"\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final Pattern PATTERN_PGYER = Pattern.compile("aKey = '(.+?)'");

    private static String getWeiyunPostJson(String in) {
        JSONObject inObj = JSON.parseObject(in);
        JSONObject outObj = JSON.parseObject(JSON_STR_WEIYUN);

        JSONObject body = outObj.getJSONObject("req_body").getJSONObject("ReqMsg_body")
                .getJSONObject("weiyun.WeiyunSharePartDownloadMsgReq_body");

        body.put("share_key", inObj.getString("share_key"));
        body.put("pack_name", inObj.getString("share_name"));
        body.put("pdir_key", inObj.getString("pdir_key"));

        JSONArray fileList = inObj.getJSONArray("file_list");
        for (int i = 0, n = fileList.size(); i < n; i++) {
            JSONObject file = fileList.getJSONObject(i);
            file.remove("file_name");
            file.remove("file_size");
        }

        body.put("file_list", fileList);

        return outObj.toString();
    }

    public static String weiyun(OkHttpClient okHttpClient, String url) throws IOException {
        String body;
        Matcher matcher;

        body = okHttpClient.newCall(new Request.Builder().url(url).build()).execute().body().string();
        matcher = PATTERN_WEIYUN_1.matcher(body);
        if (!matcher.find()) {
            throw new IOException("Can't get url");
        }

        body = okHttpClient.newCall(new Request.Builder().url(URL_WEIYUN_OUTLINK)
                .post(new FormEncodingBuilder()
                        .add("data", getWeiyunPostJson(matcher.group(1))).build()).build()).execute()
                .body().string();
        matcher = PATTERN_WEIYUN_2.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IOException("Can't get url");
        }
    }

    public static String pgyer(OkHttpClient okHttpClient, String url) throws IOException {
        String body;
        Matcher matcher;

        body = okHttpClient.newCall(new Request.Builder().url(url).build()).execute().body().string();
        matcher = PATTERN_PGYER.matcher(body);
        if (matcher.find()) {
            return "http://www.pgyer.com/app/install/" + matcher.group(1);
        } else {
            throw new IOException("Can't get url");
        }
    }

    public static String spider(OkHttpClient okHttpClient, String disc, String url) throws IOException {
        switch (disc) {
            case SITE_WEIYUN:
                return weiyun(okHttpClient, url);
            case SITE_PGYER:
                return pgyer(okHttpClient, url);
            default:
                throw new IOException("Can't detect site " + disc);
        }
    }
}
