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

import com.hippo.httpclient.HttpClient;
import com.hippo.httpclient.HttpRequest;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.client.ac.ACEngine;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.network.NMBHttpRequest;
import com.hippo.yorozuya.PriorityThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NMBClient {

    public static final String TAG = NMBClient.class.getSimpleName();

    public static final int METHOD_GET_COOKIE = 0;

    public static final int METHOD_GET_POST_LIST = 1;

    public static final int METHOD_GET_POST = 2;

    public static final int METHOD_GET_REFERENCE = 3;

    public static final int METHOD_GET_REPLY = 4;

    public static final int METHOD_GET_FEED = 5;

    public static final int METHOD_ADD_FEED = 6;

    public static final int METHOD_DEL_FEED = 7;

    private final ThreadPoolExecutor mRequestThreadPool;
    private final HttpClient mHttpClient;

    public NMBClient(Context context) {
        int poolSize = 3;
        BlockingQueue<Runnable> requestWorkQueue = new LinkedBlockingQueue<>();
        ThreadFactory threadFactory = new PriorityThreadFactory(TAG,
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mRequestThreadPool = new ThreadPoolExecutor(poolSize, poolSize,
                1L, TimeUnit.SECONDS, requestWorkQueue, threadFactory);
        mHttpClient = NMBApplication.getNMBHttpClient(context);
    }

    public void execute(NMBRequest request) {
        if (!request.isCanceled()) {
            Task task = new Task(request.method, request.site, request.callback);
            task.executeOnExecutor(mRequestThreadPool, request.args);
            request.task = task;
        } else {
            request.callback.onCancelled();
        }
    }

    class Task extends AsyncTask<Object, Void, Object> {

        private int mMethod;
        private Site mSite;
        private Callback mCallback;
        private HttpRequest mHttpRequest;

        private boolean mStop;

        public Task(int method, Site site, Callback callback) {
            mMethod = method;
            mSite = site;
            mCallback = callback;
            mHttpRequest = new NMBHttpRequest(mSite);
        }

        public void stop() {
            if (!mStop) {
                mStop = true;

                Status status = getStatus();
                if (status == Status.PENDING) {
                    cancel(false);

                    // If it is pending, onPostExecute will not be called,
                    // need to call mListener.onCanceled here
                    mCallback.onCancelled();

                    // Clear
                    mHttpRequest = null;
                    mCallback = null;
                } else if (status == Status.RUNNING) {
                    if (mHttpRequest != null) {
                        mHttpRequest.cancel();
                    }
                }
            }
        }

        private Object getCookie() throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.getCookie(mHttpClient, mHttpRequest);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getPostList(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.getPostList(mHttpClient, mHttpRequest, (String) params[0]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getPost(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.getPost(mHttpClient, mHttpRequest, (String) params[0]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getReference(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.getReference(mHttpClient, mHttpRequest, (String) params[0]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object reply(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.reply(mHttpClient, mHttpRequest, params[0]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object getFeed(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.getFeed(mHttpClient, mHttpRequest, (String) params[0], (Integer) params[1]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object addFeed(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.addFeed(mHttpClient, mHttpRequest, (String) params[0], (String) params[1]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        private Object delFeed(Object... params) throws Exception {
            switch (mSite.getId()) {
                case Site.AC:
                    return ACEngine.delFeed(mHttpClient, mHttpRequest, (String) params[0], (String) params[1]);
                default:
                    return new IllegalStateException("Can't detect site " + mSite);
            }
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                switch (mMethod) {
                    case METHOD_GET_COOKIE:
                        return getCookie();
                    case METHOD_GET_POST_LIST:
                        return getPostList(params);
                    case METHOD_GET_POST:
                        return getPost(params);
                    case METHOD_GET_REFERENCE:
                        return getReference(params);
                    case METHOD_GET_REPLY:
                        return reply(params);
                    case METHOD_GET_FEED:
                        return getFeed(params);
                    case METHOD_ADD_FEED:
                        return addFeed(params);
                    case METHOD_DEL_FEED:
                        return delFeed(params);
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
            if (result instanceof CancelledException) {
                mCallback.onCancelled();
            } else if (result instanceof Exception) {
                mCallback.onFailure((Exception) result);
            } else {
                mCallback.onSuccess(result);
            }

            // Clear
            mHttpRequest = null;
            mCallback = null;
        }
    }

    public interface Callback<E> {

        void onSuccess(E result);

        void onFailure(Exception e);

        void onCancelled();
    }
}
