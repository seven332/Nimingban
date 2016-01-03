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

package com.hippo.nimingban.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.DrawableHolder;
import com.hippo.conaco.ProgressNotify;
import com.hippo.conaco.Unikery;
import com.hippo.drawable.ImageDrawable;
import com.hippo.io.UniFileInputStreamPipe;
import com.hippo.nimingban.Analysis;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.util.Settings;
import com.hippo.unifile.UniFile;
import com.hippo.widget.FixedAspectImageView;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class HeaderImageView extends FixedAspectImageView
        implements Unikery, View.OnClickListener, View.OnLongClickListener {

    private static final String KEY_SUPER = "super";
    private static final String KEY_IMAGE_UNI_FILE_URI = "image_uni_file_uri";

    private int mTaskId = Unikery.INVAILD_ID;

    private Conaco mConaco;

    private final long[] mHits = new long[8];

    private UniFile mImageFile;
    private TempDataContainer mContainer;
    private DrawableHolder mHolder;

    private OnLongClickImageListener mOnLongClickImageListener;

    public HeaderImageView(Context context) {
        super(context);
        init(context);
    }

    public HeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeaderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mConaco = NMBApplication.getConaco(context);
        setScaleType(ScaleType.CENTER_CROP);
        setSoundEffectsEnabled(false);
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void setOnLongClickImageListener(OnLongClickImageListener listener) {
        mOnLongClickImageListener = listener;
    }

    @Override
    public void onClick(@NonNull View v) {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length-1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 3000)) {
            // Analysis
            Analysis.action(getContext(), "change_header");

            Arrays.fill(mHits, 0);
            Toast.makeText(getContext(), "（<ゝω・）☆ Kira", Toast.LENGTH_SHORT).show();
            load();
        }
    }

    @Override
    public boolean onLongClick(@NonNull View v) {
        if (mImageFile != null && mOnLongClickImageListener != null) {
            // Analysis
            Analysis.action(getContext(), "enlarge_header");

            return mOnLongClickImageListener.onLongClickImage(mImageFile);
        } else {
            return false;
        }
    }

    public void load() {
        mContainer = new TempDataContainer(getContext());
        ConacoTask.Builder builder = new ConacoTask.Builder()
                .setUnikery(this)
                .setKey(null)
                .setUrl("http://cover.acfunwiki.org/cover.php")
                .setDataContainer(mContainer);
        mConaco.load(builder);
    }

    public void unload() {
        mConaco.cancel(this);
        setImageDrawableSafely(null, mHolder != null && mHolder.isInMemoryCache());

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    public void setTaskId(int id) {
        mTaskId = id;
    }

    @Override
    public int getTaskId() {
        return mTaskId;
    }

    @Override
    public void onMiss(Conaco.Source source) {
    }

    @Override
    public void onRequest() {
    }

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
    }

    private void setImageDrawableSafely(Drawable drawable, boolean inMemoryCache) {
        Drawable oldDrawable = getDrawable();
        if (oldDrawable instanceof TransitionDrawable) {
            TransitionDrawable tDrawable = (TransitionDrawable) oldDrawable;
            int number = tDrawable.getNumberOfLayers();
            if (number > 0) {
                oldDrawable = tDrawable.getDrawable(number - 1);
            }
        }

        if (oldDrawable instanceof ImageDrawable) {
            ImageDrawable imageDrawable = (ImageDrawable) oldDrawable;
            if (imageDrawable.isLarge() || !inMemoryCache) {
                imageDrawable.recycle();
            }
        }

        setImageDrawable(drawable);
    }

    @Override
    public boolean onGetDrawable(@NonNull DrawableHolder holder, Conaco.Source source) {
        // Update image file
        if (mImageFile != null) {
            mImageFile = null;
        }
        if (mContainer != null) {
            mImageFile = mContainer.mTempFile;
            mContainer = null;
        }

        DrawableHolder olderHolder = mHolder;
        mHolder = holder;
        holder.obtain();

        Drawable drawable = holder.getDrawable();
        if (drawable instanceof ImageDrawable) {
            ((ImageDrawable) drawable).start();
        }

        setImageDrawableSafely(drawable, olderHolder != null && olderHolder.isInMemoryCache());

        if (olderHolder != null) {
            olderHolder.release();
        }

        return true;
    }

    @Override
    public void onSetDrawable(Drawable drawable) {
        setImageDrawableSafely(drawable, mHolder != null && mHolder.isInMemoryCache());

        // Release old holder
        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    public void onFailure() {
        mContainer = null;
    }

    @Override
    public void onCancel() {
        mContainer = null;
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle saved = new Bundle();
        saved.putParcelable(KEY_SUPER, super.onSaveInstanceState());
        if (mImageFile != null) {
            saved.putParcelable(KEY_IMAGE_UNI_FILE_URI, mImageFile.getUri());
        }
        return saved;
    }

    private void setImageFile(UniFile file) {
        Drawable drawable = NMBApplication.getSimpleDrawableHelper(getContext())
                .decode(new UniFileInputStreamPipe(file));
        if (drawable == null) {
            return;
        }

        // Update image file
        if (mImageFile != null) {
            mImageFile = null;
        }
        mImageFile = file;
        mContainer = null;

        if (drawable instanceof ImageDrawable) {
            ((ImageDrawable) drawable).start();
        }

        setImageDrawableSafely(drawable, mHolder != null && mHolder.isInMemoryCache());

        if (mHolder != null) {
            mHolder.release();
            mHolder = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle saved = (Bundle) state;
        Uri uri = saved.getParcelable(KEY_IMAGE_UNI_FILE_URI);
        if (uri != null) {
            UniFile file = UniFile.fromUri(getContext(), uri);
            if (file != null && file.exists()) {
                setImageFile(file);
            }
        }

        super.onRestoreInstanceState(saved.getParcelable(KEY_SUPER));
    }

    public interface OnLongClickImageListener {

        boolean onLongClickImage(UniFile imageFile);
    }

    private static class TempDataContainer implements DataContainer {

        private Context mContext;
        private String mName;
        private UniFile mTempFile;

        public TempDataContainer(Context context) {
            mContext = context.getApplicationContext();
        }

        /**
         * http://stackoverflow.com/questions/332079
         *
         * @param bytes The bytes to convert.
         * @return A {@link String} converted from the bytes of a hashable key used
         *         to store a filename on the disk, to hex digits.
         */
        private static String bytesToHexString(final byte[] bytes) {
            final StringBuilder builder = new StringBuilder();
            for (final byte b : bytes) {
                final String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        }

        private static String getKeyForUrl(String url) {
            String key;
            try {
                final MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(url.getBytes());
                key = bytesToHexString(digest.digest());
            } catch (final NoSuchAlgorithmException e) {
                key = String.valueOf(url.hashCode());
            }
            return key;
        }

        @Override
        public void onUrlMoved(String requestUrl, String responseUrl) {
            mName = "ac-cover-" + getKeyForUrl(responseUrl);
        }

        @Override
        public boolean save(InputStream is, long length, String mediaType, ProgressNotify notify) {
            OutputStream os = null;
            try {
                boolean autoSave = Settings.getSaveImageAuto() && mName != null;
                if (autoSave) {
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mediaType);
                    if (TextUtils.isEmpty(extension)) {
                        extension = "jpg";
                    }
                    String filename = mName + '.' + extension;
                    UniFile dir = Settings.getImageSaveLocation();
                    if (dir != null) {
                        mTempFile = dir.createFile(filename);
                    } else {
                        mTempFile = UniFile.fromFile(NMBAppConfig.createTempFileWithFilename(filename));
                    }
                } else {
                    mTempFile = UniFile.fromFile(NMBAppConfig.createTempFile());
                }

                if (mTempFile == null) {
                    return false;
                }
                os = mTempFile.openOutputStream();
                IOUtils.copy(is, os);

                // Notify media scanner
                if (autoSave) {
                    mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, mTempFile.getUri()));
                }

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                IOUtils.closeQuietly(os);
            }
        }

        @Override
        public InputStreamPipe get() {
            if (mTempFile == null) {
                return null;
            } else {
                return new UniFileInputStreamPipe(mTempFile);
            }
        }

        @Override
        public void remove() {
            mTempFile.delete();
        }
    }
}
