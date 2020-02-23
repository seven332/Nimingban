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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.hippo.app.CheckBoxDialogBuilder;
import com.hippo.drawerlayout.DrawerLayout;
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.easyrecyclerview.MarginItemDecoration;
import com.hippo.easyrecyclerview.RawMarginItemDecoration;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.GuideHelper;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.PermissionRequester;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBException;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.Notice;
import com.hippo.nimingban.client.UpdateHelper;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.ac.data.ACItemUtils;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.CommonPost;
import com.hippo.nimingban.client.data.DisplayForum;
import com.hippo.nimingban.client.data.DumpSite;
import com.hippo.nimingban.client.data.Forum;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.client.data.UpdateStatus;
import com.hippo.nimingban.dao.ACForumRaw;
import com.hippo.nimingban.util.Crash;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.ForumAutoSortingUtils;
import com.hippo.nimingban.util.LinkMovementMethod2;
import com.hippo.nimingban.util.PostIgnoreUtils;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.FontTextView;
import com.hippo.nimingban.widget.LeftDrawer;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.nimingban.widget.MarqueeReplyView;
import com.hippo.nimingban.widget.RightDrawer;
import com.hippo.ripple.Ripple;
import com.hippo.text.Html;
import com.hippo.text.URLImageGetter;
import com.hippo.unifile.UniFile;
import com.hippo.util.ActivityHelper;
import com.hippo.util.DrawableManager;
import com.hippo.view.SimpleDoubleTapListener;
import com.hippo.view.SimpleGestureListener;
import com.hippo.widget.AutoWrapLayout;
import com.hippo.widget.drawerlayout.ActionBarDrawerToggle;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ObjectUtils;
import com.hippo.yorozuya.ResourcesUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public final class ListActivity extends AbsActivity
        implements RightDrawer.RightDrawerHelper, LeftDrawer.Helper {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private static final int BACK_PRESSED_INTERVAL = 2000;

    public static final int REQUEST_CODE_SETTINGS = 0;
    public static final int REQUEST_CODE_SORT_FORUMS = 1;

    public static final int RESULT_CODE_REFRESH = 10;

    private NMBClient mNMBClient;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;
    private LeftDrawer mLeftDrawer;
    private RightDrawer mRightDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private MenuItem mRule;
    private MenuItem mNotice;
    private MenuItem mCreatePost;
    private MenuItem mSortForumsMenu;

    private @Nullable Forum mCurrentForum;

    private PostHelper mPostHelper;
    private PostAdapter mPostAdapter;
    private RecyclerView.OnScrollListener mOnScrollListener;

    private NMBRequest mNMBRequest;
    private NMBRequest mUpdateRequest;
    private NMBRequest mCommonPostsRequest;
    private NMBRequest mNoticeRequest;

    // Double click back exit
    private long mPressBackTime = 0;

    private List<WeakReference<ListHolder>> mListHolderList = new LinkedList<>();

    private Map<String, CharSequence> mForumNames = new HashMap<>();

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
        setContentView(R.layout.activity_list);

        mNMBClient = NMBApplication.getNMBClient(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mContentLayout = (ContentLayout) mDrawerLayout.findViewById(R.id.content_layout);
        mRecyclerView = mContentLayout.getRecyclerView();
        mLeftDrawer = (LeftDrawer) mDrawerLayout.findViewById(R.id.left_drawer);
        mRightDrawer = (RightDrawer) mDrawerLayout.findViewById(R.id.right_drawer);

        final GestureDetector gestureDetector = new GestureDetector(this, new SimpleGestureListener());
        gestureDetector.setOnDoubleTapListener(new SimpleDoubleTapListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mPostHelper.refresh();
                return true;
            }
        });
        mToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        setSupportActionBar(mToolbar);
        // I like hardcode
        mToolbar.setSubtitle("A岛·adnmb.com");

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
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
                    setMenuItemVisible(mRule, true);
                    setMenuItemVisible(mNotice, true);
                    setMenuItemVisible(mCreatePost, true);
                    setMenuItemVisible(mSortForumsMenu, false);
                }
            }

            @Override
            public void onDrawerOpened(View view) {
                if (mLeftDrawer == view) {
                    super.onDrawerOpened(view);
                }
                if (mRightDrawer == view) {
                    setMenuItemVisible(mRule, false);
                    setMenuItemVisible(mNotice, false);
                    setMenuItemVisible(mCreatePost, false);
                    setMenuItemVisible(mSortForumsMenu, true);
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mDrawerLayout.setDrawerShadow(ContextCompat.getDrawable(this, R.drawable.drawer_shadow_left), Gravity.LEFT);
        mDrawerLayout.setDrawerShadow(ContextCompat.getDrawable(this, R.drawable.drawer_shadow_right), Gravity.RIGHT);
        mDrawerLayout.setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));

        mPostHelper = new PostHelper();
        mPostHelper.setEmptyString(getString(R.string.no_post));
        mContentLayout.setHelper(mPostHelper);
        if (Settings.getFastScroller()) {
            mContentLayout.showFastScroll();
        } else {
            mContentLayout.hideFastScroll();
        }

        mPostAdapter = new PostAdapter();
        mRecyclerView.setAdapter(mPostAdapter);
        mRecyclerView.setSelector(Ripple.generateRippleDrawable(
                this, ResourcesUtils.getAttrBoolean(this, R.attr.dark)));
        mRecyclerView.setDrawSelectorOnTop(true);
        mRecyclerView.setOnItemClickListener(new ClickPostListener());
        mRecyclerView.setOnItemLongClickListener(new EasyRecyclerView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(EasyRecyclerView parent, View view, int position, long id) {
                showIgnorePostDialog(position);
                return true;
            }
        });
        mRecyclerView.hasFixedSize();
        mRecyclerView.setClipToPadding(false);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    pauseHolders();
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    resumeHolders();
                }
            }
        };
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        int interval = getResources().getDimensionPixelOffset(R.dimen.card_interval);
        if (getResources().getBoolean(R.bool.two_way)) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            MarginItemDecoration decoration = new MarginItemDecoration(interval, interval, interval, interval, interval);
            mRecyclerView.addItemDecoration(decoration);
            decoration.applyPaddings(mRecyclerView);
            mRecyclerView.setItemAnimator(new SlideInUpAnimator());
        } else {
            int halfInterval = interval / 2;
            mRecyclerView.addItemDecoration(new RawMarginItemDecoration(0, halfInterval, 0, halfInterval));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setPadding(0, halfInterval, 0, halfInterval);
        }

        mLeftDrawer.setHelper(this);

        mRightDrawer.setRightDrawerHelper(this);

        if (savedInstanceState == null) {
            int ils = Settings.getImageLoadingStrategy();
            if (ils == Settings.IMAGE_LOADING_STRATEGY_ALL ||
                    (ils == Settings.IMAGE_LOADING_STRATEGY_WIFI && NMBApplication.isConnectedWifi(ListActivity.this))) {
                mLeftDrawer.loadHeaderImageView();
            }
        }

        updateForums(true);

        checkForAppStart();

        Messenger.getInstance().register(Constants.MESSENGER_ID_CREATE_POST, this);
        Messenger.getInstance().register(Constants.MESSENGER_ID_FAST_SCROLLER, this);

        // Check permission
        PermissionRequester.request(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.write_storage_permission_tip), PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);

        if (Settings.getGuideListActivity()) {
            showLeftDrawerGuide();
        }
    }

    private void showLeftDrawerGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setMessagePosition(Gravity.LEFT)
                .setMessage(getString(R.string.swipe_right_open_menu))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRightDrawerGuide();
                    }
                }).show();
    }

    private void showRightDrawerGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setMessagePosition(Gravity.RIGHT)
                .setMessage(getString(R.string.swipe_left_open_forum_list))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showToolbarGuide();
                    }
                }).show();
    }

    private void showToolbarGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setMessagePosition(Gravity.TOP)
                .setMessage(getString(R.string.double_click_toolbar_refresh))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Settings.putGuideListActivity(false);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.you_rejected_me, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void resumeHolders() {
        Iterator<WeakReference<ListHolder>> iterator = mListHolderList.iterator();
        while (iterator.hasNext()) {
            ListHolder holder = iterator.next().get();
            if (holder != null) {
                // Only resume attached view holder
                if (holder.itemView.getParent() != null) {
                    holder.thumb.start();
                }
            } else {
                iterator.remove();
            }
        }
    }

    private void showIgnorePostDialog(final int position) {
        final Post post = mPostHelper.getDataAt(position);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Intent intent = new Intent(ListActivity.this, TypeSendActivity.class);
                        intent.setAction(TypeSendActivity.ACTION_REPORT);
                        intent.putExtra(TypeSendActivity.KEY_SITE, mCurrentForum.getNMBSite().getId());
                        intent.putExtra(TypeSendActivity.KEY_ID, mCurrentForum.getNMBSite().getReportForumId());
                        intent.putExtra(TypeSendActivity.KEY_TEXT, ">>No." + post.getNMBPostId() + "\n");
                        startActivity(intent);
                        break;
                    case 1:
                        new AlertDialog.Builder(ListActivity.this)
                                .setTitle(R.string.ignore_post_confirm_title)
                                .setMessage(Settings.getEnableStrictIgnoreMode() ? R.string.ignore_post_confirm_message_strict : R.string.ignore_post_confirm_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        PostIgnoreUtils.INSTANCE.putIgnoredPost(post.getNMBPostId());
                                        mPostHelper.removeAt(position);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                        break;
                }
            }
        };

        new AlertDialog.Builder(ListActivity.this)
                .setTitle("No." + post.getNMBPostId()).setItems(R.array.post_dialog, listener).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeHolders();
    }

    private void pauseHolders() {
        Iterator<WeakReference<ListHolder>> iterator = mListHolderList.iterator();
        while (iterator.hasNext()) {
            ListHolder holder = iterator.next().get();
            if (holder != null) {
                holder.thumb.stop();
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseHolders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Messenger.getInstance().unregister(Constants.MESSENGER_ID_CREATE_POST, this);
        Messenger.getInstance().unregister(Constants.MESSENGER_ID_FAST_SCROLLER, this);

        if (mUpdateRequest != null) {
            mUpdateRequest.cancel();
            mUpdateRequest = null;
        }

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }

        if (mCommonPostsRequest != null) {
            mCommonPostsRequest.cancel();
            mCommonPostsRequest = null;
        }

        if (mNoticeRequest != null) {
            mNoticeRequest.cancel();
            mNoticeRequest = null;
        }

        mRecyclerView.removeOnScrollListener(mOnScrollListener);

        for (WeakReference<ListHolder> ref : mListHolderList) {
            ListHolder holder = ref.get();
            if (holder != null) {
                holder.thumb.unload();
            }
        }
        mListHolderList.clear();
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

        // Check analysis
        if (!Settings.getSetAnalysis()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Settings.putSetAnalysis(true);
                    Settings.putAnalysis(which == DialogInterface.BUTTON_POSITIVE);
                }
            };

            try {
                CharSequence message = Html.fromHtml(IOUtils.readString(
                        getResources().openRawResource(R.raw.analysis_plain), "UTF-8"));
                Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.data_analysis)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.agree, listener)
                        .setNegativeButton(R.string.disagree, listener)
                        .show();
                TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setMovementMethod(new LinkMovementMethod2(ListActivity.this));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        NMBRequest request;

        // Check update
        request = new NMBRequest();
        mUpdateRequest = request;
        request.setMethod(NMBClient.METHOD_UPDATE);
        request.setCallback(new NMBClient.Callback<UpdateStatus>() {
            @Override
            public void onSuccess(UpdateStatus result) {
                Log.d("UpdateStatus", ObjectUtils.toString(result));

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

        // Get common post
        request = new NMBRequest();
        mCommonPostsRequest = request;
        request.setSite(ACSite.getInstance());
        request.setMethod(NMBClient.METHOD_COMMON_POSTS);
        request.setCallback(new NMBClient.Callback<List<CommonPost>>() {
            @Override
            public void onSuccess(List<CommonPost> result) {
                mCommonPostsRequest = null;
                DB.setACCommonPost(result);
            }

            @Override
            public void onFailure(Exception e) {
                mCommonPostsRequest = null;
                e.printStackTrace();
            }

            @Override
            public void onCancel() {
                mCommonPostsRequest = null;
            }
        });
        mNMBClient.execute(request);

        // Get Notice
        getNotice(false);
    }

    private void getNotice(final boolean forceDisplay) {
        NMBRequest request = new NMBRequest();
        mNoticeRequest = request;
        request.setSite(ACSite.getInstance());
        request.setMethod(NMBClient.METHOD_NOTICE);
        request.setCallback(new NMBClient.Callback<Notice>() {
            @Override
            public void onSuccess(Notice result) {
                mNoticeRequest = null;

                if (isFinishing()) {
                    return;
                }
                if (!result.enable) {
                    return;
                }
                long oldDate = Settings.getNoticeDate();
                final long newDate = result.date;
                if (newDate <= oldDate && !forceDisplay) {
                    return;
                }

                final CheckBoxDialogBuilder builder = new CheckBoxDialogBuilder(
                        ListActivity.this, Html.fromHtml(result.content), getString(R.string.get_it), !forceDisplay, false);
                Dialog dialog = builder.setTitle(R.string.notice).setOnDismissListener(
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (builder.isShowCheckbox() && builder.isChecked()) {
                                    Settings.putNoticeDate(newDate);
                                }
                            }
                }).setPositiveButton(android.R.string.ok, null).show();
                ((TextView) dialog.findViewById(R.id.message)).setMovementMethod(
                        new LinkMovementMethod2(ListActivity.this));
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                mNoticeRequest = null;
            }

            @Override
            public void onCancel() {
                mNoticeRequest = null;
            }
        });
        mNMBClient.execute(request);
    }

    // Update current forum and update UI
    private void updateCurrentForum(Forum forum) {
        mCurrentForum = forum;
        mRightDrawer.setActivatedForum(forum);
        updateTitleByForum(mCurrentForum);
        ForumAutoSortingUtils.addACForumFrequency(forum);
    }

    private void updateForums(boolean firstTime) {
        Forum currentForum = mCurrentForum;
        boolean sorting = Settings.getForumAutoSorting();
        List<DisplayForum> forums = DB.getACForums(true, sorting); // TODO DB.getForums
        mRightDrawer.setForums(forums);

        // Try to find the same forum
        if (currentForum != null) {
            for (DisplayForum forum : forums) {
                if (currentForum.getNMBSite() == forum.getNMBSite() &&
                        currentForum.getNMBId().equals(forum.getNMBId())) {
                    updateCurrentForum(forum);
                    return;
                }
            }
        }

        if (forums.size() > 0) {
            updateCurrentForum(forums.get(0));
        } else {
            updateCurrentForum(null);
        }

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
        } else if (Constants.MESSENGER_ID_FAST_SCROLLER == id) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mPostAdapter.notifyDataSetChanged();
            } else if (resultCode == RESULT_CODE_REFRESH) {
                mPostHelper.refresh();
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
        if (mDrawerLayout != null && (mDrawerLayout.isDrawerOpen(Gravity.LEFT) ||
                mDrawerLayout.isDrawerOpen(Gravity.RIGHT))) {
            mDrawerLayout.closeDrawers();
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
        mRule = menu.findItem(R.id.action_rule);
        mNotice = menu.findItem(R.id.action_notice);
        mCreatePost = menu.findItem(R.id.action_create_post);
        mSortForumsMenu = menu.findItem(R.id.action_sort_forums);

        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mRule.setVisible(false);
            mNotice.setVisible(false);
            mCreatePost.setVisible(false);
            mSortForumsMenu.setVisible(true);
        } else {
            mRule.setVisible(true);
            mNotice.setVisible(true);
            mCreatePost.setVisible(true);
            mSortForumsMenu.setVisible(false);
        }

        return true;
    }

    private Spanned fixURLSpan(Spanned spanned) {
        Spannable spannable;
        if (spanned instanceof Spannable) {
            spannable = (Spannable) spanned;
        } else {
            spannable = new SpannableString(spanned);
        }

        URLSpan[] urlSpans = spannable.getSpans(0, spanned.length(), URLSpan.class);
        if (urlSpans == null) {
            return spanned;
        }

        for (URLSpan urlSpan : urlSpans) {
            String url = urlSpan.getURL();
            if (TextUtils.isEmpty(url)) {
                spannable.removeSpan(urlSpan);
            }

            try {
                new URL(url);
            } catch (MalformedURLException e) {
                URL absoluteUrl;
                // It might be relative path
                try {
                    // Use absolute url
                    absoluteUrl = new URL(new URL(ACUrl.getHost()), url);
                    int start = spannable.getSpanStart(urlSpan);
                    int end = spannable.getSpanEnd(urlSpan);
                    spannable.removeSpan(urlSpan);
                    spannable.setSpan(new URLSpan(absoluteUrl.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (MalformedURLException e1) {
                    // Can't get url
                    spannable.removeSpan(urlSpan);
                }
            }
        }

        return spannable;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                return true;
            case R.id.action_rule:
                if (mCurrentForum != null && mCurrentForum.getNMBMsg() != null) {
                    View view = getLayoutInflater().inflate(R.layout.dialog_rule, null);
                    TextView tv = (TextView) view.findViewById(R.id.text);
                    tv.setText(fixURLSpan(Html.fromHtml(mCurrentForum.getNMBMsg(),
                            new URLImageGetter(tv, NMBApplication.getConaco(this)), null)));
                    tv.setMovementMethod(new LinkMovementMethod2(ListActivity.this));
                    new AlertDialog.Builder(this).setTitle(R.string.rule).setView(view).show();
                }
                return true;
            case R.id.action_notice:
                if (mCurrentForum != null)
                    getNotice(true);
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
            setTitle(null);
        }
    }

    @Override
    public void onClickCommonPosts() {
        AutoWrapLayout layout = new AutoWrapLayout(ListActivity.this);
        final Dialog dialog = new AlertDialog.Builder(this).setView(layout).show();

        List<CommonPost> list = DB.getAllACCommentPost();
        int padding = LayoutUtils.dp2pix(this, 4);
        layout.setPadding(padding, 0, padding, 0);
        LayoutInflater inflater = getLayoutInflater();
        for (final CommonPost cp : list) {
            inflater.inflate(R.layout.item_dialog_comment_post, layout);
            TextView tv = (TextView) layout.getChildAt(layout.getChildCount() - 1);
            tv.setText(cp.name);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    Intent intent = new Intent(ListActivity.this, PostActivity.class);
                    intent.setAction(PostActivity.ACTION_SITE_ID);
                    intent.putExtra(PostActivity.KEY_SITE, ACSite.getInstance().getId());
                    intent.putExtra(PostActivity.KEY_ID, cp.id);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onSelectForum(Forum forum) {
        if (mCurrentForum == null ||
                (mCurrentForum.getNMBSite() != forum.getNMBSite() ||
                        !mCurrentForum.getNMBId().equals(forum.getNMBId()))) {
            updateCurrentForum(forum);
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
            mPostHelper.refresh();
        }
    }

    @Override
    public void OnLongClickImage(UniFile imageFile) {
        Intent intent = new Intent(this, GalleryActivity2.class);
        intent.setAction(GalleryActivity2.ACTION_IMAGE_FILE);
        intent.putExtra(GalleryActivity2.KEY_UNI_FILE_URI, imageFile.getUri());
        startActivity(intent);
    }

    public class SearchDialogHelper implements View.OnClickListener {
        public View mView;
        public EditText mEditText;

        public View mPositive;
        public View mNegative;
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
            mNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            mNeutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            mPositive.setOnClickListener(this);
            mNegative.setOnClickListener(this);
            mNeutral.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mNegative == v) {
                Intent intent = new Intent(ListActivity.this, PostActivity.class);
                intent.setAction(PostActivity.ACTION_SITE_REPLY_ID);
                intent.putExtra(PostActivity.KEY_SITE, ACSite.getInstance().getId());
                intent.putExtra(PostActivity.KEY_ID, Integer.toString(MathUtils.random(1, 16000000))); // TODO how to get the max id
                startActivity(intent);
                mDialog.dismiss();
                return;
            }

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
                if (!TextUtils.isDigitsOnly(keyword)) {
                    Toast.makeText(ListActivity.this, R.string.invalid_post_id, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ListActivity.this, PostActivity.class);
                intent.setAction(PostActivity.ACTION_SITE_REPLY_ID);
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
                .setNegativeButton(R.string.lucky, null)
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

    private class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView leftText;
        public TextView centerText;
        public TextView rightText;
        public FontTextView content;
        public TextView bottomText;
        public LoadImageView thumb;
        public View bottom;
        public MarqueeReplyView reply;

        public ListHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            centerText = (TextView) itemView.findViewById(R.id.center_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (FontTextView) itemView.findViewById(R.id.content);
            bottomText = (TextView) itemView.findViewById(R.id.bottom_text);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
            reply = (MarqueeReplyView) itemView.findViewById(R.id.reply);
            bottom = itemView.findViewById(R.id.bottom);

            thumb.setOnClickListener(this);

            Drawable drawable = DrawableManager.getDrawable(ListActivity.this, R.drawable.v_comment_multiple_outline_x16);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            bottomText.setCompoundDrawables(drawable, null, null, null);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mPostHelper.size()) {
                Post post = mPostHelper.getDataAt(position);
                String key = post.getNMBImageKey();
                String image = post.getNMBImageUrl();
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(image)) {
                    Intent intent = new Intent(ListActivity.this, GalleryActivity2.class);
                    intent.setAction(GalleryActivity2.ACTION_SINGLE_IMAGE);
                    intent.putExtra(GalleryActivity2.KEY_SITE, post.getNMBSite().getId());
                    intent.putExtra(GalleryActivity2.KEY_ID, post.getNMBId());
                    intent.putExtra(GalleryActivity2.KEY_KEY, key);
                    intent.putExtra(GalleryActivity2.KEY_IMAGE, image);
                    ListActivity.this.startActivity(intent);
                }
            }
        }

        public boolean setReplies(Reply[] replies) {
            if (replies == null || replies.length == 0) {
                reply.setVisibility(View.INVISIBLE);
                return false;
            } else {
                reply.setVisibility(View.VISIBLE);
                reply.setReplies(replies);
                return true;
            }
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<ListHolder> {

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ListHolder holder = new ListHolder(getLayoutInflater().inflate(R.layout.item_list, parent, false));
            mListHolderList.add(new WeakReference<>(holder));
            return holder;
        }

        @Override
        public void onBindViewHolder(ListHolder holder, int position) {
            Post post = mPostHelper.getDataAt(position);
            holder.leftText.setText(post.getNMBDisplayUsername());
            ACForumRaw forum = DB.getACForumForForumid(post.getNMBFid());
            if (forum != null) {
                String displayName = forum.getDisplayname();
                CharSequence name = mForumNames.get(displayName);
                if (name == null) {
                    if (displayName == null) {
                        name = "Forum";
                    } else {
                        name = Html.fromHtml(displayName);
                    }
                    mForumNames.put(displayName, name);
                }
                holder.centerText.setText(name);
            } else {
                holder.centerText.setText("No." + post.getNMBId());
            }
            holder.rightText.setText(ReadableTime.getDisplayTime(post.getNMBTime()));
            ACItemUtils.setContentText(holder.content, post.getNMBDisplayContent());
            holder.bottomText.setText(post.getNMBReplyDisplayCount());

            View bottom = holder.bottom;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bottom.getLayoutParams();
            String thumbKey = post.getNMBThumbKey();
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
            if (!TextUtils.isEmpty(thumbKey) && !TextUtils.isEmpty(thumbUrl) && tryShowImage) {
                showImage = true;

                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.unload();
                holder.thumb.load(thumbKey, thumbUrl, loadFromNetwork);
            } else {
                showImage = false;

                holder.thumb.setVisibility(View.GONE);
                holder.thumb.unload();
            }

            boolean showReplies;
            Reply[] replies = post.getNMBReplies();
            if (Settings.getDynamicComments()) {
                showReplies = holder.setReplies(replies);
            } else {
                showReplies = holder.setReplies(null);
            }

            if (showImage && !showReplies) {
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.thumb);
                lp.addRule(RelativeLayout.BELOW, 0);
                bottom.setLayoutParams(lp);
            } else if (showImage) {
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
                lp.addRule(RelativeLayout.BELOW, R.id.thumb);
                bottom.setLayoutParams(lp);
            } else {
                lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
                lp.addRule(RelativeLayout.BELOW, R.id.content);
                bottom.setLayoutParams(lp);
            }

            holder.content.setTextSize(Settings.getFontSize());
            holder.content.setLineSpacing(LayoutUtils.dp2pix(ListActivity.this, Settings.getLineSpacing()), 1.0f);
            if (Settings.getFixEmojiDisplay()) {
                holder.content.useCustomTypeface();
            } else {
                holder.content.useOriginalTypeface();
            }
        }

        @Override
        public int getItemCount() {
            return mPostHelper.size();
        }

        @Override
        public void onViewAttachedToWindow(ListHolder holder) {
            holder.thumb.start();
        }

        @Override
        public void onViewDetachedFromWindow(ListHolder holder) {
            holder.thumb.stop();
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
        protected boolean shouldRemoveDuplications() {
            return true;
        }

        @Override
        protected boolean isTheSame(Post d1, Post d2) {
            return ObjectUtils.equal(d1.getNMBId(), d2.getNMBId());
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
                String forumId = mCurrentForum.getNMBId();
                NMBRequest request = new NMBRequest();
                mNMBRequest = request;
                request.setSite(mCurrentForum.getNMBSite());
                request.setMethod(NMBClient.METHOD_GET_POST_LIST);
                request.setArgs(forumId, page);
                request.setCallback(new ListListener(taskId, page, forumId, request));
                mNMBClient.execute(request);
            }
        }
    }

    private class ListListener implements NMBClient.Callback<List<Post>> {

        private int mTaskId;
        private int mTaskPage;
        private String mForumId;
        private NMBRequest mRequest;

        public ListListener(int taskId, int taskPage, String forumId, NMBRequest request) {
            mTaskId = taskId;
            mTaskPage = taskPage;
            mForumId = forumId;
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
                    // Remove ignored posts
                    Iterator<Post> postIterator = result.iterator();
                    while (postIterator.hasNext()) {
                        Post post = postIterator.next();
                        if (PostIgnoreUtils.INSTANCE.checkPostIgnored(post.getNMBPostId()))
                            postIterator.remove();
                    }

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
