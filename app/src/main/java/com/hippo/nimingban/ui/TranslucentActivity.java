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

public abstract class TranslucentActivity extends AbsActivity implements TranslucentHelper.SuperActivity {

    private TranslucentHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHelper = new TranslucentHelper();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void superSetContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(int layoutResID) {
        mHelper.setContentView(this, layoutResID);
    }

    public void setStatusBarColor(int color) {
        mHelper.setStatusBarColor(color);
    }

    public void setNavigationBarColor(int color) {
        mHelper.setNavigationBarColor(color);
    }

    public void setShowStatusBar(boolean showStatusBar) {
        mHelper.setShowStatusBar(showStatusBar);
    }

    public void setShowNavigationBar(boolean showNavigationBar) {
        mHelper.setShowNavigationBar(showNavigationBar);
    }
}
