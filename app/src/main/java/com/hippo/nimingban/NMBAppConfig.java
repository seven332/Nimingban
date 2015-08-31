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

package com.hippo.nimingban;

import android.content.Context;
import android.support.annotation.Nullable;

import com.hippo.yorozuya.FileUtils;

import java.io.File;

public class NMBAppConfig {



    public static @Nullable File getTempDir(Context context) {
        File temp = context.getCacheDir();
        if (FileUtils.ensureDirectory(temp)) {
            return temp;
        } else {
            return null;
        }
    }

    public static @Nullable File createTempFile(Context context) {
        return createTempFile(context, null);
    }

    public static @Nullable File createTempFile(Context context, String extension) {
        File tempDir = getTempDir(context);
        if (tempDir == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String filename = Long.toString(now + i);
            if (extension != null) {
                filename = filename + '.' + extension;
            }
            File tempFile = new File(tempDir, filename);
            if (!tempFile.exists()) {
                return tempFile;
            }
        }

        // Unbelievable
        return null;
    }
}
