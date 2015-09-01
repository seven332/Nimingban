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

package com.hippo.nimingban.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.hippo.nimingban.R;
import com.hippo.widget.DrawerListView;

public class LeftDrawer extends LinearLayout implements AdapterView.OnItemClickListener {

    private static final int INDEX_SETTINGS = 0;

    private HeaderImageView mHeader;
    private DrawerListView mDrawerListView;

    private long mHit;

    private Helper mHelper;

    public LeftDrawer(Context context) {
        super(context);
        init(context);
    }

    public LeftDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LeftDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.widget_left_drawer, this);
        mHeader = (HeaderImageView) findViewById(R.id.header);
        mDrawerListView = (DrawerListView) findViewById(R.id.drawer_list_view);

        Resources resources = context.getResources();

        Drawable[] drawables = {
                resources.getDrawable(R.drawable.ic_setting_light_x24) // TODO darktheme
        };
        String[] strings = {
                "Settings" // TODO hardcode
        };

        mDrawerListView.setData(drawables, strings);
        mDrawerListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Avoid qiuck click action
        long now = System.currentTimeMillis();
        if (now - mHit > 500) {
            switch (position) {
                case INDEX_SETTINGS:
                    if (mHelper != null) {
                        mHelper.onClickSettings();
                    }
                    break;
            }
        }
        mHit = now;
    }

    public void setHelper(Helper helper) {
        mHelper = helper;
    }

    public interface Helper {

        void onClickSettings();
    }
}
