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
import android.os.Environment;
import android.support.annotation.Nullable;

import com.hippo.yorozuya.FileUtils;

import java.io.File;

public class NMBAppConfig {

    private static final String APP_DIRNAME = "nmb";

    private static final String CRASH_DIRNAME = "crash";
    private static final String DOODLE_DIRNAME = "doodle";
    private static final String IMAGE_DIRNAME = "image";
    private static final String COOKIES_DIRNAME = "cookies";

    public static @Nullable File getExternalAppDir() {
        if (Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(Environment.getExternalStorageDirectory(), APP_DIRNAME);
            if (FileUtils.ensureDirectory(file)) {
                return file;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * mkdirs and get
     */
    public static @Nullable File getFileInAppDir(String filename) {
        File appFolder = getExternalAppDir();
        if (appFolder != null) {
            File dir = new File(appFolder, filename);
            if (FileUtils.ensureDirectory(dir)) {
                return dir;
            }
        }

        return null;
    }

    public static @Nullable File getCrashDir() {
        return getFileInAppDir(CRASH_DIRNAME);
    }

    public static @Nullable File getDoodleDir() {
        return getFileInAppDir(DOODLE_DIRNAME);
    }

    public static @Nullable File getImageDir() {
        return getFileInAppDir(IMAGE_DIRNAME);
    }

    public static @Nullable File getCookiesDir() {
        return getFileInAppDir(COOKIES_DIRNAME);
    }

    public static @Nullable File getTempDir(Context context) {
        File temp = new File(context.getCacheDir(), "temp");
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
        return FileUtils.createTempFile(getTempDir(context), extension);
    }

    public static @Nullable File createTempDir(Context context) {
        return FileUtils.createTempDir(getTempDir(context));
    }
}
