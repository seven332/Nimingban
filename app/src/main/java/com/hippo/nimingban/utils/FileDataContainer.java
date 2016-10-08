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

package com.hippo.nimingban.utils;

/*
 * Created by Hippo on 10/7/2016.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.conaco.DataContainer;
import com.hippo.conaco.ProgressNotifier;
import com.hippo.streampipe.InputStreamPipe;
import com.hippo.yorozuya.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileDataContainer implements DataContainer {

    private final File mFile;

    public FileDataContainer(@NonNull File file) {
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public boolean isEnabled() {
        // Always true
        return true;
    }

    @Override
    public void onUrlMoved(String requestUrl, String responseUrl) {}

    @Override
    public boolean save(InputStream is, long length, @Nullable String mediaType, @Nullable ProgressNotifier notify) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(mFile);

            final byte buffer[] = new byte[1024 * 4];
            long receivedSize = 0;
            int bytesRead;

            while((bytesRead = is.read(buffer)) !=-1) {
                os.write(buffer, 0, bytesRead);
                receivedSize += bytesRead;
                if (length > 0 && notify != null) {
                    notify.notifyProgress((long) bytesRead, receivedSize, length);
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Nullable
    @Override
    public InputStreamPipe get() {
        return new FileInputStreamPipe(mFile);
    }

    @Override
    public void remove() {
        mFile.delete();
    }
}
