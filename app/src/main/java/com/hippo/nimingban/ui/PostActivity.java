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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.conaco.Conaco;
import com.hippo.effect.ViewTransition;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.NMBUrl;
import com.hippo.nimingban.client.ReferenceSpan;
import com.hippo.nimingban.client.ac.data.ACReference;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.widget.ContentLayout;
import com.hippo.nimingban.widget.LinkifyTextView;
import com.hippo.nimingban.widget.LoadImageView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.TextUtils2;
import com.hippo.vectorold.content.VectorContext;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.yorozuya.MathUtils;

import java.util.List;

public class PostActivity extends AppCompatActivity {

    public static final String ACTION_POST = "com.hippo.nimingban.ui.PostActivity.action.POST";
    public static final String ACTION_SITE_ID = "com.hippo.nimingban.ui.PostActivity.action.SITE_ID";

    public static final String KEY_POST = "post";
    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";

    private NMBClient mNMBClient;
    private Conaco mConaco;

    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;

    private ReplyHelper mReplyHelper;
    private ReplyAdapter mReplyAdapter;

    private NMBRequest mNMBRequest;

    private int mSite;
    private String mId;

    private int mPageSize = -1;

    // false for error
    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_POST.equals(action)) {
            Post post = intent.getParcelableExtra(KEY_POST);
            if (post != null) {
                mSite = post.getNMBSite();
                mId = post.getNMBId();
                return true;
            }
        } else if (ACTION_SITE_ID.equals(action)) {
            int site = intent.getIntExtra(KEY_SITE, -1);
            String id = intent.getStringExtra(KEY_ID);
            if (site != -1 && id != null) { // TODO check is site valid
                mSite = site;
                mId = id;
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

        setContentView(R.layout.activity_post);

        mNMBClient = NMBApplication.getNMBClient(this);
        mConaco = NMBApplication.getConaco(this);

        mContentLayout = (ContentLayout) findViewById(R.id.content_layout);
        mRecyclerView = mContentLayout.getRecyclerView();

        mReplyHelper = new ReplyHelper();
        mContentLayout.setHelper(mReplyHelper);

        mReplyAdapter = new ReplyAdapter();
        mRecyclerView.setAdapter(mReplyAdapter);
        mRecyclerView.setSelector(RippleSalon.generateRippleDrawable(false));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setOnItemClickListener(new ClickReplyListener());
        mRecyclerView.hasFixedSize();

        // Refresh
        mReplyHelper.firstRefresh();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(VectorContext.wrapContext(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_post, menu);
        return true;
    }


    private class GoToDialogHelper implements View.OnClickListener,
            DialogInterface.OnDismissListener, EditText.OnEditorActionListener {

        private int mPages;

        private View mView;
        private EditText mEditText;

        private Dialog mDialog;

        @SuppressLint("InflateParams")
        private GoToDialogHelper(int pages, int currentPage) {
            mPages = pages;
            mView = getLayoutInflater().inflate(R.layout.dialog_go_to, null);
            mEditText = (EditText) mView.findViewById(R.id.edit_text);
            mEditText.setHint(getResources().getQuantityString(R.plurals.go_to_hint, pages, currentPage + 1, pages));
            mEditText.setOnEditorActionListener(this);
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
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                onClick(null);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onClick(View v) {
            // Do not check v, because onEditorAction pass null
            String str = mEditText.getText().toString();
            try {
                int page = Integer.parseInt(str) - 1;
                if (page >= 0 && page < mPages) {
                    mReplyHelper.goTo(page);
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                } else {
                    Toast.makeText(PostActivity.this, R.string.go_to_error_out_of_range, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(PostActivity.this, R.string.go_to_error_invalid, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_go_to:
                int pages = mReplyHelper.getPages();
                if (pages > 0 && mReplyHelper.canGoTo()) {
                    GoToDialogHelper helper = new GoToDialogHelper(pages, mReplyHelper.getCurrentPage());
                    AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.go_to)
                            .setView(helper.getView())
                            .setPositiveButton(android.R.string.ok, null)
                            .create();
                    dialog.show();
                    helper.setPositiveButtonClickListener(dialog);
                }
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleURLSpan(URLSpan urlSpan) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        try {
            i.setData(Uri.parse(urlSpan.getURL()));
            startActivity(i);
        } catch (Exception e) {
            // Avoid something wrong
            e.printStackTrace();
        }
    }

    private final class ReferenceDialogHelper implements AlertDialog.OnDismissListener,
            NMBClient.Callback<ACReference>, View.OnClickListener {

        private int mSite;
        private String mId;

        private View mView;
        private ViewTransition mViewTransition;

        private TextView mLeftText;
        private TextView mRightText;
        private LinkifyTextView mContent;
        private LoadImageView mThumb;
        private View mButton;

        private AlertDialog mDialog;

        private NMBRequest mRequest;

        @SuppressLint("InflateParams")
        public ReferenceDialogHelper(int site, String id) {
            mSite = site;
            mId = id;

            mView = getLayoutInflater().inflate(R.layout.dialog_reference, null);

            View progress = mView.findViewById(R.id.progress_view);
            View reference = mView.findViewById(R.id.reference);
            mViewTransition = new ViewTransition(progress, reference);

            mLeftText = (TextView) reference.findViewById(R.id.left_text);
            mRightText = (TextView) reference.findViewById(R.id.right_text);
            mContent = (LinkifyTextView) reference.findViewById(R.id.content);
            mThumb = (LoadImageView) reference.findViewById(R.id.thumb);
            mButton = reference.findViewById(R.id.button);

            mContent.setOnClickListener(this);
            RippleSalon.addRipple(mButton, false); // TODO darktheme
        }

        @Override
        public void onClick(View v) {
            ClickableSpan span = mContent.getCurrentSpan();
            mContent.clearCurrentSpan();

            if (span instanceof URLSpan) {
                handleURLSpan((URLSpan) span);
            } else if (span instanceof ReferenceSpan) {
                handleReferenceSpan((ReferenceSpan) span);
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
            request.setSite(NMBClient.AC);
            request.setMethod(NMBClient.METHOD_GET_REFERENCE);
            request.setArgs(NMBUrl.getReferenceUrl(mSite, mId));
            request.setCallback(this);
            mNMBClient.execute(request);
        }

        private void onGetReference(final Reply reply, boolean animation) {
            mLeftText.setText(TextUtils2.combine(reply.getNMBDisplayTime(), "  ", reply.getNMBDisplay()));
            mRightText.setText(reply.getNMBId());
            mContent.setText(reply.getNMBDisplayContent());

            String thumbUrl = reply.getNMBThumbUrl();
            if (!TextUtils.isEmpty(thumbUrl)) {
                mThumb.setVisibility(View.VISIBLE);
                mThumb.load(mConaco, thumbUrl, thumbUrl);
            } else {
                mThumb.setVisibility(View.GONE);
                mConaco.load(mThumb, null);
            }

            mViewTransition.showView(1, animation);

            String postId = reply.getNMBPostId();

            if (postId != null && !postId.equals(PostActivity.this.mId) && mDialog != null && mDialog.isShowing()) {
                mButton.setVisibility(View.VISIBLE);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PostActivity.this, PostActivity.class);
                        intent.setAction(ACTION_SITE_ID);
                        intent.putExtra(KEY_SITE, mSite);
                        intent.putExtra(KEY_ID, reply.getNMBPostId());
                        PostActivity.this.startActivity(intent);
                    }
                });
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
            e.printStackTrace(); // TODO a toast

            mRequest = null;

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }

        @Override
        public void onCancelled() {
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
        }
    }

    private void handleReferenceSpan(ReferenceSpan referenceSpan) {
        ReferenceDialogHelper helper = new ReferenceDialogHelper(referenceSpan.getSite(), referenceSpan.getId());
        AlertDialog dialog = new AlertDialog.Builder(this).setView(helper.getView())
                .setOnDismissListener(helper).create();
        helper.setDialog(dialog);
        dialog.show();
        helper.request();
    }

    private class ClickReplyListener implements EasyRecyclerView.OnItemClickListener {

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
    }

    private class ReplyHolder extends RecyclerView.ViewHolder {

        public TextView leftText;
        public TextView rightText;
        public LinkifyTextView content;
        public LoadImageView thumb;

        public ReplyHolder(View itemView) {
            super(itemView);

            leftText = (TextView) itemView.findViewById(R.id.left_text);
            rightText = (TextView) itemView.findViewById(R.id.right_text);
            content = (LinkifyTextView) itemView.findViewById(R.id.content);
            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
        }
    }

    private class ReplyAdapter extends RecyclerView.Adapter<ReplyHolder> {

        @Override
        public ReplyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ReplyHolder(getLayoutInflater().inflate(R.layout.item_post, parent, false));
        }

        @Override
        public void onBindViewHolder(ReplyHolder holder, int position) {
            Reply reply = mReplyHelper.getDataAt(position);
            holder.leftText.setText(TextUtils2.combine(reply.getNMBDisplayTime(), "  ", reply.getNMBDisplay()));
            holder.rightText.setText(reply.getNMBId());
            holder.content.setText(reply.getNMBDisplayContent());

            String thumbUrl = reply.getNMBThumbUrl();
            if (!TextUtils.isEmpty(thumbUrl)) {
                holder.thumb.setVisibility(View.VISIBLE);
                holder.thumb.load(mConaco, thumbUrl, thumbUrl);
            } else {
                holder.thumb.setVisibility(View.GONE);
                mConaco.load(holder.thumb, null);
            }
        }

        @Override
        public int getItemCount() {
            return mReplyHelper.size();
        }
    }

    private class ReplyHelper extends ContentLayout.ContentHelper<Reply> {

        @Override
        protected Context getContext() {
            return PostActivity.this;
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

            NMBRequest request = new NMBRequest();
            mNMBRequest = request;
            request.setSite(mSite);
            request.setMethod(NMBClient.METHOD_GET_POST);
            request.setArgs(NMBUrl.getPostUrl(mSite, mId, page));
            request.setCallback(new PostListener(taskId, type, page, request));
            mNMBClient.execute(request);
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

                List<Reply> replies = result.second;
                if (mPage == 0) {
                    mPageSize = replies.size();
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

                if (!empty && (mTaskType == ContentLayout.ContentHelper.TYPE_NEXT_PAGE ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_NEXT_PAGE_KEEP_POS ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_REFRESH ||
                        mTaskType == ContentLayout.ContentHelper.TYPE_SOMEWHERE) &&
                        mReplyHelper.size() == post.getNMBReplyCount() + 1) { // post is in data, so +1
                    mReplyHelper.setPages(mPage + 1); // this is the last page
                } else if (mPageSize == 0) {
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
            }
            // Clear
            mRequest = null;
        }

        @Override
        public void onCancelled() {
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
