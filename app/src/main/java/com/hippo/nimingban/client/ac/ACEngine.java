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
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.client.CancelledException;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.StringEscape;
import com.hippo.nimingban.client.ac.data.ACCdnPath;
import com.hippo.nimingban.client.ac.data.ACFeed;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.ac.data.ACPost;
import com.hippo.nimingban.client.ac.data.ACPostStruct;
import com.hippo.nimingban.client.ac.data.ACReference;
import com.hippo.nimingban.client.ac.data.ACReplyStruct;
import com.hippo.nimingban.client.ac.data.ACSearchItem;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.CommonPost;
import com.hippo.nimingban.client.data.DumpSite;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.StringUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class ACEngine {
    private ACEngine() {}

    private static final String TAG = ACEngine.class.getSimpleName();

    private static final MediaType MEDIA_TYPE_IMAGE_ALL = MediaType.parse("image/*");
    private static final MediaType MEDIA_TYPE_IMAGE_JPEG = MediaType.parse("image/jpeg");

    private static final String UNKNOWN = "Unknown";

    private static void throwException(Call call, String body, Exception e) throws Exception {
        if (call.isCanceled()) {
            throw new CancelledException();
        }

        if (e instanceof NMBException) {
            if (!UNKNOWN.equals(e.getMessage())) {
                throw e;
            }
        }

        if (TextUtils.isEmpty(body)) {
            return;
        }

        try {
            JSONObject jo = JSON.parseObject(body);
            if (!jo.getBoolean("success")) {
                throw new NMBException(ACSite.getInstance(), jo.getString("msg"));
            }
        } catch (Exception ee) {
            // Ignore
        }

        Document doc = Jsoup.parse(body);
        List<Element> elements = doc.getElementsByClass("error");
        if (!elements.isEmpty()) {
            throw new NMBException(ACSite.getInstance(), elements.get(0).text());
        }

        try {
            throw new NMBException(ACSite.getInstance(), StringEscape.unescapeJson(body));
        } catch (StringEscape.UnescapeException ee) {
            // Ignore
        }
    }

    public static Call prepareGetCookie(OkHttpClient okHttpClient) {
        String url = ACUrl.getHost() + ACUrl.API_GET_COOKIE;
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static Boolean doGetCookie(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            if (!"\"ok\"".equals(body)) {
                throw new NMBException(ACSite.getInstance(), UNKNOWN);
            }

            return true;
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetCdnPath(OkHttpClient okHttpClient) {
        String url = ACUrl.getHost() + ACUrl.API_GET_CDN_PATH;
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<ACCdnPath> doGetCdnPath(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();
            List<ACCdnPath> cdn = JSON.parseArray(body, ACCdnPath.class);
            Log.d(TAG, "cdn path: " + cdn);
            return cdn;
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetCommonPosts(OkHttpClient okHttpClient) {
        String url = ACUrl.API_COMMON_POSTS;
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<CommonPost> doGetCommonPosts(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            List<CommonPost> result = JSON.parseArray(body, CommonPost.class);
            if (result == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getForumList");
            }
            return result;
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetForumList(OkHttpClient okHttpClient) {
        String url = ACUrl.getHost() + ACUrl.API_GET_FORUM_LIST;
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<ACForumGroup> doGetForumList(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            List<ACForumGroup> result = JSON.parseArray(body, ACForumGroup.class);
            if (result == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getForumList");
            }
            return result;
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetPostList(OkHttpClient okHttpClient, String id, int page) {
        String url = ACUrl.getPostListUrl(id, page);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<Post> doGetPostList(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();
            List<ACPost> acPosts = JSON.parseArray(body, ACPost.class);
            if (acPosts == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getPostList");
            }

            List<Post> result = new ArrayList<>(acPosts.size());
            for (ACPost acPost : acPosts) {
                if (acPost != null) {
                    acPost.generateSelfAndReplies(ACSite.getInstance());
                    result.add(acPost);
                }
            }

            return result;
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetPost(OkHttpClient okHttpClient, String id, int page) {
        String url = ACUrl.getPostUrl(id, page);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static Pair<Post, List<Reply>> doGetPost(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();
            ACPost acPost = JSON.parseObject(body, ACPost.class);
            if (acPost == null) {
                throw new NMBException(ACSite.getInstance(), "Can't parse json when getPost");
            }
            acPost.generateSelfAndReplies(ACSite.getInstance());
            return new Pair<Post, List<Reply>>(acPost, new ArrayList<Reply>(acPost.replys));
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetReference(OkHttpClient okHttpClient, String id) {
        String url = ACUrl.getReferenceUrl(id);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static Reply doGetReference(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            ACReference reference = new ACReference();
            Document doc = Jsoup.parse(body, ACUrl.getHost() + "/");
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
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareReply(OkHttpClient okHttpClient, ACReplyStruct struct) throws Exception {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
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
        if (struct.water) {
            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"water\""),
                    RequestBody.create(null, "true"));
        }

        if (struct.image != null) {
            final byte[] bytes;
            File file = compressBitmap(struct.image, struct.imageType);

            final String imageType;
            final InputStreamPipe imagePipe;
            if (file == null) {
                // Origin image
                imageType = struct.imageType;
                imagePipe = struct.image;
            } else {
                // Compressed image
                // gif or jpeg
                imageType = "image/gif".equals(struct.imageType) ? "image/gif" : "image/jpeg";
                imagePipe = new FileInputStreamPipe(file);
            }

            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(imageType);
            if (TextUtils.isEmpty(extension)) {
                extension = "jpg";
            }
            final String filename = "a." + extension;

            MediaType mediaType = MediaType.parse(imageType);
            if (mediaType == null) {
                mediaType = MEDIA_TYPE_IMAGE_ALL;
            }

            try {
                imagePipe.obtain();
                bytes = IOUtils.getAllByte(imagePipe.open());
            } finally {
                imagePipe.close();
                imagePipe.release();
            }

            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"" + filename + "\""),
                    RequestBody.create(mediaType, bytes));
        }

        String url = ACUrl.getHost() + ACUrl.API_REPLY;
        Log.d(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        return okHttpClient.newCall(request);
    }

    public static Void doReply(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    throw new NMBException(ACSite.getInstance(), jo.getString("msg"));
                }
            } catch (Exception e) {
                if (body.contains("class=\"success\"")) {
                    return null;
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareGetFeed(OkHttpClient okHttpClient, String uuid, int page) {
        String url = ACUrl.getFeedUrl(uuid, page);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<Post> doGetFeed(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

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
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareAddFeed(OkHttpClient okHttpClient, String uuid, String tid) {
        String url = ACUrl.getAddFeedUrl(uuid, tid);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static Void doAddFeed(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            if (body.equals("\"\\u8ba2\\u9605\\u5927\\u6210\\u529f\\u2192_\\u2192\"")) {
                return null;
            } else {
                throw new NMBException(ACSite.getInstance(), UNKNOWN);
            }
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareDelFeed(OkHttpClient okHttpClient, String uuid, String tid) {
        String url = ACUrl.getDelFeedUrl(uuid, tid);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static Void doDelFeed(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            if (body.equals("\"\\u53d6\\u6d88\\u8ba2\\u9605\\u6210\\u529f!\"")) {
                return null;
            } else {
                throw new NMBException(ACSite.getInstance(), UNKNOWN);
            }
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    public static Call prepareCreatePost(OkHttpClient okHttpClient, ACPostStruct struct) throws Exception {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
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
        if (struct.water) {
            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"water\""),
                    RequestBody.create(null, "true"));
        }

        if (struct.image != null) {
            final byte[] bytes;
            File file = compressBitmap(struct.image, struct.imageType);

            final String imageType;
            final InputStreamPipe imagePipe;
            if (file == null) {
                // Origin image
                imageType = struct.imageType;
                imagePipe = struct.image;
            } else {
                // Compressed image
                // gif or jpeg
                imageType = "image/gif".equals(struct.imageType) ? "image/gif" : "image/jpeg";
                imagePipe = new FileInputStreamPipe(file);
            }

            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(imageType);
            if (TextUtils.isEmpty(extension)) {
                extension = "jpg";
            }
            final String filename = "a." + extension;

            MediaType mediaType = MediaType.parse(imageType);
            if (mediaType == null) {
                mediaType = MEDIA_TYPE_IMAGE_ALL;
            }

            try {
                imagePipe.obtain();
                bytes = IOUtils.getAllByte(imagePipe.open());
            } finally {
                imagePipe.close();
                imagePipe.release();
            }

            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"" + filename + "\""),
                    RequestBody.create(mediaType, bytes));
        }

        String url = ACUrl.getHost() + ACUrl.API_CREATE_POST;
        Log.d(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        return okHttpClient.newCall(request);
    }

    public static Void doCreatePost(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            try {
                JSONObject jo = JSON.parseObject(body);
                if (jo.getBoolean("success")) {
                    return null;
                } else {
                    throw new NMBException(ACSite.getInstance(), jo.getString("msg"));
                }
            } catch (Exception e) {
                if (body.contains("class=\"success\"")) {
                    return null;
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }

    private static final long MAX_IMAGE_SIZE = 2000 * 1024;

    private static int getBitmapWidth(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            return options.outWidth;
        } catch (FileNotFoundException e) {
            return 0;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static boolean compressGifsicle(File input, File output) throws IOException {
        final String gifsicleFilename;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            gifsicleFilename = "libgifsicle_executable.so";
        } else {
            gifsicleFilename = "libgifsicle_executable_legacy.so";
        }

        final File gifsicle = new File(NMBAppConfig.getNativeLibDir(), gifsicleFilename);
        if (!gifsicle.canExecute()) {
            return false;
        }

        float scale = (float) Math.sqrt((float) MAX_IMAGE_SIZE / (float) input.length());
        int width = (int) (getBitmapWidth(input) * scale);
        if (width <= 0) {
            return false;
        }

        final int offset = width / 5;
        for (int i = 0; i < 5 && width > 0; i++, width -= offset) {
            String cmd = String.format(Locale.US, "%s --resize-width %d --output %s %s",
                    gifsicle.getPath(), width, output.getPath(), input.getPath());
            String[] envp = { "LD_LIBRARY_PATH=" + NMBAppConfig.getNativeLibDir() };
            Process process = Runtime.getRuntime().exec(cmd, envp);
            try {
                if (process.waitFor() != 0) {
                    return false;
                }
                if (output.length() < MAX_IMAGE_SIZE) {
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

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
            if (size < MAX_IMAGE_SIZE) {
                temp.delete();
                return null;
            }

            if ("image/gif".equals(imageType)) {
                File output = NMBAppConfig.createTempFile();
                if (output == null) {
                    throw new NMBException(DumpSite.getInstance(), "Can't create temp file");
                }

                if (compressGifsicle(temp, output)) {
                    return output;
                } else {
                    throw new NMBException(DumpSite.getInstance(), "Can't compress gif");
                }
            } else {
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
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(isp.open(), null, options);
                    } catch (OutOfMemoryError e) {
                        // Ignore
                    }
                    if (bitmap == null) {
                        throw new NMBException(ACSite.getInstance(), "Can't decode bitmap");
                    }

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
            }
        } finally {
            isp.close();
            isp.release();
            IOUtils.closeQuietly(os);
        }
    }

    public static Call prepareSearch(OkHttpClient okHttpClient, String keyword, int page) throws UnsupportedEncodingException {
        String url = ACUrl.getSearchUrl(keyword, page);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request);
    }

    public static List<ACSearchItem> doSearch(Call call) throws Exception {
        String body = null;
        try {
            Response response = call.execute();
            body = response.body().string();

            JSONArray ja = JSON.parseObject(body).getJSONObject("hits").getJSONArray("hits");
            List<ACSearchItem> result = new ArrayList<>();
            for (int i = 0, n = ja.size(); i < n; i++) {
                JSONObject jo = ja.getJSONObject(i);
                ACSearchItem item = jo.getObject("_source", ACSearchItem.class);
                item.id = jo.getString("_id");
                item.generate(ACSite.getInstance());
                result.add(item);
            }

            return result;
        } catch (Exception e) {
            throwException(call, body, e);
            throw e;
        }
    }
}
