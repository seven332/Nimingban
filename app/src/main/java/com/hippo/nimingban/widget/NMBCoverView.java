/*
 * Copyright 2016 Hippo Seven
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

package com.hippo.nimingban.widget;

/*
 * Created by Hippo on 10/7/2016.
 */

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hippo.conaco.DataContainer;
import com.hippo.gukize.GukizeView;
import com.hippo.nimingban.R;
import com.hippo.nimingban.utils.FileDataContainer;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.ResourcesUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO auto save cover
public class NMBCoverView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    private static final String URL_COVER = "http://cover.acfunwiki.org/cover.php";
    private static final String TIP_LOAD = "（<ゝπ・）☆ Kira";

    private GukizeView mCurrentCover;
    private GukizeView mBackupCover;
    private File mCoverFile;
    private File mCoverTempFile;
    private DataContainer mCoverTempContainer;
    private SaveTask mSaveTask;
    private boolean mLoadLocalDone;
    private boolean mPendingLoadNewCover;

    public NMBCoverView(Context context) {
        super(context);
        init(context);
    }

    public NMBCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NMBCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_nmb_cover, this, true);
        mCurrentCover = (GukizeView) findViewById(R.id.cover1);
        mBackupCover = (GukizeView) findViewById(R.id.cover2);
        mCoverFile = new File(context.getFilesDir(), "cover");
        mCoverTempFile = new File(context.getFilesDir(), "cover.temp");
        mCoverTempContainer = new FileDataContainer(mCoverTempFile);

        setOnClickListener(this);
        setOnLongClickListener(this);

        final CoverViewListener listener = new CoverViewListener();
        mCurrentCover.setListener(listener);
        mBackupCover.setListener(listener);

        // Load default color drawable
        mCurrentCover.setVisibility(VISIBLE);
        mBackupCover.setVisibility(GONE);
        mCurrentCover.setCustomDrawable(new ColorDrawable(ResourcesUtils.getAttrColor(context, R.attr.colorAccent)));

        // Load cover file
        mBackupCover.load(null, null, new FileDataContainer(mCoverFile));
    }

    public void loadNewCover() {
        if (!mLoadLocalDone) {
            // Loading local cover now.
            // Load new cover after local done.
            mPendingLoadNewCover = true;
        } else if (mBackupCover.isLoading() || mSaveTask != null || mPendingLoadNewCover) {
            // Loading new cover now or will load new cover.
            // Do nothing.
        } else {
            loadNewCoverInternal();
        }
    }

    public void loadNewCoverInternal() {
        mBackupCover.load(null, URL_COVER, mCoverTempContainer);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // A trick to make width == height
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void onClick(View v) {
        // TODO show large image
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(getContext(), TIP_LOAD, Toast.LENGTH_SHORT).show();
        loadNewCover();
        return true;
    }

    private class CoverViewListener implements GukizeView.Listener {

        @Override
        public void onLoad() {}

        @Override
        public void onProgress(long l, long l1, long l2) {}

        @Override
        public void onFailure() {
            mLoadLocalDone = true;

            if (mPendingLoadNewCover) {
                mPendingLoadNewCover = false;
                loadNewCoverInternal();
            }
        }

        @Override
        public void onCancel() {
            // Should not set mLoadLocalDone true here
            // NMBCoverView will not cancel local cover loading
        }

        @Override
        public void onRetry() {}

        @Override
        public void onSuccess() {
            mLoadLocalDone = true;

            // Exchange mCurrentCover and mBackupCover
            final GukizeView temp = mCurrentCover;
            mCurrentCover = mBackupCover;
            mBackupCover = temp;

            // Show current cover, clear backup cover
            mCurrentCover.setVisibility(VISIBLE);
            mBackupCover.setVisibility(GONE);
            mBackupCover.setCustomDrawable(null);

            // Copy cover file
            new SaveTask().execute();
        }
    }

    // Task to copy mCoverTempFile to mCoverFile
    private class SaveTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mSaveTask = this;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(mCoverTempFile);
                os = new FileOutputStream(mCoverFile);
                IOUtils.copy(is, os);
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
                mCoverTempFile.delete();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mSaveTask = null;
            if (mPendingLoadNewCover) {
                mPendingLoadNewCover = false;
                loadNewCoverInternal();
            }
        }
    }
}
