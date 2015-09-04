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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.util.Settings;
import com.hippo.styleable.StyleableActivity;
import com.hippo.unifile.UniFile;
import com.hippo.util.ReadableTime;

import java.util.Arrays;

public class SettingsActivity extends StyleableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainFragement()).commit();
    }

    public static final class MainFragement extends PreferenceFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        public static final int REQUEST_CODE_PICK_IMAGE_DIR = 0;
        public static final int REQUEST_CODE_PICK_IMAGE_DIR_L = 1;

        private static final String KEY_AC_COOKIES = "ac_cookies";

        private Context mContext;

        private SwitchPreference mPrettyTime;
        private Preference mACCookies;
        private Preference mImageSaveLocation;

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

            mPrettyTime = (SwitchPreference) findPreference(Settings.KEY_PRETTY_TIME);
            mACCookies = findPreference(KEY_AC_COOKIES);
            mImageSaveLocation = findPreference(Settings.KEY_IMAGE_SAVE_LOACTION);

            mPrettyTime.setOnPreferenceChangeListener(this);

            mACCookies.setOnPreferenceClickListener(this);
            mImageSaveLocation.setOnPreferenceClickListener(this);

            long time = System.currentTimeMillis() - 3 * ReadableTime.HOUR_MILLIS;
            String plain = ReadableTime.getPlainTime(time);
            String timeAgo = ReadableTime.getTimeAgo(time);
            mPrettyTime.setSummaryOn(resources.getString(R.string.main_pretty_time_summary, timeAgo, plain));
            mPrettyTime.setSummaryOff(resources.getString(R.string.main_pretty_time_summary, plain, timeAgo));

            long maxAge = ACSite.getInstance().getCookieMaxAge(context);
            setACCookiesSummary(maxAge);

            updateImageSaveLocation();
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
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_DIR_L);
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

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (KEY_AC_COOKIES.equals(key)) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length-1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 3000)) {
                    Arrays.fill(mHits, 0);
                    // TODO
                }
                return true;
            } else if (Settings.KEY_IMAGE_SAVE_LOACTION.equals(key)) {
                int sdk = Build.VERSION.SDK_INT;
                if (sdk < Build.VERSION_CODES.KITKAT) {
                    openDirPicker();
                } else if (sdk < Build.VERSION_CODES.LOLLIPOP) {
                    showDirPickerDialogKK();
                } else {
                    showDirPickerDialogL();
                }
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (Settings.KEY_PRETTY_TIME.equals(key)) {
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
