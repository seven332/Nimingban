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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hippo.conaco.DataContainer;
import com.hippo.conaco.ProgressNotifier;
import com.hippo.gukize.GukizeView;
import com.hippo.nimingban.R;
import com.hippo.nimingban.utils.FileDataContainer;
import com.hippo.nimingban.utils.FileInputStreamPipe;
import com.hippo.streampipe.InputStreamPipe;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.ResourcesUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

public class NMBCoverView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    private static final String URL_COVER = "http://cover.acfunwiki.org/cover.php";
    private static final String TIP_LOAD = "（<ゝπ・）☆ Kira";

    private GukizeView mCurrentCover;
    private GukizeView mBackupCover;
    private File mCoverFile;
    private File mCoverTempFile;
    private DataContainer mCoverTempContainer;
    private final ReentrantLock mCoverFileLock = new ReentrantLock();
    private final ReentrantLock mCoverTempFileLock = new ReentrantLock();

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
        mCoverTempContainer = new CoverContainer(mCoverTempFile, mCoverTempFileLock);

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
        mBackupCover.load(null, null, new CoverContainer(mCoverFile, mCoverFileLock));
    }

    public void loadNewCover() {
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
        public void onFailure() {}
        @Override
        public void onCancel() {}
        @Override
        public void onRetry() {}
        @Override
        public void onSuccess() {
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
        protected Boolean doInBackground(Void... params) {
            mCoverFileLock.lock();
            mCoverTempFileLock.lock();
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
                mCoverFileLock.unlock();
                mCoverTempFileLock.unlock();
            }
        }
    }

    //////////
    // Class to make cover file safe
    //////////
    private static class CoverContainer extends FileDataContainer {
        private final ReentrantLock mLock;
        public CoverContainer(@NonNull File file, ReentrantLock lock) {
            super(file);
            mLock = lock;
        }
        @Nullable
        @Override
        public InputStreamPipe get() {
            return new SafeInputStreamPipe(getFile(), mLock);
        }
        @Override
        public boolean save(InputStream is, long length,
                @Nullable String mediaType, @Nullable ProgressNotifier notify) {
            mLock.lock();
            final boolean ret = super.save(is, length, mediaType, notify);
            mLock.unlock();
            return ret;
        }
    }

    private static class SafeInputStreamPipe extends FileInputStreamPipe {
        private final ReentrantLock mLock;
        public SafeInputStreamPipe(@NonNull File file, ReentrantLock lock) {
            super(file);
            mLock = lock;
        }
        @NonNull
        @Override
        public InputStream open() throws IOException {
            mLock.lock();
            return super.open();
        }
        @Override
        public void close() {
            super.close();
            mLock.unlock();
        }
    }
}
