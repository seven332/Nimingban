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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.util.Settings;
import com.hippo.styleable.StyleableActivity;
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

        private static final String KEY_AC_COOKIES = "ac_cookies";

        private Context mContext;

        private SwitchPreference mPrettyTime;
        private Preference mACCookies;

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

            mPrettyTime.setOnPreferenceChangeListener(this);

            mACCookies.setOnPreferenceClickListener(this);

            long time = System.currentTimeMillis() - 3 * ReadableTime.HOUR_MILLIS;
            String plain = ReadableTime.getPlainTime(time);
            String timeAgo = ReadableTime.getTimeAgo(time);
            mPrettyTime.setSummaryOn(resources.getString(R.string.main_pretty_time_summary, timeAgo, plain));
            mPrettyTime.setSummaryOff(resources.getString(R.string.main_pretty_time_summary, plain, timeAgo));

            long maxAge = ACSite.getInstance().getCookieMaxAge(context);
            setACCookiesSummary(maxAge);
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
