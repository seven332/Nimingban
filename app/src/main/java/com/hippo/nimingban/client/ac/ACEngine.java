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

import com.alibaba.fastjson.JSON;
import com.hippo.httpclient.HttpClient;
import com.hippo.httpclient.HttpRequest;
import com.hippo.httpclient.HttpResponse;
import com.hippo.nimingban.client.CancelledException;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.ac.data.ACPost;
import com.hippo.nimingban.client.data.Post;

import java.util.ArrayList;
import java.util.List;

public class ACEngine {

    public static final String HOST = "http://h.nimingban.com/";

    private static final String API_GET_FORUM_LIST = HOST + "Api/getForumList";

    public static List<ACForumGroup> getForumList(HttpClient httpClient, HttpRequest httpRequest) throws Exception {
        try {
            httpRequest.setUrl(API_GET_FORUM_LIST);
            HttpResponse response = httpClient.execute(httpRequest);
            List<ACForumGroup> result = JSON.parseArray(response.getString(), ACForumGroup.class);
            if (result == null) {
                throw new NMBException(NMBClient.AC, "Can't parse json when getForumList");
            }
            return result;
        } catch (Exception e) {
            if (httpRequest.isCancelled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        } finally {
            httpRequest.disconnect();
        }
    }

    public static List<Post> getPostList(HttpClient httpClient, HttpRequest httpRequest, String url) throws Exception {
        try {
            httpRequest.setUrl(url);
            HttpResponse response = httpClient.execute(httpRequest);
            List<ACPost> acPosts = JSON.parseArray(response.getString(), ACPost.class);
            if (acPosts == null) {
                throw new NMBException(NMBClient.AC, "Can't parse json when getPostList");
            }

            List<Post> result = new ArrayList<>(acPosts.size());
            for (ACPost acPost : acPosts) {
                if (acPost != null) {
                    acPost.generate();
                    result.add(acPost);
                }
            }

            return result;
        } catch (Exception e) {
            if (httpRequest.isCancelled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        } finally {
            httpRequest.disconnect();
        }
    }
}
