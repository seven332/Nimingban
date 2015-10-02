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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.ac.data.ACBingSearchItem;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.rippleold.RippleSalon;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.MarginItemDecoration;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.List;

public class BingSearchActivity extends AbsActivity implements EasyRecyclerView.OnItemClickListener {

    public static final String ACTION_SEARCH = "com.hippo.nimingban.ui.BingSearchActivity.action.SEARCH";

    public static final String KEY_KEYWORD = "keyword";

    private NMBClient mNMBClient;

    private SearchAdapter mSearchAdapter;
    private SearchHelper mSearchHelper;

    private NMBRequest mNMBRequest;

    private String mKeyword;

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.AppTheme_Dark;
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
                setTitle(getString(R.string.bing_search_title, keyword));
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

        setContentView(R.layout.activity_bing_search);
        setActionBarUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_left_dark_x24));

        ContentLayout contentLayout = (ContentLayout) findViewById(R.id.content_layout);
        EasyRecyclerView recyclerView = contentLayout.getRecyclerView();

        mSearchHelper = new SearchHelper();
        mSearchHelper.setEmptyString(getString(R.string.not_found));
        contentLayout.setHelper(mSearchHelper);

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
        } else {
            recyclerView.addItemDecoration(new MarginItemDecoration(0, halfInterval, 0, halfInterval));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setPadding(0, halfInterval, 0, halfInterval);
        }

        mSearchHelper.firstRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
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
        intent.putExtra(PostActivity.KEY_ID, mSearchHelper.getDataAt(position).id);
        startActivity(intent);
        return true;
    }

    private class SearchHolder extends RecyclerView.ViewHolder {

        public TextView leftText;
        private TextView content;

        public SearchHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            content = (TextView) itemView.findViewById(R.id.content);
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchHolder> {

        @Override
        public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SearchHolder(getLayoutInflater().inflate(R.layout.item_bing_search, parent, false));
        }

        @Override
        public void onBindViewHolder(SearchHolder holder, int position) {
            ACBingSearchItem item = mSearchHelper.getDataAt(position);
            holder.leftText.setText("No." + item.id);
            holder.content.setText(item.context);

            holder.content.setTextSize(Settings.getFontSize());
            holder.content.setLineSpacing(LayoutUtils.dp2pix(BingSearchActivity.this, Settings.getLineSpacing()), 1.0f);
        }

        @Override
        public int getItemCount() {
            return mSearchHelper.size();
        }
    }

    private class SearchHelper extends ContentLayout.ContentHelper<ACBingSearchItem> {

        @Override
        protected void getPageData(int taskId, int type, int page) {
            if (mNMBRequest != null) {
                mNMBRequest.cancel();
                mNMBRequest = null;
            }

            NMBRequest request = new NMBRequest();
            mNMBRequest = request;
            request.setSite(ACSite.getInstance());
            request.setMethod(NMBClient.METHOD_BING_SEARCH);
            request.setArgs(mKeyword, page);
            request.setCallback(new SearchListener(taskId, type, page));
            mNMBClient.execute(request);
        }

        @Override
        protected Context getContext() {
            return BingSearchActivity.this;
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

    private class SearchListener implements NMBClient.Callback<List<ACBingSearchItem>> {

        private int mTaskId;
        private int mTaskType;
        private int mPage;

        public SearchListener(int taskId, int taskType, int page) {
            mTaskId = taskId;
            mTaskType = taskType;
            mPage = page;
        }

        @Override
        public void onSuccess(List<ACBingSearchItem> result) {
            // Clear
            mNMBRequest = null;

            if (result.isEmpty()) {
                mSearchHelper.onGetEmptyData(mTaskId);
                if (mTaskType == ContentLayout.ContentHelper.TYPE_NEXT_PAGE ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_NEXT_PAGE_KEEP_POS) {
                    mSearchHelper.setPages(mPage);
                } else {
                    mSearchHelper.setPages(0);
                }
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
