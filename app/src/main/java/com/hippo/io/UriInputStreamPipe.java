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

package com.hippo.io;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.io.InputStreamPipe;

import java.io.IOException;
import java.io.InputStream;

public class UriInputStreamPipe implements InputStreamPipe {

    private Context mContext;
    private Uri mUri;

    private InputStream mIs;

    public UriInputStreamPipe(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
    }

    @Override
    public void obtain() {
        // Empty
    }

    @Override
    public void release() {
        // Empty
    }

    @NonNull
    @Override
    public InputStream open() throws IOException {
        InputStream is = mContext.getContentResolver().openInputStream(mUri);
        if (is == null) {
            throw new IOException("Can't openInputStream from " + mUri);
        } else {
            mIs = is;
            return is;
        }
    }

    @Override
    public void close() {
        if (mIs != null) {
            IOUtils.closeQuietly(mIs);
            mIs = null;
        }
    }
}
