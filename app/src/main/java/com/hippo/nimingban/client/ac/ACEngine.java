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
import com.hippo.io.FileInputStreamPipe;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.client.CancelledException;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.ac.data.ACFeed;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.ac.data.ACPost;
import com.hippo.nimingban.client.ac.data.ACPostStruct;
import com.hippo.nimingban.client.ac.data.ACReference;
import com.hippo.nimingban.client.ac.data.ACReplyStruct;
import com.hippo.nimingban.client.ac.data.ACSearchItem;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.DumpSite;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.okhttp.GoodRequestBuilder;
import com.hippo.okhttp.ResponseUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.StringUtils;
import com.hippo.yorozuya.io.InputStreamPipe;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ACEngine {

    private static final String TAG = ACEngine.class.getSimpleName();

    private static final MediaType MEDIA_TYPE_IMAGE_ALL = MediaType.parse("image/*");
    private static final MediaType MEDIA_TYPE_IMAGE_JPEG = MediaType.parse("image/jpeg");

    public static Call prepareGetCookie(OkHttpClient okHttpClient) {
        String url = ACUrl.API_GET_COOKIE;
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static Boolean doGetCookie(Call call) throws Exception {
        try {
            Response response = call.execute();
            ResponseUtils.storeCookies(response);
            String body = response.body().string();

            return body.equals("\"ok\"");
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareGetForumList(OkHttpClient okHttpClient) {
        String url = ACUrl.API_GET_FORUM_LIST;
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<ACForumGroup> doGetForumList(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();

            List<ACForumGroup> result = JSON.parseArray(body, ACForumGroup.class);
            if (result == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getForumList");
            }
            return result;
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareGetPostList(OkHttpClient okHttpClient, String id, int page) {
        String url = ACUrl.getPostListUrl(id, page);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<Post> doGetPostList(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();
            List<ACPost> acPosts = JSON.parseArray(body, ACPost.class);
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
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareGetPost(OkHttpClient okHttpClient, String id, int page) {
        String url = ACUrl.getPostUrl(id, page);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static Pair<Post, List<Reply>> doGetPost(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();
            ACPost acPost = JSON.parseObject(body, ACPost.class);
            if (acPost == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getPost");
            }
            acPost.generateSelfAndReplies(ACSite.getInstance());
            return new Pair<Post, List<Reply>>(acPost, new ArrayList<Reply>(acPost.replys));
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareGetReference(OkHttpClient okHttpClient, String id) {
        String url = ACUrl.getReferenceUrl(id);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static Reply doGetReference(Call call) throws Exception {
        try {
            Response response = call.execute();

            ACReference reference = new ACReference();
            Document doc = Jsoup.parse(response.body().byteStream(), "UTF-8", ACUrl.HOST + "/");
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
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareReply(OkHttpClient okHttpClient, ACReplyStruct struct) throws Exception {
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"name\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.name)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"email\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.email)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"title\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.title)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"content\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.content)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"resto\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.resto)));
        InputStreamPipe isPipe = struct.image;

        if (isPipe != null) {
            String filename;
            MediaType mediaType;
            byte[] bytes;
            File file = compressBitmap(isPipe, struct.imageType);
            if (file == null) {
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(struct.imageType);
                if (TextUtils.isEmpty(extension)) {
                    extension = "jpg";
                }
                filename = "a." + extension;

                mediaType = MediaType.parse(struct.imageType);
                if (mediaType == null) {
                    mediaType = MEDIA_TYPE_IMAGE_ALL;
                }

                try {
                    isPipe.obtain();
                    bytes = IOUtils.getAllByte(isPipe.open());
                } finally {
                    isPipe.close();
                    isPipe.release();
                }
            } else {
                filename = "a.jpg";
                mediaType = MEDIA_TYPE_IMAGE_JPEG;

                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    bytes = IOUtils.getAllByte(is);
                } finally {
                    IOUtils.closeQuietly(is);
                    file.delete();
                }
            }
            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"" + filename + "\""),
                    RequestBody.create(mediaType, bytes));
        }

        String url = ACUrl.API_REPLY;
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url)
                .post(builder.build())
                .build();
        return okHttpClient.newCall(request);
    }

    public static Void doReply(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();
            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    String msg = jo.getString("msg");
                    throw new NMBException(ACSite.getInstance(), msg);
                }
            } catch (Exception e) {
                if  (e instanceof NMBException) {
                    throw e;
                }

                Document doc = Jsoup.parse(body);
                List<Element> elements = doc.getElementsByClass("success");
                if (!elements.isEmpty()) {
                    return null;
                } else {
                    elements = doc.getElementsByTag("h1");
                    if (!elements.isEmpty()) {
                        throw new NMBException(ACSite.getInstance(), elements.get(0).text());
                    } else {
                        throw new NMBException(ACSite.getInstance(), "Unknown");
                    }
                }
            }
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareGetFeed(OkHttpClient okHttpClient, String uuid, int page) {
        String url = ACUrl.getFeedUrl(uuid, page);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<Post> doGetFeed(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();

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
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareAddFeed(OkHttpClient okHttpClient, String uuid, String tid) {
        String url = ACUrl.getAddFeedUrl(uuid, tid);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static Void doAddFeed(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();

            if (body.equals("\"\\u8ba2\\u9605\\u5927\\u6210\\u529f\\u2192_\\u2192\"")) {
                return null;
            } else {
                throw new NMBException(ACSite.getInstance(), "Unknown error");
            }
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareDelFeed(OkHttpClient okHttpClient, String uuid, String tid) {
        String url = ACUrl.getDelFeedUrl(uuid, tid);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static Void doDelFeed(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();

            if (body.equals("\"\\u53d6\\u6d88\\u8ba2\\u9605\\u6210\\u529f!\"")) {
                return null;
            } else {
                throw new NMBException(ACSite.getInstance(), "Unknown error");
            }
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    public static Call prepareCreatePost(OkHttpClient okHttpClient, ACPostStruct struct) throws Exception {
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"name\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.name)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"email\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.email)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"title\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.title)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"content\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.content)));
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"fid\""),
                RequestBody.create(null, StringUtils.avoidNull(struct.fid)));
        InputStreamPipe isPipe = struct.image;

        if (isPipe != null) {
            String filename;
            MediaType mediaType;
            byte[] bytes;
            File file = compressBitmap(isPipe, struct.imageType);
            if (file == null) {
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(struct.imageType);
                if (TextUtils.isEmpty(extension)) {
                    extension = "jpg";
                }
                filename = "a." + extension;

                mediaType = MediaType.parse(struct.imageType);
                if (mediaType == null) {
                    mediaType = MEDIA_TYPE_IMAGE_ALL;
                }

                try {
                    isPipe.obtain();
                    bytes = IOUtils.getAllByte(isPipe.open());
                } finally {
                    isPipe.close();
                    isPipe.release();
                }
            } else {
                filename = "a.jpg";
                mediaType = MEDIA_TYPE_IMAGE_JPEG;

                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    bytes = IOUtils.getAllByte(is);
                } finally {
                    IOUtils.closeQuietly(is);
                    file.delete();
                }
            }
            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"" + filename + "\""),
                    RequestBody.create(mediaType, bytes));
        }

        String url = ACUrl.API_CREATE_POST;
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url)
                .post(builder.build())
                .build();
        return okHttpClient.newCall(request);
    }

    public static Void doCreatePost(Call call) throws Exception {
        try {
            Response response = call.execute();
            String body = response.body().string();
            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    String msg = jo.getString("msg");
                    throw new NMBException(ACSite.getInstance(), msg);
                }
            } catch (Exception e) {
                if  (e instanceof NMBException) {
                    throw e;
                }

                Document doc = Jsoup.parse(body);
                List<Element> elements = doc.getElementsByClass("success");
                if (!elements.isEmpty()) {
                    return null;
                } else {
                    elements = doc.getElementsByTag("h1");
                    if (!elements.isEmpty()) {
                        throw new NMBException(ACSite.getInstance(), elements.get(0).text());
                    } else {
                        throw new NMBException(ACSite.getInstance(), "Unknown");
                    }
                }
            }
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;

    /**
     * @return null for not changed
     */
    public static File compressBitmap(InputStreamPipe isp, String imageType) throws Exception {
        OutputStream os = null;
        try {
            isp.obtain();

            File temp = NMBAppConfig.createTempFile();
            if (temp == null) {
                throw new NMBException(DumpSite.getInstance(), "Can't create temp file");
            }

            os = new FileOutputStream(temp);
            IOUtils.copy(isp.open(), os);
            isp.close();
            os.close();

            long size = temp.length();
            if (size < MAX_IMAGE_SIZE && !"image/jpeg".equals(imageType) && !"image/jpg".equals(imageType)) {
                temp.delete();
                return null;
            }

            int[] sampleScaleArray = new int[1];
            BitmapUtils.decodeStream(new FileInputStreamPipe(temp), -1, -1, -1, true, true, sampleScaleArray);
            int sampleScale = sampleScaleArray[0];
            if (sampleScale < 1) {
                throw new NMBException(DumpSite.getInstance(), "Can't get bitmap size");
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            int i = (int) (Math.log(sampleScale) / Math.log(2));
            while (true) {
                options.inSampleSize = (int) Math.pow(2, i);
                Bitmap bitmap = BitmapFactory.decodeStream(isp.open(), null, options);
                isp.close();

                os = new FileOutputStream(temp);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
                os.close();

                bitmap.recycle();

                size = temp.length();
                if (size < MAX_IMAGE_SIZE) {
                    return temp;
                }

                i++;
            }
        } finally {
            isp.close();
            isp.release();
            IOUtils.closeQuietly(os);
        }
    }

    private static Pattern URL_PATTERN = Pattern.compile("http://h.nimingban.com/t/(\\d+)");

    public static Call prepareSearch(OkHttpClient okHttpClient, String keyword, int page) throws UnsupportedEncodingException {
        String url = ACUrl.getBingSearchUrl(keyword, page);
        Log.d(TAG, url);
        Request request = new GoodRequestBuilder(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<ACSearchItem> doSearch(Call call) throws Exception {
        try {
            Response response = call.execute();

            Document doc = Jsoup.parse(response.body().byteStream(), "UTF-8", "http://www.bing.com/");
            Elements elements = doc.getElementsByClass("b_algo");

            List<ACSearchItem> result = new ArrayList<>();
            for (int i = 0, n = elements.size(); i < n; i++) {
                Element element = elements.get(i);

                Elements urls = element.getElementsByTag("a");
                if (urls.size() <= 0) {
                    continue;
                }
                Matcher matcher = URL_PATTERN.matcher(urls.attr("href"));
                String id;
                if (matcher.find()) {
                    id = matcher.group(1);
                } else {
                    continue;
                }

                Elements captions = elements.get(i).getElementsByClass("b_caption");
                if (captions.size() <= 0) {
                    continue;
                }
                Elements contents = captions.get(0).getElementsByTag("p");
                if (contents.size() <= 0) {
                    continue;
                }
                String content = contents.get(0).text();

                ACSearchItem item = new ACSearchItem();
                item.id = id;
                item.context = content;
                result.add(item);
            }

            return result;
        } catch (IOException e) {
            if (call.isCanceled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        }
    }
}
