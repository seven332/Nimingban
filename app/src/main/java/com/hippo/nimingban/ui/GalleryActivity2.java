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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.ProgressNotify;
import com.hippo.io.FileInputStreamPipe;
import com.hippo.io.UniFileInputStreamPipe;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.GalleryPage;
import com.hippo.unifile.UniFile;
import com.hippo.widget.viewpager.PagerHolder;
import com.hippo.widget.viewpager.RecyclerPagerAdapter;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO show all image in post
public class GalleryActivity2 extends SwipeActivity {

    public static final String[] IMAGE_EXTENSIONS = {
            "jpg",
            "jpeg",
            "png",
            "gif"
    };

    public static final String ACTION_SINGLE_IMAGE = "com.hippo.nimingban.ui.GalleryActivity2.action.SINGLE_IMAGE";
    public static final String ACTION_IMAGE_FILE = "com.hippo.nimingban.ui.GalleryActivity2.action.IMAGE_FILE";

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
        return false;
    }

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme_NoActionBar_Translucent_Transparent;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.AppTheme_Dark_NoActionBar_Translucent_Transparent;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!handlerIntent(getIntent())) {
            finish();
            return;
        }

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        ToolbarActivityHelper.setContentView(this, R.layout.activity_gallery_2);
        setActionBarUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_left_dark_x24));

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPager.setAdapter(mGalleryAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mViewPager != null) {
            // Unload all pager
            for (int i = 0, n = mViewPager.getChildCount(); i < n; i++) {
                View child = mViewPager.getChildAt(i);
                if (child instanceof GalleryPage) {
                    ((GalleryPage) child).unload();
                }
            }
        }

        if (mSaveTask != null) {
            mSaveTask.onActivityDestory();
            mSaveTask = null;
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
                    mGalleryAdapter.saveCurrentImage(false);
                }
                return true;
            case R.id.action_share:
                if (mSaveTask == null) {
                    mGalleryAdapter.saveCurrentImage(true);
                }
                return true;
            case R.id.action_refresh:
                if (mSaveTask == null) {
                    mGalleryAdapter.reloadCurrentImage();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @param uri the save image file url, null for fail
     * @param share true for share
     */
    public void onSaveTaskOver(Uri uri, boolean share) {
        if (mSaveTask != null) {
            mSaveTask = null;

            if (share) {
                if (uri == null) {
                    Toast.makeText(this, R.string.cant_save_image, Toast.LENGTH_SHORT).show();
                } else {
                    String mimeType = getContentResolver().getType(uri);
                    if (TextUtils.isEmpty(mimeType)) {
                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                        if (TextUtils.isEmpty(mimeType)) {
                            mimeType = "image/*";
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setType(mimeType);
                    startActivity(Intent.createChooser(intent, getString(R.string.share_image)));
                }
            } else {
                Toast.makeText(this, uri != null ? R.string.save_successfully : R.string.save_failed, Toast.LENGTH_SHORT).show();
            }
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

        public abstract void reloadCurrentImage();

        public abstract void saveCurrentImage(boolean share);
    }

    private class ImageFileAdapter extends GalleryAdapter {

        private File mImageFile;

        public ImageFileAdapter(File imageFile) {
            mImageFile = imageFile;
        }

        public UniFile getCurrentImageSaveFile() {
            UniFile dir = Settings.getImageSaveLocation();
            if (dir != null) {
                return dir.createFile(mImageFile.getName());
            } else {
                return null;
            }
        }

        @Override
        public void reloadCurrentImage() {
            // Empty
        }

        @Override
        public void saveCurrentImage(boolean share) {
            GalleryHolder holder = getPagerHolder(0);
            if (holder == null || !holder.galleryPage.isLoaded()) {
                onSaveTaskOver(null, share);
                return;
            }

            UniFile uniFile = getCurrentImageSaveFile();
            if (uniFile == null) {
                onSaveTaskOver(null, share);
                return;
            }

            mSaveTask = new ImageFileSaveTask(GalleryActivity2.this, mImageFile, uniFile, share);
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
            String key;
            DataContainer container;
            UniFile dir = Settings.getImageSaveLocation();
            if (Settings.getSaveImageAuto() && dir != null) {
                key = null;
                container = new UniFileDataContain(dir, mSite.getReadableName(GalleryActivity2.this) + "-" + mId);
            } else {
                key = mImage;
                container = null;
            }

            holder.galleryPage.load(key, mImage, container);
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
        public void reloadCurrentImage() {
            GalleryHolder holder = getPagerHolder(0);
            if (holder == null) {
                return;
            }

            // Unload
            holder.galleryPage.unload();

            // Remove in cache
            NMBApplication.getConaco(GalleryActivity2.this).getBeerBelly().remove(mImage);

            // Remove all in save location
            UniFile dir = Settings.getImageSaveLocation();
            if (dir != null) {
                String name = mSite.getReadableName(GalleryActivity2.this) + "-" + mId;
                for (String extension : IMAGE_EXTENSIONS) {
                    UniFile file = dir.findFile(name + '.' + extension);
                    if (file != null) {
                        file.delete();
                    }
                }
            }

            // Load
            bindPagerHolder(holder, 0);
        }

        @Override
        public void saveCurrentImage(boolean share) {
            GalleryHolder holder = getPagerHolder(0);
            if (holder == null || !holder.galleryPage.isLoaded()) {
                onSaveTaskOver(null, share);
                return;
            }

            UniFile dir = Settings.getImageSaveLocation();
            if (dir == null) {
                onSaveTaskOver(null, share);
                return;
            }
            String name = mSite.getReadableName(GalleryActivity2.this) + "-" + mId;

            mSaveTask = new SingleImageSaveTask(GalleryActivity2.this, dir, name, mImage, share);
            mSaveTask.execute();
        }
    }

    private static UniFile findFileForName(UniFile dir, String name, String[] extensions, String[] resultExtension) {
        for (String extension : extensions) {
            UniFile file = dir.findFile(name + '.' + extension);
            if (file != null) {
                if (resultExtension != null && resultExtension.length > 0) {
                    resultExtension[0] = extension;
                }
                return file;
            }
        }
        return null;
    }

    /**
     * Let conaco store image to image save location directly
     */
    private static class UniFileDataContain implements DataContainer {

        private UniFile mDir;
        private String mName;
        private @Nullable UniFile mFile;
        private @Nullable String mExtension;

        public UniFileDataContain(@NonNull UniFile dir, String name) {
            mDir = dir;
            mName = name;
        }

        @Override
        public boolean save(InputStream is, long length, String mediaType, ProgressNotify notify) {
            OutputStream os = null;
            try {
                if (mFile == null) {
                    mFile = mDir.createFile(mName);
                }
                if (mFile == null) {
                    return false;
                }

                os = mFile.openOutputStream();

                final byte buffer[] = new byte[1024 * 4];
                long receivedSize = 0;
                int bytesRead;

                while((bytesRead = is.read(buffer)) !=-1) {
                    os.write(buffer, 0, bytesRead);
                    receivedSize += bytesRead;
                    if (length > 0) {
                        notify.notifyProgress((long) bytesRead, receivedSize, length);
                    }
                }
                os.flush();
                IOUtils.closeQuietly(os);

                // Get extension
                String extension = null;
                if (mediaType != null) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mediaType);
                }
                if (TextUtils.isEmpty(extension)) {
                    extension = "jpg";
                }

                // Rename file if the extension is wrong
                if (!extension.equals(mExtension)) {
                    mFile.renameTo(mFile.getName() + '.' + extension);
                }

                return true;
            } catch (IOException e) {
                IOUtils.closeQuietly(os);
                mFile.delete();
                return false;
            }
        }

        @Override
        public InputStreamPipe get() {
            String[] extension = new String[1];
            UniFile file = findFileForName(mDir, mName, IMAGE_EXTENSIONS, extension);
            if (file != null) {
                mFile = file;
                mExtension = extension[0];
                return new UniFileInputStreamPipe(mFile);
            }

            return null;
        }

        @Override
        public void remove() {
            // Empty
        }
    }

    private static abstract class SaveTask extends AsyncTask<Void, Void, Uri> {

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

        String newFilename = filename + "." + extension;
        // Remove old file
        UniFile parent = uniFile.getParentFile();
        if (parent != null) {
            UniFile oldFile = parent.findFile(newFilename);
            if (oldFile != null) {
                oldFile .delete();
            }
        }
        // Rename
        return uniFile.renameTo(newFilename);
    }

    private static class ImageFileSaveTask extends SaveTask {

        private Context mContext;
        private File mFrom;
        private UniFile mTo;
        private boolean mShare;

        public ImageFileSaveTask(Context context, File from, UniFile to, boolean share) {
            mContext = context;
            mFrom = from;
            mTo = to;
            mShare = share;
        }

        @Override
        protected Uri doInBackground(Void... params) {
            boolean ok;

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
                return null;
            }

            if (addImageExtension(mTo)) {
                return mTo.getUri();
            } else {
                mTo.delete();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (mContext instanceof GalleryActivity2) {
                ((GalleryActivity2) mContext).onSaveTaskOver(uri, mShare);
            }
        }

        @Override
        public void onActivityDestory() {
            mContext = mContext.getApplicationContext();
        }
    }

    private static class SingleImageSaveTask extends SaveTask {

        private Context mContext;
        private UniFile mDir;
        private String mName;
        private String mKey;
        private boolean mShare;

        public SingleImageSaveTask(Context context, UniFile dir, String name, String key, boolean share) {
            mContext = context;
            mDir = dir;
            mName = name;
            mKey = key;
            mShare = share;
        }

        @Override
        protected Uri doInBackground(Void... params) {
            // First try to find file in dir
            UniFile file = findFileForName(mDir, mName, IMAGE_EXTENSIONS, null);
            if (file != null) {
                // Check is it a image
                Bitmap bitmap = BitmapUtils.decodeStream(new UniFileInputStreamPipe(file), 100, 100, -1, true, false, null);
                if (bitmap != null) {
                    // It is image
                    bitmap.recycle();
                    return file.getUri();
                } else {
                    file.delete();
                }
            }

            file = mDir.createFile(mName);
            if (file == null) {
                return null;
            }

            boolean ok = true;

            OutputStream os = null;
            try {
                os = file.openOutputStream();
                Conaco conaco = NMBApplication.getConaco(mContext);
                ok = conaco.getBeerBelly().pullFromDiskCache(mKey, os);
            } catch (IOException e) {
                ok = false;
            } finally {
                IOUtils.closeQuietly(os);
            }

            if (!ok) {
                file.delete();
                return null;
            }

            if (addImageExtension(file)) {
                return file.getUri();
            } else {
                file.delete();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (mContext instanceof GalleryActivity2) {
                ((GalleryActivity2) mContext).onSaveTaskOver(uri, mShare);
            }
        }

        @Override
        public void onActivityDestory() {
            mContext = mContext.getApplicationContext();
        }
    }
}
