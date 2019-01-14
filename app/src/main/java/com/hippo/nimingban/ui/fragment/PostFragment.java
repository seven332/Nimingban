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

package com.hippo.nimingban.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.effect.ViewTransition;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.NMBUrl;
import com.hippo.nimingban.client.ReferenceSpan;
import com.hippo.nimingban.client.ac.NMBUriParser;
import com.hippo.nimingban.client.ac.data.ACReference;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.ui.GalleryActivity2;
import com.hippo.nimingban.ui.PostActivity;
import com.hippo.nimingban.util.MinMaxFilter;
import com.hippo.nimingban.util.OpenUrlHelper;
import com.hippo.nimingban.util.PostIgnoreUtils;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LinkifyTextView;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.ripple.Ripple;
import com.hippo.util.ActivityHelper;
import com.hippo.util.DrawableManager;
import com.hippo.util.ExceptionUtils;
import com.hippo.util.TextUtils2;
import com.hippo.widget.Slider;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PostFragment extends BaseFragment
        implements Messenger.Receiver,
        EasyRecyclerView.OnItemClickListener,
        EasyRecyclerView.OnItemLongClickListener,
        Toolbar.OnMenuItemClickListener {

    public static final String ACTION_POST = "com.hippo.nimingban.ui.PostActivity.action.POST";
    public static final String ACTION_SITE_ID = "com.hippo.nimingban.ui.PostActivity.action.SITE_ID";
    public static final String ACTION_SITE_REPLY_ID = "com.hippo.nimingban.ui.PostActivity.action.SITE_REPLY_ID";

    public static final String KEY_ACTION = "action";
    public static final String KEY_POST = "post";
    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";
    public static final String KEY_DATA = "data";

    /**
     * Intent.ACTION_PROCESS_TEXT
     */
    public static final String ACTION_PROCESS_TEXT = "android.intent.action.PROCESS_TEXT";
    /**
     * Intent.EXTRA_PROCESS_TEXT
     */
    public static final String EXTRA_PROCESS_TEXT = "android.intent.extra.PROCESS_TEXT";
    /**
     * Intent.EXTRA_PROCESS_TEXT_READONLY
     */
    public static final String EXTRA_PROCESS_TEXT_READONLY = "android.intent.extra.PROCESS_TEXT_READONLY";

    private NMBClient mNMBClient;

    private Toolbar mToolbar;
    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;

    private ReplyHelper mReplyHelper;
    private ReplyAdapter mReplyAdapter;
    private RecyclerView.OnScrollListener mOnScrollListener;

    private NMBRequest mNMBRequest;

    private Site mSite;
    private String mId;
    private String mReplyId;

    private CharSequence mPostUser;

    private int mOpColor;

    private int mPageSize = -1;

    private List<WeakReference<ReplyHolder>> mHolderList = new LinkedList<>();

    private Callback mCallback;

    private boolean isLoaded;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private boolean handleArgs(Bundle args) {
        if (args == null) {
            return false;
        }

        String action = args.getString(KEY_ACTION);
        if (ACTION_POST.equals(action)) {
            Post post = args.getParcelable(KEY_POST);
            if (post != null) {
                mSite = post.getNMBSite();
                mId = post.getNMBId();
                mPostUser = post.getNMBDisplayUsername();
                return true;
            }
        } else if (ACTION_SITE_ID.equals(action)) {
            int site = args.getInt(KEY_SITE, -1);
            String id = args.getString(KEY_ID);
            if (Site.isValid(site) && id != null) {
                mSite = Site.fromId(site);
                mId = id;
                return true;
            }
        } else if (ACTION_SITE_REPLY_ID.equals(action)) {
            int site = args.getInt(KEY_SITE, -1);
            String id = args.getString(KEY_ID);
            if (Site.isValid(site) && id != null) {
                mSite = Site.fromId(site);
                mReplyId = id;
                return true;
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            NMBUriParser.PostResult result = NMBUriParser.parsePostUri((Uri) args.getParcelable(KEY_DATA));
            mSite = result.site;
            mId = result.id;
            if (mSite != null && mId != null) {
                return true;
            }
        }

        return false;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNMBClient = NMBApplication.getNMBClient(getContext());

        if (!handleArgs(getArguments())) {
            getFragmentHost().finishFragment(this);
            return;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.activity_toolbar, container, false);
        ViewGroup contentPanel = (ViewGroup) view.findViewById(R.id.content_panel);
        ViewGroup contentView = (ViewGroup) inflater.inflate(R.layout.fragment_post, contentPanel, true);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        // I like hardcode
        mToolbar.setSubtitle("A岛·adnmb.com");
        if (mId != null) {
            mToolbar.setTitle(mSite.getPostTitle(getContext(), mId));
        } else {
            mToolbar.setTitle(getString(R.string.thread));
        }
        mToolbar.setNavigationIcon(DrawableManager.getDrawable(getContext(), R.drawable.v_arrow_left_dark_x24));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentHost().finishFragment(PostFragment.this);
            }
        });
        mToolbar.inflateMenu(R.menu.activity_post);
        mToolbar.setOnMenuItemClickListener(this);

        mContentLayout = (ContentLayout) contentView.findViewById(R.id.content_layout);
        mRecyclerView = mContentLayout.getRecyclerView();

        mReplyHelper = new ReplyHelper();
        mReplyHelper.setEmptyString(getString(R.string.not_found));
        mContentLayout.setHelper(mReplyHelper);
        if (Settings.getFastScroller()) {
            mContentLayout.showFastScroll();
        } else {
            mContentLayout.hideFastScroll();
        }

        mReplyAdapter = new ReplyAdapter();
        mRecyclerView.setAdapter(mReplyAdapter);
        mRecyclerView.setSelector(Ripple.generateRippleDrawable(
                getContext(), ResourcesUtils.getAttrBoolean(getContext(), R.attr.dark)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setOnItemClickListener(this);
        mRecyclerView.setOnItemLongClickListener(this);
        mRecyclerView.hasFixedSize();
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

        mOpColor = getResources().getColor(R.color.colorAccent);

        // Refresh
        mReplyHelper.firstRefresh();

        Messenger.getInstance().register(Constants.MESSENGER_ID_REPLY, this);
        Messenger.getInstance().register(Constants.MESSENGER_ID_FAST_SCROLLER, this);

        isLoaded = false;

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Messenger.getInstance().unregister(Constants.MESSENGER_ID_REPLY, this);
        Messenger.getInstance().unregister(Constants.MESSENGER_ID_FAST_SCROLLER, this);

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }

        mRecyclerView.removeOnScrollListener(mOnScrollListener);

        for (WeakReference<ReplyHolder> ref : mHolderList) {
            ReplyHolder holder = ref.get();
            if (holder != null) {
                holder.thumb.unload();
            }
        }
        mHolderList.clear();
    }

    private void resumeHolders() {
        Iterator<WeakReference<ReplyHolder>> iterator = mHolderList.iterator();
        while (iterator.hasNext()) {
            ReplyHolder holder = iterator.next().get();
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

    @Override
    public void onResume() {
        super.onResume();
        resumeHolders();
    }

    private void pauseHolders() {
        Iterator<WeakReference<ReplyHolder>> iterator = mHolderList.iterator();
        while (iterator.hasNext()) {
            ReplyHolder holder = iterator.next().get();
            if (holder != null) {
                holder.thumb.stop();
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseHolders();
    }

    @Override
    public void onReceive(int id, Object obj) {
        if (Constants.MESSENGER_ID_REPLY == id) {
            if (mId.equals(obj)) {
                int currentPage = mReplyHelper.getPageForBottom();
                int pages = mReplyHelper.getPages();
                if (currentPage >= 0 && currentPage + 1 == pages) {
                    // It is the last page, refresh it
                    mReplyHelper.doGetData(ContentLayout.ContentHelper.TYPE_REFRESH_PAGE,
                            currentPage, ContentLayout.ContentHelper.REFRESH_TYPE_FOOTER);
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
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!isLoaded) return true;

        switch (item.getItemId()) {
            case R.id.action_go_to:
                int pages = mReplyHelper.getPages();
                if (pages > 0 && mReplyHelper.canGoTo()) {
                    GoToDialogHelper helper = new GoToDialogHelper(pages, mReplyHelper.getPageForTop());
                    AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle(R.string.go_to)
                            .setView(helper.getView())
                            .setPositiveButton(android.R.string.ok, null)
                            .create();
                    dialog.show();
                    helper.setPositiveButtonClickListener(dialog);
                }
                return true;
            case R.id.action_reply:
                mCallback.reply(mSite, mId, null, false);
                return true;
            case R.id.action_add_feed:
                NMBRequest request1 = new NMBRequest();
                request1.setSite(mSite);
                request1.setMethod(NMBClient.METHOD_ADD_FEED);
                request1.setArgs(mSite.getUserId(getContext()), mId);
                request1.setCallback(new FeedListener(getContext(), true));
                mNMBClient.execute(request1);
                return true;
            case R.id.action_remove_feed:
                NMBRequest request2 = new NMBRequest();
                request2.setSite(mSite);
                request2.setMethod(NMBClient.METHOD_DEL_FEED);
                request2.setArgs(mSite.getUserId(getContext()), mId);
                request2.setCallback(new FeedListener(getContext(), false));
                mNMBClient.execute(request2);
                return true;
            case R.id.action_share:
                ActivityHelper.share(getActivity(), NMBUrl.getBrowsablePostUrl(mSite, mId, 0));
                return true;
            case R.id.action_open_in_other_app:
                OpenUrlHelper.openUrl(getActivity(), NMBUrl.getBrowsablePostUrl(mSite, mId, 0), false);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GoToDialogHelper implements View.OnClickListener,
            DialogInterface.OnDismissListener, Slider.OnSetProgressListener, TextWatcher {

        private int mPages;

        private View mView;
        private Slider mSlider;
        private EditText mEditText;

        private Dialog mDialog;

        @SuppressLint("InflateParams")
        private GoToDialogHelper(int pages, int currentPage) {
            mPages = pages;
            mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_go_to, null);
            ((TextView) mView.findViewById(R.id.start)).setText("1");
            ((TextView) mView.findViewById(R.id.end)).setText(Integer.toString(pages));
            mSlider = (Slider) mView.findViewById(R.id.slider);
            mSlider.setRange(1, pages);
            mSlider.setProgress(currentPage + 1);
            mSlider.setOnSetProgressListener(this);
            mEditText = (EditText) mView.findViewById(R.id.page_input);
            mEditText.setText(Integer.toString(currentPage + 1)); // Android still treating int as resid
            mEditText.setHint("1");
            mEditText.setFilters(new InputFilter[] {new MinMaxFilter(1, pages)});
            mEditText.addTextChangedListener(this);
        }

        public View getView() {
            return mView;
        }

        public void setPositiveButtonClickListener(AlertDialog dialog) {
            mDialog = dialog;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
            dialog.setOnDismissListener(this);
        }

        @Override
        public void onClick(View v) {
            int page = mSlider.getProgress() - 1;
            if (page >= 0 && page < mPages) {
                mReplyHelper.goTo(page);
                if (mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                }
            } else {
                Toast.makeText(getContext(), R.string.go_to_error_out_of_range, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog = null;
        }

        @Override
        public void onSetProgress(Slider slider, int newProgress, int oldProgress, boolean byUser, boolean confirm) {
            if (!byUser) return;
            mEditText.setText(String.valueOf(newProgress)); // To prevent Android treat progress as resid
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int i = s.length() > 0 ? Integer.parseInt(s.toString()) : 1;
            mSlider.setProgress(i);
        }

        @Override
        public void onFingerDown() {}

        @Override
        public void onFingerUp() {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }


    private void handleURLSpan(URLSpan urlSpan) {
        OpenUrlHelper.openUrl(getContext(), urlSpan.getURL(), true);
    }

    private final class ReferenceDialogHelper implements AlertDialog.OnDismissListener,
            NMBClient.Callback<ACReference>, View.OnClickListener {

        private Site mSite;
        private String mId;

        private View mView;
        private ViewTransition mViewTransition;

        public TextView mLeftText;
        public TextView mCenterText;
        public TextView mRightText;
        private LinkifyTextView mContent;
        private LoadImageView mThumb;
        private View mButton;

        private AlertDialog mDialog;

        private NMBRequest mRequest;

        private Reply mReply;

        @SuppressLint("InflateParams")
        public ReferenceDialogHelper(Site site, String id) {
            mSite = site;
            mId = id;

            mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_reference, null);

            View progress = mView.findViewById(R.id.progress_view);
            View reference = mView.findViewById(R.id.reference);
            mViewTransition = new ViewTransition(progress, reference);

            mLeftText = (TextView) reference.findViewById(R.id.left_text);
            mCenterText = (TextView) reference.findViewById(R.id.center_text);
            mRightText = (TextView) reference.findViewById(R.id.right_text);
            mContent = (LinkifyTextView) reference.findViewById(R.id.content);
            mThumb = (LoadImageView) reference.findViewById(R.id.thumb);
            mButton = reference.findViewById(R.id.button);

            mContent.setOnClickListener(this);
            mThumb.setOnClickListener(this);
            Ripple.addRipple(mButton, ResourcesUtils.getAttrBoolean(getContext(), R.attr.dark));
        }

        @Override
        public void onClick(View v) {
            if (v == mContent) {
                ClickableSpan span = mContent.getCurrentSpan();
                mContent.clearCurrentSpan();

                if (span instanceof URLSpan) {
                    handleURLSpan((URLSpan) span);
                } else if (span instanceof ReferenceSpan) {
                    handleReferenceSpan((ReferenceSpan) span);
                }
            } else if (v == mThumb) {
                String key;
                String image;
                if (mReply != null && !TextUtils.isEmpty(key = mReply.getNMBImageKey()) &&
                        !TextUtils.isEmpty(image = mReply.getNMBImageUrl())) {
                    Intent intent = new Intent(getActivity(), GalleryActivity2.class);
                    intent.setAction(GalleryActivity2.ACTION_SINGLE_IMAGE);
                    intent.putExtra(GalleryActivity2.KEY_SITE, mSite.getId());
                    intent.putExtra(GalleryActivity2.KEY_ID, mReply.getNMBId());
                    intent.putExtra(GalleryActivity2.KEY_KEY, key);
                    intent.putExtra(GalleryActivity2.KEY_IMAGE, image);
                    startActivity(intent);
                }
            }
        }

        public void setDialog(AlertDialog dialog) {
            mDialog = dialog;
        }

        public View getView() {
            return mView;
        }

        public void request() {
            // Try to find in data list first
            ReplyHelper replyHelper = mReplyHelper;
            for (int i = 0, n = replyHelper.size(); i < n; i++) {
                Reply reply = replyHelper.getDataAt(i);
                if (mId.equals(reply.getNMBId())) {
                    onGetReference(reply, false);
                    return;
                }
            }

            NMBRequest request = new NMBRequest();
            mRequest = request;
            request.setSite(mSite);
            request.setMethod(NMBClient.METHOD_GET_REFERENCE);
            request.setArgs(mId);
            request.setCallback(this);
            mNMBClient.execute(request);
        }

        private void onGetReference(final Reply reply, boolean animation) {
            mReply = reply;

            mLeftText.setText(highlightOp(reply));
            mCenterText.setText("No." + reply.getNMBId());
            mRightText.setText(ReadableTime.getDisplayTime(reply.getNMBTime()));
            mContent.setText(reply.getNMBDisplayContent());

            String thumbKey = reply.getNMBThumbKey();
            String thumbUrl = reply.getNMBThumbUrl();

            boolean showImage;
            boolean loadFromNetwork;
            int ils = Settings.getImageLoadingStrategy();
            if (ils == Settings.IMAGE_LOADING_STRATEGY_ALL ||
                    (ils == Settings.IMAGE_LOADING_STRATEGY_WIFI && NMBApplication.isConnectedWifi(getContext()))) {
                showImage = true;
                loadFromNetwork = true;
            } else {
                showImage = Settings.getImageLoadingStrategy2();
                loadFromNetwork = false;
            }

            if (!TextUtils.isEmpty(thumbKey) && !TextUtils.isEmpty(thumbUrl) && showImage) {
                mThumb.setVisibility(View.VISIBLE);
                mThumb.unload();
                mThumb.load(thumbKey, thumbUrl, loadFromNetwork);
            } else {
                mThumb.setVisibility(View.GONE);
                mThumb.unload();
            }

            mContent.setTextSize(Settings.getFontSize());
            mContent.setLineSpacing(LayoutUtils.dp2pix(getContext(), Settings.getLineSpacing()), 1.0f);
            if (Settings.getFixEmojiDisplay()) {
                mContent.useCustomTypeface();
            } else {
                mContent.useOriginalTypeface();
            }

            mViewTransition.showView(1, animation);

            String postId = reply.getNMBPostId();

            if (postId != null && !postId.equals(PostFragment.this.mId) && mDialog != null && mDialog.isShowing()) {
                mButton.setVisibility(View.VISIBLE);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PostActivity.class);
                        intent.setAction(ACTION_SITE_ID);
                        intent.putExtra(KEY_SITE, mSite.getId());
                        intent.putExtra(KEY_ID, reply.getNMBPostId());
                        startActivity(intent);
                    }
                });
            }

            // TODO: Need getting rid of unwanted requests (example: post image thumbnail)
            if (Settings.getEnableStrictIgnoreMode() && PostIgnoreUtils.INSTANCE.checkPostIgnored(postId)) {
                mLeftText.setText("");
                mCenterText.setText("No.9999999");
                mRightText.setText(R.string.from_the_future);
                mThumb.setVisibility(View.GONE);
                mThumb.unload();
                mContent.setText(R.string.ignore_post_message);
                mButton.setVisibility(View.GONE);
                mButton.setOnClickListener(null);
            }

            mRequest = null;
            mDialog = null;
        }

        @Override
        public void onSuccess(ACReference result) {
            onGetReference(result, true);
        }

        @Override
        public void onFailure(Exception e) {
            mLeftText.setVisibility(View.GONE);
            mCenterText.setVisibility(View.GONE);
            mRightText.setVisibility(View.GONE);
            mContent.setText(R.string.cant_get_the_reference);
            mThumb.setVisibility(View.GONE);
            mViewTransition.showView(1, true);

            mRequest = null;
            mDialog = null;
        }

        @Override
        public void onCancel() {
            mRequest = null;

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog = null;

            if (mRequest != null) {
                mRequest.cancel();
                mRequest = null;
            }

            mThumb.unload();
        }
    }

    private CharSequence highlightOp(Reply reply) {
        CharSequence user = reply.getNMBDisplayUsername();

        if (2 == Settings.getChaosLevel()) { // Absolutely chaotic
            return user;
        }

        if (!TextUtils.isEmpty(user) && TextUtils2.contentEquals(user, mPostUser)) {
            Spannable spannable;
            if (user instanceof Spannable) {
                spannable = (Spannable) user;
            } else {
                spannable = new SpannableString(user);
            }

            int length = user.length();
            if (spannable.getSpans(0, length, Object.class).length == 0) {
                StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(mOpColor);
                spannable.setSpan(styleSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(colorSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannable;
        } else {
            return user;
        }
    }

    private void handleReferenceSpan(ReferenceSpan referenceSpan) {
        ReferenceDialogHelper helper = new ReferenceDialogHelper(referenceSpan.getSite(), referenceSpan.getId());
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(helper.getView())
                .setOnDismissListener(helper).create();
        helper.setDialog(dialog);
        dialog.show();
        helper.request();
    }

    private class ReplyDailogHelper implements DialogInterface.OnClickListener {

        private Reply mReply;
        private List<ResolveInfo> mResolveInfoList;

        public ReplyDailogHelper(Reply reply, List<ResolveInfo> resolveInfoList) {
            mReply = reply;
            mResolveInfoList = resolveInfoList;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    mCallback.reply(mSite, mId, ">>No." + mReply.getNMBId() + "\n", false); // TODO Let site decides it
                    break;
                case 1:
                    // Copy
                    Context context = getContext();
                    if (context != null) {
                        ClipboardManager cbm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        cbm.setPrimaryClip(ClipData.newPlainText(null, mReply.getNMBDisplayContent()));
                        Toast.makeText(getContext(), R.string.comment_copied_clipboard, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    // Send
                    ActivityHelper.share(getActivity(), mReply.getNMBDisplayContent().toString());
                    break;
                case 3: {
                    // Report
                    mCallback.reply(mSite, mSite.getReportForumId(), ">>No." + mReply.getNMBId() + "\n", true); // TODO Let site decides it
                    break;
                }
                default: {
                    if (mResolveInfoList == null) {
                        break;
                    }
                    int index = which - 4;
                    if (index < mResolveInfoList.size() && index >= 0) {
                        ResolveInfo info = mResolveInfoList.get(index);
                        Intent intent = new Intent()
                                .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                                .setAction(ACTION_PROCESS_TEXT)
                                .setType("text/plain")
                                .putExtra(EXTRA_PROCESS_TEXT_READONLY, true)
                                .putExtra(EXTRA_PROCESS_TEXT, mReply.getNMBDisplayContent().toString());

                        startActivity(intent);
                    }
                    break;
                }
            }
        }
    }

    private void showReplyDialog(int position) {
        List<CharSequence> itemList = new ArrayList<>();
        String[] items = getResources().getStringArray(R.array.reply_dialog);
        Collections.addAll(itemList, items);

        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent()
                .setAction(ACTION_PROCESS_TEXT)
                .setType("text/plain"), 0);
        for (ResolveInfo info : resolveInfos) {
            itemList.add(info.loadLabel(pm));
        }

        ReplyDailogHelper helper = new ReplyDailogHelper(mReplyHelper.getDataAt(position), resolveInfos);
        new AlertDialog.Builder(getContext()).setItems(itemList.toArray(new CharSequence[itemList.size()]), helper).show();
    }

    @Override
    public boolean onItemLongClick(EasyRecyclerView parent, View view, int position, long id) {
        showReplyDialog(position);
        return true;
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
        if (holder instanceof ReplyHolder) {
            ReplyHolder replyHolder = (ReplyHolder) holder;
            ClickableSpan span = replyHolder.content.getCurrentSpan();
            replyHolder.content.clearCurrentSpan();

            if (span instanceof URLSpan) {
                handleURLSpan((URLSpan) span);
                return true;
            } else if (span instanceof ReferenceSpan) {
                handleReferenceSpan((ReferenceSpan) span);
                return true;
            }
        }
        return false;
    }

    private class ReplyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView leftText;
        public TextView centerText;
        public TextView rightText;
        public LinkifyTextView content;
        public LoadImageView thumb;

        public ReplyHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            centerText = (TextView) itemView.findViewById(R.id.center_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (LinkifyTextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);

            thumb.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < mReplyHelper.size()) {
                Reply reply = mReplyHelper.getDataAt(position);
                String key = reply.getNMBImageKey();
                String image = reply.getNMBImageUrl();
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(image)) {
                    Intent intent = new Intent(getActivity(), GalleryActivity2.class);
                    intent.setAction(GalleryActivity2.ACTION_SINGLE_IMAGE);
                    intent.putExtra(GalleryActivity2.KEY_SITE, reply.getNMBSite().getId());
                    intent.putExtra(GalleryActivity2.KEY_ID, reply.getNMBId());
                    intent.putExtra(GalleryActivity2.KEY_KEY, key);
                    intent.putExtra(GalleryActivity2.KEY_IMAGE, image);
                    startActivity(intent);
                }
            }
        }
    }

    private class ReplyAdapter extends RecyclerView.Adapter<ReplyHolder> {

        @Override
        public ReplyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ReplyHolder holder = new ReplyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
            mHolderList.add(new WeakReference<>(holder));
            return holder;
        }

        @Override
        public void onBindViewHolder(ReplyHolder holder, int position) {
            Reply reply = mReplyHelper.getDataAt(position);
            holder.leftText.setText(highlightOp(reply));
            holder.centerText.setText("No." + reply.getNMBId());
            holder.rightText.setText(ReadableTime.getDisplayTime(reply.getNMBTime()));
            holder.content.setText(reply.getNMBDisplayContent());

            String thumbKey = reply.getNMBThumbKey();
            String thumbUrl = reply.getNMBThumbUrl();

            boolean showImage;
            boolean loadFromNetwork;
            int ils = Settings.getImageLoadingStrategy();
            if (ils == Settings.IMAGE_LOADING_STRATEGY_ALL ||
                    (ils == Settings.IMAGE_LOADING_STRATEGY_WIFI &&
                            getContext() != null && NMBApplication.isConnectedWifi(getContext()))) {
                showImage = true;
                loadFromNetwork = true;
            } else {
                showImage = Settings.getImageLoadingStrategy2();
                loadFromNetwork = false;
            }

            if (!TextUtils.isEmpty(thumbKey) && !TextUtils.isEmpty(thumbUrl) && showImage) {
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.unload();
                holder.thumb.load(thumbKey, thumbUrl, loadFromNetwork);
            } else {
                holder.thumb.setVisibility(View.GONE);
                holder.thumb.unload();
            }

            holder.content.setTextSize(Settings.getFontSize());
            // NOTE getContext() may return null
            holder.content.setLineSpacing(LayoutUtils.dp2pix(holder.content.getContext(),
                    Settings.getLineSpacing()), 1.0f);
            if (Settings.getFixEmojiDisplay()) {
                holder.content.useCustomTypeface();
            } else {
                holder.content.useOriginalTypeface();
            }
        }

        @Override
        public int getItemCount() {
            return mReplyHelper.size();
        }

        @Override
        public void onViewAttachedToWindow(ReplyHolder holder) {
            holder.thumb.start();
        }

        @Override
        public void onViewDetachedFromWindow(ReplyHolder holder) {
            holder.thumb.stop();
        }
    }

    private class ReplyHelper extends ContentLayout.ContentHelper<Reply> {

        @Override
        protected Context getContext() {
            return PostFragment.this.getContext();
        }

        @Override
        protected void notifyDataSetChanged() {
            mReplyAdapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mReplyAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            mReplyAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        protected void getPageData(int taskId, int type, int page) {
            if (mNMBRequest != null) {
                mNMBRequest.cancel();
                mNMBRequest = null;
            }

            if (mReplyId != null) {
                mReplyHelper.showProgressBar();
                NMBRequest request = new NMBRequest();
                mNMBRequest = request;
                request.setSite(mSite);
                request.setMethod(NMBClient.METHOD_GET_REFERENCE);
                request.setArgs(mReplyId);
                request.setCallback(new GetPostIdFromReferenceListener());
                mNMBClient.execute(request);
            } else {
                if (Settings.getEnableStrictIgnoreMode() && PostIgnoreUtils.INSTANCE.checkPostIgnored(mId)) {
                    isLoaded = false;
                    mReplyHelper.showText(getString(R.string.ignore_post_message));
                    return;
                }

                NMBRequest request = new NMBRequest();
                mNMBRequest = request;
                request.setSite(mSite);
                request.setMethod(NMBClient.METHOD_GET_POST);
                request.setArgs(mId, page);
                request.setCallback(new PostListener(taskId, type, page, request));
                mNMBClient.execute(request);
            }
        }
    }

    private class GetPostIdFromReferenceListener implements NMBClient.Callback<ACReference> {

        @Override
        public void onSuccess(ACReference result) {
            if (Settings.getEnableStrictIgnoreMode() && PostIgnoreUtils.INSTANCE.checkPostIgnored(result.postId)) {
                isLoaded = false;
                mReplyHelper.showText(getString(R.string.ignore_post_message));
                return;
            }

            mReplyId = null;
            mId = result.postId;
            mToolbar.setTitle(mSite.getPostTitle(getContext(), mId));
            mReplyHelper.refresh();
        }

        @Override
        public void onFailure(Exception e) {
            mReplyHelper.showText(ExceptionUtils.getReadableString(getContext(), e));
        }

        @Override
        public void onCancel() {
        }
    }

    private class PostListener implements NMBClient.Callback<Pair<Post, List<Reply>>> {

        private int mTaskId;
        private int mTaskType;
        private int mPage;
        private NMBRequest mRequest;

        public PostListener(int taskId, int type, int page, NMBRequest request) {
            mTaskId = taskId;
            mTaskType = type;
            mPage = page;
            mRequest = request;
        }

        @Override
        public void onSuccess(Pair<Post, List<Reply>> result) {
            if (mNMBRequest == mRequest) {
                // It is current request

                // Clear
                mNMBRequest = null;

                Post post = result.first;
                mPostUser = post.getNMBDisplayUsername();

                List<Reply> replies = result.second;
                if (mPage == 0) {
                    mPageSize = 0;
                    for (Reply reply : replies) {
                        // Remove ad
                        if (!reply.getNMBId().equals("9999999")) {
                            mPageSize++;
                        }
                    }
                    replies.add(0, post);
                }

                boolean empty;
                if (replies.isEmpty()) {
                    empty = true;
                    mReplyHelper.onGetEmptyData(mTaskId);
                } else {
                    empty = false;
                    mReplyHelper.onGetPageData(mTaskId, replies);
                }

                if (mPageSize == 0) {
                    mReplyHelper.setPages(1); // Only post, no reply
                } else if (empty && (mTaskType == ContentLayout.ContentHelper.TYPE_NEXT_PAGE ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_NEXT_PAGE_KEEP_POS)) {
                    mReplyHelper.setPages(mPage); // previous page is the last page
                } else if (mPageSize != -1) {
                    mReplyHelper.setPages(MathUtils.ceilDivide(post.getNMBReplyCount(), mPageSize)); // Guess2
                } else if (mTaskType == ContentLayout.ContentHelper.TYPE_REFRESH_PAGE ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_PRE_PAGE ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_PRE_PAGE_KEEP_POS ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_SOMEWHERE) {
                    // Keep the pages
                } else {
                    int pages = mReplyHelper.getPages();
                    if (pages != -1 && pages != Integer.MAX_VALUE) {
                        // Keep it
                    } else if (empty) {
                        mReplyHelper.setPages(1); // At least we get post
                    } else {
                        mReplyHelper.setPages(Integer.MAX_VALUE); // Keep going
                    }
                }

                isLoaded = true;
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

                mReplyHelper.onGetExpection(mTaskId, e);

                isLoaded = false;
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

                isLoaded = false;
            }
            // Clear
            mRequest = null;
        }
    }

    private static class FeedListener implements NMBClient.Callback<Void> {

        private Context mContext;
        private boolean mAdd;

        public FeedListener(Context context, boolean add) {
            mContext = context.getApplicationContext();
            mAdd = add;
        }

        @Override
        public void onSuccess(Void result) {
            Toast.makeText(mContext, mAdd ? R.string.add_feed_successfully : R.string.remove_feed_successfully, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Exception e) {
            Toast.makeText(mContext, mContext.getString(mAdd ? R.string.add_feed_failed :
                            R.string.remove_feed_failed) + "\n" + ExceptionUtils.getReadableString(mContext, e),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Log.d("TAG", "FeedListener onCancel");
        }
    }

    public interface Callback {

        void reply(Site site, String id, String presetText, boolean report);
    }
}
