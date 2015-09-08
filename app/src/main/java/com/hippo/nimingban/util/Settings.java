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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.nimingban.NMBAppConfig;
import com.hippo.unifile.UniFile;
import com.hippo.yorozuya.NumberUtils;

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

    public static int getIntFromStr(String key, int defValue) {
        return NumberUtils.parseIntSafely(sSettingsPre.getString(key, Integer.toString(defValue)), defValue);
    }

    public static void putIntToStr(String key, int value) {
        sSettingsPre.edit().putString(key, Integer.toString(value)).apply();
    }

    public static final int IMAGE_LOADING_STRATEGY_ALL = 0;
    public static final int IMAGE_LOADING_STRATEGY_WIFI = 1;
    public static final int IMAGE_LOADING_STRATEGY_NO = 2;

    public static final String KEY_DARK_THEME = "dark_theme";
    public static final boolean DEFAULT_DARK_THEME = false;

    public static final String KEY_PRETTY_TIME = "pretty_time";
    public static final boolean DEFAULT_PRETTY_TIME = true;

    public static final String KEY_FONT_SIZE = "font_size";
    public static final int DEFAULT_FONT_SIZE = 16;

    public static final String KEY_LINE_SPACING = "line_spacing";
    public static final int DEFAULT_LINE_SPACING = 1;

    public static final String KEY_IMAGE_LOADING_STRATEGY = "image_loading_strategy";
    public static final int DEFAULT_IMAGE_LOADING_STRATEGY = 0;
    public static final String KEY_IMAGE_SAVE_LOACTION = "image_save_location";

    public static final String KEY_IMAGE_SAVE_SCHEME = "image_scheme";
    public static final String KEY_IMAGE_SAVE_AUTHORITY = "image_authority";
    public static final String KEY_IMAGE_SAVE_PATH = "image_path";
    public static final String KEY_IMAGE_SAVE_QUERY = "image_query";
    public static final String KEY_IMAGE_SAVE_FRAGMENT = "image_fragment";

    public static final String KEY_SET_ANALYSIS = "set_analysis";
    public static final boolean DEFAULT_SET_ANALYSIS = false;
    public static final String KEY_ANALYSIS = "analysis";
    public static final boolean DEFAULT_ANALYSIS = false;

    public static boolean getDarkTheme() {
        return getBoolean(KEY_DARK_THEME, DEFAULT_DARK_THEME);
    }

    public static boolean getPrettyTime() {
        return getBoolean(KEY_PRETTY_TIME, DEFAULT_PRETTY_TIME);
    }

    public static int getFontSize() {
        return getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public static void putFontSize(int value) {
        putInt(KEY_FONT_SIZE, value);
    }

    public static int getLineSpacing() {
        return getInt(KEY_LINE_SPACING, DEFAULT_LINE_SPACING);
    }

    public static void putLineSpacing(int value) {
        putInt(KEY_LINE_SPACING, value);
    }

    public static int getImageLoadingStrategy() {
        return getIntFromStr(KEY_IMAGE_LOADING_STRATEGY, DEFAULT_IMAGE_LOADING_STRATEGY);
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

    public static boolean getSetAnalysis() {
        return getBoolean(KEY_SET_ANALYSIS, DEFAULT_SET_ANALYSIS);
    }

    public static void putSetAnalysis(boolean value) {
        putBoolean(KEY_SET_ANALYSIS, value);
    }

    public static boolean getAnalysis() {
        return getBoolean(KEY_ANALYSIS, DEFAULT_ANALYSIS);
    }

    public static void putAnalysis(boolean value) {
        putBoolean(KEY_ANALYSIS, value);
    }

    public static final String KEY_CRASH_FILENAME = "crash_filename";
    public static final String VALUE_CRASH_FILENAME = null;

    public static String getCrashFilename() {
        return getString(KEY_CRASH_FILENAME, VALUE_CRASH_FILENAME);
    }

    @SuppressLint("CommitPrefEdits")
    public static void putCrashFilename(String value) {
        sSettingsPre.edit().putString(KEY_CRASH_FILENAME, value).commit();
    }
}
