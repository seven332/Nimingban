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

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.hippo.conaco.Conaco;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.widget.GalleryPage;
import com.hippo.unifile.UniFile;
import com.hippo.widget.viewpager.PagerHolder;
import com.hippo.widget.viewpager.RecyclerPagerAdapter;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO show all image in post
public class GalleryActivity2 extends AppCompatActivity {

    public static final String ACTION_SINGLE_IMAGE = "com.hippo.nimingban.ui.GalleryActivity.action.SINGLE_IMAGE";
    public static final String ACTION_IMAGE_FILE = "com.hippo.nimingban.ui.GalleryActivity.action.IMAGE_FILE";

    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FILE_URI = "file_uri";

    private ViewPager mViewPager;
    private GalleryAdapter mGalleryAdapter;

    private SaveTask mSaveTask;

    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_SINGLE_IMAGE.equals(action)) {
            int site = intent.getIntExtra(KEY_SITE, -1);
            String id = intent.getStringExtra(KEY_ID);
            String image = intent.getStringExtra(KEY_IMAGE);
            if (Site.isValid(site) && id != null && image != null) {
                mGalleryAdapter = new SingleImageAdapter(Site.fromId(site), id, image);
                return true;
            }
        } else if (ACTION_IMAGE_FILE.equals(action)) {
            Uri fileUri = intent.getParcelableExtra(KEY_FILE_URI);
            File file = new File(fileUri.getPath());
            if (file.exists()) {
                mGalleryAdapter = new ImageFileAdapter(file);
                return true;
            }
        }

        if (mSaveTask != null) {
            mSaveTask.onActivityDestory();
            mSaveTask = null;
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

        setContentView(R.layout.activity_gallery_2);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPager.setAdapter(mGalleryAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unload all pager
        for (int i = 0, n = mViewPager.getChildCount(); i < n; i++) {
            View child = mViewPager.getChildAt(i);
            if (child instanceof GalleryPage) {
                ((GalleryPage) child).unload();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_gallery_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                if (mSaveTask == null) {
                    mGalleryAdapter.saveCurrentImage();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSaveTaskOver(boolean ok) {
        if (mSaveTask != null) {
            mSaveTask = null;

            Toast.makeText(this, ok ? R.string.save_successfully : R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private class GalleryHolder extends PagerHolder {

        public GalleryPage galleryPage;

        public GalleryHolder(View itemView) {
            super(itemView);

            galleryPage = (GalleryPage) itemView;
        }
    }

    private abstract class GalleryAdapter extends RecyclerPagerAdapter<GalleryHolder> {

        @NonNull
        @Override
        public GalleryHolder createPagerHolder(ViewGroup container) {
            return new GalleryHolder(GalleryActivity2.this.getLayoutInflater()
                    .inflate(R.layout.item_gallery, container, false));
        }

        public abstract void saveCurrentImage();
    }

    private class ImageFileAdapter extends GalleryAdapter {

        private File mImageFile;

        public ImageFileAdapter(File imageFile) {
            mImageFile = imageFile;
        }

        @Override
        public void saveCurrentImage() {
            GalleryHolder holder = getPagerHolder(0);
            if (holder == null || !holder.galleryPage.isLoaded()) {
                onSaveTaskOver(false);
            }

            File dir = NMBAppConfig.getImageDir(); // TODO get from settings
            if (dir == null) {
                onSaveTaskOver(false);
            }

            File file = new File(dir, mImageFile.getName());
            mSaveTask = new ImageFileSaveTask(GalleryActivity2.this, mImageFile, UniFile.fromFile(file));
            mSaveTask.execute();
        }

        @Override
        public void bindPagerHolder(GalleryHolder holder, int position) {
            Drawable drawable = NMBApplication.getSimpleDrawableHelper(GalleryActivity2.this)
                    .decode(new FileInputStreamPipe(mImageFile));
            if (drawable != null) {
                holder.galleryPage.showDrawable(drawable);
            } else {
                holder.galleryPage.showFailedText();
            }
        }

        @Override
        public void unbindPagerHolder(GalleryHolder holder, int position) {
            holder.galleryPage.unload();
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

    private class SingleImageAdapter extends GalleryAdapter {

        private Site mSite;
        private String mId;
        private String mImage;

        public SingleImageAdapter(Site site, String id, String image) {
            mSite = site;
            mId = id;
            mImage = image;
        }

        @Override
        public void bindPagerHolder(GalleryHolder holder, int position) {
            holder.galleryPage.load(mId, mImage);
        }

        @Override
        public void unbindPagerHolder(GalleryHolder holder, int position) {
            holder.galleryPage.unload();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public void saveCurrentImage() {
            GalleryHolder holder = getPagerHolder(0);
            if (holder == null || !holder.galleryPage.isLoaded()) {
                onSaveTaskOver(false);
            }

            File dir = NMBAppConfig.getImageDir(); // TODO get from settings
            if (dir == null) {
                onSaveTaskOver(false);
            }

            File file = new File(dir, mSite.getReadableName(GalleryActivity2.this) + "-" + mId);
            mSaveTask = new SingleImageSaveTask(GalleryActivity2.this, UniFile.fromFile(file), mImage);
            mSaveTask.execute();
        }
    }

    private static abstract class SaveTask extends AsyncTask<Void, Void, Boolean> {

        public abstract void onActivityDestory();
    }

    private static boolean addImageExtension(UniFile uniFile) {
        String filename = uniFile.getName();
        String extension = FileUtils.getExtensionFromFilename(filename);
        if (extension != null) {
            return true;
        }

        // Get extexsin
        InputStream is = null;
        try {
            is = uniFile.openInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(options.outMimeType);
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(is);
        }

        return uniFile.renameTo(filename + "." + extension);
    }

    private static class ImageFileSaveTask extends SaveTask {

        private Context mContext;
        private File mFrom;
        private UniFile mTo;

        public ImageFileSaveTask(Context context, File from, UniFile to) {
            mContext = context;
            mFrom = from;
            mTo = to;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean ok = true;

            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(mFrom);
                os = mTo.openOutputStream();
                IOUtils.copy(is, os);
                ok = true;
            } catch (IOException e) {
                ok = false;
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }

            if (!ok) {
                mTo.delete();
                return false;
            }

            return addImageExtension(mTo);
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (mContext instanceof GalleryActivity2) {
                ((GalleryActivity2) mContext).onSaveTaskOver(ok);
            }
        }

        @Override
        public void onActivityDestory() {
            mContext = mContext.getApplicationContext();
        }
    }

    private static class SingleImageSaveTask extends SaveTask {

        private Context mContext;
        private UniFile mUniFile;
        private String mKey;

        public SingleImageSaveTask(Context context, UniFile uniFile, String key) {
            mContext = context;
            mUniFile = uniFile;
            mKey = key;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean ok = true;

            OutputStream os = null;
            try {
                os = mUniFile.openOutputStream();
                Conaco conaco = NMBApplication.getConaco(mContext);
                ok = conaco.getBeerBelly().pullFromDiskCache(mKey, os);
            } catch (IOException e) {
                ok = false;
            } finally {
                IOUtils.closeQuietly(os);
            }

            if (!ok) {
                mUniFile.delete();
                return false;
            }

            return addImageExtension(mUniFile);
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (mContext instanceof GalleryActivity2) {
                ((GalleryActivity2) mContext).onSaveTaskOver(ok);
            }
        }

        @Override
        public void onActivityDestory() {
            mContext = mContext.getApplicationContext();
        }
    }
}
