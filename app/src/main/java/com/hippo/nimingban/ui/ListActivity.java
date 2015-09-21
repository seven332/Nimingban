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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.hippo.conaco.Conaco;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.UpdateHelper;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.DisplayForum;
import com.hippo.nimingban.client.data.DumpSite;
import com.hippo.nimingban.client.data.Forum;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.client.data.UpdateStatus;
import com.hippo.nimingban.util.Crash;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LeftDrawer;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.nimingban.widget.RightDrawer;
import com.hippo.rippleold.RippleSalon;
import com.hippo.unifile.UniFile;
import com.hippo.util.ActivityHelper;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.MarginItemDecoration;
import com.hippo.widget.slidingdrawerlayout.ActionBarDrawerToggle;
import com.hippo.widget.slidingdrawerlayout.SlidingDrawerLayout;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.SimpleHandler;

import java.io.File;
import java.util.List;

public final class ListActivity extends AbsActivity
        implements RightDrawer.OnSelectForumListener, LeftDrawer.Helper {

    private static final int BACK_PRESSED_INTERVAL = 2000;

    public static final int REQUEST_CODE_SETTINGS = 0;
    public static final int REQUEST_CODE_SORT_FORUMS = 1;

    private NMBClient mNMBClient;
    private Conaco mConaco;

    private SlidingDrawerLayout mSlidingDrawerLayout;
    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;
    private LeftDrawer mLeftDrawer;
    private RightDrawer mRightDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private MenuItem mCreatePost;
    private MenuItem mRefreshMenu;
    private MenuItem mSortForumsMenu;

    private @Nullable Forum mCurrentForum;

    private PostHelper mPostHelper;
    private PostAdapter mPostAdapter;

    private NMBRequest mNMBRequest;
    private NMBRequest mUpdateRequest;

    // Double click back exit
    private long mPressBackTime = 0;

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
        setContentView(R.layout.activity_list);

        mNMBClient = NMBApplication.getNMBClient(this);
        mConaco = NMBApplication.getConaco(this);

        mSlidingDrawerLayout = (SlidingDrawerLayout) findViewById(R.id.drawer_layout);
        mContentLayout = (ContentLayout) mSlidingDrawerLayout.findViewById(R.id.content_layout);
        mRecyclerView = mContentLayout.getRecyclerView();
        mLeftDrawer = (LeftDrawer) mSlidingDrawerLayout.findViewById(R.id.left_drawer);
        mRightDrawer = (RightDrawer) mSlidingDrawerLayout.findViewById(R.id.right_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mSlidingDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (mLeftDrawer == drawerView) {
                    super.onDrawerSlide(drawerView, slideOffset);
                }
            }

            private void setMenuItemVisible(MenuItem item, boolean visible) {
                if (item != null) {
                    item.setVisible(visible);
                }
            }

            @Override
            public void onDrawerClosed(View view) {
                if (mLeftDrawer == view) {
                    super.onDrawerClosed(view);
                }
                if (mRightDrawer == view) {
                    setMenuItemVisible(mCreatePost, true);
                    setMenuItemVisible(mRefreshMenu, true);
                    setMenuItemVisible(mSortForumsMenu, false);
                }
            }

            @Override
            public void onDrawerOpened(View view) {
                if (mLeftDrawer == view) {
                    super.onDrawerOpened(view);
                }
                if (mRightDrawer == view) {
                    setMenuItemVisible(mCreatePost, false);
                    setMenuItemVisible(mRefreshMenu, false);
                    setMenuItemVisible(mSortForumsMenu, true);
                }
            }
        };
        mSlidingDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mSlidingDrawerLayout.setDrawerShadow(ContextCompat.getDrawable(this, R.drawable.drawer_shadow_left), Gravity.LEFT);
        mSlidingDrawerLayout.setDrawerShadow(ContextCompat.getDrawable(this, R.drawable.drawer_shadow_right), Gravity.RIGHT);

        mPostHelper = new PostHelper();
        mPostHelper.setEmptyString(getString(R.string.no_post));
        mContentLayout.setHelper(mPostHelper);

        mPostAdapter = new PostAdapter();
        mRecyclerView.setAdapter(mPostAdapter);
        mRecyclerView.setSelector(RippleSalon.generateRippleDrawable(
                ResourcesUtils.getAttrBoolean(this, R.attr.dark)));
        mRecyclerView.setDrawSelectorOnTop(true);
        mRecyclerView.setOnItemClickListener(new ClickPostListener());
        RecyclerView.LayoutManager layoutManager;
        if (LayoutUtils.isTable(this)) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new LinearLayoutManager(this);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.hasFixedSize();
        mRecyclerView.setClipToPadding(false);
        int halfInterval = getResources().getDimensionPixelOffset(R.dimen.card_interval) / 2;
        mRecyclerView.addItemDecoration(new MarginItemDecoration(halfInterval));
        mRecyclerView.setPadding(halfInterval, halfInterval, halfInterval, halfInterval);

        mLeftDrawer.setHelper(this);

        mRightDrawer.setOnSelectForumListener(this);

        if (savedInstanceState == null) {
            mLeftDrawer.loadHeaderImageView();
        }

        updateForums(true);

        checkForAppStart();

        Messenger.getInstance().register(Constants.MESSENGER_ID_CREATE_POST, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        for (int i = 0, n = mRecyclerView.getChildCount(); i < n; i++) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i));
            if (holder instanceof ListHolder) {
                ((ListHolder) holder).resumeReplies();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (int i = 0, n = mRecyclerView.getChildCount(); i < n; i++) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i));
            if (holder instanceof ListHolder) {
                ((ListHolder) holder).pauseReplies();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Messenger.getInstance().unregister(Constants.MESSENGER_ID_CREATE_POST, this);

        if (mUpdateRequest != null) {
            mUpdateRequest.cancel();
            mUpdateRequest = null;
        }

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }

        for (int i = 0, n = mRecyclerView.getChildCount(); i < n; i++) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i));
            if (holder instanceof ListHolder) {
                ((ListHolder) holder).clearReplies();
            }
        }
    }

    private void checkForAppStart() {
        // Check crash
        if (Crash.hasCrashFile()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.it_is_important)
                    .setMessage(R.string.crash_tip)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String content = Crash.getCrashContent();
                            ActivityHelper.sendEmail(ListActivity.this,
                                    "hipposeven332$gmail.com".replaceAll("\\$", "@"),
                                    "I found a bug in nimingban", content);
                        }
                    }).setNegativeButton(android.R.string.cancel, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Crash.resetCrashFile();
                        }
                    })
                    .show();
        }

        // Check image save location
        UniFile uniFile = Settings.getImageSaveLocation();
        if (uniFile == null || !uniFile.ensureDir()) {
            Toast.makeText(this, R.string.cant_make_sure_image_save_location, Toast.LENGTH_SHORT).show();
        }

        // Check update
        NMBRequest request = new NMBRequest();
        mUpdateRequest = request;
        request.setMethod(NMBClient.METHOD_UPDATE);
        request.setCallback(new NMBClient.Callback<UpdateStatus>() {
            @Override
            public void onSuccess(UpdateStatus result) {
                mUpdateRequest = null;
                UpdateHelper.showUpdateDialog(ListActivity.this, result);
            }

            @Override
            public void onFailure(Exception e) {
                mUpdateRequest = null;
                e.printStackTrace();
            }

            @Override
            public void onCancel() {
                mUpdateRequest = null;
            }
        });
        mNMBClient.execute(request);

        // Check analysis
        if (!Settings.getSetAnalysis()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Settings.putSetAnalysis(true);
                    Settings.putAnalysis(which == DialogInterface.BUTTON_POSITIVE);
                }
            };
            new AlertDialog.Builder(this)
                    .setTitle(R.string.data_analysis)
                    .setMessage(R.string.data_analysis_plain)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNegativeButton(android.R.string.cancel, listener)
                    .show();
        }
    }

    private void updateForums(boolean firstTime) {
        Forum currentForum = mCurrentForum;
        List<DisplayForum> forums = DB.getACForums(true); // TODO DB.getForums
        mRightDrawer.setForums(forums);

        if (currentForum != null) {
            for (DisplayForum forum : forums) {
                if (currentForum.getNMBSite() == forum.getNMBSite() &&
                        currentForum.getNMBId().equals(forum.getNMBId())) {
                    if (!currentForum.getNMBDisplayname().equals(forum.getNMBDisplayname())) {
                        updateTitleByForum(forum);
                    }
                    mCurrentForum = forum;
                    return;
                }
            }
        }

        if (forums.size() > 0) {
            mCurrentForum = forums.get(0);
        } else {
            mCurrentForum = null;
        }

        updateTitleByForum(mCurrentForum);

        if (firstTime) {
            mPostHelper.firstRefresh();
        } else {
            mPostHelper.refresh();
        }
    }

    @Override
    public void onReceive(int id, Object obj) {
        if (Constants.MESSENGER_ID_CREATE_POST == id) {
            if (mCurrentForum != null && mCurrentForum.getNMBId().equals(obj)) {
                int currentPage = mPostHelper.getPageForBottom();
                if (currentPage == 0) {
                    mPostHelper.refresh();
                }
            }
        } else {
            super.onReceive(id, obj);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mPostAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == REQUEST_CODE_SORT_FORUMS) {
            if (resultCode == RESULT_OK) {
                updateForums(true);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (mSlidingDrawerLayout != null && (mSlidingDrawerLayout.isDrawerOpen(Gravity.LEFT) ||
                mSlidingDrawerLayout.isDrawerOpen(Gravity.RIGHT))) {
            mSlidingDrawerLayout.closeDrawers();
        } else {
            long time = System.currentTimeMillis();
            if (time - mPressBackTime > BACK_PRESSED_INTERVAL) {
                // It is the last scene
                mPressBackTime = time;
                Toast.makeText(this, R.string.press_twice_exit, Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list, menu);
        mCreatePost = menu.findItem(R.id.action_create_post);
        mRefreshMenu = menu.findItem(R.id.action_refresh);
        mSortForumsMenu = menu.findItem(R.id.action_sort_forums);

        if (mSlidingDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mCreatePost.setVisible(false);
            mRefreshMenu.setVisible(false);
            mSortForumsMenu.setVisible(true);
        } else {
            mCreatePost.setVisible(true);
            mRefreshMenu.setVisible(true);
            mSortForumsMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mSlidingDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mSlidingDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mSlidingDrawerLayout.openDrawer(Gravity.LEFT);
                }
                mSlidingDrawerLayout.closeDrawer(Gravity.RIGHT);
                return true;
            case R.id.action_create_post:
                if (mCurrentForum != null) {
                    intent = new Intent(this, TypeSendActivity.class);
                    intent.setAction(TypeSendActivity.ACTION_CREATE_POST);
                    intent.putExtra(TypeSendActivity.KEY_SITE, mCurrentForum.getNMBSite().getId());
                    intent.putExtra(TypeSendActivity.KEY_ID, mCurrentForum.getNMBId());
                    startActivity(intent);
                }
                return true;
            case R.id.action_refresh:
                mPostHelper.refresh();
                return true;
            case R.id.action_sort_forums:
                intent = new Intent(this, SortForumsActivity.class);
                intent.putExtra(SortForumsActivity.KEY_SITE, ACSite.getInstance().getId()); // TODO support other site
                startActivityForResult(intent, REQUEST_CODE_SORT_FORUMS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateTitleByForum(Forum forum) {
        if (forum != null) {
            setTitle(forum.getNMBDisplayname());
        } else {
            setTitle(getString(R.string.app_name));
        }
    }

    @Override
    public void onSelectForum(Forum forum) {
        if (mCurrentForum == null ||
                (mCurrentForum.getNMBSite() != forum.getNMBSite() ||
                        !mCurrentForum.getNMBId().equals(forum.getNMBId()))) {
            mCurrentForum = forum;
            updateTitleByForum(mCurrentForum);
            mSlidingDrawerLayout.closeDrawer(Gravity.RIGHT);
            mPostHelper.refresh();
        }
    }

    @Override
    public void OnLongClickImage(File imageFile) {
        Intent intent = new Intent(this, GalleryActivity2.class);
        intent.setAction(GalleryActivity2.ACTION_IMAGE_FILE);
        intent.putExtra(GalleryActivity2.KEY_FILE_URI, Uri.fromFile(imageFile));
        startActivity(intent);
    }

    public class SearchDialogHelper implements View.OnClickListener {
        public View mView;
        public EditText mEditText;

        public View mPositive;
        public View mNeutral;

        public Dialog mDialog;

        @SuppressLint("InflateParams")
        public SearchDialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_search, null);
            mEditText = (EditText) mView.findViewById(R.id.edit_text);
        }

        public View getView() {
            return mView;
        }

        public void setDialog(AlertDialog dialog) {
            mDialog = dialog;
            mPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            mPositive.setOnClickListener(this);
            mNeutral.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String keyword = mEditText.getText().toString().trim();
            if (TextUtils.isEmpty(keyword)) {
                Toast.makeText(ListActivity.this, R.string.keyword_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (mPositive == v) {
                Intent intent = new Intent(ListActivity.this, SearchActivity.class);
                intent.setAction(SearchActivity.ACTION_SEARCH);
                intent.putExtra(SearchActivity.KEY_KEYWORD, keyword);
                startActivity(intent);
                mDialog.dismiss();
            } else if (mNeutral == v) {
                Intent intent = new Intent(ListActivity.this, PostActivity.class);
                intent.setAction(PostActivity.ACTION_SITE_ID);
                intent.putExtra(PostActivity.KEY_SITE, ACSite.getInstance().getId());
                intent.putExtra(PostActivity.KEY_ID, keyword);
                startActivity(intent);
                mDialog.dismiss();
            }
        }
    }

    @Override
    public void onClickSearch() {
        SearchDialogHelper helper = new SearchDialogHelper();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.search)
                .setView(helper.getView())
                .setPositiveButton(R.string.search, null)
                .setNeutralButton(R.string.go_to_post, null)
                .show();
        helper.setDialog(dialog);
    }

    @Override
    public void onClickFeed() {
        Intent intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickRecord() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

    private class ClickPostListener implements EasyRecyclerView.OnItemClickListener {

        @Override
        public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
            Intent intent = new Intent(ListActivity.this, PostActivity.class);
            intent.setAction(PostActivity.ACTION_POST);
            intent.putExtra(PostActivity.KEY_POST, mPostHelper.getDataAt(position));
            startActivity(intent);
            return true;
        }
    }

    private class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            ViewSwitcher.ViewFactory, Runnable {

        private Handler mHandler;

        public TextView leftText;
        public TextView centerText;
        public TextView rightText;
        public TextView content;
        public TextView bottomText;
        public LoadImageView thumb;
        public TextSwitcher reply;

        private int mShowIndex;
        private Reply[] mReplies;

        private boolean mRunning = false;

        public ListHolder(View itemView) {
            super(itemView);

            mHandler = SimpleHandler.getInstance();

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            centerText = (TextView) itemView.findViewById(R.id.center_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (TextView) itemView.findViewById(R.id.content);
            bottomText = (TextView) itemView.findViewById(R.id.bottom_text);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
            reply = (TextSwitcher) itemView.findViewById(R.id.reply);

            thumb.setOnClickListener(this);

            reply.setFactory(this);

            Drawable drawable = VectorDrawable.create(ListActivity.this, R.drawable.ic_comment_multiple_outline);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            bottomText.setCompoundDrawables(drawable, null, null, null);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mPostHelper.size()) {
                Post post = mPostHelper.getDataAt(position);
                String image = post.getNMBImageUrl();
                if (!TextUtils.isEmpty(image)) {
                    Intent intent = new Intent(ListActivity.this, GalleryActivity2.class);
                    intent.setAction(GalleryActivity2.ACTION_SINGLE_IMAGE);
                    intent.putExtra(GalleryActivity2.KEY_SITE, post.getNMBSite().getId());
                    intent.putExtra(GalleryActivity2.KEY_ID, post.getNMBId());
                    intent.putExtra(GalleryActivity2.KEY_IMAGE, image);
                    ListActivity.this.startActivity(intent);
                }
            }
        }

        public long getReplyInterval() {
            return MathUtils.random(4000, 6000);
        }

        public void setReplies(Reply[] replies) {
            mHandler.removeCallbacks(this);
            mRunning = false;

            if (replies == null || replies.length == 0) {
                mReplies = null;
                reply.setVisibility(View.GONE);
            } else {
                mReplies = replies;
                mShowIndex = 0;
                reply.setVisibility(View.VISIBLE);
                reply.setText(replies[0].getNMBDisplayContent());

                if (replies.length > 1 && !mRunning) {
                    mRunning = true;
                    mHandler.postDelayed(this, getReplyInterval());
                }
            }
        }

        public void resumeReplies() {
            if (mReplies != null && mReplies.length > 1 && !mRunning) {
                mRunning = true;
                mHandler.postDelayed(this, getReplyInterval());
            }
        }

        public void pauseReplies() {
            if (mReplies != null && mReplies.length > 1) {
                mRunning = false;
                mHandler.removeCallbacks(this);
            }
        }

        public void clearReplies() {
            mRunning = false;
            mHandler.removeCallbacks(this);
            mReplies = null;
        }

        @Override
        public View makeView() {
            return getLayoutInflater().inflate(R.layout.item_list_reply, reply, false);
        }

        @Override
        public void run() {
            if (mReplies == null) {
                return;
            }

            mShowIndex = (mShowIndex + 1) % mReplies.length;
            reply.setText(mReplies[mShowIndex].getNMBDisplayContent());

            if (mRunning) {
                mHandler.postDelayed(this, getReplyInterval());
            }
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<ListHolder> {

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListHolder(getLayoutInflater().inflate(R.layout.item_list, parent, false));
        }

        @Override
        public void onBindViewHolder(ListHolder holder, int position) {
            Post post = mPostHelper.getDataAt(position);
            holder.leftText.setText(post.getNMBDisplayUsername());
            holder.centerText.setText("No." + post.getNMBId());
            holder.rightText.setText(ReadableTime.getDisplayTime(post.getNMBTime()));
            holder.content.setText(post.getNMBDisplayContent());
            holder.bottomText.setText(post.getNMBReplyDisplayCount());

            TextView bottomText = holder.bottomText;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bottomText.getLayoutParams();
            String thumbUrl = post.getNMBThumbUrl();

            boolean tryShowImage;
            boolean loadFromNetwork;
            int ils = Settings.getImageLoadingStrategy();
            if (ils == Settings.IMAGE_LOADING_STRATEGY_ALL ||
                    (ils == Settings.IMAGE_LOADING_STRATEGY_WIFI && NMBApplication.isConnectedWifi(ListActivity.this))) {
                tryShowImage = true;
                loadFromNetwork = true;
            } else {
                tryShowImage = Settings.getImageLoadingStrategy2();
                loadFromNetwork = false;
            }

            boolean showImage;
            if (!TextUtils.isEmpty(thumbUrl) && tryShowImage) {
                showImage = true;

                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.unload();
                holder.thumb.load(thumbUrl, thumbUrl, loadFromNetwork);
            } else {
                showImage = false;

                holder.thumb.setVisibility(View.GONE);
                holder.thumb.unload();
            }

            Reply[] replies = post.getNMBReplies();

            holder.setReplies(replies);

            if (showImage && replies.length == 0) {
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.thumb);
                lp.addRule(RelativeLayout.BELOW, 0);
                bottomText.setLayoutParams(lp);
            } else if (replies.length > 0) {
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
                lp.addRule(RelativeLayout.BELOW, R.id.reply);
                bottomText.setLayoutParams(lp);
            } else {
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
                lp.addRule(RelativeLayout.BELOW, R.id.content);
                bottomText.setLayoutParams(lp);
            }

            holder.content.setTextSize(Settings.getFontSize());
            holder.content.setLineSpacing(LayoutUtils.dp2pix(ListActivity.this, Settings.getLineSpacing()), 1.0f);
        }

        @Override
        public int getItemCount() {
            return mPostHelper.size();
        }

        @Override
        public void onViewAttachedToWindow(ListHolder holder) {
            holder.resumeReplies();
        }

        @Override
        public void onViewDetachedFromWindow(ListHolder holder) {
            holder.pauseReplies();
        }
    }

    private class PostHelper extends ContentLayout.ContentHelper<Post> {

        @Override
        protected Context getContext() {
            return ListActivity.this;
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
            mPostAdapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mPostAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            mPostAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        protected void getPageData(int taskId, int type, int page) {
            if (mNMBRequest != null) {
                mNMBRequest.cancel();
                mNMBRequest = null;
            }

            if (mCurrentForum == null) {
                onGetExpection(taskId, new NMBException(DumpSite.getInstance(), getString(R.string.no_forum)));
            } else {
                NMBRequest request = new NMBRequest();
                mNMBRequest = request;
                request.setSite(mCurrentForum.getNMBSite());
                request.setMethod(NMBClient.METHOD_GET_POST_LIST);
                request.setArgs(mCurrentForum.getNMBId(), page);
                request.setCallback(new ListListener(taskId, page, request));
                mNMBClient.execute(request);
            }
        }
    }

    private class ListListener implements NMBClient.Callback<List<Post>> {

        private int mTaskId;
        private int mTaskPage;
        private NMBRequest mRequest;

        public ListListener(int taskId, int taskPage, NMBRequest request) {
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
                    mPostHelper.setPages(mTaskPage);
                    mPostHelper.onGetEmptyData(mTaskId);
                } else {
                    mPostHelper.setPages(Integer.MAX_VALUE);
                    mPostHelper.onGetPageData(mTaskId, result);
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

                mPostHelper.onGetExpection(mTaskId, e);
            }
            // Clear
            mRequest = null;
        }

        @Override
        public void onCancel() {
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
