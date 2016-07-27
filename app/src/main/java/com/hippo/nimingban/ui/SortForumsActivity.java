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
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.hippo.app.ProgressDialogBuilder;
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.effect.ViewTransition;
import com.hippo.nimingban.GuideHelper;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.ac.data.ACForum;
import com.hippo.nimingban.client.ac.data.ACForumGroup;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.dao.ACForumRaw;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.Settings;
import com.hippo.util.DrawableManager;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.ViewUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.dao.query.LazyList;

public class SortForumsActivity extends TranslucentActivity {

    public static final String KEY_SITE = "site";

    private Site mSite;

    private View mTip;
    private EasyRecyclerView mRecyclerView;
    private ViewTransition mViewTransition;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
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
        return Settings.getColorStatusBar() ? R.style.NormalActivity : R.style.NormalActivity_NoStatus;
    }

    @Override
    protected int getDarkThemeResId() {
        return Settings.getColorStatusBar() ? R.style.NormalActivity_Dark : R.style.NormalActivity_Dark_NoStatus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!handlerIntent(getIntent())) {
            finish();
            return;
        }

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        ToolbarActivityHelper.setContentView(this, R.layout.activity_forum_sort);
        setActionBarUpIndicator(DrawableManager.getDrawable(this, R.drawable.v_arrow_left_dark_x24));

        mTip = findViewById(R.id.tip);
        mRecyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);
        mViewTransition = new ViewTransition(mTip, mRecyclerView);

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

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        mAdapter = new ForumAdapter();
        mAdapter.setHasStableIds(true);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);      // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping

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
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        updateLazyList(false);

        if (Settings.getGuideSortForumsActivity()) {
            showEyeGuide();
        }
    }

    private void showEyeGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.LEFT)
                .setMessage(getString(R.string.click_eye_icon))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showModifyGuide();
                    }
                }).show();
    }

    private void showModifyGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.CENTER)
                .setMessage(getString(R.string.modify_forum_title))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSwipeGuide();
                    }
                }).show();
    }

    private void showSwipeGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.CENTER)
                .setMessage(getString(R.string.swipe_forum_remove))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showGreenBlockGuide();
                    }
                }).show();
    }

    private void showGreenBlockGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.RIGHT)
                .setMessage(getString(R.string.drag_green_block_exchange))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRefreshGuide();
                    }
                }).show();
    }

    @SuppressWarnings("WrongConstant")
    private void showRefreshGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.RIGHT | Gravity.TOP)
                .setMessage(getString(R.string.click_refresh_icon_update_list))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAddGuide();
                    }
                }).show();
    }

    @SuppressWarnings("WrongConstant")
    private void showAddGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.RIGHT | Gravity.TOP)
                .setMessage(getString(R.string.click_add_icon_add_forum))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Settings.putGuideSortForumsActivity(false);
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forum_sort, menu);
        return true;
    }

    private class ForumDialogHelper implements View.OnClickListener {

        private ACForumRaw mRaw;

        private View mView;

        private EditText mName;
        private EditText mId;

        private Dialog mDialog;
        private Button mButton;

        public ForumDialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_forum, null);
            mName = (EditText) mView.findViewById(R.id.name);
            mId = (EditText) mView.findViewById(R.id.id);
        }

        public ForumDialogHelper(ACForumRaw raw) {
            mRaw = raw;

            mView = getLayoutInflater().inflate(R.layout.dialog_forum, null);
            mName = (EditText) mView.findViewById(R.id.name);
            mId = (EditText) mView.findViewById(R.id.id);

            mName.setText(mRaw.getDisplayname());
            mId.setText(mRaw.getForumid());
            mId.setEnabled(false);
        }

        public View getView() {
            return mView;
        }

        public void setDialog(AlertDialog dialog) {
            mDialog = dialog;
            mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            mButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mButton == v) {
                String name = mName.getText().toString();
                String id = mId.getText().toString();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(id)) {
                    Toast.makeText(SortForumsActivity.this, R.string.name_id_cant_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mRaw == null) {
                    ACForumRaw raw = DB.getACForumForForumid(id);
                    if (raw != null) {
                        Toast.makeText(SortForumsActivity.this,
                                getString(R.string.forum_id_exists, raw.getDisplayname()), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DB.addACForums(name, id);
                } else {
                    mRaw.setDisplayname(name);
                    mRaw.setForumid(id);
                    DB.updateACForum(mRaw);
                }

                updateLazyList(false);
                mAdapter.notifyDataSetChanged();
                mNeedUpdate = true;
                mDialog.dismiss();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh: {
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
            }
            case R.id.action_add: {
                ForumDialogHelper helper = new ForumDialogHelper();
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.add_forum)
                        .setView(helper.getView())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                helper.setDialog(dialog);
                return true;
            }
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

    private class ForumHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener {

        public View swipeHandler;
        public ImageView visibility;
        public TextView forum;
        public View dragHandler;

        public ForumHolder(View itemView) {
            super(itemView);

            swipeHandler = itemView.findViewById(R.id.swipe_handler);
            visibility = (ImageView) itemView.findViewById(R.id.visibility);
            forum = (TextView) itemView.findViewById(R.id.forum);
            dragHandler = itemView.findViewById(R.id.drag_handler);

            visibility.setOnClickListener(this);
            forum.setOnClickListener(this);

            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_activated},
                    DrawableManager.getDrawable(SortForumsActivity.this, R.drawable.v_eye_on_x24));
            drawable.addState(new int[]{},
                    DrawableManager.getDrawable(SortForumsActivity.this, R.drawable.v_eye_off_x24));
            visibility.setImageDrawable(drawable);
        }

        @Override
        public void onClick(@NonNull View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mLazyList.size()) {
                if (visibility == v) {
                    ACForumRaw raw = mLazyList.get(position);
                    DB.setACForumVisibility(raw, !raw.getVisibility());

                    // Update UI
                    visibility.setActivated(raw.getVisibility());

                    mNeedUpdate = true;
                } else if (forum == v) {
                    ACForumRaw raw = mLazyList.get(position);
                    ForumDialogHelper helper = new ForumDialogHelper(raw);
                    AlertDialog dialog = new AlertDialog.Builder(SortForumsActivity.this)
                            .setTitle(R.string.modify_forum)
                            .setView(helper.getView())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    helper.setDialog(dialog);
                }
            }
        }

        @Override
        public View getSwipeableContainerView() {
            return swipeHandler;
        }
    }

    private class ForumAdapter extends RecyclerView.Adapter<ForumHolder>
            implements DraggableItemAdapter<ForumHolder>,
            SwipeableItemAdapter<ForumHolder> {

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

        @Override
        public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
            return true;
        }

        @Override
        public int onGetSwipeReactionType(ForumHolder holder, int position, int x, int y) {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
        }

        @Override
        public void onSetSwipeBackground(ForumHolder holder, int position, int type) {
            // Empty
        }

        @Override
        public void onSwipeItemStarted(ForumHolder holder, int position) {
            // Empty
        }

        @Override
        public SwipeResultAction onSwipeItem(ForumHolder holder, int position, int result) {
            switch (result) {
                // swipe right
                case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
                case SwipeableItemConstants.RESULT_SWIPED_LEFT:
                    return new DeleteAction(position);
                case SwipeableItemConstants.RESULT_CANCELED:
                default:
                    return null;
            }
        }
    }

    private class DeleteAction extends SwipeResultActionRemoveItem {

        private int mPosition;

        public DeleteAction(int position) {
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            final int position = mPosition;
            final ACForumRaw raw = mLazyList.get(position);
            if (raw != null) {
                DB.removeACForum(mLazyList.get(position));
                updateLazyList(true);
                mAdapter.notifyItemRemoved(position);
                mNeedUpdate = true;
            }
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

            DB.addACForums(list);
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
        public void onCancel() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Log.d("TAG", "ForumsListener onCancelled");
        }
    }
}
