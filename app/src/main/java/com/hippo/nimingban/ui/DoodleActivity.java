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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hippo.app.ProgressDialogBuilder;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.R;
import com.hippo.nimingban.widget.DoodleView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.styleable.StyleableActivity;
import com.hippo.util.ReadableTime;
import com.hippo.yorozuya.MathUtils;

import java.io.File;

public final class DoodleActivity extends StyleableActivity implements View.OnClickListener, DoodleView.Helper {

    private DoodleView mDoodleView;

    private View mPalette;
    private View mUndo;
    private View mRedo;
    private View mClear;
    private View mSave;

    private File mOutputFile;

    private Dialog mExitWaitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File dir = NMBAppConfig.getDoodleDir();
        if (dir != null) {
            String filename = "doodle-" + ReadableTime.getFilenamableTime(System.currentTimeMillis()) + ".png";
            mOutputFile = new File(dir, filename);
        }

        setContentView(R.layout.activity_doodle);

        mDoodleView = (DoodleView) findViewById(R.id.doodle_view);
        mPalette = findViewById(R.id.palette);
        mUndo = findViewById(R.id.undo);
        mRedo = findViewById(R.id.redo);
        mClear = findViewById(R.id.clear);
        mSave = findViewById(R.id.save);

        mDoodleView.setHelper(this);

        RippleSalon.addRipple(mPalette, true);
        RippleSalon.addRipple(mUndo, true);
        RippleSalon.addRipple(mRedo, true);
        RippleSalon.addRipple(mClear, true);
        RippleSalon.addRipple(mSave, true);

        mPalette.setOnClickListener(this);
        mUndo.setOnClickListener(this);
        mRedo.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mSave.setOnClickListener(this);

        updateUndoRedo();

        if (mOutputFile == null) {
            Toast.makeText(this, R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mOutputFile != null) {
            mExitWaitingDialog = new ProgressDialogBuilder(this)
                    .setTitle(R.string.please_wait)
                    .setMessage(R.string.saving)
                    .setCancelable(false)
                    .show();
            mDoodleView.save(mOutputFile);
        } else {
            super.onBackPressed();
        }
    }

    private void updateUndoRedo() {
        mUndo.setEnabled(mDoodleView.canUndo());
        mRedo.setEnabled(mDoodleView.canRedo());
    }

    @Override
    public void onClick(View v) {
        if (mPalette == v) {
            mDoodleView.setPaintColor(MathUtils.random(0x00ffffff) | 0xff000000);
        } else if (mUndo == v) {
            mDoodleView.undo();
        } else if (mRedo == v) {
            mDoodleView.redo();
        } else if (mClear == v) {
            mDoodleView.clear();
        } else if (mSave == v) {
            if (mOutputFile == null) {
                Toast.makeText(this, R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
            } else {
                mDoodleView.save(mOutputFile);
            }
        }
    }

    @Override
    public void onStoreChange(DoodleView view) {
        updateUndoRedo();
    }

    @Override
    public void onSavingFinished(boolean ok) {
        if (mExitWaitingDialog != null) {
            mExitWaitingDialog.dismiss();
            mExitWaitingDialog = null;
            Intent intent = new Intent();
            intent.setData(Uri.fromFile(mOutputFile));
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, ok ? R.string.save_successfully : R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
