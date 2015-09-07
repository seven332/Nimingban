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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.conaco.Conaco;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.TextUtils2;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.List;

public final class FeedActivity extends AbsActivity implements EasyRecyclerView.OnItemClickListener {

    private Conaco mConaco;
    private NMBClient mNMBClient;

    private FeedAdapter mFeedAdapter;
    private FeedHelper mFeedHelper;

    private NMBRequest mNMBRequest;

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.AppTheme_Dark;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConaco = NMBApplication.getConaco(this);
        mNMBClient = NMBApplication.getNMBClient(this);

        setContentView(R.layout.activity_feed);

        ContentLayout contentLayout = (ContentLayout) findViewById(R.id.content_layout);
        EasyRecyclerView recyclerView = contentLayout.getRecyclerView();

        mFeedHelper = new FeedHelper();
        mFeedHelper.setEmptyString(getString(R.string.no_feed));
        contentLayout.setHelper(mFeedHelper);

        mFeedAdapter = new FeedAdapter();
        recyclerView.setAdapter(mFeedAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setSelector(RippleSalon.generateRippleDrawable(ResourcesUtils.getAttrBoolean(this, R.attr.dark)));
        recyclerView.setOnItemClickListener(this);
        recyclerView.hasFixedSize();

        mFeedHelper.firstRefresh();
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.setAction(PostActivity.ACTION_POST);
        intent.putExtra(PostActivity.KEY_POST, mFeedHelper.getDataAt(position));
        startActivity(intent);
        return true;
    }

    private class FeedHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView leftText;
        private TextView rightText;
        private TextView content;
        private LoadImageView thumb;

        public FeedHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (TextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);

            thumb.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mFeedHelper.size()) {
                Post post = mFeedHelper.getDataAt(position);
                String image = post.getNMBImageUrl();
                if (!TextUtils.isEmpty(image)) {
                    Intent intent = new Intent(FeedActivity.this, GalleryActivity2.class);
                    intent.setAction(GalleryActivity2.ACTION_SINGLE_IMAGE);
                    intent.putExtra(GalleryActivity2.KEY_SITE, post.getNMBSite().getId());
                    intent.putExtra(GalleryActivity2.KEY_ID, post.getNMBId());
                    intent.putExtra(GalleryActivity2.KEY_IMAGE, image);
                    FeedActivity.this.startActivity(intent);
                }
            }
        }
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedHolder> {

        @Override
        public FeedHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new FeedHolder(getLayoutInflater().inflate(R.layout.item_feed, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(FeedHolder holder, int i) {
            Post post = mFeedHelper.getDataAt(i);
            holder.leftText.setText(TextUtils2.combine(post.getNMBDisplayUsername(), "    ",
                    ReadableTime.getDisplayTime(post.getNMBTime())));
            holder.content.setText(post.getNMBDisplayContent());

            String thumbUrl = post.getNMBThumbUrl();
            if (!TextUtils.isEmpty(thumbUrl) && NMBAppConfig.needloadImage(FeedActivity.this)) {
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.load(thumbUrl, thumbUrl);
            } else {
                holder.thumb.setVisibility(View.GONE);
                mConaco.load(holder.thumb, null);
            }
        }

        @Override
        public int getItemCount() {
            return mFeedHelper.size();
        }
    }

    private class FeedHelper extends ContentLayout.ContentHelper<Post> {

        @Override
        protected void getPageData(int taskId, int type, int page) {
            Site site = ACSite.getInstance(); // TODO only AC ?
            NMBRequest request = new NMBRequest();
            mNMBRequest = request;
            request.setSite(site);
            request.setMethod(NMBClient.METHOD_GET_FEED);
            request.setArgs(site.getUserId(FeedActivity.this), page);
            request.setCallback(new FeedListener(taskId, page, request));
            mNMBClient.execute(request);
        }

        @Override
        protected Context getContext() {
            return FeedActivity.this;
        }

        @Override
        protected void notifyDataSetChanged() {
            mFeedAdapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mFeedAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            mFeedAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    private class FeedListener implements NMBClient.Callback<List<Post>> {

        private int mTaskId;
        private int mTaskPage;
        private NMBRequest mRequest;

        public FeedListener(int taskId, int taskPage, NMBRequest request) {
            mTaskId = taskId;
            mTaskPage = taskPage;
            mRequest = request;
        }

        @Override
        public void onSuccess(List<Post> result) {
            if (mNMBRequest == mRequest) {
                // It is current request

                // Clear
                mNMBRequest = null;

                if (result.isEmpty()) {
                    mFeedHelper.setPages(mTaskPage);
                    mFeedHelper.onGetEmptyData(mTaskId);
                } else {
                    mFeedHelper.setPages(Integer.MAX_VALUE);
                    mFeedHelper.onGetPageData(mTaskId, result);
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

                mFeedHelper.onGetExpection(mTaskId, e);
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
