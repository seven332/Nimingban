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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.TextUtils2;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.LinearDividerItemDecoration;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.List;

public class ListActivity extends AppCompatActivity {

    private NMBClient mNMBClient;
    private Conaco mConaco;

    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;

    private ListHelper mListHelper;
    private ListAdapter mListAdapter;

    private NMBRequest mNMBRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mNMBClient = NMBApplication.getNMBClient(this);
        mConaco = NMBApplication.getConaco(this);

        mContentLayout = (ContentLayout) findViewById(R.id.content_layout);
        mRecyclerView = mContentLayout.getRecyclerView();

        mListHelper = new ListHelper();
        mContentLayout.setHelper(mListHelper);

        mListAdapter = new ListAdapter();
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new LinearDividerItemDecoration(
                LinearDividerItemDecoration.VERTICAL,
                ResourcesUtils.getAttrColor(this, R.attr.colorDivider),
                LayoutUtils.dp2pix(this, 1)));
        mRecyclerView.setSelector(RippleSalon.generateRippleDrawable(false));
        mRecyclerView.hasFixedSize();

        // Refresh
        mListHelper.firstRefresh();
    }

    private class ListHolder extends RecyclerView.ViewHolder {

        public TextView leftText;
        public TextView rightText;
        public TextView content;
        public LoadImageView thumb;

        public ListHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (TextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ListHolder> {

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListHolder(getLayoutInflater().inflate(R.layout.item_list, parent, false));
        }

        @Override
        public void onBindViewHolder(ListHolder holder, int position) {
            Post post = mListHelper.getDataAt(position);
            holder.leftText.setText(TextUtils2.combine(post.getNMBTime(), "  ", post.getNMBUser()));
            holder.rightText.setText(post.getNMBReplyCount());
            holder.content.setText(post.getNMBContent());

            String thumbUrl = post.getNMBThumbUrl();
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
            return mListHelper.size();
        }
    }

    private class ListHelper extends ContentLayout.ContentHelper<Post> {

        @Override
        protected Context getContext() {
            return ListActivity.this;
        }

        @Override
        protected RecyclerView.LayoutManager generateLayoutManager() {
            return new LinearLayoutManager(ListActivity.this);
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
            mListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mListAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            mListAdapter.notifyItemRangeInserted(positionStart, itemCount);
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
            request.setMethod(NMBClient.METHOD_GET_POST_LIST);
            request.setArgs(NMBUrl.getPostListUrl(NMBClient.AC, "4", page));
            request.setCallback(new ListListener(taskId));
            mNMBClient.execute(request);
        }
    }

    private class ListListener implements NMBClient.Callback<List<Post>> {

        private int mTaskId;

        public ListListener(int taskId) {
            mTaskId = taskId;
        }

        @Override
        public void onSuccess(List<Post> result) {
            // Clear
            mNMBRequest = null;

            mListHelper.setPageCount(Integer.MAX_VALUE);
            mListHelper.onGetPageData(mTaskId, result);
        }

        @Override
        public void onFailure(Exception e) {
            // Clear
            mNMBRequest = null;

            mListHelper.onGetPageData(mTaskId, e);
        }

        @Override
        public void onCancelled() {
            // Clear
            mNMBRequest = null;
        }
    }
}
