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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;

import com.google.analytics.tracking.android.EasyTracker;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.util.Settings;
import com.hippo.yorozuya.Messenger;

public abstract class AbsPreferenceActivity extends AppCompatPreferenceActivity implements Messenger.Receiver {

    @StyleRes
    protected abstract int getLightThemeResId();

    @StyleRes
    protected abstract int getDarkThemeResId();

    private boolean mTrackStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.getDarkTheme() ? getDarkThemeResId() : getLightThemeResId());

        super.onCreate(savedInstanceState);

        Messenger.getInstance().register(Constants.MESSENGER_ID_CHANGE_THEME, this);
    }

    @Override
    protected void onDestroy() {
        Messenger.getInstance().unregister(Constants.MESSENGER_ID_CHANGE_THEME, this);

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Settings.getAnalysis()) {
            EasyTracker.getInstance(this).activityStart(this);
            mTrackStarted = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mTrackStarted) {
            EasyTracker.getInstance(this).activityStop(this);
            mTrackStarted = false;
        }
    }

    @Override
    public void onReceive(final int id, final Object obj) {
        if (id == Constants.MESSENGER_ID_CHANGE_THEME) {
            recreate();
        }
    }

    protected void setActionBarUpIndicator(Drawable drawable) {
        ActionBarDrawerToggle.Delegate delegate = getDrawerToggleDelegate();
        if (delegate != null) {
            delegate.setActionBarUpIndicator(drawable, 0);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
}
