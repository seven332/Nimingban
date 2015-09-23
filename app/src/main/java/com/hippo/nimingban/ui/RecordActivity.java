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

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.ProgressNotify;
import com.hippo.effect.ViewTransition;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.dao.ACRecordRaw;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.SimpleImageView;
import com.hippo.widget.Snackbar;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.MarginItemDecoration;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.File;
import java.io.InputStream;

import de.greenrobot.dao.query.LazyList;

public final class RecordActivity extends AbsActivity
        implements EasyRecyclerView.OnItemClickListener {

    private LazyList<ACRecordRaw> mLazyList;

    private EasyRecyclerView mRecyclerView;
    private ViewTransition mViewTransition;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.AppTheme_Dark;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record);
        setActionBarUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_left_dark_x24));

        View tip = findViewById(R.id.tip);
        mRecyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);
        mViewTransition = new ViewTransition(tip, mRecyclerView);
        SimpleImageView imageView = (SimpleImageView) findViewById(R.id.empty_image);

        imageView.setDrawable(VectorDrawable.create(this, R.drawable.ic_empty));

        // Layout Manager
        if (getResources().getBoolean(R.bool.two_way)) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new LinearLayoutManager(this);
        }

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        mAdapter = new RecordAdapter();
        mAdapter.setHasStableIds(true);
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.hasFixedSize();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setOnItemClickListener(this);
        mRecyclerView.setSelector(RippleSalon.generateRippleDrawable(ResourcesUtils.getAttrBoolean(this, R.attr.dark)));
        mRecyclerView.setDrawSelectorOnTop(true);
        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setClipChildren(false);
        int halfInterval = getResources().getDimensionPixelOffset(R.dimen.card_interval) / 2;
        mRecyclerView.addItemDecoration(new MarginItemDecoration(halfInterval));
        mRecyclerView.setPadding(halfInterval, halfInterval, halfInterval, halfInterval);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);

        updateLazyList();
        checkEmpty(false);

        Messenger.getInstance().register(Constants.MESSENGER_ID_UPDATE_RECORD, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Messenger.getInstance().unregister(Constants.MESSENGER_ID_UPDATE_RECORD, this);

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        if (mLazyList != null) {
            mLazyList.close();
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
    public void onReceive(final int id, final Object obj) {
        if (id == Constants.MESSENGER_ID_UPDATE_RECORD) {
            updateLazyList();
            mAdapter.notifyDataSetChanged();
            checkEmpty(true);
        } else {
            super.onReceive(id, obj);
        }
    }

    // Remember to notify
    private void updateLazyList() {
        LazyList<ACRecordRaw> lazyList = DB.getACRecordLazyList();
        if (mLazyList != null) {
            mLazyList.close();
        }
        mLazyList = lazyList;
    }

    private void checkEmpty(boolean animation) {
        if (mAdapter.getItemCount() == 0) {
            mViewTransition.showView(0, animation);
        } else {
            mViewTransition.showView(1, animation);
        }
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        ACRecordRaw raw = mLazyList.get(position);
        String postId = raw.getPostid();
        if (!TextUtils.isEmpty(postId)) {
            Intent intent = new Intent(this, PostActivity.class);
            intent.setAction(PostActivity.ACTION_SITE_ID);
            intent.putExtra(PostActivity.KEY_SITE, ACSite.getInstance().getId());
            intent.putExtra(PostActivity.KEY_ID, postId);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    private class RecordHolder extends AbstractSwipeableItemViewHolder {

        public View cardView;
        public TextView leftText;
        public TextView rightText;
        private TextView content;
        private LoadImageView thumb;

        public RecordHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            leftText = (TextView) itemView.findViewById(R.id.left_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (TextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
        }

        @Override
        public View getSwipeableContainerView() {
            return cardView;
        }
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecordHolder>
            implements SwipeableItemAdapter<RecordHolder> {

        @Override
        public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecordHolder(RecordActivity.this.getLayoutInflater().inflate(R.layout.item_record, parent, false));
        }

        @Override
        public void onBindViewHolder(RecordHolder holder, int position) {
            ACRecordRaw raw = mLazyList.get(position);
            String leftText = null;
            switch (raw.getType()) {
                case DB.AC_RECORD_POST:
                    leftText = getString(R.string.create_post);
                    break;
                case DB.AC_RECORD_REPLY:
                    leftText = getString(R.string.reply);
            }
            holder.leftText.setText(leftText);
            holder.rightText.setText(ReadableTime.getDisplayTime(raw.getTime()));
            holder.content.setText(raw.getContent());

            String image = raw.getImage();
            if (!TextUtils.isEmpty(image)) {
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.unload();
                holder.thumb.load(image, "dump", new LocalPathDataContain(image), false);
            } else {
                holder.thumb.setVisibility(View.GONE);
                holder.thumb.unload();
            }

            holder.content.setTextSize(Settings.getFontSize());
            holder.content.setLineSpacing(LayoutUtils.dp2pix(RecordActivity.this, Settings.getLineSpacing()), 1.0f);
        }

        @Override
        public long getItemId(int position) {
            return mLazyList.get(position).getId();
        }

        @Override
        public int getItemCount() {
            return mLazyList.size();
        }

        @Override
        public int onGetSwipeReactionType(RecordHolder holder, int i, int i1, int i2) {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
        }

        @Override
        public void onSetSwipeBackground(RecordHolder holder, int i, int i1) {
            // Empty
        }

        @Override
        public void onSwipeItemStarted(RecordHolder holder, int position) {
            // Empty
        }

        @Override
        public int onSwipeItem(RecordHolder holder, int position, int result) {
            switch (result) {
                // remove
                case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
                // other --- do nothing
                case RecyclerViewSwipeManager.RESULT_CANCELED:
                default:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
            }
        }

        @Override
        public void onPerformAfterSwipeReaction(RecordHolder holder, int position, int result, int reaction) {
            if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
                final ACRecordRaw raw = mLazyList.get(position);
                if (raw != null) {
                    DB.removeACRecord(mLazyList.get(position));
                    updateLazyList();
                    notifyItemRemoved(position);
                    checkEmpty(true);

                    Snackbar snackbar = Snackbar.make(mRecyclerView, R.string.record_deleted, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DB.addACRecord(raw.getType(), raw.getRecordid(), raw.getPostid(),
                                    raw.getContent(), raw.getImage(), raw.getTime());
                            updateLazyList();
                            notifyDataSetChanged();
                            checkEmpty(true);
                        }
                    });
                    snackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                String image = raw.getImage();
                                if (image != null) {
                                    new File(image).delete();
                                }
                            }
                        }
                    });
                    snackbar.show();
                }
            }
        }
    }

    private static class LocalPathDataContain implements DataContainer {

        private File mFile;

        public LocalPathDataContain(String path) {
            File file = new File(path);
            if (file.isFile()) {
                mFile = file;
            }
        }

        @Override
        public boolean save(InputStream is, long length, String mediaType, ProgressNotify notify) {
            return false;
        }

        @Override
        public InputStreamPipe get() {
            if (mFile != null) {
                return new FileInputStreamPipe(mFile);
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            // Empty
        }
    }
}
