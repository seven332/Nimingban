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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.hippo.app.ProgressDialogBuilder;
import com.hippo.effect.ViewTransition;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.ac.data.ACForum;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.dao.ACForumRaw;
import com.hippo.nimingban.util.DB;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.SimpleImageView;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.yorozuya.ViewUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.dao.query.LazyList;

public class SortForumsActivity extends AbsActivity {

    public static final String KEY_SITE = "site";

    private Site mSite;

    private View mTip;
    private EasyRecyclerView mRecyclerView;
    private ViewTransition mViewTransition;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    private boolean mNeedUpdate;

    private Dialog mProgressDialog;

    private NMBRequest mNMBRequest;

    // TODO support other site
    private LazyList<ACForumRaw> mLazyList;

    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        int site = intent.getIntExtra(KEY_SITE, -1);
        if (Site.isValid(site)) {
            mSite = Site.fromId(site);
            return true;
        }

        return false;
    }

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

        if (!handlerIntent(getIntent())) {
            finish();
            return;
        }

        setContentView(R.layout.activity_forum_sort);

        mTip = findViewById(R.id.tip);
        mRecyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);
        mViewTransition = new ViewTransition(mTip, mRecyclerView);
        SimpleImageView imageView = (SimpleImageView) findViewById(R.id.empty_image);

        imageView.setDrawable(VectorDrawable.create(this, R.drawable.ic_empty));

        // Layout Manager
        mLayoutManager = new LinearLayoutManager(this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.shadow_8dp));

        mAdapter = new ForumAdapter();
        mAdapter.setHasStableIds(true);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);      // wrap for dragging

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.hasFixedSize();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        updateLazyList(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forum_sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (mNMBRequest != null) {
                    return true;
                }

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mNMBRequest != null) {
                            mNMBRequest.cancel();
                            mNMBRequest = null;
                        }
                    }
                };

                mProgressDialog = new ProgressDialogBuilder(this)
                        .setTitle(R.string.please_wait)
                        .setMessage(R.string.refreshing_forum_list)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, listener)
                        .show();

                NMBClient client = NMBApplication.getNMBClient(this);
                NMBRequest request = new NMBRequest();
                mNMBRequest = request;
                request.setSite(mSite);
                request.setMethod(NMBClient.METHOD_GET_FORUM_LIST);
                request.setCallback(new ForumsListener());
                client.execute(request);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
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

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNeedUpdate) {
            setResult(RESULT_OK);
        }

        super.onBackPressed();
    }

    private void updateLazyList(boolean animation) {
        LazyList<ACForumRaw> lazyList = DB.getACForumLazyList();
        if (mLazyList != null) {
            mLazyList.close();
        }
        mLazyList = lazyList;

        mViewTransition.showView(mLazyList.isEmpty() ? 0 : 1, animation);
    }

    private class ForumHolder extends AbstractDraggableItemViewHolder implements View.OnClickListener {

        public View visibility;
        public TextView forum;
        public View dragHandler;

        public ForumHolder(View itemView) {
            super(itemView);

            visibility = itemView.findViewById(R.id.visibility);
            forum = (TextView) itemView.findViewById(R.id.forum);
            dragHandler = itemView.findViewById(R.id.drag_handler);

            visibility.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mLazyList.size()) {
                ACForumRaw raw = mLazyList.get(position);
                DB.setACForumVisibility(raw, !raw.getVisibility());

                // Update UI
                visibility.setActivated(raw.getVisibility());

                mNeedUpdate = true;
            }
        }
    }

    private class ForumAdapter extends RecyclerView.Adapter<ForumHolder>
            implements DraggableItemAdapter<ForumHolder> {

        @Override
        public ForumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ForumHolder(getLayoutInflater().inflate(R.layout.item_forum_sort, parent, false));
        }

        @Override
        public void onBindViewHolder(ForumHolder holder, int position) {
            ACForumRaw raw = mLazyList.get(position);
            holder.visibility.setActivated(raw.getVisibility());
            holder.forum.setText(raw.getDisplayname());
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
        public boolean onCheckCanStartDrag(ForumHolder holder, int position, int x, int y) {
            return ViewUtils.isViewUnder(holder.dragHandler, x, y, 0);
        }

        @Override
        public ItemDraggableRange onGetItemDraggableRange(ForumHolder holder, int position) {
            return null;
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            if (fromPosition == toPosition) {
                return;
            }

            List<ACForumRaw> changed = new ArrayList<>(Math.abs(fromPosition - toPosition) + 1);
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i <= toPosition; i++) {
                    changed.add(mLazyList.get(i));
                }
            } else {
                for (int i = fromPosition; i >= toPosition; i--) {
                    changed.add(mLazyList.get(i));
                }
            }

            int previousPriority = changed.get(changed.size() - 1).getPriority();
            for (ACForumRaw raw : changed) {
                int priority = raw.getPriority();
                raw.setPriority(previousPriority);
                previousPriority = priority;
            }

            DB.updateACForum(changed);
            updateLazyList(false);
            notifyItemMoved(fromPosition, toPosition);

            mNeedUpdate = true;
        }
    }

    private class ForumsListener implements NMBClient.Callback<List<ACForumGroup>> {

        @Override
        public void onSuccess(List<ACForumGroup> result) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            List<ACForum> list = new LinkedList<>();
            for (ACForumGroup forumGroup : result) {
                for (ACForum forum : forumGroup.forums) {
                    list.add(forum);
                }
            }

            DB.setACForums(list);
            updateLazyList(false);
            mAdapter.notifyDataSetChanged();

            mNeedUpdate = true;
        }

        @Override
        public void onFailure(Exception e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Toast.makeText(SortForumsActivity.this, R.string.refresh_forum_list_failed, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Log.d("TAG", "ForumsListener onCancelled");
        }
    }
}
