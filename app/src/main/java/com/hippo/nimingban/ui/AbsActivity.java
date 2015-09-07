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

import android.os.Bundle;

import com.hippo.nimingban.Constants;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.util.Settings;
import com.hippo.styleable.StyleableActivity;
import com.hippo.yorozuya.Messenger;
import com.tendcloud.tenddata.TCAgent;

public abstract class AbsActivity extends StyleableActivity implements Messenger.Receiver {

    protected abstract int getLightThemeResId();

    protected abstract int getDarkThemeResId();

    private boolean mHasResume;

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
    protected void onResume() {
        super.onResume();

        if (NMBApplication.hasInitTCAgent(this) && Settings.getAnalysis()) {
            mHasResume = true;
            TCAgent.onResume(this);
        } else {
            mHasResume = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mHasResume) {
            TCAgent.onPause(this);
        }
    }

    @Override
    public void onReceive(final int id, final Object obj) {
        recreate();
    }
}
