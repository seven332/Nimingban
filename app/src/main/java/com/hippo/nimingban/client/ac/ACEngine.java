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

import android.util.Log;
import android.util.Pair;

import com.alibaba.fastjson.JSON;
import com.hippo.httpclient.HttpClient;
import com.hippo.httpclient.HttpRequest;
import com.hippo.httpclient.HttpResponse;
import com.hippo.nimingban.client.CancelledException;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.ac.data.ACPost;
import com.hippo.nimingban.client.ac.data.ACReference;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class ACEngine {

    private static final String API_GET_COOKIE = ACUrl.HOST + "/Api/getCookie";
    private static final String API_GET_FORUM_LIST = ACUrl.HOST + "/Api/getForumList";
    private static final String API_REPLY = ACUrl.HOST + "/Home/Forum/doReplyThread.html";

    public static Boolean getCookie(HttpClient httpClient, HttpRequest httpRequest) throws Exception {
        try {
            httpRequest.setUrl(API_GET_COOKIE);
            HttpResponse response = httpClient.execute(httpRequest);
            String content = response.getString();

            Log.d("TAG", "Get AC cookie content: " + content);

            return content.equals("\"ok\"");
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
                    acPost.generate(NMBClient.AC);
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

    public static Pair<Post, List<Reply>> getPost(HttpClient httpClient,
            HttpRequest httpRequest, String url) throws Exception {
        try {
            httpRequest.setUrl(url);
            HttpResponse response = httpClient.execute(httpRequest);
            ACPost acPost = JSON.parseObject(response.getString(), ACPost.class);
            if (acPost == null) {
                throw new NMBException(NMBClient.AC, "Can't parse json when getPost");
            }
            acPost.generateSelfAndReplies(NMBClient.AC);
            return new Pair<Post, List<Reply>>(acPost, new ArrayList<Reply>(acPost.replys));
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

    public static Reply getReference(HttpClient httpClient,
            HttpRequest httpRequest, String url) throws Exception {
        try {
            httpRequest.setUrl(url);
            HttpResponse response = httpClient.execute(httpRequest);
            String body = response.getString();

            ACReference reference = new ACReference();
            Document doc = Jsoup.parse(body);
            List<Element> elements = doc.getAllElements();
            for (Element element : elements) {
                String className = element.className();
                if ("h-threads-item-reply h-threads-item-ref".equals(className)) {
                    reference.id = element.attr("data-threads-id");
                } else if ("h-threads-img-a".equals(className)) {
                    reference.image = element.attr("href");
                } else if ("h-threads-img".equals(className)) {
                    reference.thumb = element.attr("src");
                } else if ("h-threads-info-title".equals(className)) {
                    reference.title = element.text();
                } else if ("h-threads-info-email".equals(className)) {
                    // TODO email or user ?
                    reference.user = element.text();
                } else if ("h-threads-info-createdat".equals(className)) {
                    reference.time = element.text();
                } else if ("h-threads-info-uid".equals(className)) {
                    String user = element.text();
                    if (user.startsWith("ID:")) {
                        reference.userId = user.substring(3);
                    } else {
                        reference.userId = user;
                    }
                    reference.admin = element.childNodeSize() > 1;
                } else if ("h-threads-info-id".equals(className)) {
                    String href = element.attr("href");
                    if (href.startsWith("/t/")) {
                        int index = href.indexOf('?');
                        if (index >= 0) {
                            reference.postId = href.substring(3, index);
                        } else {
                            reference.postId = href.substring(3);
                        }
                    }
                } else if ("h-threads-content".equals(className)) {
                    reference.content = element.html();
                }
            }

            reference.generate(NMBClient.AC);

            return reference;

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
