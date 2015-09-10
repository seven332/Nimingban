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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.network.TransportableHttpCookie;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.util.Settings;
import com.hippo.unifile.UniFile;
import com.hippo.util.ActivityHelper;
import com.hippo.widget.Slider;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.Messenger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AbsActivity {

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.AppTheme_Dark;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainFragement()).commit();
    }

    public static final class MainFragement extends PreferenceFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        public static final int REQUEST_CODE_PICK_IMAGE_DIR = 0;
        public static final int REQUEST_CODE_PICK_IMAGE_DIR_L = 1;

        private static final String KEY_DARK_THEME = "dark_theme";
        private static final String KEY_TEXT_FORMAT = "text_format";
        private static final String KEY_AC_COOKIES = "ac_cookies";
        private static final String KEY_SAVE_COOKIES = "save_cookies";
        private static final String KEY_RESTORE_COOKIES = "restore_cookies";
        private static final String KEY_AUTHOR = "author";
        private static final String KEY_SOURCE = "source";

        private Context mContext;

        private Preference mDarkTheme;
        private SwitchPreference mPrettyTime;
        private Preference mTextFormat;
        private Preference mACCookies;
        private Preference mSaveCookies;
        private Preference mRestoreCookies;
        private Preference mImageSaveLocation;
        private Preference mAuthor;
        private Preference mSource;

        private TimingLife mTimingLife;

        private final long[] mHits = new long[8];

        @Override
        public void onAttach(Activity context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopTimingLife();
        }

        @Override
        public Context getContext() {
            return mContext;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_settings);

            Context context = getContext();
            Resources resources = context.getResources();

            mDarkTheme = findPreference(KEY_DARK_THEME);
            mPrettyTime = (SwitchPreference) findPreference(Settings.KEY_PRETTY_TIME);
            mTextFormat = findPreference(KEY_TEXT_FORMAT);
            mACCookies = findPreference(KEY_AC_COOKIES);
            mSaveCookies = findPreference(KEY_SAVE_COOKIES);
            mRestoreCookies = findPreference(KEY_RESTORE_COOKIES);
            mImageSaveLocation = findPreference(Settings.KEY_IMAGE_SAVE_LOACTION);
            mAuthor = findPreference(KEY_AUTHOR);
            mSource = findPreference(KEY_SOURCE);

            mDarkTheme.setOnPreferenceChangeListener(this);
            mPrettyTime.setOnPreferenceChangeListener(this);

            mTextFormat.setOnPreferenceClickListener(this);
            mACCookies.setOnPreferenceClickListener(this);
            mSaveCookies.setOnPreferenceClickListener(this);
            mRestoreCookies.setOnPreferenceClickListener(this);
            mImageSaveLocation.setOnPreferenceClickListener(this);
            mAuthor.setOnPreferenceClickListener(this);
            mSource.setOnPreferenceClickListener(this);

            updateTextFormatSummary();

            long time = System.currentTimeMillis() - 3 * ReadableTime.HOUR_MILLIS;
            String plain = ReadableTime.getPlainTime(time);
            String timeAgo = ReadableTime.getTimeAgo(time);
            mPrettyTime.setSummaryOn(resources.getString(R.string.main_pretty_time_summary, timeAgo, plain));
            mPrettyTime.setSummaryOff(resources.getString(R.string.main_pretty_time_summary, plain, timeAgo));

            long maxAge = ACSite.getInstance().getCookieMaxAge(context);
            setACCookiesSummary(maxAge);

            updateImageSaveLocation();

            mAuthor.setSummary("Hippo <hipposeven332$gmail.com>".replaceAll("\\$", "@"));
        }

        private void setACCookiesSummary(long maxAge) {
            Resources resources = getContext().getResources();
            if (maxAge == -1) {
                mACCookies.setSummary(resources.getString(R.string.main_ac_cookies_summary_forever));
                stopTimingLife();
            } else if (maxAge == -2) {
                mACCookies.setSummary(resources.getString(R.string.main_ac_cookies_summary_no));
                stopTimingLife();
            } else {
                long time = maxAge * 1000;
                mACCookies.setSummary(resources.getString(R.string.main_ac_cookies_summary_valid,
                        ReadableTime.getTimeInterval(time)));
                startTimingLife(time);
            }
        }

        private void startTimingLife(long millisInFuture) {
            if (mTimingLife == null) {
                mTimingLife = new TimingLife(millisInFuture, 1000);
                mTimingLife.start();
            }
        }

        private void stopTimingLife() {
            if (mTimingLife != null) {
                mTimingLife.cancel();
                mTimingLife = null;
            }
        }

        private void openDirPicker() {
            UniFile uniFile = Settings.getImageSaveLocation();
            Intent intent = new Intent(getContext(), DirPickerActivity.class);
            if (uniFile != null) {
                intent.putExtra(DirPickerActivity.KEY_FILE_URI, uniFile.getUri());
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_DIR);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void openDirPickerL() {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            try {
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_DIR_L);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), R.string.em_cant_find_activity, Toast.LENGTH_SHORT).show();
            }
        }

        private void showDirPickerDialogKK() {
            new AlertDialog.Builder(getContext()).setMessage(R.string.pick_dir_kk)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openDirPicker();
                        }
                    }).show();
        }

        private void showDirPickerDialogL() {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            openDirPicker();
                            break;
                        case DialogInterface.BUTTON_NEUTRAL:
                            openDirPickerL();
                            break;
                    }
                }
            };

            new AlertDialog.Builder(getContext()).setMessage(R.string.pick_dir_l)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNeutralButton(R.string.document, listener)
                    .show();
        }

        private class SaveCookieTask extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {
                File dir = NMBAppConfig.getCookiesDir();
                if (dir == null) {
                    return null;
                }

                SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(getContext());
                List<TransportableHttpCookie> list = cookieStore.getTransportableCookies();

                boolean ok;
                File file = new File(dir, ReadableTime.getFilenamableTime(System.currentTimeMillis()));
                FileWriter fileWriter = null;
                try {
                    boolean first = true;
                    fileWriter = new FileWriter(file);
                    fileWriter.append('[');
                    for (TransportableHttpCookie thc : list) {
                        if (first) {
                            first = false;
                        } else {
                            fileWriter.append(',');
                        }
                        fileWriter.append(JSON.toJSONString(thc));
                    }
                    fileWriter.append(']');
                    fileWriter.flush();
                    ok = true;
                } catch (Exception e) {
                    ok = false;
                } finally {
                    IOUtils.closeQuietly(fileWriter);
                }

                if (!ok) {
                    file.delete();
                    return null;
                } else {
                    return file.getPath();
                }
            }

            @Override
            protected void onPostExecute(String path) {
                mSaveCookies.setEnabled(true);
                Context context = getContext();
                Toast.makeText(getContext(), path == null ? context.getString(R.string.save_cookies_failed) :
                        context.getString(R.string.save_cookies_to, path), Toast.LENGTH_SHORT).show();
            }
        }

        private class RestoreCookieDialogHelper implements DialogInterface.OnClickListener {

            private File[] mFiles;

            public RestoreCookieDialogHelper() {
                File dir = NMBAppConfig.getCookiesDir();
                if (dir == null) {
                    mFiles = new File[0];
                } else {
                    mFiles = dir.listFiles();
                    Arrays.sort(mFiles);
                }
            }

            private String[] getList() {
                int n = mFiles.length;
                String[] strings = new String[n];
                for (int i = 0; i < n; i++) {
                    strings[i] = mFiles[i].getName();
                }
                return strings;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = mFiles[which];
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    String str = IOUtils.readString(is, "UTF-8");
                    List<TransportableHttpCookie> list = JSON.parseArray(str, TransportableHttpCookie.class);
                    SimpleCookieStore cookieStore = NMBApplication.getSimpleCookieStore(getContext());
                    cookieStore.removeAll();
                    for (TransportableHttpCookie thc : list) {
                        URL url;
                        try {
                            url = new URL(thc.url);
                        } catch (MalformedURLException e) {
                            continue;
                        }
                        HttpCookie cookie = thc.to();
                        if (cookie == null) {
                            continue;
                        }
                        cookieStore.add(url, cookie);
                    }
                    Toast.makeText(getContext(), R.string.restore_cookies_successfully, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.not_valid_cookie_file, Toast.LENGTH_SHORT).show();
                } finally {
                    IOUtils.closeQuietly(is);
                    // Restore timing
                    setACCookiesSummary(-2);
                    setACCookiesSummary(ACSite.getInstance().getCookieMaxAge(getContext()));
                }
            }
        }

        private void updateTextFormatSummary() {
            mTextFormat.setSummary(getContext().getResources().getString(
                    R.string.main_text_format_summary, Settings.getFontSize(),
                    Settings.getLineSpacing()));
        }

        private class TextFormatDialogHelper implements Slider.OnSetProgressListener,
                DialogInterface.OnClickListener {

            public View mView;
            public TextView mPreview;
            public Slider mFontSize;
            public Slider mLineSpacing;

            @SuppressLint("InflateParams")
            public TextFormatDialogHelper() {
                mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_text_format, null);
                mPreview = (TextView) mView.findViewById(R.id.preview);
                mFontSize = (Slider) mView.findViewById(R.id.font_size);
                mLineSpacing = (Slider) mView.findViewById(R.id.line_spacing);

                int fontSize = Settings.getFontSize();
                int lineSpacing = Settings.getLineSpacing();

                mPreview.setTextSize(fontSize);
                mPreview.setLineSpacing(LayoutUtils.dp2pix(getContext(), lineSpacing), 1.0f);

                mFontSize.setProgress(fontSize);
                mLineSpacing.setProgress(lineSpacing);
                mFontSize.setOnSetProgressListener(this);
                mLineSpacing.setOnSetProgressListener(this);
            }

            public View getView() {
                return mView;
            }

            @Override
            public void onSetProgress(Slider slider, int newProgress, int oldProgress, boolean byUser, boolean confirm) {
                if (slider == mFontSize && byUser) {
                    mPreview.setTextSize(newProgress);
                } else if (slider == mLineSpacing && byUser) {
                    mPreview.setLineSpacing(LayoutUtils.dp2pix(getContext(), newProgress), 1.0f);
                }
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Settings.putFontSize(mFontSize.getProgress());
                    Settings.putLineSpacing(mLineSpacing.getProgress());

                    ((SettingsActivity) getContext()).setResult(RESULT_OK);

                    updateTextFormatSummary();
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (KEY_TEXT_FORMAT.equals(key)) {
                TextFormatDialogHelper helper = new TextFormatDialogHelper();
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.text_format)
                        .setView(helper.getView())
                        .setPositiveButton(android.R.string.ok, helper)
                        .show();
            } else if (KEY_AC_COOKIES.equals(key)) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 3000)) {
                    Arrays.fill(mHits, 0);
                    // TODO
                }
                return true;
            } else if (KEY_SAVE_COOKIES.equals(key)) {
                mSaveCookies.setEnabled(false);
                new SaveCookieTask().execute();
                return true;
            } else if (KEY_RESTORE_COOKIES.equals(key)) {
                RestoreCookieDialogHelper helper = new RestoreCookieDialogHelper();
                String[] list = helper.getList();
                if (list.length == 0) {
                    Toast.makeText(getContext(), R.string.cant_find_cookie_file, Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.select_cookie_file)
                            .setItems(list, helper)
                            .show();
                }
            } else if (Settings.KEY_IMAGE_SAVE_LOACTION.equals(key)) {
                int sdk = Build.VERSION.SDK_INT;
                if (sdk < Build.VERSION_CODES.KITKAT) {
                    openDirPicker();
                } else if (sdk < Build.VERSION_CODES.LOLLIPOP) {
                    showDirPickerDialogKK();
                } else {
                    showDirPickerDialogL();
                }
            } else if (KEY_AUTHOR.equals(key)) {
                ActivityHelper.sendEmail(getActivity(),
                        "hipposeven332$gmail.com".replaceAll("\\$", "@"),
                        "About Nimingban",
                        null);
            } else if (KEY_SOURCE.equals(key)) {
                ActivityHelper.openUri(getActivity(), Uri.parse("https://github.com/seven332/Nimingban"));
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (KEY_DARK_THEME.equals(key)) {
                Messenger.getInstance().notify(Constants.MESSENGER_ID_CHANGE_THEME, newValue);
            } else if (Settings.KEY_PRETTY_TIME.equals(key)) {
                ((SettingsActivity) getContext()).setResult(RESULT_OK);
                return true;
            }
            return true;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE_DIR:
                    if (resultCode == RESULT_OK) {
                        UniFile uniFile = UniFile.fromUri(getContext(), data.getData());
                        if (uniFile != null) {
                            Settings.putImageSaveLocation(uniFile);
                            updateImageSaveLocation();
                        } else {
                            Toast.makeText(getContext(), R.string.cant_get_image_save_location, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case REQUEST_CODE_PICK_IMAGE_DIR_L:
                    if (resultCode == RESULT_OK) {
                        Uri treeUri = data.getData();
                        getContext().getContentResolver().takePersistableUriPermission(
                                treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        UniFile uniFile = UniFile.fromTreeUri(getContext(), treeUri);
                        if (uniFile != null) {
                            Settings.putImageSaveLocation(uniFile);
                            updateImageSaveLocation();
                        } else {
                            Toast.makeText(getContext(), R.string.cant_get_image_save_location, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }

        public void updateImageSaveLocation() {
            UniFile uniFile = Settings.getImageSaveLocation();
            if (uniFile == null) {
                mImageSaveLocation.setSummary(R.string.main_image_save_locatio_summary_invalid);
            } else {
                mImageSaveLocation.setSummary(uniFile.getUri().toString());
            }
        }

        private class TimingLife extends CountDownTimer {

            public TimingLife(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);
            }

            @Override
            public void onTick(long millisUntilFinished) {
                setACCookiesSummary(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
            }
        }
    }
}
