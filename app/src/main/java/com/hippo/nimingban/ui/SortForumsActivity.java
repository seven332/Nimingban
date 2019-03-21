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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

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
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.effect.ViewTransition;
import com.hippo.nimingban.GuideHelper;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.dao.ACForumRaw;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.ForumAutoSortingUtils;
import com.hippo.nimingban.util.Settings;
import com.hippo.text.Html;
import com.hippo.util.DrawableManager;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.LazyList;

public class SortForumsActivity extends TranslucentActivity {

    public static final String KEY_SITE = "site";

    private Site mSite;

    private boolean mAutoSorting = Settings.getForumAutoSorting();

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

    private NMBRequest mNMBRequest;

    // TODO support other site
    private LazyList<ACForumRaw> mLazyList;
    @SuppressLint("UseSparseArrays")
    private Map<Integer, CharSequence> mForumNames = new HashMap<>();

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
        } else {
            // Eye guide was shown
            tryShowSecondGuide();
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
                        tryShowSecondGuide();
                        Settings.putGuideSortForumsActivity(false);
                    }
                }).show();
    }

    private void tryShowSecondGuide() {
        if (Settings.getGuidePinningStar() && mAutoSorting) {
            showStarGuide();
        } else if (Settings.getGuideSortingFourBars() && !mAutoSorting) {
            showFourBarsGuide();
        }
    }

    private void showFourBarsGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.RIGHT)
                .setMessage(getString(R.string.drag_four_bars_exchange))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Settings.putGuideSortingFourBars(false);
                    }
                }).show();
    }

    private void showStarGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setPaddingTop(LayoutUtils.dp2pix(this, 56))
                .setPaddingBottom(LayoutUtils.dp2pix(this, 56))
                .setMessagePosition(Gravity.RIGHT)
                .setMessage(getString(R.string.click_star_icon_pin))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Settings.putGuidePinningStar(false);
                    }
                }).show();
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
            mForumNames.clear();
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
        LazyList<ACForumRaw> lazyList = DB.getACForumLazyList(mAutoSorting);
        if (mLazyList != null) {
            mLazyList.close();
            mForumNames.clear();
        }
        mLazyList = lazyList;

        mViewTransition.showView(mLazyList.isEmpty() ? 0 : 1, animation);
    }

    private static final int TYPE_FORUM = 0;
    private static final int TYPE_SWITCH = 1;

    private class ForumHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        public View swipeHandler;
        public ImageView visibility;
        public Switch autoSortingSwitch;
        public TextView forum;
        public View dragHandler;
        public ImageView pinning;

        public ForumHolder(View itemView, int viewType) {
            super(itemView);

            swipeHandler = itemView.findViewById(R.id.swipe_handler);
            visibility = (ImageView) itemView.findViewById(R.id.visibility);
            autoSortingSwitch = itemView.findViewById(R.id.auto_sorting_switch);
            forum = (TextView) itemView.findViewById(R.id.forum);
            dragHandler = itemView.findViewById(R.id.drag_handler);
            pinning = itemView.findViewById(R.id.pinning);

            if (viewType == TYPE_FORUM) {
                autoSortingSwitch.setVisibility(View.GONE);

                visibility.setOnClickListener(this);
                forum.setOnClickListener(this);
                pinning.setOnClickListener(this);

                StateListDrawable visibilityDrawable = new StateListDrawable();
                visibilityDrawable.addState(new int[]{android.R.attr.state_activated},
                        DrawableManager.getDrawable(SortForumsActivity.this, R.drawable.v_eye_on_x24));
                visibilityDrawable.addState(new int[]{},
                        DrawableManager.getDrawable(SortForumsActivity.this, R.drawable.v_eye_off_x24));
                visibility.setImageDrawable(visibilityDrawable);

                StateListDrawable pinningDrawable = new StateListDrawable();
                pinningDrawable.addState(new int[]{android.R.attr.state_activated},
                        DrawableManager.getDrawable(SortForumsActivity.this, R.drawable.v_star_x24));
                pinningDrawable.addState(new int[]{},
                        DrawableManager.getDrawable(SortForumsActivity.this, R.drawable.v_star_border_x24));
                pinning.setImageDrawable(pinningDrawable);
            } else if (viewType == TYPE_SWITCH) {
                visibility.setVisibility(View.GONE);
                dragHandler.setVisibility(View.GONE);
                pinning.setVisibility(View.GONE);

                autoSortingSwitch.setChecked(mAutoSorting);
                forum.setText(R.string.main_forum_auto_sorting);

                autoSortingSwitch.setOnCheckedChangeListener(this);
                forum.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(@NonNull View v) {
            int position = getAdapterPosition();
            if (position > 0 && position <= mLazyList.size()) {
                if (visibility == v) {
                    ACForumRaw raw = mLazyList.get(position - 1);
                    DB.setACForumVisibility(raw, !raw.getVisibility());

                    // Update UI
                    visibility.setActivated(raw.getVisibility());

                    mNeedUpdate = true;
                } else if (pinning == v) {
                    List<ACForumRaw> changed = new ArrayList<>();
                    ACForumRaw clicked = mLazyList.get(position - 1);
                    boolean isClickedPinned = ForumAutoSortingUtils.isACForumPinned(clicked);

                    ACForumRaw first = mLazyList.get(0);
                    if (!isClickedPinned && ForumAutoSortingUtils.isACForumPinned(first)) {
                        // unpin the first if it's been pinned already
                        ForumAutoSortingUtils.unpinACForum(first);
                        changed.add(first);
                    }

                    if (!isClickedPinned) {
                        ForumAutoSortingUtils.pinACForum(clicked);
                    } else {
                        ForumAutoSortingUtils.unpinACForum(clicked);
                    }
                    changed.add(clicked);

                    DB.updateACForum(changed);
                    updateLazyList(false);
                    mNeedUpdate = true;

                    // don't know where the previous top will go, so update all the list
                    mAdapter.notifyDataSetChanged();
                }
            } else if (position == 0) {
                if (forum == v) {
                    autoSortingSwitch.performClick();
                }
            }
        }

        @Override
        public View getSwipeableContainerView() {
            return swipeHandler;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (autoSortingSwitch == buttonView) {
                mAutoSorting = isChecked;
                Settings.putForumAutoSorting(isChecked);

                // update the list to sort them according to the present setting
                updateLazyList(false);
                // the list could be dramatically different so update them all
                mAdapter.notifyDataSetChanged();

                // show guide
                tryShowSecondGuide();

                mNeedUpdate = true;
            }
        }
    }

    private class ForumAdapter extends RecyclerView.Adapter<ForumHolder>
            implements DraggableItemAdapter<ForumHolder>,
            SwipeableItemAdapter<ForumHolder> {

        private static final long ID_SWITCH = -0xffL;

        @Override
        public ForumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ForumHolder(getLayoutInflater().inflate(R.layout.item_forum_sort, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(ForumHolder holder, int position) {
            if (position > 0) {
                ACForumRaw raw = mLazyList.get(position - 1);
                holder.visibility.setActivated(raw.getVisibility());
                if (mAutoSorting) {
                    holder.dragHandler.setVisibility(View.GONE);
                    holder.pinning.setVisibility(View.VISIBLE);
                    holder.pinning.setActivated(ForumAutoSortingUtils.isACForumPinned(raw));
                } else {
                    holder.pinning.setVisibility(View.GONE);
                    holder.dragHandler.setVisibility(View.VISIBLE);
                }

                CharSequence name = mForumNames.get(position);
                if (name == null) {
                    if (raw.getDisplayname() == null) {
                        name = "Forum";
                    } else {
                        name = Html.fromHtml(raw.getDisplayname());
                    }
                    mForumNames.put(position, name);
                }
                holder.forum.setText(name);
            }
        }

        @Override
        public long getItemId(int position) {
            if (position > 0) {
                return mLazyList.get(position - 1).getId();
            } else {
                return ID_SWITCH;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_SWITCH;
            } else {
                return TYPE_FORUM;
            }
        }

        @Override
        public int getItemCount() {
            return mLazyList.size() + 1;
        }

        @Override
        public boolean onCheckCanStartDrag(ForumHolder holder, int position, int x, int y) {
            return position != 0 && ViewUtils.isViewUnder(holder.dragHandler, x, y, 0);
        }

        @Override
        public ItemDraggableRange onGetItemDraggableRange(ForumHolder holder, int position) {
            return new ItemDraggableRange(1, mLazyList.size());
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            if (fromPosition == toPosition) {
                return;
            }

            List<ACForumRaw> changed = new ArrayList<>(Math.abs(fromPosition - toPosition) + 1);
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i <= toPosition; i++) {
                    changed.add(mLazyList.get(i - 1));
                }
            } else {
                for (int i = fromPosition; i >= toPosition; i--) {
                    changed.add(mLazyList.get(i - 1));
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
            if (position == 0 || mLazyList.get(position - 1).getOfficial()) {
                return SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY;
            } else {
                return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
            }
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
            final ACForumRaw raw = mLazyList.get(position - 1);
            if (raw != null) {
                DB.removeACForum(mLazyList.get(position - 1));
                updateLazyList(true);
                mAdapter.notifyItemRemoved(position);
                mNeedUpdate = true;
            }
        }
    }
}
