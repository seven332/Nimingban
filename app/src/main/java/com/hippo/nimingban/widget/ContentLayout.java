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

package com.hippo.nimingban.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.easyrecyclerview.FastScroller;
import com.hippo.easyrecyclerview.HandlerDrawable;
import com.hippo.easyrecyclerview.LayoutManagerUtils;
import com.hippo.effect.ViewTransition;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.ac.data.ACPost;
import com.hippo.refreshlayout.RefreshLayout;
import com.hippo.util.ExceptionUtils;
import com.hippo.widget.ProgressView;
import com.hippo.yorozuya.IntIdGenerator;
import com.hippo.yorozuya.IntList;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.Say;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContentLayout extends FrameLayout {

    private static final String TAG = ContentLayout.class.getSimpleName();

    private ProgressView mProgressView;
    private ViewGroup mTipView;
    private ViewGroup mContentView;

    private RefreshLayout mRefreshLayout;
    private EasyRecyclerView mRecyclerView;
    private FastScroller mFastScroller;
    private View mImageView;
    private TextView mTextView;

    private int mRecyclerViewOriginTop;
    private int mRecyclerViewOriginBottom;

    public ContentLayout(Context context) {
        super(context);
        init(context);
    }

    public ContentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_content_layout, this);

        mProgressView = (ProgressView) findViewById(R.id.progress);
        mTipView = (ViewGroup) findViewById(R.id.tip);
        mContentView = (ViewGroup) findViewById(R.id.content_view);

        mRefreshLayout = (RefreshLayout) mContentView.findViewById(R.id.refresh_layout);
        mFastScroller = (FastScroller) mContentView.findViewById(R.id.fast_scroller);
        mRecyclerView = (EasyRecyclerView) mRefreshLayout.findViewById(R.id.recycler_view);
        mImageView = mTipView.getChildAt(0);
        mTextView = (TextView) mTipView.getChildAt(1);

        mFastScroller.attachToRecyclerView(mRecyclerView);
        HandlerDrawable drawable = new HandlerDrawable();
        drawable.setColor(ResourcesUtils.getAttrColor(context, R.attr.colorAccent));
        mFastScroller.setHandlerDrawable(drawable);

        mRefreshLayout.setHeaderColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_purple,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_cyan,
                R.color.loading_indicator_green,
                R.color.loading_indicator_yellow);
        mRefreshLayout.setFooterColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_green,
                R.color.loading_indicator_orange);
        mRefreshLayout.setHeaderProgressBackgroundColorSchemeColor(ResourcesUtils.getAttrColor(context, R.attr.colorPure));

        mRecyclerViewOriginTop = mRecyclerView.getPaddingTop();
        mRecyclerViewOriginBottom = mRecyclerView.getPaddingBottom();
    }

    public EasyRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setHelper(ContentHelper helper) {
        helper.init(this);
    }

    public void showFastScroll() {
        if (!mFastScroller.isAttached()) {
            mFastScroller.attachToRecyclerView(mRecyclerView);
        }
    }

    public void hideFastScroll() {
        mFastScroller.detachedFromRecyclerView();
    }

    public void setFitPaddingTop(int fitPaddingTop) {
        // RecyclerView
        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), mRecyclerViewOriginTop + fitPaddingTop, mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());
        // RefreshLayout
        mRefreshLayout.setProgressViewOffset(false, fitPaddingTop, fitPaddingTop + LayoutUtils.dp2pix(getContext(), 32)); // TODO
    }

    public void setFitPaddingBottom(int fitPaddingBottom) {
        // RecyclerView
        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(),
                mRecyclerView.getPaddingTop(), mRecyclerView.getPaddingRight(),
                mRecyclerViewOriginBottom + fitPaddingBottom);
    }

    public abstract static class ContentHelper<E> {

        private static final String TAG = ContentHelper.class.getSimpleName();

        public static final int TYPE_REFRESH = 0;
        public static final int TYPE_PRE_PAGE = 1;
        public static final int TYPE_PRE_PAGE_KEEP_POS = 2;
        public static final int TYPE_NEXT_PAGE = 3;
        public static final int TYPE_NEXT_PAGE_KEEP_POS = 4;
        public static final int TYPE_SOMEWHERE = 5;
        public static final int TYPE_REFRESH_PAGE = 6;

        public static final int REFRESH_TYPE_HEADER = 0;
        public static final int REFRESH_TYPE_FOOTER = 1;
        public static final int REFRESH_TYPE_PROGRESS_VIEW = 2;

        private ProgressView mProgressView;
        private ViewGroup mTipView;
        private ViewGroup mContentView;

        private RefreshLayout mRefreshLayout;
        private EasyRecyclerView mRecyclerView;
        private View mImageView;
        private TextView mTextView;

        private ViewTransition mViewTransition;

        /**
         * Store data
         */
        private List<E> mData = new ArrayList<>();

        /**
         * Generate task id
         */
        private IntIdGenerator mIdGenerator = new IntIdGenerator();

        /**
         * Store the page divider index
         * <p>
         * For example, the data contain page 3, page 4, page 5,
         * page 3 size is 7, page 4 size is 8, page 5 size is 9,
         * so <code>mPageDivider</code> contain 7, 15, 24.
         */
        private IntList mPageDivider = new IntList();

        /**
         * The first page in <code>mData</code>
         */
        private int mStartPage;

        /**
         * The last page + 1 in <code>mData</code>
         */
        private int mEndPage;

        /**
         * The available page count.
         */
        private int mPages;

        private int mCurrentTaskId;
        private int mCurrentTaskType;
        private int mCurrentTaskPage;

        private int mNextPageScrollSize;

        private String mEmptyString = "No hint";

        private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!mRefreshLayout.isRefreshing() && mRefreshLayout.isAlmostBottom() && mEndPage < mPages) {
                    // Get next page
                    mRefreshLayout.setFooterRefreshing(true);
                    mOnRefreshListener.onFooterRefresh();
                }
            }
        };

        private RefreshLayout.OnRefreshListener mOnRefreshListener = new RefreshLayout.OnRefreshListener() {
            @Override
            public void onHeaderRefresh() {
                if (mStartPage > 0) {
                    mCurrentTaskId = mIdGenerator.nextId();
                    mCurrentTaskType = TYPE_PRE_PAGE_KEEP_POS;
                    mCurrentTaskPage = mStartPage - 1;
                    getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
                } else {
                    doRefresh();
                }
            }

            @Override
            public void onFooterRefresh() {
                if (mEndPage < mPages) {
                    // Get next page
                    mCurrentTaskId = mIdGenerator.nextId();
                    mCurrentTaskType = TYPE_NEXT_PAGE_KEEP_POS;
                    mCurrentTaskPage = mEndPage;
                    getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
                } else if (mEndPage == mPages) {
                    // Refresh last page
                    mCurrentTaskId = mIdGenerator.nextId();
                    mCurrentTaskType = TYPE_REFRESH_PAGE;
                    mCurrentTaskPage = mEndPage - 1;
                    getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
                } else {
                    Log.e(TAG, "Try to footer refresh, but mEndPage = " + mEndPage + ", mPages = " + mPages);
                }
            }
        };

        private final LayoutManagerUtils.OnScrollToPositionListener mOnScrollToPositionListener =
                new LayoutManagerUtils.OnScrollToPositionListener() {
                    @Override
                    public void onScrollToPosition(int position) {
                        ContentHelper.this.onScrollToPosition();
                    }
                };

        private void init(ContentLayout contentLayout) {
            mNextPageScrollSize = LayoutUtils.dp2pix(contentLayout.getContext(), 48);

            mProgressView = contentLayout.mProgressView;
            mTipView = contentLayout.mTipView;
            mContentView = contentLayout.mContentView;

            mRefreshLayout = contentLayout.mRefreshLayout;
            mRecyclerView = contentLayout.mRecyclerView;
            mImageView = contentLayout.mImageView;
            mTextView = contentLayout.mTextView;

            mViewTransition = new ViewTransition(mContentView, mProgressView, mTipView);
            mViewTransition.showView(2, false);

            mRecyclerView.addOnScrollListener(mOnScrollListener);
            mRefreshLayout.setOnRefreshListener(mOnRefreshListener);

            mTipView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            });
        }


        /**
         * Call {@link #onGetPageData(int, List)} when get data
         *
         * @param taskId task id
         * @param page   the page to get
         */
        protected abstract void getPageData(int taskId, int type, int page);

        protected abstract Context getContext();

        protected abstract void notifyDataSetChanged();

        protected abstract void notifyItemRangeRemoved(int positionStart, int itemCount);

        protected abstract void notifyItemRangeInserted(int positionStart, int itemCount);

        protected void onScrollToPosition() {
        }

        protected void onShowProgress() {
        }

        protected void onShowText() {
        }

        public void setEnable(boolean enable) {
            mRefreshLayout.setEnabled(enable);
        }

        public void setEmptyString(String str) {
            mEmptyString = str;
        }

        /**
         * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
         */
        public E getDataAt(int location) {
            return mData.get(location);
        }

        public int size() {
            return mData.size();
        }

        public void setPages(int pages) {
            // TODO what it pages > mEndPage
            mPages = pages;
        }

        public int getPages() {
            return mPages;
        }

        public void addAt(int index, E data) {
            mData.add(index, data);

            for (int i = 0, n = mPageDivider.size(); i < n; i++) {
                int divider = mPageDivider.get(i);
                if (index < divider) {
                    mPageDivider.set(i, divider + 1);
                }
            }

            notifyItemRangeInserted(index, 1);
        }

        public void removeAt(int index) {
            mData.remove(index);

            for (int i = 0, n = mPageDivider.size(); i < n; i++) {
                int divider = mPageDivider.get(i);
                if (index < divider) {
                    mPageDivider.set(i, divider - 1);
                }
            }

            notifyItemRangeRemoved(index, 1);
        }

        public void onGetEmptyData(int taskId) {
            if (mCurrentTaskId != taskId) {
                return;
            }

            switch (mCurrentTaskType) {
                case TYPE_REFRESH:
                case TYPE_SOMEWHERE:
                    showText(mEmptyString);
                    break;
                case TYPE_NEXT_PAGE:
                case TYPE_NEXT_PAGE_KEEP_POS:
                    // Last page ?
                    break;
                case TYPE_PRE_PAGE:
                case TYPE_PRE_PAGE_KEEP_POS:
                case TYPE_REFRESH_PAGE:
                    // TODO
                    break;
            }

            mRefreshLayout.setHeaderRefreshing(false);
            mRefreshLayout.setFooterRefreshing(false);
        }

        public void onGetPageData(int taskId, List<E> data) {
            if (mCurrentTaskId == taskId) {
                showContent();

                int dataSize;
                switch (mCurrentTaskType) {
                    case TYPE_REFRESH:
                        mStartPage = 0;
                        mEndPage = 1;
                        mPageDivider.clear();
                        mPageDivider.add(data.size());

                        mData.clear();
                        mData.addAll(data);
                        notifyDataSetChanged();

                        mRecyclerView.stopScroll();
                        LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), 0, 0);
                        onScrollToPosition();
                        break;
                    case TYPE_PRE_PAGE:
                    case TYPE_PRE_PAGE_KEEP_POS:
                        mData.addAll(0, data);
                        notifyItemRangeInserted(0, data.size());

                        dataSize = data.size();
                        for (int i = 0, n = mPageDivider.size(); i < n; i++) {
                            mPageDivider.set(i, mPageDivider.get(i) + dataSize);
                        }
                        mPageDivider.add(0, dataSize);

                        mStartPage--;
                        // assert mStartPage >= 0
                        if (mCurrentTaskType == TYPE_PRE_PAGE_KEEP_POS) {
                            mRecyclerView.stopScroll();
                            LayoutManagerUtils.scrollToPositionProperly(mRecyclerView.getLayoutManager(), getContext(),
                                    dataSize - 1, mOnScrollToPositionListener);
                        } else {
                            mRecyclerView.stopScroll();
                            LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), 0, 0);
                            onScrollToPosition();
                        }
                        break;
                    case TYPE_NEXT_PAGE:
                    case TYPE_NEXT_PAGE_KEEP_POS:
                        dataSize = data.size();
                        int oldDataSize = mData.size();

                        if (data.get(0) instanceof ACPost) {
                            //remove repeat ACPost
                            Iterator iterator = data.iterator();
                            while (iterator.hasNext()) {
                                Object post = iterator.next();
                                if (mData.contains(post)) {
                                    dataSize--;
                                }else {
                                    mData.add((E) post);
                                }
                            }
                        } else {
                            mData.addAll(data);
                        }

                        notifyItemRangeInserted(oldDataSize, dataSize);

                        mPageDivider.add(oldDataSize + dataSize);

                        mEndPage++;
                        if (mCurrentTaskType == TYPE_NEXT_PAGE_KEEP_POS) {
                            mRecyclerView.stopScroll();
                            mRecyclerView.smoothScrollBy(0, mNextPageScrollSize);
                            onScrollToPosition();
                        } else {
                            mRecyclerView.stopScroll();
                            LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), oldDataSize, 0);
                            onScrollToPosition();
                        }
                        break;
                    case TYPE_SOMEWHERE:
                        mData.clear();
                        mData.addAll(data);
                        notifyDataSetChanged();

                        mStartPage = mCurrentTaskPage;
                        mEndPage = mCurrentTaskPage + 1;

                        mPageDivider.clear();
                        mPageDivider.add(data.size());

                        mRecyclerView.stopScroll();
                        LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), 0, 0);
                        onScrollToPosition();
                        break;
                    case TYPE_REFRESH_PAGE:
                        if (mCurrentTaskPage < mStartPage || mCurrentTaskPage >= mEndPage) {
                            Log.e(TAG, "TYPE_REFRESH_PAGE, but mCurrentTaskPage = " + mCurrentTaskPage +
                                    ", mStartPage = " + mStartPage + ", mEndPage = " + mEndPage);
                            break;
                        }

                        int oldIndexStart = mCurrentTaskPage == mStartPage ? 0 : mPageDivider.get(mCurrentTaskPage - mStartPage - 1);
                        int oldIndexEnd = mPageDivider.get(mCurrentTaskPage - mStartPage);
                        mData.subList(oldIndexStart, oldIndexEnd).clear();
                        int newIndexStart = oldIndexStart;
                        int newIndexEnd = newIndexStart + data.size();
                        mData.addAll(oldIndexStart, data);
                        notifyDataSetChanged();

                        for (int i = mCurrentTaskPage - mStartPage, n = mPageDivider.size(); i < n; i++) {
                            mPageDivider.set(i, mPageDivider.get(i) - oldIndexEnd + newIndexEnd);
                        }

                        if (newIndexEnd > oldIndexEnd && newIndexEnd > 0) {
                            mRecyclerView.stopScroll();
                            LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), newIndexEnd - 1, 0);
                            onScrollToPosition();
                        }
                        break;
                }
            }

            mRefreshLayout.setHeaderRefreshing(false);
            mRefreshLayout.setFooterRefreshing(false);
        }

        public void onGetExpection(int taskId, Exception e) {
            if (mCurrentTaskId == taskId) {
                if (e != null) {
                    e.printStackTrace();
                }

                mRefreshLayout.setHeaderRefreshing(false);
                mRefreshLayout.setFooterRefreshing(false);
                Say.d(TAG, "Get page data failed " + e.getClass().getName() + " " + e.getMessage());
                String readableError = ExceptionUtils.getReadableString(getContext(), e);
                String reason = ExceptionUtils.getReasonString(getContext(), e);
                if (reason != null) {
                    readableError += '\n' + reason;
                }
                if (mViewTransition.getShownViewIndex() == 0) {
                    Toast.makeText(getContext(), readableError, Toast.LENGTH_SHORT).show();
                } else {
                    showText(readableError);
                }
            }
        }

        public void showContent() {
            mViewTransition.showView(0);
        }

        private boolean isContentShowning() {
            return mViewTransition.getShownViewIndex() == 0;
        }

        public void showProgressBar() {
            mViewTransition.showView(1, false);
        }

        public void showProgressBar(boolean animation) {
            if (mViewTransition.showView(1, animation)) {
                onShowProgress();
            }
        }

        public void showText(CharSequence text) {
            mTextView.setText(text);
            if (mViewTransition.showView(2)) {
                onShowText();
            }
        }

        public void showEmptyString() {
            showText(mEmptyString);
        }

        /**
         * Be carefull
         */
        public void doGetData(int type, int page, int refreshType) {
            switch (refreshType) {
                default:
                case REFRESH_TYPE_HEADER:
                    showContent();
                    mRefreshLayout.setFooterRefreshing(false);
                    mRefreshLayout.setHeaderRefreshing(true);
                    break;
                case REFRESH_TYPE_FOOTER:
                    showContent();
                    mRefreshLayout.setHeaderRefreshing(false);
                    mRefreshLayout.setFooterRefreshing(true);
                    break;
                case REFRESH_TYPE_PROGRESS_VIEW:
                    showProgressBar();
                    mRefreshLayout.setHeaderRefreshing(false);
                    mRefreshLayout.setFooterRefreshing(false);
                    break;
            }

            mCurrentTaskId = mIdGenerator.nextId();
            mCurrentTaskType = type;
            mCurrentTaskPage = page;
            getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
        }

        private void doRefresh() {
            mCurrentTaskId = mIdGenerator.nextId();
            mCurrentTaskType = TYPE_REFRESH;
            mCurrentTaskPage = 0;
            getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
        }

        /**
         * Lisk {@link #refresh()}, but no animation when show progress bar
         */
        public void firstRefresh() {
            showProgressBar(false);
            doRefresh();
        }

        /**
         * Show progress bar first, than do refresh
         */
        public void refresh() {
            showProgressBar();
            doRefresh();
        }

        private void cancelCurrentTask() {
            mCurrentTaskId = mIdGenerator.nextId();
            mRefreshLayout.setHeaderRefreshing(false);
            mRefreshLayout.setFooterRefreshing(false);
        }

        private int getPageStart(int page) {
            if (mStartPage == page) {
                return 0;
            } else {
                return mPageDivider.get(page - mStartPage - 1);
            }
        }

        private int getPageEnd(int page) {
            return mPageDivider.get(page - mStartPage);
        }

        private int getPageForPosition(int position) {
            if (position < 0) {
                return -1;
            }

            IntList pageDivider = mPageDivider;
            for (int i = 0, n = pageDivider.size(); i < n; i++) {
                if (position < pageDivider.get(i)) {
                    return i + mStartPage;
                }
            }

            return -1;
        }

        public int getPageForTop() {
            return getPageForPosition(LayoutManagerUtils.getFirstVisibleItemPosition(mRecyclerView.getLayoutManager()));
        }

        public int getPageForBottom() {
            return getPageForPosition(LayoutManagerUtils.getLastVisibleItemPosition(mRecyclerView.getLayoutManager()));
        }

        public boolean canGoTo() {
            return isContentShowning();
        }

        /**
         * Check range first!
         *
         * @param page the targe page
         * @throws IndexOutOfBoundsException
         */
        public void goTo(int page) throws IndexOutOfBoundsException {
            if (page < 0 || page >= mPages) {
                throw new IndexOutOfBoundsException("Page count is " + mPages + ", page is " + page);
            } else if (page >= mStartPage && page < mEndPage) {
                cancelCurrentTask();

                int position = getPageStart(page);
                mRecyclerView.stopScroll();
                LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), position, 0);
                onScrollToPosition();
            } else if (page == mStartPage - 1) {
                mRefreshLayout.setFooterRefreshing(false);
                mRefreshLayout.setHeaderRefreshing(true);

                mCurrentTaskId = mIdGenerator.nextId();
                mCurrentTaskType = TYPE_PRE_PAGE;
                mCurrentTaskPage = page;
                getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
            } else if (page == mEndPage) {
                mRefreshLayout.setHeaderRefreshing(false);
                mRefreshLayout.setFooterRefreshing(true);

                mCurrentTaskId = mIdGenerator.nextId();
                mCurrentTaskType = TYPE_NEXT_PAGE;
                mCurrentTaskPage = page;
                getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
            } else {
                mRefreshLayout.setFooterRefreshing(false);
                mRefreshLayout.setHeaderRefreshing(true);

                mCurrentTaskId = mIdGenerator.nextId();
                mCurrentTaskType = TYPE_SOMEWHERE;
                mCurrentTaskPage = page;
                getPageData(mCurrentTaskId, mCurrentTaskType, mCurrentTaskPage);
            }
        }
    }
}
