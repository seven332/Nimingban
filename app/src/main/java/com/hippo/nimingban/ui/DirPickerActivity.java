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
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.nimingban.PermissionRequester;
import com.hippo.nimingban.R;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.DirExplorer;
import com.hippo.rippleold.RippleSalon;
import com.hippo.yorozuya.ResourcesUtils;

import java.io.File;

public class DirPickerActivity extends TranslucentActivity implements View.OnClickListener, DirExplorer.OnChangeDirListener {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;

    public static final String KEY_FILE_URI = "file_uri";

    private TextView mPath;
    private DirExplorer mDirExplorer;
    private View mOk;

    @Override
    protected int getLightThemeResId() {
        return Settings.getColorStatusBar() ? R.style.NormalActivity : R.style.NormalActivity_NoStatus;
    }

    @Override
    protected int getDarkThemeResId() {
        return Settings.getColorStatusBar() ? R.style.NormalActivity_Dark : R.style.NormalActivity_Dark_NoStatus;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        ToolbarActivityHelper.setContentView(this, R.layout.activity_dir_picker);
        setActionBarUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_left_dark_x24));

        mPath = (TextView) findViewById(R.id.path);
        mDirExplorer = (DirExplorer) findViewById(R.id.dir_explorer);
        mOk = findViewById(R.id.ok);

        File file = null;
        Intent intent = getIntent();
        if (intent != null) {
            Uri fileUri = intent.getParcelableExtra(KEY_FILE_URI);
            if (fileUri != null) {
                file = new File(fileUri.getPath());
            }
        }
        mDirExplorer.setCurrentFile(file);
        mDirExplorer.setOnChangeDirListener(this);

        RippleSalon.addRipple(mOk, ResourcesUtils.getAttrBoolean(this, R.attr.dark));

        mOk.setOnClickListener(this);

        mPath.setText(mDirExplorer.getCurrentFile().getPath());

        // Check permission
        PermissionRequester.request(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                getString(R.string.dir_picker_permission_tip), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.you_rejected_me, Toast.LENGTH_SHORT).show();
            }
            mDirExplorer.updateFileList();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
    public void onClick(@NonNull View v) {
        if (mOk == v) {
            File file = mDirExplorer.getCurrentFile();
            if (!file.canWrite()) {
                Toast.makeText(this, R.string.directory_not_writable, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent();
                intent.setData(Uri.fromFile(file));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onChangeDir(File dir) {
        mPath.setText(dir.getPath());
    }
}
