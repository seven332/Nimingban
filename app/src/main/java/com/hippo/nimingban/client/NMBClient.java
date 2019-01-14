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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.client.ac.ACEngine;
import com.hippo.nimingban.client.ac.data.ACPostStruct;
import com.hippo.nimingban.client.ac.data.ACReplyStruct;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.util.FixedThreadPoolExecutor;
import com.hippo.yorozuya.PriorityThreadFactory;
import com.hippo.yorozuya.SimpleHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NMBClient {

    public static final String TAG = NMBClient.class.getSimpleName();

    public static final int METHOD_NOTICE = -6;

    public static final int METHOD_CONVERT = -5;

    public static final int METHOD_COMMON_POSTS = -4;

    public static final int METHOD_DISC = -3;

    public static final int METHOD_UPDATE = -2;

    public static final int METHOD_GET_FORUM_LIST = -1;

    public static final int METHOD_GET_COOKIE = 0;

    public static final int METHOD_GET_POST_LIST = 1;

    public static final int METHOD_GET_POST = 2;

    public static final int METHOD_GET_REFERENCE = 3;

    public static final int METHOD_REPLY = 4;

    public static final int METHOD_GET_FEED = 5;

    public static final int METHOD_ADD_FEED = 6;

    public static final int METHOD_DEL_FEED = 7;

    public static final int METHOD_CREATE_POST = 8;

    public static final int METHOD_SEARCH = 10;

    public static final int METHOD_GET_CDN_PATH = 11;

    private final ThreadPoolExecutor mRequestThreadPool;
    private final OkHttpClient mOkHttpClient;

    public NMBClient(Context context) {
        ThreadFactory threadFactory = new PriorityThreadFactory(TAG,
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mRequestThreadPool = FixedThreadPoolExecutor.newInstance(
            3, 32, 1L, TimeUnit.SECONDS, threadFactory);
        mOkHttpClient = NMBApplication.getOkHttpClient(context);
    }

    public void execute(NMBRequest request) {
        if (!request.isCancelled()) {
            Task task = new Task(request.method, request.site, request.callback);
            task.executeOnExecutor(mRequestThreadPool, request.args);
            request.task = task;
        } else {
            request.callback.onCancel();
        }
    }

    public void execute(AsyncTask<?, ?, ?> task) {
        task.executeOnExecutor(mRequestThreadPool);
    }

    public class Task extends AsyncTask<Object, Void, Object> {

        private int mMethod;
        private Site mSite;
        private Callback mCallback;
        private Call mCall;

        private boolean mStop;

        public Task(int method, Site site, Callback callback) {
            mMethod = method;
            mSite = site;
            mCallback = callback;
        }

        public void stop() {
            if (!mStop) {
                mStop = true;

                if (mCallback != null) {
                    final Callback finalCallback = mCallback;
                    SimpleHandler.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            finalCallback.onCancel();
                        }
                    });
                }

                Status status = getStatus();
                if (status == Status.PENDING) {
                    cancel(false);
                } else if (status == Status.RUNNING) {
                    if (mCall != null) {
                        mCall.cancel();
                    }
                }

                // Clear
                mCall = null;
                mCallback = null;
            }
        }

        private Object getCommonPosts() throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetCommonPosts(mOkHttpClient);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetCommonPosts(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getForumList() throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetForumList(mOkHttpClient);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetForumList(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getCookie() throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetCookie(mOkHttpClient);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetCookie(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getPostList(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetPostList(mOkHttpClient, (String) params[0], (Integer) params[1]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetPostList(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getPost(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetPost(mOkHttpClient, (String) params[0], (Integer) params[1]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetPost(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getReference(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetReference(mOkHttpClient, (String) params[0]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetReference(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object reply(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareReply(mOkHttpClient, (ACReplyStruct) params[0]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doReply(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }


        private Object getFeed(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetFeed(mOkHttpClient, (String) params[0], (Integer) params[1]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetFeed(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object addFeed(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareAddFeed(mOkHttpClient, (String) params[0], (String) params[1]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doAddFeed(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object delFeed(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareDelFeed(mOkHttpClient, (String) params[0], (String) params[1]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doDelFeed(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object createPost(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareCreatePost(mOkHttpClient, (ACPostStruct) params[0]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doCreatePost(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object search(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareSearch(mOkHttpClient, (String) params[0], (Integer) params[1]);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doSearch(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getCdnPath() throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    Call call = ACEngine.prepareGetCdnPath(mOkHttpClient);
                    if (!mStop) {
                        mCall = call;
                        return ACEngine.doGetCdnPath(call);
                    } else {
                        throw new CancelledException();
                    }
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                switch (mMethod) {
                    case METHOD_NOTICE: {
                        Log.d(TAG, "http://cover.acfunwiki.org/nmb-notice.json");
                        Request request = new Request.Builder().url("http://cover.acfunwiki.org/nmb-notice.json").build();
                        Call call = mOkHttpClient.newCall(request);
                        if (!mStop) {
                            mCall = call;
                            Response response = call.execute();
                            Notice notice = JSON.parseObject(response.body().string(), Notice.class);
                            if (notice == null) {
                                throw new Exception("No notice");
                            }
                            return notice;
                        } else {
                            throw new CancelledException();
                        }
                    }
                    case METHOD_CONVERT: {
                        Call call = ConvertEngine.prepareConvert(mOkHttpClient, (String) params[0], (String) params[1]);
                        if (!mStop) {
                            mCall = call;
                            return ConvertEngine.doConvert(call);
                        } else {
                            throw new CancelledException();
                        }
                    }
                    case METHOD_COMMON_POSTS:
                        return getCommonPosts();
                    case METHOD_DISC:
                        return DiscEngine.spider(mOkHttpClient, (String) params[0], (String) params[1]);
                    case METHOD_UPDATE: {
                        Call call = UpdateEngine.prepareUpdate(mOkHttpClient);
                        if (!mStop) {
                            mCall = call;
                            return UpdateEngine.doUpdate(call);
                        } else {
                            throw new CancelledException();
                        }
                    }
                    case METHOD_GET_FORUM_LIST:
                        return getForumList();
                    case METHOD_GET_COOKIE:
                        return getCookie();
                    case METHOD_GET_POST_LIST:
                        return getPostList(params);
                    case METHOD_GET_POST:
                        return getPost(params);
                    case METHOD_GET_REFERENCE:
                        return getReference(params);
                    case METHOD_REPLY:
                        return reply(params);
                    case METHOD_GET_FEED:
                        return getFeed(params);
                    case METHOD_ADD_FEED:
                        return addFeed(params);
                    case METHOD_DEL_FEED:
                        return delFeed(params);
                    case METHOD_CREATE_POST:
                        return createPost(params);
                    case METHOD_SEARCH:
                        return search(params);
                    case METHOD_GET_CDN_PATH:
                        return getCdnPath();
                    default:
                        return new IllegalStateException("Can't detect method " + mMethod);
                }
            } catch (Exception e) {
                return e;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object result) {
            if (mCallback != null) {
                if (!(result instanceof CancelledException)) {
                    if (result instanceof Exception) {
                        mCallback.onFailure((Exception) result);
                    } else {
                        mCallback.onSuccess(result);
                    }
                } else {
                    // onCancel is called in stop
                }
            }

            // Clear
            mCall = null;
            mCallback = null;
        }
    }

    public interface Callback<E> {

        void onSuccess(E result);

        void onFailure(Exception e);

        void onCancel();
    }
}
