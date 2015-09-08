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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hippo.httpclient.FormDataPoster;
import com.hippo.httpclient.HttpClient;
import com.hippo.httpclient.HttpRequest;
import com.hippo.httpclient.HttpResponse;
import com.hippo.httpclient.StringData;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.network.InputStreamPipeData;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.client.CancelledException;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.ac.data.ACFeed;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.ac.data.ACPost;
import com.hippo.nimingban.client.ac.data.ACPostStruct;
import com.hippo.nimingban.client.ac.data.ACReference;
import com.hippo.nimingban.client.ac.data.ACReplyStruct;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// TODO let Engine create url
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
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getForumList");
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
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getPostList");
            }

            List<Post> result = new ArrayList<>(acPosts.size());
            for (ACPost acPost : acPosts) {
                if (acPost != null) {
                    acPost.generate(ACSite.getInstance());
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
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getPost");
            }
            acPost.generateSelfAndReplies(ACSite.getInstance());
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

            reference.generate(ACSite.getInstance());

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


    public static Void reply(HttpClient httpClient, HttpRequest httpRequest,
            ACReplyStruct struct) throws Exception {
        try {
            StringData name = new StringData(struct.name);
            name.setName("name");
            StringData email = new StringData(struct.email);
            email.setName("email");
            StringData title = new StringData(struct.title);
            title.setName("title");
            StringData content = new StringData(struct.content);
            content.setName("content");
            StringData resto = new StringData(struct.resto);
            resto.setName("resto");

            InputStreamPipeData image;
            InputStreamPipe isp = struct.image;
            if (isp == null) {
                image = null;
            } else {
                InputStreamPipe newIsp = compressBitmap(isp);
                if (newIsp == null) {
                    image = new InputStreamPipeData(isp);
                    String filename;
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(struct.imageType);
                    if (TextUtils.isEmpty(extension)) {
                        extension = "jpg";
                    }
                    filename = "a." + extension;
                    image.setName("image");
                    image.setFilename(filename);
                    image.setProperty("Content-Type", struct.imageType == null ? "image/*" : struct.imageType);
                } else {
                    Log.d("TAG", "image = new InputStreamPipeData(newIsp)");
                    image = new InputStreamPipeData(newIsp);
                    image.setName("image");
                    image.setFilename("a.jpg");
                    image.setProperty("Content-Type", "image/jpeg");
                }
            }

            FormDataPoster httpImpl = new FormDataPoster(name, email, title, content, resto, image);
            httpRequest.setUrl(API_REPLY);
            httpRequest.setHttpImpl(httpImpl);
            HttpResponse response = httpClient.execute(httpRequest);

            String body = response.getString();

            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    String msg = jo.getString("msg");
                    throw new NMBException(ACSite.getInstance(), msg);
                }
            } catch (Exception e) {
                Document doc = Jsoup.parse(body);
                List<Element> elements = doc.getElementsByClass("success");
                if (!elements.isEmpty()) {
                    return null;
                } else {
                    throw new NMBException(ACSite.getInstance(), "Unknown"); // Can't get error message from body
                }
            }

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

    public static Void reply2(HttpClient httpClient, HttpRequest httpRequest,
            ACReplyStruct struct) throws Exception {
        try {
            StringData name = new StringData(struct.name);
            name.setName("name");
            StringData email = new StringData(struct.email);
            email.setName("email");
            StringData title = new StringData(struct.title);
            title.setName("title");
            StringData emotion = new StringData(null);
            emotion.setName("emotion");
            StringData content = new StringData(struct.content);
            content.setName("content");

            InputStreamPipeData image;
            InputStreamPipe isp = struct.image;
            if (isp == null) {
                image = null;
            } else {
                InputStreamPipe newIsp = compressBitmap(isp);
                if (newIsp == null) {
                    image = new InputStreamPipeData(isp);
                    String filename;
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(struct.imageType);
                    if (TextUtils.isEmpty(extension)) {
                        extension = "jpg";
                    }
                    filename = "a." + extension;
                    image.setName("image");
                    image.setFilename(filename);
                    image.setProperty("Content-Type", struct.imageType == null ? "image/*" : struct.imageType);
                } else {
                    image = new InputStreamPipeData(newIsp);
                    image.setName("image");
                    image.setFilename("a.jpg");
                    image.setProperty("Content-Type", "image/jpeg");
                }
            }

            FormDataPoster httpImpl = new FormDataPoster(name, email, title, emotion, content, image);
            String url = ACUrl.HOST + "/api/t/" + struct.resto + "/create";
            httpRequest.setUrl(url);
            httpRequest.setHttpImpl(httpImpl);
            HttpResponse response = httpClient.execute(httpRequest);

            String body = response.getString();

            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    String msg = jo.getString("msg");
                    throw new NMBException(ACSite.getInstance(), msg);
                }
            } catch (Exception e) {
                Document doc = Jsoup.parse(body);
                List<Element> elements = doc.getElementsByClass("success");
                if (!elements.isEmpty()) {
                    return null;
                } else {
                    throw new NMBException(ACSite.getInstance(), "Unknown"); // Can't get error message from body
                }
            }
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

    public static List<Post> getFeed(HttpClient httpClient, HttpRequest httpRequest,
            String uuid, int page) throws Exception {
        try {

            httpRequest.setUrl(ACUrl.getFeedUrl(uuid, page));
            HttpResponse response = httpClient.execute(httpRequest);
            String body = response.getString();

            List<ACFeed> acFeeds = JSON.parseArray(body, ACFeed.class);

            if (acFeeds == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getPostList");
            }

            List<Post> result = new ArrayList<>(acFeeds.size());
            for (ACFeed feed : acFeeds) {
                if (feed != null) {
                    feed.generate(ACSite.getInstance());
                    result.add(feed);
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

    public static Void addFeed(HttpClient httpClient, HttpRequest httpRequest,
            String uuid, String tid) throws Exception {
        try {
            httpRequest.setUrl(ACUrl.getAddFeedUrl(uuid, tid));
            HttpResponse response = httpClient.execute(httpRequest);
            String body = response.getString();

            if (body.equals("\"\\u8ba2\\u9605\\u5927\\u6210\\u529f\\u2192_\\u2192\"")) {
                return null;
            } else {
                throw new NMBException(ACSite.getInstance(), "Unknown error");
            }
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

    public static Void delFeed(HttpClient httpClient, HttpRequest httpRequest,
            String uuid, String tid) throws Exception {
        try {
            httpRequest.setUrl(ACUrl.getDelFeedUrl(uuid, tid));
            HttpResponse response = httpClient.execute(httpRequest);
            String body = response.getString();

            if (body.equals("\"\\u53d6\\u6d88\\u8ba2\\u9605\\u6210\\u529f!\"")) {
                return null;
            } else {
                throw new NMBException(ACSite.getInstance(), "Unknown error");
            }
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

    public static Void createPost(HttpClient httpClient, HttpRequest httpRequest,
            ACPostStruct struct) throws Exception {
        try {
            StringData name = new StringData(struct.name);
            name.setName("name");
            StringData email = new StringData(struct.email);
            email.setName("email");
            StringData title = new StringData(struct.title);
            title.setName("title");
            StringData content = new StringData(struct.content);
            content.setName("content");
            StringData resto = new StringData(struct.fid);
            resto.setName("fid");

            InputStreamPipeData image;
            InputStreamPipe isp = struct.image;
            if (isp == null) {
                image = null;
            } else {
                InputStreamPipe newIsp = compressBitmap(isp);
                if (newIsp == null) {
                    image = new InputStreamPipeData(isp);
                    String filename;
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(struct.imageType);
                    if (TextUtils.isEmpty(extension)) {
                        extension = "jpg";
                    }
                    filename = "a." + extension;
                    image.setName("image");
                    image.setFilename(filename);
                    image.setProperty("Content-Type", struct.imageType == null ? "image/*" : struct.imageType);
                } else {
                    image = new InputStreamPipeData(newIsp);
                    image.setName("image");
                    image.setFilename("a.jpg");
                    image.setProperty("Content-Type", "image/jpeg");
                }
            }

            FormDataPoster httpImpl = new FormDataPoster(name, email, title, content, resto, image);
            httpRequest.setUrl(ACUrl.API_CREATE_POST);
            httpRequest.setHttpImpl(httpImpl);
            HttpResponse response = httpClient.execute(httpRequest);

            String body = response.getString();

            Log.d("TAG", body);

            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    String msg = jo.getString("msg");
                    throw new NMBException(ACSite.getInstance(), msg);
                }
            } catch (Exception e) {
                Document doc = Jsoup.parse(body);
                List<Element> elements = doc.getElementsByClass("success");
                if (!elements.isEmpty()) {
                    return null;
                } else {
                    throw new NMBException(ACSite.getInstance(), "Unknown"); // Can't get error message from body
                }
            }
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

    private static final long MAX_IMAGE_SIZE = 500 * 1024;

    /**
     * @return null for not changed
     */
    public static InputStreamPipe compressBitmap(InputStreamPipe isp) throws IOException {
        OutputStream os = null;
        try {
            isp.obtain();

            File temp = NMBAppConfig.createTempFile();
            if (temp == null) {
                throw new IOException("Can't create temp file");
            }

            os = new FileOutputStream(temp);
            IOUtils.copy(isp.open(), os);
            isp.close();
            os.close();

            BitmapFactory.Options options = new BitmapFactory.Options();
            int i = 0;
            while (true) {
                options.inSampleSize = (int) Math.pow(2, i);
                Bitmap bitmap = BitmapFactory.decodeStream(isp.open(), null, options);
                isp.close();

                os = new FileOutputStream(temp);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
                os.close();

                bitmap.recycle();

                long size = temp.length();
                if (size < MAX_IMAGE_SIZE) {
                    return new FileInputStreamPipe(temp);
                }

                i++;
            }
        } finally {
            isp.close();
            isp.release();
            IOUtils.closeQuietly(os);
        }
    }
}
