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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.app.ProgressDialogBuilder;
import com.hippo.io.UriInputStreamPipe;
import com.hippo.nimingban.Analysis;
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
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.nimingban.util.DB;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.ExceptionUtils;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.SimpleImageView;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.widget.recyclerview.SimpleHolder;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.SimpleHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

// TODO add edit text for name, title and so on
public final class TypeSendActivity extends TranslucentActivity implements View.OnClickListener {

    @IntDef({METHOD_NONE, METHOD_REPLY, METHOD_CREATE_POST})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Method {}

    private static final String TAG = TypeSendActivity.class.getSimpleName();

    public static final String ACTION_REPLY = "com.hippo.nimingban.ui.TypeSendActivity.action.REPLY";
    public static final String ACTION_CREATE_POST = "com.hippo.nimingban.ui.TypeSendActivity.action.CREATE_POST";
    public static final String ACTION_REPORT = "com.hippo.nimingban.ui.TypeSendActivity.action.REPORT";

    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";
    public static final String KEY_TEXT = "text";

    public static final int REQUEST_CODE_SELECT_IMAGE = 0;
    public static final int REQUEST_CODE_DRAFT = 1;
    public static final int REQUEST_CODE_DOODLE = 2;
    public static final int REQUEST_CODE_CAMERA = 3;

    public static final int METHOD_NONE = 0;
    public static final int METHOD_REPLY = 1;
    public static final int METHOD_CREATE_POST = 2;

    @Method
    private int mMethod = METHOD_NONE;

    private boolean mShare = false;

    private NMBClient mNMBClient;

    private EditText mEditText;
    private View mEmoji;
    private View mImage;
    private View mDraw;
    private View mDraft;
    private View mSend;
    private View mImagePreview;
    private ImageView mPreview;
    private View mDelete;
    private SimpleImageView mIndicator;
    private View mWritableItem;
    private EditText mName;
    private EditText mEmail;
    private EditText mTitle;
    private TextView mMoreWritableItemsText;
    private View mSelectForum;
    private TextView mForumText;

    private List<DisplayForum> mForums;
    private CharSequence[] mForumNames;

    private Site mSite;
    private String mId;
    private String mPresetText;

    private Uri mSeletedImageUri;
    private String mSeletedImageType;
    private Bitmap mSeletedImageBitmap;

    private Uri mCameraImageUri;

    private Dialog mProgressDialog;
    private NMBRequest mNMBRequest;

    private String mConvertConfig;

    // false for error
    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        boolean report = false;

        String action = intent.getAction();
        String type = intent.getType();
        if (ACTION_REPLY.equals(action)) {
            mMethod = METHOD_REPLY;
            setTitle(R.string.reply);
        } else if (ACTION_CREATE_POST.equals(action)) {
            mMethod = METHOD_CREATE_POST;
            setTitle(R.string.create_post);
        } else if (ACTION_REPORT.equals(action)) {
            mMethod = METHOD_CREATE_POST;
            setTitle(R.string.report);
            report = true;
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {
            mMethod = METHOD_CREATE_POST;
            setTitle(R.string.create_post);
            mShare = true;
        }

        if (mShare && type != null) {
            // TODO for other site
            mSite = ACSite.getInstance();
            if ("text/plain".equals(type)) {
                mPresetText = intent.getStringExtra(Intent.EXTRA_TEXT);
                return true;
            } else if (type.startsWith("image/")) {
                mSeletedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                return true;
            }
        } else if (mMethod != METHOD_NONE) {
            int site = intent.getIntExtra(KEY_SITE, -1);
            String id = intent.getStringExtra(KEY_ID);
            mPresetText = intent.getStringExtra(KEY_TEXT);
            if (Site.isValid(site) && id != null) {
                mSite = Site.fromId(site);
                mId = id;

                if (report) {
                    Toast.makeText(this, R.string.report_tip, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    protected int getLightThemeResId() {
        return R.style.NormalActivity;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.NormalActivity_Dark;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!handlerIntent(getIntent())) {
            finish();
            return;
        }

        mNMBClient = NMBApplication.getNMBClient(this);

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        ToolbarActivityHelper.setContentView(this, R.layout.activity_type_send);
        setActionBarUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_left_dark_x24));

        mEditText = (EditText) findViewById(R.id.edit_text);
        mEmoji = findViewById(R.id.emoji);
        mImage = findViewById(R.id.image);
        mDraw = findViewById(R.id.draw);
        mDraft = findViewById(R.id.draft);
        mSend = findViewById(R.id.send);
        mImagePreview = findViewById(R.id.image_preview);
        mPreview = (ImageView) mImagePreview.findViewById(R.id.preview);
        mDelete = mImagePreview.findViewById(R.id.delete);
        mIndicator = (SimpleImageView) findViewById(R.id.indicator);
        mWritableItem = findViewById(R.id.writable_item);
        mName = (EditText) findViewById(R.id.name);
        mEmail = (EditText) findViewById(R.id.email);
        mTitle = (EditText) findViewById(R.id.title);
        mMoreWritableItemsText = (TextView) findViewById(R.id.more_writable_items_text);
        mSelectForum = findViewById(R.id.select_forum);
        mForumText = (TextView) mSelectForum.findViewById(R.id.forum_text);

        RippleSalon.addRipple(mEmoji, true);
        RippleSalon.addRipple(mImage, true);
        RippleSalon.addRipple(mDraw, true);
        RippleSalon.addRipple(mDraft, true);
        RippleSalon.addRipple(mSend, true);
        RippleSalon.addRipple(mIndicator, ResourcesUtils.getAttrBoolean(this, R.attr.dark));

        mEmoji.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mDraw.setOnClickListener(this);
        mDraft.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mIndicator.setOnClickListener(this);
        mForumText.setOnClickListener(this);
        mPreview.setOnClickListener(this);

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_activated}, VectorDrawable.create(this, R.drawable.ic_chevron_up));
        drawable.addState(new int[]{}, VectorDrawable.create(this, R.drawable.ic_chevron_down));
        mIndicator.setDrawable(drawable);

        mWritableItem.setVisibility(View.GONE);

        if (mShare) {
            mMoreWritableItemsText.setVisibility(View.GONE);
            mSelectForum.setVisibility(View.VISIBLE);

            mForums = DB.getACForums(false);
            if (mForums.size() == 0) {
                Toast.makeText(this, R.string.cant_find_forum, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                int n = mForums.size();
                mForumNames = new CharSequence[n];
                for (int i = 0; i < n; i++) {
                    mForumNames[i] = mForums.get(i).getNMBDisplayname();
                }
                setForum(0);
            }
        } else {
            mMoreWritableItemsText.setVisibility(View.VISIBLE);
            mSelectForum.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mPresetText)) {
            mEditText.append(mPresetText);
        }

        handleSelectedImageUri(mSeletedImageUri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPreview != null && mImagePreview != null && mEditText != null) {
            clearImagePreview();
        }

        if (mNMBRequest != null) {
            mNMBRequest.cancel();
            mNMBRequest = null;
        }
    }

    @Override
    public void onBackPressed() {
        final String text = mEditText.getText().toString().trim();
        if (!text.isEmpty()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        DB.addDraft(mEditText.getText().toString());
                        finish();
                    } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                        finish();
                    }
                }
            };

            AlertDialog dialog = new AlertDialog.Builder(this).setMessage(R.string.save_text_draft)
                    .setPositiveButton(R.string.save, listener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.dont_save, listener)
                    .show();
            Button button = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            button.setTextColor(getResources().getColor(R.color.red_500));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_type_send, menu);

        String country = Locale.getDefault().getCountry();
        if ("CN".equals(country)) {
            mConvertConfig = "s2twp.json";
        } else if ("TW".equals(country)) {
            mConvertConfig = "tw2sp.json";
        }

        if (TextUtils.isEmpty(mConvertConfig) || !Settings.getConvert()) {
            MenuItem item = menu.findItem(R.id.action_convert);
            if (item != null) {
                item.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_convert:
                if (TextUtils.isEmpty(mConvertConfig) || mProgressDialog != null) {
                    return true;
                }
                String text = mEditText.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    return true;
                }
                showProgressDialog(R.string.converting);

                NMBRequest request = new NMBRequest();
                mNMBRequest = request;
                request.setSite(mSite);
                request.setMethod(NMBClient.METHOD_CONVERT);
                request.setArgs(mConvertConfig, text);
                request.setCallback(new ConvertListener());
                mNMBClient.execute(request);

                return true;
            case R.id.action_text_image:
                if (TextUtils.isEmpty(mEditText.getText().toString())) {
                    return true;
                }

                if (mProgressDialog != null) {
                    return true;
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
                            Toast.makeText(TypeSendActivity.this, R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setForum(int position) {
        DisplayForum forum = mForums.get(position);
        mId = forum.getNMBId();
        mForumText.setText(forum.getNMBDisplayname());
    }

    private boolean hasACCookies() {
        SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(this);
        URL url;
        try {
            url = new URL(ACUrl.HOST);
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

        mProgressDialog = new ProgressDialogBuilder(this)
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
        struct.image = mSeletedImageUri != null ? new UriInputStreamPipe(getApplicationContext(), mSeletedImageUri) : null;
        struct.imageType = mSeletedImageType;

        NMBRequest request = new NMBRequest();
        request.setSite(mSite);
        request.setMethod(NMBClient.METHOD_REPLY);
        request.setArgs(struct);
        request.setCallback(new ActionListener(this, mMethod, mId, struct.content, mSeletedImageBitmap));
        mNMBClient.execute(request);

        finish();
    }

    private void doCreatePost() {
        ACPostStruct struct = new ACPostStruct();
        struct.name = mName.getText().toString();
        struct.email = mEmail.getText().toString();
        struct.title = mTitle.getText().toString();
        struct.content = mEditText.getText().toString();
        struct.fid = mId;
        struct.image = mSeletedImageUri != null ? new UriInputStreamPipe(getApplicationContext(), mSeletedImageUri) : null;
        struct.imageType = mSeletedImageType;

        NMBRequest request = new NMBRequest();
        request.setSite(mSite);
        request.setMethod(NMBClient.METHOD_CREATE_POST);
        request.setArgs(struct);
        request.setCallback(new ActionListener(this, mMethod, mId, struct.content, mSeletedImageBitmap));
        mNMBClient.execute(request);

        finish();
    }

    private void getCookies() {
        showProgressDialog(R.string.getting_cookies);

        NMBRequest request = new NMBRequest();
        mNMBRequest = request;
        request.setSite(mSite);
        request.setMethod(NMBClient.METHOD_GET_COOKIE);
        request.setCallback(new GetCookieListener());
        mNMBClient.execute(request);
    }

    private void tryGettingCookies() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        getCookies();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        doAction();
                        break;
                }
            }
        };

        new AlertDialog.Builder(this).setTitle(R.string.no_cookies)
                .setMessage(R.string.no_cookies_ac)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .setNeutralButton(R.string.i_dont_care, listener).show();
    }

    private class EmojiDialogHelper implements EasyRecyclerView.OnItemClickListener,
            DialogInterface.OnDismissListener {

        private Dialog mDialog;
        private View mView;

        private EmojiDialogHelper() {
            @SuppressLint("InflateParams")
            EasyRecyclerView recyclerView = (EasyRecyclerView) TypeSendActivity.this
                    .getLayoutInflater().inflate(R.layout.dialog_emoji, null);
            recyclerView.setAdapter(new EmojiAdapter());
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                    3, StaggeredGridLayoutManager.VERTICAL));// TODO adjust by view width
            recyclerView.setSelector(RippleSalon.generateRippleDrawable(
                    ResourcesUtils.getAttrBoolean(TypeSendActivity.this, R.attr.dark)));
            recyclerView.setOnItemClickListener(this);
            mView = recyclerView;
        }

        public View getView() {
            return mView;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
            if (mDialog != null) {
                EditText editText = mEditText;
                String emoji = Emoji.EMOJI_VALUE[position];
                int start = Math.max(editText.getSelectionStart(), 0);
                int end = Math.max(editText.getSelectionEnd(), 0);
                editText.getText().replace(Math.min(start, end), Math.max(start, end),
                        emoji, 0, emoji.length());
                mDialog.dismiss();
                mDialog = null;
            }
            return true;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog = null;
        }


        private class EmojiAdapter extends RecyclerView.Adapter<SimpleHolder> {

            @SuppressLint("InflateParams")
            @Override
            public SimpleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new SimpleHolder(TypeSendActivity.this
                        .getLayoutInflater().inflate(R.layout.item_emoji, null));
            }

            @Override
            public void onBindViewHolder(SimpleHolder holder, int position) {
                ((TextView) holder.itemView).setText(Emoji.EMOJI_NAME[position]);
            }

            @Override
            public int getItemCount() {
                return Emoji.COUNT;
            }
        }
    }

    private void showEmojiDialog() {
        EmojiDialogHelper helper = new EmojiDialogHelper();
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(helper.getView())
                .setOnDismissListener(helper)
                .create();
        helper.setDialog(dialog);
        dialog.show();
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
                        File dir = NMBAppConfig.getPhotoDir();
                        if (dir == null)
                            break;
                        File temp = new File(dir, ReadableTime.getFilenamableTime(System.currentTimeMillis()) + ".jpg");
                        mCameraImageUri = Uri.fromFile(temp);
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                        break;
                }
            }
        };

        new AlertDialog.Builder(this).setItems(R.array.image_dialog, listener).show();
    }

    private void showForumDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setForum(which);
            }
        };

        new AlertDialog.Builder(this).setItems(mForumNames, listener).show();
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
            showEmojiDialog();
        } else if (mImage == v) {
            showImageDialog();
        } else if (mDraft == v) {
            Intent intent = new Intent(TypeSendActivity.this, DraftActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DRAFT);
        } else if (mDraw == v) {
            Intent intent = new Intent(TypeSendActivity.this, DoodleActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DOODLE);
        } else if (mDelete == v) {
            clearImagePreview();
        } else if (mIndicator == v) {
            View view = mWritableItem;
            if (view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
                v.setActivated(true);
            } else {
                view.setVisibility(View.GONE);
                v.setActivated(false);
            }
        } else if (v == mForumText) {
            showForumDialog();
        } else if (v == mPreview) {
            if (mSeletedImageUri != null) {
                Intent intent = new Intent(this, GalleryActivity2.class);
                intent.setAction(GalleryActivity2.ACTION_IMAGE_FILE);
                intent.putExtra(GalleryActivity2.KEY_FILE_URI, mSeletedImageUri);
                startActivity(intent);
            }
        }
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

        ContentResolver resolver = getContentResolver();
        String type = resolver.getType(uri);
        if (type == null) {
            type =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    FileUtils.getExtensionFromFilename(uri.toString()));
        }

        int maxSize = LayoutUtils.dp2pix(this, 256);
        Bitmap bitmap = BitmapUtils.decodeStream(new UriInputStreamPipe(this, uri), maxSize, maxSize);
        if (bitmap != null) {
            setImagePreview(uri, type, bitmap);
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE_SELECT_IMAGE || requestCode == REQUEST_CODE_DOODLE) && resultCode == RESULT_OK) {
            handleSelectedImageUri(data.getData());
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            handleSelectedImageUri(mCameraImageUri);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri saveEditTextToImage() {
        EditText v = mEditText;
        Layout layout = v.getLayout();
        if (layout == null) {
            return null;
        }

        int padding = LayoutUtils.dp2pix(this, 16);
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(layout.getWidth() + (2 * padding),
                    layout.getHeight() + (2 * padding), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(ResourcesUtils.getAttrColor(this, R.attr.colorPure));
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
            // Analysis
            if (mMethod == METHOD_REPLY) {
                Analysis.replyPost(mContext, mId, true);
            } else {
                Analysis.createPost(mContext, mId, true);
            }

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
            // Analysis
            if (mMethod == METHOD_REPLY) {
                Analysis.replyPost(mContext, mId, false);
            } else {
                Analysis.createPost(mContext, mId, false);
            }

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
                    Messenger.getInstance().notify(METHOD_REPLY == mMethod ? Constants.MESSENGER_ID_REPLY : Constants.MESSENGER_ID_CREATE_POST, mId);
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

            Toast.makeText(TypeSendActivity.this, R.string.got_cookies, Toast.LENGTH_SHORT).show();

            doAction();
        }

        @Override
        public void onFailure(Exception e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Toast.makeText(TypeSendActivity.this, getString(R.string.cant_get_cookies) + "\n" +
                    ExceptionUtils.getReadableString(TypeSendActivity.this, e), Toast.LENGTH_SHORT).show();
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

    private class ConvertListener implements NMBClient.Callback<String> {

        @Override
        public void onSuccess(String result) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            mEditText.setText(result);

            Toast.makeText(TypeSendActivity.this, R.string.convert_successfully, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Exception e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Toast.makeText(TypeSendActivity.this, getString(R.string.convert_failed) + "\n" +
                    ExceptionUtils.getReadableString(TypeSendActivity.this, e), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mNMBRequest = null;

            Log.d(TAG, "ConvertListener onCancel");
        }
    }
}
