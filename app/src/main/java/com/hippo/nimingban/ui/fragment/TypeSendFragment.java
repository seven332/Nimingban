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

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.app.ProgressDialogBuilder;
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.easyrecyclerview.SimpleHolder;
import com.hippo.io.UriInputStreamPipe;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.Emoji;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.ac.data.ACPostStruct;
import com.hippo.nimingban.client.ac.data.ACReplyStruct;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.DisplayForum;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.content.UniversalProvider;
import com.hippo.nimingban.drawable.RoundSideDrawable;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.ui.DoodleActivity;
import com.hippo.nimingban.ui.DraftActivity;
import com.hippo.nimingban.ui.GalleryActivity2;
import com.hippo.nimingban.ui.QRCodeScanActivity;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.OpenUrlHelper;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.FontTextView;
import com.hippo.nimingban.widget.NMBEditText;
import com.hippo.ripple.Ripple;
import com.hippo.util.DrawableManager;
import com.hippo.util.ExceptionUtils;
import com.hippo.widget.SimpleImageView;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.SimpleHandler;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class TypeSendFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = TypeSendFragment.class.getSimpleName();

    @IntDef({METHOD_REPLY, METHOD_CREATE_POST})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Method {}

    public static final String ACTION_REPLY = "com.hippo.nimingban.ui.TypeSendActivity.action.REPLY";
    public static final String ACTION_CREATE_POST = "com.hippo.nimingban.ui.TypeSendActivity.action.CREATE_POST";
    public static final String ACTION_REPORT = "com.hippo.nimingban.ui.TypeSendActivity.action.REPORT";

    public static final String KEY_ACTION = "action";
    public static final String KEY_TYPE = "type";
    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";
    public static final String KEY_TEXT = "text";
    public static final String KEY_EXTRA_TEXT = Intent.EXTRA_TEXT;
    public static final String KEY_EXTRA_STREAM = Intent.EXTRA_STREAM;

    public static final int REQUEST_CODE_SELECT_IMAGE = 0;
    public static final int REQUEST_CODE_DRAFT = 1;
    public static final int REQUEST_CODE_DOODLE = 2;
    public static final int REQUEST_CODE_CAMERA = 3;

    public static final int METHOD_REPLY = 0;
    public static final int METHOD_CREATE_POST = 1;

    private List<DisplayForum> mForums;
    private CharSequence[] mForumNames;

    private boolean mReport;

    @Method
    private int mMethod = METHOD_CREATE_POST;
    private Site mSite;
    private String mId;
    private String mTitleText;
    private String mPresetText;

    private Uri mSeletedImageUri;
    private String mSeletedImageType;
    private Bitmap mSeletedImageBitmap;

    private Uri mCameraImageUri;

    private NMBClient mNMBClient;

    private NMBEditText mEditText;
    private SimpleImageView mEmoji;
    private View mImage;
    private View mDraw;
    private View mDraft;
    private View mSend;
    private View mImagePreview;
    private ImageView mPreview;
    private View mDelete;
    private ImageView mIndicator;
    private View mWritableItem;
    private EditText mName;
    private EditText mEmail;
    private EditText mTitle;
    private CheckBox mWatermark;
    private TextView mForumText;
    private EasyRecyclerView mEmojiKeyboard;

    private Drawable mEmojiOff;
    private Drawable mEmojiOn;

    private Dialog mProgressDialog;
    private NMBRequest mNMBRequest;

    private Callback mCallback;

    private IWXAPI mWxApi;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private boolean handleArgs(Bundle args) {
        if (args == null) {
            return false;
        }

        boolean share = false;
        mReport = false;
        String action = args.getString(KEY_ACTION);
        String type = args.getString(KEY_TYPE);

        if (ACTION_REPLY.equals(action)) {
            mMethod = METHOD_REPLY;
            mTitleText = getString(R.string.reply);
        } else if (ACTION_CREATE_POST.equals(action)) {
            mMethod = METHOD_CREATE_POST;
            mTitleText = getString(R.string.create_post);
        } else if (ACTION_REPORT.equals(action)) {
            mMethod = METHOD_CREATE_POST;
            mTitleText = getString(R.string.report);
            mReport = true;
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {
            mMethod = METHOD_CREATE_POST;
            mTitleText = getString(R.string.create_post);
            share = true;
        } else {
            return false;
        }

        if (share && type != null) {
            // Share
            // TODO for other site
            mSite = ACSite.getInstance();
            if ("text/plain".equals(type)) {
                mPresetText = args.getString(KEY_EXTRA_TEXT);
                return true;
            } else if (type.startsWith("image/")) {
                mSeletedImageUri = args.getParcelable(KEY_EXTRA_STREAM);
                return true;
            }
        } else {
            int site = args.getInt(KEY_SITE, -1);
            String id = args.getString(KEY_ID);
            mPresetText = args.getString(KEY_TEXT);
            if (Site.isValid(site) && id != null) {
                mSite = Site.fromId(site);
                mId = id;
                return true;
            }
        }

        return false;
    }

    private void prepareForCreatePost() {
        // Get all forums
        boolean sorting = Settings.getForumAutoSorting();
        mForums = DB.getACForums(false, sorting);
        if (mForums.size() == 0) {
            // TODO error
        } else {
            int n = mForums.size();
            mForumNames = new CharSequence[n];
            for (int i = 0; i < n; i++) {
                mForumNames[i] = mForums.get(i).getNMBDisplayname();
            }
        }

        // If no id, use first forum
        if (mId == null) {
            mId = mForums.get(0).id;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNMBClient = NMBApplication.getNMBClient(getContext());

        if (!handleArgs(getArguments())) {
            mMethod = METHOD_CREATE_POST;
            mSite = ACSite.getInstance();
            mId = null;
            mTitleText = getString(R.string.create_post);
            mPresetText = null;
            mSeletedImageUri = null;
        }

        // Show toast when report
        if (mReport) {
            Toast.makeText(getContext(), R.string.report_tip, Toast.LENGTH_SHORT).show();
        }

        // Prepare for create post
        if (METHOD_CREATE_POST == mMethod) {
            prepareForCreatePost();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.activity_toolbar, container, false);
        ViewGroup contentPanel = (ViewGroup) view.findViewById(R.id.content_panel);
        ViewGroup contentView = (ViewGroup) inflater.inflate(R.layout.fragment_type_send, contentPanel, true);

        view.setId(R.id.type_send);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(mTitleText);
        toolbar.setNavigationIcon(DrawableManager.getDrawable(getContext(), R.drawable.v_arrow_left_dark_x24));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClickBack(TypeSendFragment.this);
            }
        });

        mEditText = (NMBEditText) contentView.findViewById(R.id.edit_text);
        mEmoji = (SimpleImageView) contentView.findViewById(R.id.emoji);
        mImage = contentView.findViewById(R.id.image);
        mDraw = contentView.findViewById(R.id.draw);
        mDraft = contentView.findViewById(R.id.draft);
        mSend = contentView.findViewById(R.id.send);
        mImagePreview = contentView.findViewById(R.id.image_preview);
        mPreview = (ImageView) mImagePreview.findViewById(R.id.preview);
        mDelete = mImagePreview.findViewById(R.id.delete);
        mWatermark = (CheckBox) mImagePreview.findViewById(R.id.watermark);
        mIndicator = (ImageView) contentView.findViewById(R.id.indicator);
        TextView moreWritableItemsText = (TextView) contentView.findViewById(R.id.more_writable_items_text);
        View selectForum = contentView.findViewById(R.id.select_forum);
        mForumText = (TextView) selectForum.findViewById(R.id.forum_text);
        mWritableItem = contentView.findViewById(R.id.writable_item);
        mName = (EditText) mWritableItem.findViewById(R.id.name);
        mEmail = (EditText) mWritableItem.findViewById(R.id.email);
        mTitle = (EditText) mWritableItem.findViewById(R.id.title);

        mEmojiOff = DrawableManager.getDrawable(getContext(), R.drawable.v_emoji_off_dark);
        mEmojiOn = DrawableManager.getDrawable(getContext(), R.drawable.v_emoji_on_dark);
        mEmoji.setDrawable(mEmojiOff);

        if (Settings.getFixEmojiDisplay()) {
            mEditText.useCustomTypeface();
        } else {
            mEditText.useOriginalTypeface();
        }
        mEditText.requestFocus();
        mEditText.requestFocusFromTouch();

        // Show ime
        SimpleHandler.getInstance().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TypeSendFragment.this.isAdded()) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditText, 0);
                }
            }
        }, 300);

        Ripple.addRipple(mEmoji, true);
        Ripple.addRipple(mImage, true);
        Ripple.addRipple(mDraw, true);
        Ripple.addRipple(mDraft, true);
        Ripple.addRipple(mSend, true);
        Ripple.addRipple(mIndicator, ResourcesUtils.getAttrBoolean(getContext(), R.attr.dark));

        mEmoji.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mDraw.setOnClickListener(this);
        mDraft.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mIndicator.setOnClickListener(this);
        mPreview.setOnClickListener(this);

        // TODO Use AnimatedVectorDrawable
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_activated}, DrawableManager.getDrawable(getContext(), R.drawable.v_chevron_up_x24));
        drawable.addState(new int[]{}, DrawableManager.getDrawable(getContext(), R.drawable.v_chevron_down_x24));
        mIndicator.setImageDrawable(drawable);

        mWatermark.setChecked(Settings.getWatermark());

        if (METHOD_CREATE_POST == mMethod) {
            moreWritableItemsText.setVisibility(View.GONE);
            selectForum.setVisibility(View.VISIBLE);

            mForumText.setBackgroundDrawable(new RoundSideDrawable(
                    ResourcesUtils.getAttrColor(getContext(), R.attr.colorRoundSide)));

            int index = -1;
            for (int i = 0, length = mForums.size(); i < length; i++) {
                if (mId.equals(mForums.get(i).id)) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                Toast.makeText(getContext(), getString(R.string.cant_find_the_forum, mId), Toast.LENGTH_SHORT).show();
                setForum(0);
            } else {
                setForum(index);
            }

            // Can't select forum if you want report
            if (!mReport) {
                mForumText.setOnClickListener(this);
            }
        } else {
            moreWritableItemsText.setVisibility(View.VISIBLE);
            selectForum.setVisibility(View.GONE);
        }

        // Append preset text
        if (!TextUtils.isEmpty(mPresetText)) {
            mEditText.append(mPresetText);
        }

        handleSelectedImageUri(mSeletedImageUri);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mPreview != null && mImagePreview != null && mEditText != null) {
            clearImagePreview();
        }

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }

        // Hide ime keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Cancel FLAG_ALT_FOCUSABLE_IM
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mWxApi != null) {
            mWxApi.detach();
            mWxApi = null;
        }
    }

    /**
     * @return True for finish now, false for wait
     */
    public boolean checkBeforeFinish() {
        if (isEmojiKeyboardShown()) {
            hideEmojiKeyboard();
            return false;
        }

        final String text = mEditText.getText().toString().trim();
        if (!text.isEmpty()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        DB.addDraft(mEditText.getText().toString());
                        getFragmentHost().finishFragment(TypeSendFragment.this);
                    } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                        getFragmentHost().finishFragment(TypeSendFragment.this);
                    }
                }
            };

            AlertDialog dialog = new AlertDialog.Builder(getContext()).setMessage(R.string.save_text_draft)
                    .setPositiveButton(R.string.save, listener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.dont_save, listener)
                    .show();
            Button button = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            button.setTextColor(getResources().getColor(R.color.red_500));

            return false;
        } else {
            return true;
        }
    }

    private class EmojiKeyboardHelper extends RecyclerView.Adapter<SimpleHolder>
            implements EasyRecyclerView.OnItemClickListener {

        @Override
        public SimpleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SimpleHolder holder = new SimpleHolder(
                    getActivity().getLayoutInflater().inflate(R.layout.item_emoji, parent, false));
            if (Settings.getFixEmojiDisplay()) {
                ((FontTextView) holder.itemView).useCustomTypeface();
            } else {
                ((FontTextView) holder.itemView).useOriginalTypeface();
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(SimpleHolder holder, int position) {
            ((TextView) holder.itemView).setText(Emoji.EMOJI_NAME[position]);
        }

        @Override
        public int getItemCount() {
            return Emoji.COUNT;
        }

        @Override
        public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
            EditText editText = mEditText;
            String emoji = Emoji.EMOJI_VALUE[position];
            int start = Math.max(editText.getSelectionStart(), 0);
            int end = Math.max(editText.getSelectionEnd(), 0);
            editText.getText().replace(Math.min(start, end), Math.max(start, end),
                    emoji, 0, emoji.length());
            return true;
        }
    }

    private boolean isEmojiKeyboardShown() {
        return mEmojiKeyboard != null && mEmojiKeyboard.getVisibility() == View.VISIBLE;
    }

    private void showEmojiKeyboard() {
        mEmoji.setDrawable(mEmojiOn);

        // Hide ime keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Add FLAG_ALT_FOCUSABLE_IM
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        view = getView();
        if (view == null) {
            return;
        }

        if (mEmojiKeyboard == null) {
            ViewStub stub = (ViewStub) view.findViewById(R.id.stub);
            mEmojiKeyboard = (EasyRecyclerView) stub.inflate();
            EmojiKeyboardHelper helper = new EmojiKeyboardHelper();
            mEmojiKeyboard.setAdapter(helper);
            mEmojiKeyboard.setLayoutManager(new StaggeredGridLayoutManager(
                    3, StaggeredGridLayoutManager.VERTICAL));// TODO adjust by view width
            mEmojiKeyboard.setSelector(Ripple.generateRippleDrawable(
                    getContext(), ResourcesUtils.getAttrBoolean(getContext(), R.attr.dark)));
            mEmojiKeyboard.setOnItemClickListener(helper);
        }
        mEmojiKeyboard.setVisibility(View.VISIBLE);

        // Show a stub dialog to make FLAG_ALT_FOCUSABLE_IM work
        final Dialog dialog = new Dialog(getContext(), R.style.Theme_Dialog_DoNotDim);
        dialog.show();
        SimpleHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

    private void hideEmojiKeyboard() {
        mEmoji.setDrawable(mEmojiOff);

        // Clear FLAG_ALT_FOCUSABLE_IM
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        // Show ime keyboard
        SimpleHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEditText, 0);
            }
        });

        mEmojiKeyboard.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (mSend == v) {
            if (mProgressDialog != null || mNMBRequest != null) {
                return;
            }

            if (hasACCookies()) {
                doAction();
            } else {
                tryGettingCookies();
            }
        } else if (mEmoji == v) {
            if (isEmojiKeyboardShown()) {
                hideEmojiKeyboard();
            } else {
                showEmojiKeyboard();
            }
        } else if (mImage == v) {
            showImageDialog();
        } else if (mDraft == v) {
            Intent intent = new Intent(getActivity(), DraftActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DRAFT);
        } else if (mDraw == v) {
            Intent intent = new Intent(getActivity(), DoodleActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DOODLE);
        } else if (mDelete == v) {
            clearImagePreview();
        } else if (mIndicator == v) {
            if (mWritableItem.getVisibility() == View.VISIBLE) {
                mWritableItem.setVisibility(View.GONE);
                v.setActivated(false);
            } else {
                mWritableItem.setVisibility(View.VISIBLE);
                v.setActivated(true);
            }
        } else if (v == mForumText) {
            showForumDialog();
        } else if (v == mPreview) {
            if (mSeletedImageUri != null) {
                Intent intent = new Intent(getActivity(), GalleryActivity2.class);
                intent.setAction(GalleryActivity2.ACTION_IMAGE_FILE);
                intent.putExtra(GalleryActivity2.KEY_UNI_FILE_URI, mSeletedImageUri);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE_SELECT_IMAGE || requestCode == REQUEST_CODE_DOODLE)
                && resultCode == Activity.RESULT_OK) {
            handleSelectedImageUri(data.getData());
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            handleSelectedImageUri(mCameraImageUri);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setForum(int position) {
        DisplayForum forum = mForums.get(position);
        mId = forum.getNMBId();
        mForumText.setText(forum.getNMBDisplayname());
    }

    private boolean hasACCookies() {
        SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(getContext());
        URL url;
        try {
            url = new URL(ACUrl.getHost());
        } catch (MalformedURLException e) {
            // WTF ?
            return true;
        }
        return cookieStore.contain(url, "userhash");
    }

    private void doAction() {
        if (mMethod == METHOD_REPLY) {
            doReply();
        } else if (mMethod == METHOD_CREATE_POST) {
            doCreatePost();
        } else {
            Log.d(TAG, "WTF?, an unknown method in TypeSendActivity " + mMethod);
        }
    }

    private void showProgressDialog(int resId) {
        if (mProgressDialog != null) {
            return;
        }

        DialogInterface.OnClickListener clicklistener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mNMBRequest != null) {
                    mNMBRequest.cancel();
                    mNMBRequest = null;
                }
            }
        };

        DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mProgressDialog = null;
            }
        };

        mProgressDialog = new ProgressDialogBuilder(getContext())
                .setTitle(R.string.please_wait)
                .setMessage(resId)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, clicklistener)
                .setOnDismissListener(dismissListener)
                .show();
    }

    private void doReply() {
        ACReplyStruct struct = new ACReplyStruct();
        struct.name = mName.getText().toString();
        struct.email = mEmail.getText().toString();
        struct.title = mTitle.getText().toString();
        struct.content = mEditText.getText().toString();
        struct.resto = mId;
        struct.image = mSeletedImageUri != null ? new UriInputStreamPipe(
                getContext().getApplicationContext(), mSeletedImageUri) : null;
        struct.imageType = mSeletedImageType;
        struct.water = mWatermark.isChecked();

        NMBRequest request = new NMBRequest();
        request.setSite(mSite);
        request.setMethod(NMBClient.METHOD_REPLY);
        request.setArgs(struct);
        request.setCallback(new ActionListener(getContext(), mMethod, mId, struct.content, mSeletedImageBitmap));
        mNMBClient.execute(request);

        SimpleHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                getFragmentHost().finishFragment(TypeSendFragment.this);
            }
        });
    }

    private void doCreatePost() {
        ACPostStruct struct = new ACPostStruct();
        struct.name = mName.getText().toString();
        struct.email = mEmail.getText().toString();
        struct.title = mTitle.getText().toString();
        struct.content = mEditText.getText().toString();
        struct.fid = mId;
        struct.image = mSeletedImageUri != null ? new UriInputStreamPipe(
                getContext().getApplicationContext(), mSeletedImageUri) : null;
        struct.imageType = mSeletedImageType;
        struct.water = mWatermark.isChecked();

        NMBRequest request = new NMBRequest();
        request.setSite(mSite);
        request.setMethod(NMBClient.METHOD_CREATE_POST);
        request.setArgs(struct);
        request.setCallback(new ActionListener(getContext(), mMethod, mId, struct.content, mSeletedImageBitmap));
        mNMBClient.execute(request);

        SimpleHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                getFragmentHost().finishFragment(TypeSendFragment.this);
            }
        });
    }

    private void tryGettingCookies() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        showRegisterDialog();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        showAddCookieDialog();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        doAction();
                        break;
                }
            }
        };

        new AlertDialog.Builder(getContext()).setTitle(R.string.no_cookies)
                .setMessage(R.string.no_cookies_ac)
                .setPositiveButton(R.string.register, listener)
                .setNegativeButton(R.string.add_cookies, listener)
                .setNeutralButton(R.string.i_dont_care, listener).show();
    }

    private void showRegisterDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Web
                        String url = "http://adnmb.com/Member/User/Index/sendRegister.html";
                        OpenUrlHelper.openUrl(getActivity(), url, false);
                        break;
                    case 1:
                        // WeChat
                        if (mWxApi == null) {
                            String appId = "wxe59db8095c5f16de";
                            mWxApi = WXAPIFactory.createWXAPI(getActivity(), appId);
                        }

                        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
                        req.userName = "gh_f8c1b9909e51";
                        req.path = "pages/index/index?mode=reg";
                        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;
                        mWxApi.sendReq(req);
                        break;
                }
            }
        };

        new AlertDialog.Builder(getActivity())
                .setItems(R.array.register_methods, listener)
                .show();
    }

    private void showAddCookieDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Scan
                        Intent intent = new Intent(getActivity(), QRCodeScanActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        // WeChat
                        if (mWxApi == null) {
                            String appId = "wxe59db8095c5f16de";
                            mWxApi = WXAPIFactory.createWXAPI(getActivity(), appId);
                        }

                        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
                        req.userName = "gh_f8c1b9909e51";
                        req.path = "pages/index/index?mode=cookie";
                        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;
                        mWxApi.sendReq(req);
                        break;
                }
            }
        };

        new AlertDialog.Builder(getActivity())
                .setItems(R.array.add_cookies, listener)
                .show();
    }

    private void showImageDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                getString(R.string.select_picture)), REQUEST_CODE_SELECT_IMAGE);
                        break;
                    case 1:
                        if (TextUtils.isEmpty(mEditText.getText().toString()) || mProgressDialog != null) {
                            break;
                        }

                        showProgressDialog(R.string.converting);

                        new AsyncTask<Void, Void, Uri>() {
                            @Override
                            protected Uri doInBackground(Void... params) {
                                return saveEditTextToImage();
                            }

                            @Override
                            protected void onPostExecute(Uri uri) {
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;
                                }

                                if (uri != null) {
                                    handleSelectedImageUri(uri);
                                } else {
                                    Toast.makeText(getContext(), R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }.execute();
                        break;
                    case 2:
                        File dir = NMBAppConfig.getPhotoDir();
                        if (dir == null)
                            break;
                        File temp = new File(dir, ReadableTime.getFilenamableTime(System.currentTimeMillis()) + ".jpg");
                        mCameraImageUri = Uri.fromFile(temp);
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, UniversalProvider.buildUri(temp.getPath()));
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                        break;
                }
            }
        };

        new AlertDialog.Builder(getContext()).setItems(R.array.image_dialog, listener).show();
    }

    private void showForumDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setForum(which);
            }
        };

        new AlertDialog.Builder(getContext()).setItems(mForumNames, listener).show();
    }

    private void clearImagePreview() {
        mSeletedImageUri = null;
        mSeletedImageType = null;
        mSeletedImageBitmap = null;

        mPreview.setImageDrawable(null);
        mImagePreview.setVisibility(View.GONE);

        ViewGroup.LayoutParams lp = mEditText.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        } else{
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        mEditText.setLayoutParams(lp);
    }

    private void setImagePreview(Uri uri, String type, Bitmap bitmap) {
        mSeletedImageUri = uri;
        mSeletedImageType = type;
        mSeletedImageBitmap = bitmap;

        mPreview.setImageBitmap(bitmap);
        mImagePreview.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams lp = mEditText.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else{
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        mEditText.setLayoutParams(lp);
    }

    // TODO do not do it in UI thread
    private boolean handleSelectedImageUri(Uri uri) {
        if (uri == null) {
            return false;
        }

        ContentResolver resolver = getContext().getContentResolver();
        String type = resolver.getType(uri);
        if (type == null) {
            type =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    FileUtils.getExtensionFromFilename(uri.toString()));
        }

        int maxSize = LayoutUtils.dp2pix(getContext(), 256);
        Bitmap bitmap = BitmapUtils.decodeStream(new UriInputStreamPipe(getContext(), uri), maxSize, maxSize);
        if (bitmap != null) {
            setImagePreview(uri, type, bitmap);
            return true;
        }

        return false;
    }

    private Uri saveEditTextToImage() {
        EditText v = mEditText;
        Layout layout = v.getLayout();
        if (layout == null) {
            return null;
        }

        int padding = LayoutUtils.dp2pix(getContext(), 16);
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(layout.getWidth() + (2 * padding),
                    layout.getHeight() + (2 * padding), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(ResourcesUtils.getAttrColor(getContext(), R.attr.colorPure));
        canvas.translate(padding, padding);
        layout.draw(canvas);

        File file = NMBAppConfig.createTempFile("png");
        if (file == null) {
            return null;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return Uri.fromFile(file);
        } catch (IOException e) {
            return null;
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    private static class ActionListener implements NMBClient.Callback<Void> {

        private Context mContext;
        @Method
        private int mMethod;
        private String mId;
        private String mContent;
        private Bitmap mImage;

        public ActionListener(Context context, @Method int method, String id, String content, Bitmap image) {
            mContext = context.getApplicationContext();
            mMethod = method;
            mId = id;
            mContent = content;
            mImage = image;

            Toast.makeText(context, method == METHOD_REPLY ? R.string.start_reply : R.string.start_creating_post, Toast.LENGTH_SHORT).show();
        }

        private void addToRecord(String image) {
            int type;
            String recordid = null;
            String postid;
            String content = mContent;
            if (mMethod == METHOD_REPLY) {
                type = DB.AC_RECORD_REPLY;
                postid = mId;
            } else {
                type = DB.AC_RECORD_POST;
                postid = null;
            }

            DB.addACRecord(type, recordid, postid, content, image);
            // Notify RecordActivity
            Messenger.getInstance().notify(Constants.MESSENGER_ID_UPDATE_RECORD, null);
        }

        @Override
        public void onSuccess(Void result) {
            if (mImage != null) {
                // Save image file in new thread
                // TODO looks ugly
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void[] params) {
                        String image = null;
                        if (mImage != null) {
                            File imageFile = NMBAppConfig.createRecordImageFile();
                            if (imageFile != null) {
                                try {
                                    mImage.compress(Bitmap.CompressFormat.PNG,
                                            100, new FileOutputStream(imageFile));
                                    image = imageFile.getPath();
                                } catch (FileNotFoundException e) {
                                    // Ignore
                                }
                            }
                            mImage = null;
                        }
                        return image;
                    }

                    @Override
                    protected void onPostExecute(String image) {
                        addToRecord(image);
                    }
                }.execute();
            } else {
                addToRecord(null);
            }

            SimpleHandler.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, mMethod == METHOD_REPLY ? R.string.reply_successfully :
                            R.string.create_post_successfully, Toast.LENGTH_SHORT).show();
                    Messenger.getInstance().notify(METHOD_REPLY == mMethod ? Constants.MESSENGER_ID_REPLY : Constants.MESSENGER_ID_CREATE_POST, mId);
                }
            }, 1000); // Wait a seconds to make sure the server has done with the post
        }

        @Override
        public void onFailure(final Exception e) {
            mImage = null;

            if (!TextUtils.isEmpty(mContent)) {
                DB.addDraft(mContent);
            }

            SimpleHandler.getInstance().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Context context = mContext;
                    Toast.makeText(context, context.getString(mMethod == METHOD_REPLY ? R.string.reply_failed :
                            R.string.create_post_failed) + "\n" +
                            ExceptionUtils.getReadableString(context, e), Toast.LENGTH_SHORT).show();
                    Messenger.getInstance().notify(METHOD_REPLY == mMethod ?
                            Constants.MESSENGER_ID_REPLY : Constants.MESSENGER_ID_CREATE_POST, mId);
                }
            }, 1000); // Wait a seconds to make sure the server has done with the post
        }

        @Override
        public void onCancel() {
            mImage = null;

            Log.d(TAG, "ActionListener onCancel");
        }
    }

    private class GetCookieListener implements NMBClient.Callback<Boolean> {
        @Override
        public void onSuccess(Boolean result) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Toast.makeText(getContext(), R.string.got_cookies, Toast.LENGTH_SHORT).show();

            doAction();
        }

        @Override
        public void onFailure(Exception e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Toast.makeText(getContext(), getString(R.string.cant_get_cookies) + "\n" +
                    ExceptionUtils.getReadableString(getContext(), e), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Log.d(TAG, "GetCookieListener onCancel");
        }
    }

    public interface Callback {

        void onClickBack(TypeSendFragment fragment);
    }
}
