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

package com.hippo.nimingban.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.conaco.Conaco;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.NMBUrl;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.TextUtils2;
import com.hippo.vectorold.content.VectorContext;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.LinearDividerItemDecoration;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.List;

public class PostActivity extends AppCompatActivity {

    public static final String ACTION_POST = "com.hippo.nimingban.ui.PostActivity.action.POST";

    public static final String KEY_POST = "post";

    private NMBClient mNMBClient;
    private Conaco mConaco;

    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;

    private ReplyHelper mReplyHelper;
    private ReplyAdapter mReplyAdapter;

    private NMBRequest mNMBRequest;

    private Post mPost;

    // false for error
    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_POST.equals(action)) {
            mPost = intent.getParcelableExtra(KEY_POST);
            if (mPost != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!handlerIntent(getIntent())) {
            finish();
            return;
        }

        setContentView(R.layout.activity_post);

        mNMBClient = NMBApplication.getNMBClient(this);
        mConaco = NMBApplication.getConaco(this);

        mContentLayout = (ContentLayout) findViewById(R.id.content_layout);
        mRecyclerView = mContentLayout.getRecyclerView();

        mReplyHelper = new ReplyHelper();
        mContentLayout.setHelper(mReplyHelper);

        mReplyAdapter = new ReplyAdapter();
        mRecyclerView.setAdapter(mReplyAdapter);
        mRecyclerView.addItemDecoration(new LinearDividerItemDecoration(
                LinearDividerItemDecoration.VERTICAL,
                ResourcesUtils.getAttrColor(this, R.attr.colorDivider),
                LayoutUtils.dp2pix(this, 1)));
        mRecyclerView.setSelector(RippleSalon.generateRippleDrawable(false));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.hasFixedSize();

        // Refresh
        mReplyHelper.firstRefresh();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(VectorContext.wrapContext(newBase));
    }

    private class ReplytHolder extends RecyclerView.ViewHolder {

        public TextView leftText;
        public TextView rightText;
        public TextView content;
        public LoadImageView thumb;

        public ReplytHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (TextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
        }
    }

    private class ReplyAdapter extends RecyclerView.Adapter<ReplytHolder> {

        @Override
        public ReplytHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ReplytHolder(getLayoutInflater().inflate(R.layout.item_list, parent, false));
        }

        @Override
        public void onBindViewHolder(ReplytHolder holder, int position) {
            Reply reply = mReplyHelper.getDataAt(position);
            holder.leftText.setText(TextUtils2.combine(reply.getNMBTimeStr(), "  ", reply.getNMBUser()));
            holder.rightText.setText(reply.getNMBId());
            holder.content.setText(reply.getNMBContent());

            String thumbUrl = reply.getNMBThumbUrl();
            if (thumbUrl != null) {
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.load(mConaco, thumbUrl, thumbUrl);
            } else {
                holder.thumb.setVisibility(View.GONE);
                mConaco.load(holder.thumb, null);
            }
        }

        @Override
        public int getItemCount() {
            return mReplyHelper.size();
        }
    }

    private class ReplyHelper extends ContentLayout.ContentHelper<Reply> {

        @Override
        protected Context getContext() {
            return PostActivity.this;
        }

        @Override
        protected void onScrollToPosition() {

        }

        @Override
        protected void onShowProgress() {

        }

        @Override
        protected void onShowText() {

        }

        @Override
        protected void notifyDataSetChanged() {
            mReplyAdapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mReplyAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            mReplyAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        protected void getPageData(int taskId, int type, int page) {
            if (mNMBRequest != null) {
                mNMBRequest.cancel();
                mNMBRequest = null;
            }

            NMBRequest request = new NMBRequest();
            mNMBRequest = request;
            request.setSite(NMBClient.AC);
            request.setMethod(NMBClient.METHOD_GET_POST);
            request.setArgs(NMBUrl.getPostUrl(NMBClient.AC, mPost.getNMBId(), page));
            request.setCallback(new PostListener(taskId, page, request));
            mNMBClient.execute(request);
        }
    }

    private class PostListener implements NMBClient.Callback<Pair<Post, List<Reply>>> {

        private int mTaskId;
        private int mPage;
        private NMBRequest mRequest;

        public PostListener(int taskId, int page, NMBRequest request) {
            mTaskId = taskId;
            mPage = page;
            mRequest = request;
        }

        @Override
        public void onSuccess(Pair<Post, List<Reply>> result) {
            if (mNMBRequest == mRequest) {
                // It is current request

                // Clear
                mNMBRequest = null;

                mPost = result.first;

                List<Reply> replies = result.second;
                if (mPage == 0) {
                    replies.add(0, mPost);
                }

                if (replies.isEmpty()) {
                    mReplyHelper.setPages(mPage);
                    mReplyHelper.onGetEmptyData(mTaskId);
                } else {
                    mReplyHelper.onGetPageData(mTaskId, replies);
                    if (mReplyHelper.size() == mPost.getNMBReplyCount() + 1) {
                        mReplyHelper.setPages(mPage + 1);
                    } else {
                        mReplyHelper.setPages(Integer.MAX_VALUE);
                    }
                }
            }
            // Clear
            mRequest = null;
        }

        @Override
        public void onFailure(Exception e) {
            if (mNMBRequest == mRequest) {
                // It is current request

                // Clear
                mNMBRequest = null;

                mReplyHelper.onGetExpection(mTaskId, e);
            }
            // Clear
            mRequest = null;
        }

        @Override
        public void onCancelled() {
            if (mNMBRequest == mRequest) {
                // It is current request

                // Clear
                mNMBRequest = null;
            }
            // Clear
            mRequest = null;
        }
    }
}
