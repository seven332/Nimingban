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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.nimingban.Constants;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.ac.data.ACSearchItem;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.itemanimator.FloatItemAnimator;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.MarginItemDecoration;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.List;

public class SearchActivity extends TranslucentActivity implements EasyRecyclerView.OnItemClickListener {

    public static final String ACTION_SEARCH = "com.hippo.nimingban.ui.SearchActivity.action.SEARCH";

    public static final String KEY_KEYWORD = "keyword";

    private NMBClient mNMBClient;

    private ContentLayout mContentLayout;

    private SearchAdapter mSearchAdapter;
    private SearchHelper mSearchHelper;

    private NMBRequest mNMBRequest;

    private String mKeyword;

    @Override
    protected int getLightThemeResId() {
        return Settings.getColorStatusBar() ? R.style.NormalActivity : R.style.NormalActivity_NoStatus;
    }

    @Override
    protected int getDarkThemeResId() {
        return Settings.getColorStatusBar() ? R.style.NormalActivity_Dark : R.style.NormalActivity_Dark_NoStatus;
    }

    // false for error
    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_SEARCH.equals(action)) {
            String keyword = intent.getStringExtra(KEY_KEYWORD);
            if (keyword != null) {
                mKeyword = keyword;
                setTitle(getString(R.string.search_title, keyword));
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

        mNMBClient = NMBApplication.getNMBClient(this);

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        ToolbarActivityHelper.setContentView(this, R.layout.activity_search);
        setActionBarUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_left_dark_x24));

        mContentLayout = (ContentLayout) findViewById(R.id.content_layout);
        EasyRecyclerView recyclerView = mContentLayout.getRecyclerView();

        mSearchHelper = new SearchHelper();
        mSearchHelper.setEmptyString(getString(R.string.not_found));
        mContentLayout.setHelper(mSearchHelper);
        if (Settings.getFastScroller()) {
            mContentLayout.showFastScroll();
        } else {
            mContentLayout.hideFastScroll();
        }

        mSearchAdapter = new SearchAdapter();
        recyclerView.setAdapter(mSearchAdapter);
        recyclerView.setSelector(RippleSalon.generateRippleDrawable(ResourcesUtils.getAttrBoolean(this, R.attr.dark)));
        recyclerView.setDrawSelectorOnTop(true);
        recyclerView.setOnItemClickListener(this);
        recyclerView.hasFixedSize();
        recyclerView.setClipToPadding(false);

        int halfInterval = getResources().getDimensionPixelOffset(R.dimen.card_interval) / 2;
        if (getResources().getBoolean(R.bool.two_way)) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.addItemDecoration(new MarginItemDecoration(halfInterval));
            recyclerView.setPadding(halfInterval, halfInterval, halfInterval, halfInterval);
            recyclerView.setItemAnimator(new FloatItemAnimator(recyclerView));
        } else {
            recyclerView.addItemDecoration(new MarginItemDecoration(0, halfInterval, 0, halfInterval));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setPadding(0, halfInterval, 0, halfInterval);
        }

        mSearchHelper.firstRefresh();

        Messenger.getInstance().register(Constants.MESSENGER_ID_FAST_SCROLLER, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Messenger.getInstance().unregister(Constants.MESSENGER_ID_FAST_SCROLLER, this);

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }
    }

    @Override
    public void onReceive(int id, Object obj) {
        if (Constants.MESSENGER_ID_FAST_SCROLLER == id) {
            if (obj instanceof Boolean) {
                if ((Boolean) obj) {
                    mContentLayout.showFastScroll();
                } else {
                    mContentLayout.hideFastScroll();
                }
            }
        } else {
            super.onReceive(id, obj);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.setAction(PostActivity.ACTION_SITE_ID);
        intent.putExtra(PostActivity.KEY_SITE, ACSite.getInstance().getId());
        intent.putExtra(PostActivity.KEY_ID, mSearchHelper.getDataAt(position).getNMBPostId());
        startActivity(intent);
        return true;
    }

    private class SearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView leftText;
        public TextView centerText;
        public TextView rightText;
        public TextView content;
        public LoadImageView thumb;

        public SearchHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            centerText = (TextView) itemView.findViewById(R.id.center_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (TextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);

            thumb.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mSearchHelper.size()) {
                ACSearchItem item = mSearchHelper.getDataAt(position);
                String image = item.getNMBImageUrl();
                if (!TextUtils.isEmpty(image)) {
                    Intent intent = new Intent(SearchActivity.this, GalleryActivity2.class);
                    intent.setAction(GalleryActivity2.ACTION_SINGLE_IMAGE);
                    intent.putExtra(GalleryActivity2.KEY_SITE, item.getNMBSite().getId());
                    intent.putExtra(GalleryActivity2.KEY_ID, item.getNMBId());
                    intent.putExtra(GalleryActivity2.KEY_IMAGE, image);
                    SearchActivity.this.startActivity(intent);
                }
            }
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchHolder> {

        @Override
        public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SearchHolder(getLayoutInflater().inflate(R.layout.item_search, parent, false));
        }

        @Override
        public void onBindViewHolder(SearchHolder holder, int position) {
            ACSearchItem item = mSearchHelper.getDataAt(position);
            holder.leftText.setText(item.getNMBDisplayUsername());
            holder.centerText.setText("No." + item.getNMBId());
            holder.rightText.setText(ReadableTime.getDisplayTime(item.getNMBTime()));
            holder.content.setText(item.getNMBDisplayContent());

            String thumbUrl = item.getNMBThumbUrl();

            boolean showImage;
            boolean loadFromNetwork;
            int ils = Settings.getImageLoadingStrategy();
            if (ils == Settings.IMAGE_LOADING_STRATEGY_ALL ||
                    (ils == Settings.IMAGE_LOADING_STRATEGY_WIFI && NMBApplication.isConnectedWifi(SearchActivity.this))) {
                showImage = true;
                loadFromNetwork = true;
            } else {
                showImage = Settings.getImageLoadingStrategy2();
                loadFromNetwork = false;
            }

            if (!TextUtils.isEmpty(thumbUrl) && showImage) {
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.unload();
                holder.thumb.load(thumbUrl, thumbUrl, loadFromNetwork);
            } else {
                holder.thumb.setVisibility(View.GONE);
                holder.thumb.unload();
            }

            holder.content.setTextSize(Settings.getFontSize());
            holder.content.setLineSpacing(LayoutUtils.dp2pix(SearchActivity.this, Settings.getLineSpacing()), 1.0f);
        }

        @Override
        public int getItemCount() {
            return mSearchHelper.size();
        }
    }

    private class SearchHelper extends ContentLayout.ContentHelper<ACSearchItem> {

        @Override
        protected void getPageData(int taskId, int type, int page) {
            if (mNMBRequest != null) {
                mNMBRequest.cancel();
                mNMBRequest = null;
            }

            NMBRequest request = new NMBRequest();
            mNMBRequest = request;
            request.setSite(ACSite.getInstance());
            request.setMethod(NMBClient.METHOD_SEARCH);
            request.setArgs(mKeyword, page);
            request.setCallback(new SearchListener(taskId, type, page));
            mNMBClient.execute(request);
        }

        @Override
        protected Context getContext() {
            return SearchActivity.this;
        }

        @Override
        protected void notifyDataSetChanged() {
            mSearchAdapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mSearchAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            mSearchAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    private class SearchListener implements NMBClient.Callback<List<ACSearchItem>> {

        private int mTaskId;
        private int mTaskType;
        private int mPage;

        public SearchListener(int taskId, int taskType, int page) {
            mTaskId = taskId;
            mTaskType = taskType;
            mPage = page;
        }

        @Override
        public void onSuccess(List<ACSearchItem> result) {
            // Clear
            mNMBRequest = null;

            if (result.isEmpty()) {
                mSearchHelper.onGetEmptyData(mTaskId);
                mSearchHelper.setPages(mPage);
            } else {
                mSearchHelper.onGetPageData(mTaskId, result);
                mSearchHelper.setPages(Integer.MAX_VALUE);
            }
        }

        @Override
        public void onFailure(Exception e) {
            // Clear
            mNMBRequest = null;
            mSearchHelper.onGetExpection(mTaskId, e);
        }

        @Override
        public void onCancel() {
            // Clear
            mNMBRequest = null;
        }
    }
}
