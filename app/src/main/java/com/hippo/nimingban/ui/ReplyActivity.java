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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.NMBClient;
import com.hippo.nimingban.client.NMBRequest;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.ac.data.ACReplyStruct;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.ExceptionUtils;

import java.net.MalformedURLException;
import java.net.URL;

public final class ReplyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ReplyActivity.class.getSimpleName();

    public static final String ACTION_REPLY = "com.hippo.nimingban.ui.ReplyActivity.action.REPLY";

    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";

    public NMBClient mNMBClient;

    private EditText mEditText;
    private View mEmoji;
    private View mImage;
    private View mDraw;
    private View mSend;

    private int mSite;
    private String mId;

    private ProgressDialog mGetCookiePDiglog;
    private ProgressDialog mReplyPDiglog;


    // false for error
    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_REPLY.equals(action)) {
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

        mNMBClient = NMBApplication.getNMBClient(this);

        setContentView(R.layout.activity_reply);

        mEditText = (EditText) findViewById(R.id.edit_text);
        mEmoji = findViewById(R.id.emoji);
        mImage = findViewById(R.id.image);
        mDraw = findViewById(R.id.draw);
        mSend = findViewById(R.id.send);

        RippleSalon.addRipple(mEmoji, true);
        RippleSalon.addRipple(mImage, true);
        RippleSalon.addRipple(mDraw, true);
        RippleSalon.addRipple(mSend, true);

        mEmoji.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mDraw.setOnClickListener(this);
        mSend.setOnClickListener(this);
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
        return cookieStore.contain(url, "userId");
    }

    private void doReply() {
        mReplyPDiglog = ProgressDialog.show(this, getString(R.string.replying), null, true, false);

        ACReplyStruct struct = new ACReplyStruct();
        struct.name = null;
        struct.email = null;
        struct.title = null;
        struct.content = mEditText.getText().toString();
        struct.resto = mId;
        struct.image = null;

        NMBRequest request = new NMBRequest();
        request.setSite(NMBClient.AC);
        request.setMethod(NMBClient.METHOD_GET_REPLY);
        request.setArgs(struct);
        request.setCallback(new ReplyListener());
        mNMBClient.execute(request);
    }

    private void getCookies() {
        mGetCookiePDiglog = ProgressDialog.show(this, getString(R.string.getting_cookies), null, true, false);

        NMBRequest request = new NMBRequest();
        request.setSite(NMBClient.AC);
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
                        doReply();
                        break;
                }
            }
        };

        new AlertDialog.Builder(this).setTitle(R.string.no_cookies)
                .setMessage(R.string.no_cookies_ac)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .setNeutralButton(R.string.i_dont_mind, listener).show();
    }

    @Override
    public void onClick(View v) {
        if (mSend == v) {
            if (mReplyPDiglog != null) {
                Toast.makeText(this, R.string.replying, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mGetCookiePDiglog != null) {
                Toast.makeText(this, R.string.getting_cookies, Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasACCookies()) {
                doReply();
            } else {
                tryGettingCookies();
            }
        }
    }

    private class ReplyListener implements NMBClient.Callback<Boolean> {
        @Override
        public void onSuccess(Boolean result) {
            mReplyPDiglog.dismiss();
            mReplyPDiglog = null;

            Toast.makeText(ReplyActivity.this, R.string.reply_successfully, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void onFailure(Exception e) {
            mReplyPDiglog.dismiss();
            mReplyPDiglog = null;

            Toast.makeText(ReplyActivity.this, getString(R.string.reply_failed) +
                    ExceptionUtils.getReadableString(ReplyActivity.this, e), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled() {
            mReplyPDiglog.dismiss();
            mReplyPDiglog = null;

            Log.d(TAG, "ReplyListener onCancel");
        }
    }

    private class GetCookieListener implements NMBClient.Callback<Boolean> {
        @Override
        public void onSuccess(Boolean result) {
            mGetCookiePDiglog.dismiss();
            mGetCookiePDiglog = null;

            Toast.makeText(ReplyActivity.this, R.string.got_cookies, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Exception e) {
            mGetCookiePDiglog.dismiss();
            mGetCookiePDiglog = null;

            Toast.makeText(ReplyActivity.this, getString(R.string.cant_get_cookies) +
                    ExceptionUtils.getReadableString(ReplyActivity.this, e), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled() {
            mGetCookiePDiglog.dismiss();
            mGetCookiePDiglog = null;

            Log.d(TAG, "GetCookieListener onCancel");
        }
    }
}
