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

package com.hippo.nimingban.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.nimingban.NMBAppConfig;
import com.hippo.unifile.UniFile;

public final class Settings {

    private static Context sContext;
    private static SharedPreferences sSettingsPre;

    public static void initialize(Context context) {
        sContext = context.getApplicationContext();
        sSettingsPre = PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return sSettingsPre.getBoolean(key, defValue);
    }

    public static void putBoolean(String key, boolean value) {
        sSettingsPre.edit().putBoolean(key, value).apply();
    }

    public static int getInt(String key, int defValue) {
        return sSettingsPre.getInt(key, defValue);
    }

    public static void putInt(String key, int value) {
        sSettingsPre.edit().putInt(key, value).apply();
    }

    public static long getLong(String key, long defValue) {
        return sSettingsPre.getLong(key, defValue);
    }

    public static void putLong(String key, long value) {
        sSettingsPre.edit().putLong(key, value).apply();
    }

    public static float getFloat(String key, float defValue) {
        return sSettingsPre.getFloat(key, defValue);
    }

    public static void putFloat(String key, float value) {
        sSettingsPre.edit().putFloat(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        return sSettingsPre.getString(key, defValue);
    }

    public static void putString(String key, String value) {
        sSettingsPre.edit().putString(key, value).apply();
    }


    public static final String KEY_PRETTY_TIME = "pretty_time";
    public static final boolean DEFAULT_PRETTY_TIME = true;
    public static final String KEY_IMAGE_SAVE_LOACTION = "image_save_location";

    public static final String KEY_IMAGE_SAVE_SCHEME = "image_scheme";
    public static final String KEY_IMAGE_SAVE_AUTHORITY = "image_authority";
    public static final String KEY_IMAGE_SAVE_PATH = "image_path";
    public static final String KEY_IMAGE_SAVE_QUERY = "image_query";
    public static final String KEY_IMAGE_SAVE_FRAGMENT = "image_fragment";

    public static boolean getPrettyTime() {
        return getBoolean(KEY_PRETTY_TIME, DEFAULT_PRETTY_TIME);
    }

    @Nullable
    public static UniFile getImageSaveLocation() {
        UniFile dir = null;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(getString(KEY_IMAGE_SAVE_SCHEME, null));
            builder.encodedAuthority(getString(KEY_IMAGE_SAVE_AUTHORITY, null));
            builder.encodedPath(getString(KEY_IMAGE_SAVE_PATH, null));
            builder.encodedQuery(getString(KEY_IMAGE_SAVE_QUERY, null));
            builder.encodedFragment(getString(KEY_IMAGE_SAVE_FRAGMENT, null));
            dir = UniFile.fromUri(sContext, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dir != null ? dir : UniFile.fromFile(NMBAppConfig.getImageDir());
    }

    public static void putImageSaveLocation(@NonNull UniFile location) {
        Uri uri = location.getUri();
        putString(KEY_IMAGE_SAVE_SCHEME, uri.getScheme());
        putString(KEY_IMAGE_SAVE_AUTHORITY, uri.getEncodedAuthority());
        putString(KEY_IMAGE_SAVE_PATH, uri.getEncodedPath());
        putString(KEY_IMAGE_SAVE_QUERY, uri.getEncodedQuery());
        putString(KEY_IMAGE_SAVE_FRAGMENT, uri.getEncodedFragment());
    }
}
