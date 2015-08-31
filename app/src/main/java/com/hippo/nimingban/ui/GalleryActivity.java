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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;

import com.hippo.beerbelly.SimpleDiskCache;
import com.hippo.conaco.BitmapPool;
import com.hippo.gallery.GifDecoderBuilder;
import com.hippo.gallery.glrenderer.GifTexture;
import com.hippo.gallery.glrenderer.TiledTexture;
import com.hippo.gallery.ui.GLRootView;
import com.hippo.gallery.ui.GLView;
import com.hippo.gallery.ui.GalleryPageView;
import com.hippo.gallery.ui.GalleryView;
import com.hippo.network.DownloadClient;
import com.hippo.network.DownloadRequest;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.R;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity implements GalleryProviderListener, GalleryView.ActionListener {

    public static final String ACTION_SINGLE_IMAGE = "com.hippo.nimingban.ui.GalleryActivity.action.SINGLE_IMAGE";

    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";
    public static final String KEY_IMAGE = "image";

    private BitmapPool mBitmapPool;
    private BitmapPool mGifBitmapPool;

    private GLRootView mGLRootView;

    private GalleryProvider mGalleryProvider;

    private GalleryView mGalleryView;

    private TiledTexture.Uploader mUploader;

    @NonNull
    public BitmapPool getBitmapPool() {
        if (mBitmapPool == null) {
            mBitmapPool = new BitmapPool();
        }
        return mBitmapPool;
    }

    @NonNull
    public BitmapPool getGifBitmapPool() {
        if (mGifBitmapPool == null) {
            mGifBitmapPool = new BitmapPool();
        }
        return mGifBitmapPool;
    }

    public TiledTexture.Uploader getTiledTextureUploader() {
        if (mUploader == null) {
            mUploader = new TiledTexture.Uploader(mGLRootView);
        }
        return mUploader;
    }

    // false for error
    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_SINGLE_IMAGE.equals(action)) {
            int site = intent.getIntExtra(KEY_SITE, -1);
            String id = intent.getStringExtra(KEY_ID);
            String image = intent.getStringExtra(KEY_IMAGE);
            if (site != -1 && id != null && image != null) {
                mGalleryProvider = new SingleImageGalleryProvider(this, site, id, image);
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

        GifTexture.initialize();
        TiledTexture.prepareResources();

        setContentView(R.layout.activity_gallery);

        mGalleryView = new GalleryView(this, 0, this);
        mGalleryView.setAdapter(new GalleryAdapter());

        mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
        mGLRootView.setContentPane(mGalleryView);

        mGalleryProvider.setGalleryProviderListener(this);
        if (mGalleryProvider instanceof SingleImageGalleryProvider) {
            mGalleryView.setShowEdgeView(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Free all TileTexture in GalleryView
        for (int i = 0, n = mGalleryView.getComponentCount(); i < n; i++) {
            GLView view = mGalleryView.getComponent(i);
            if (view instanceof  GalleryPageView) {
                clearTiledTextureInPage((GalleryPageView) view);
            }
        }
    }

    public void releaseImage(Object obj) {
        if (obj != null) {
            if (obj instanceof Bitmap) {
                getBitmapPool().addReusableBitmap((Bitmap) obj);
            } else if (obj instanceof Pair) {
                Pair pair = (Pair) obj;
                ((GifDecoderBuilder) pair.first).close();
                getGifBitmapPool().addReusableBitmap((Bitmap) pair.second);
            }
        }
    }

    @Override
    public void onGetImage(int index, @Nullable Object obj) {
        GalleryPageView page = mGalleryView.getPage(index);
        if (page != null) {
            if (obj == null) {
                bindBadImage(page);
            } else {
                bindImage(page, obj);
            }
        } else {
            releaseImage(obj);
        }
    }

    @Override
    public void onPagePercent(int index, float percent) {
        GalleryPageView page = mGalleryView.getPage(index);
        if (page != null) {
            bindPercent(page, percent);
        }
    }

    @Override
    public void onPageSucceed(int index) {
        GalleryPageView page = mGalleryView.getPage(index);
        if (page != null) {
            bind(page, index);
        }
    }

    @Override
    public void onPageFailed(int index, Exception e) {
        //e.printStackTrace(); // TODO
        GalleryPageView page = mGalleryView.getPage(index);
        if (page != null) {
            bindFailed(page);
        }
    }


    private void clearTiledTextureInPage(GalleryPageView view) {
        TiledTexture tiledTexture = view.mImageView.getTiledTexture();
        if (tiledTexture != null) {
            view.mImageView.setTiledTexture(null);
            tiledTexture.recycle();
        }
    }

    private void bindBadImage(GalleryPageView view) {
        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.INVISIBLE);
        //view.mIndexView.setVisibility(GLView.VISIBLE);
        //view.mIndexView.setText(".3");

        Log.d("TAG", "bindBadImage");
    }

    private void bindImage(GalleryPageView view, Object obj) {
        TiledTexture tiledTexture;
        if (obj instanceof Bitmap) {
            tiledTexture = new TiledTexture((Bitmap) obj);
            //mUploader.addTexture(tiledTexture);
        } else if (obj instanceof Pair) {
            Pair pair = (Pair) obj;
            tiledTexture = new GifTexture((GifDecoderBuilder) pair.first, (Bitmap) pair.second, getTiledTextureUploader());
            //mUploader.addTexture(tiledTexture);
        } else {
            bindBadImage(view);
            return;
        }

        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.INVISIBLE);
        //view.mIndexView.setVisibility(GLView.INVISIBLE);
        view.mImageView.setTiledTexture(tiledTexture);

        Log.d("TAG", "bindImage");
    }

    private void bindPercent(GalleryPageView view, float precent) {
        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.VISIBLE);
        view.mProgressView.setIndeterminate(false);
        view.mProgressView.setProgress(precent);
        //view.mIndexView.setVisibility(GLView.VISIBLE);

        Log.d("TAG", "bindPercent");
    }

    private void bindWait(GalleryPageView view) {
        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.INVISIBLE);
        //view.mIndexView.setVisibility(GLView.VISIBLE);

        Log.d("TAG", "bindWait");
    }

    private void bindNone(GalleryPageView view) {
        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.VISIBLE);
        view.mProgressView.setIndeterminate(true);
        //view.mIndexView.setVisibility(GLView.VISIBLE);

        Log.d("TAG", "bindNone");
    }

    private void bindFailed(GalleryPageView view) {
        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.INVISIBLE);
        //view.mIndexView.setVisibility(GLView.VISIBLE);
        //view.mIndexView.setText(".1");

        Log.d("TAG", "bindFailed");
    }

    private void bindUnknown(GalleryPageView view) {
        clearTiledTextureInPage(view);
        view.mProgressView.setVisibility(GLView.INVISIBLE);
        //view.mIndexView.setVisibility(GLView.VISIBLE);
        //view.mIndexView.setText(".2");

        Log.d("TAG", "bindUnknown");
    }

    private void bind(GalleryPageView view, int index) {
        Object result = mGalleryProvider.request(index);
        if (result instanceof Float) {
            bindPercent(view, (Float) result);
        } else if (result == GalleryProvider.RESULT_WAIT) {
            bindWait(view);
        } else if (result == GalleryProvider.RESULT_NONE) {
            bindNone(view);
        } else if (result == GalleryProvider.RESULT_FAILED) {
            bindFailed(view);
        } else {
            bindUnknown(view);
        }
    }



    @Override
    public void onTapCenter() {

    }

    @Override
    public void onSetMode(GalleryView.Mode mode) {

    }

    @Override
    public void onScrollToPage(int page, boolean internal) {

    }


    private class GalleryAdapter extends GalleryView.Adapter {

        @Override
        public int getPages() {
            return 1;
        }

        @Override
        public GalleryPageView createPage() {
            return new GalleryPageView(GalleryActivity.this);
        }

        @Override
        public void bindPage(GalleryPageView view, int index) {
            view.mProgressView.setColor(ResourcesUtils.getAttrColor(GalleryActivity.this, R.attr.colorAccent));
            bind(view, index);
        }

        @Override
        public void unbindPage(GalleryPageView view, int index) {
            view.mProgressView.setIndeterminate(false);
            clearTiledTextureInPage(view);
        }
    }

    private class SingleImageGalleryProvider implements GalleryProvider {

        private static final int STATE_NONE = 0;
        private static final int STATE_DOWNLOADING = 1;
        private static final int STATE_FAILED = 2;
        private static final int STATE_SUCCESSED = 3;

        public Context mContext;
        public int mSite;
        public String mId;
        public String mImage;
        public SimpleDiskCache mDiskCache;

        private GalleryProviderListener mListener;

        private DownloadRequest mRequest;

        private int mState = STATE_NONE;

        private float mPercent;

        private class GetImageTask extends AsyncTask<Void, Float, Exception> {

            private class DownloadListener extends DownloadClient.SimpleDownloadListener {

                public Exception exception;

                @Override
                public void onStartDownloading() {
                    publishProgress(0.0f);
                }

                @Override
                public void onDonwlad(long receivedSize, long singleReceivedSize, long totalSize) {
                    publishProgress((float) receivedSize / (float) totalSize);
                }

                @Override
                public void onFailed(Exception e) {
                    exception = e;
                }

                @Override
                public void onSucceed() {
                }
            }

            @Override
            protected Exception doInBackground(Void... params) {
                DownloadListener downloadListener = new DownloadListener();
                DownloadRequest request = new DownloadRequest();
                request.setHttpClient(NMBApplication.getNMBHttpClient(mContext));
                request.setListener(downloadListener);
                request.setUrl(mImage);
                request.setOSPipe(mDiskCache.getOutputStreamPipe(mImage));
                mRequest = request;
                DownloadClient.execute(request);
                mRequest = null;

                if (downloadListener.exception != null) {
                    mDiskCache.remove(mImage);
                    return downloadListener.exception;
                } else {
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(Float... values) {
                mPercent = values[0];
                if (mListener != null) {
                    mListener.onPagePercent(0, values[0]);
                }
            }

            @Override
            protected void onPostExecute(Exception exception) {
                if (exception == null) {
                    mState = STATE_SUCCESSED;
                    if (mListener != null) {
                        mListener.onPageSucceed(0);
                    }
                } else {
                    mState = STATE_FAILED;
                    if (mListener != null) {
                        mListener.onPageFailed(0, exception);
                    }
                }
            }
        }

        private class RenderImageTask extends AsyncTask<Void, Void, Object> {

            @Override
            protected Object doInBackground(Void... params) {
                InputStreamPipe isPipe = mDiskCache.getInputStreamPipe(mImage);

                if (isPipe == null) {
                    return null;
                }

                Bitmap bitmap = null;
                try {
                    isPipe.obtain();

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    InputStream is = isPipe.open();
                    BitmapFactory.decodeStream(is, null, options);
                    isPipe.close();

                    // Check out size
                    if (options.outWidth <= 0 || options.outHeight <= 0) {
                        isPipe.release();
                        return null;
                    }

                    if ("image/gif".equals(options.outMimeType)) {
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                        is = isPipe.open();
                        bitmap = BitmapFactory.decodeStream(is, null, options);
                        isPipe.close();

                        if (bitmap != null) {
                            // Copy the file to
                            File tempFile = NMBAppConfig.createTempFile(mContext);
                            is = isPipe.open();
                            IOUtils.copy(is, new FileOutputStream(tempFile));
                            isPipe.close();
                            is = new TempFileInputStream(tempFile);

                            GifDecoderBuilder gifDecoderBuilder = new GifDecoderBuilder(is);
                            gifDecoderBuilder.setBitmapPool(getGifBitmapPool());

                            return new Pair<>(gifDecoderBuilder, bitmap);
                        }
                        return null;
                    } else {
                        // It is not gif
                        options.inJustDecodeBounds = false;
                        options.inMutable = true;
                        options.inSampleSize = 1;
                        options.inBitmap = getBitmapPool().getInBitmap(options);
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                        is = isPipe.open();
                        bitmap = BitmapFactory.decodeStream(is, null, options);
                        isPipe.close();

                        return bitmap;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getBitmapPool().addReusableBitmap(bitmap);
                    return null;
                } finally {
                    isPipe.close();
                    isPipe.release();
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                if (o != null) {
                    if (mListener != null) {
                        mListener.onGetImage(0, o);
                    }
                } else {
                    if (mListener != null) {
                        mListener.onPageFailed(0, new Exception("Unknown"));
                    }
                }
            }
        }

        public SingleImageGalleryProvider(Context context, int site, String id, String image) {
            mContext = context;
            mSite = site;
            mId = id;
            mImage = image;
            mDiskCache = NMBApplication.getImageDiskCache(context);

            if (mDiskCache != null) {
                // Check in cache
                if (mDiskCache.contain(mImage)) {
                    mState = STATE_SUCCESSED;
                } else {
                    // Miss, get from internet
                    mState = STATE_DOWNLOADING;
                    new GetImageTask().execute();
                }
            } else {
                mState = STATE_FAILED;
                // Can't get disk cache
                if (mListener != null) {
                    mListener.onPageFailed(0, new IllegalStateException("Can't get disk cache"));
                }
            }
        }

        @Override
        public Object request(int index) {
            if (index != 0) {
                return RESULT_OUT_OF_RANGE;
            }

            switch (mState) {
                case STATE_NONE:
                    if (mDiskCache == null) {
                        return RESULT_FAILED;
                    } else {
                        new GetImageTask().execute();
                        return RESULT_WAIT;
                    }
                case STATE_DOWNLOADING:
                    return mPercent;
                case STATE_FAILED:
                    return RESULT_FAILED;
                case STATE_SUCCESSED:
                    new RenderImageTask().execute();
                    return RESULT_WAIT;
                default:
                    return RESULT_FAILED;
            }
        }

        @Override
        public Object forceRequest(int index) {
            // TODO
            return null;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public void setGalleryProviderListener(GalleryProviderListener listener) {
            mListener = listener;
        }

        @Override
        public void close() {
            if (mRequest != null) {
                mRequest.cancel();
                mRequest = null;
            }
        }

        @Override
        public void save(int index) {
            // TODO
        }
    }

    public static class TempFileInputStream extends FileInputStream {

        private File mFile;

        public TempFileInputStream(File file) throws FileNotFoundException {
            super(file);
            mFile = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            mFile.delete();
        }
    }
}
