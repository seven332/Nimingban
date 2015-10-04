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

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.hippo.nimingban.R;

public class ToolbarActivityHelper {

    public static void setContentView(AppCompatActivity activity, @LayoutRes int layoutResID) {
        activity.setContentView(R.layout.activity_toolbar);

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        ViewGroup content = (ViewGroup) activity.findViewById(R.id.content_panel);
        activity.getLayoutInflater().inflate(layoutResID, content);
        content.getChildAt(content.getChildCount() - 1).bringToFront();
    }
}
