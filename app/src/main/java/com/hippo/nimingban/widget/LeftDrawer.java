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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hippo.nimingban.Analysis;
import com.hippo.nimingban.Constants;
import com.hippo.nimingban.R;
import com.hippo.nimingban.util.Settings;
import com.hippo.rippleold.RippleSalon;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.DrawerListView;
import com.hippo.yorozuya.Messenger;
import com.hippo.yorozuya.ResourcesUtils;

import java.io.File;

public final class LeftDrawer extends ScrollView implements AdapterView.OnItemClickListener,
        HeaderImageView.OnLongClickImageListener, View.OnClickListener {

    private static final int INDEX_SEARCH = 0;
    private static final int INDEX_FEED = 1;
    private static final int INDEX_RECORD = 2;
    private static final int INDEX_SETTINGS = 3;

    private HeaderImageView mHeader;
    private DrawerListView mDrawerListView;
    private TextView mDarkTheme;

    private long mHit;

    private boolean mHitDarkTheme = false;

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
        setFillViewport(true);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);

        LayoutInflater.from(context).inflate(R.layout.widget_left_drawer, this);
        mHeader = (HeaderImageView) findViewById(R.id.header);
        mDrawerListView = (DrawerListView) findViewById(R.id.drawer_list_view);
        mDarkTheme = (TextView) findViewById(R.id.dark_theme);

        mHeader.setOnLongClickImageListener(this);

        Resources resources = context.getResources();

        Drawable search = VectorDrawable.create(context, R.drawable.ic_magnify);
        Drawable feed = VectorDrawable.create(context, R.drawable.ic_rss);
        Drawable record = VectorDrawable.create(context, R.drawable.ic_history);
        Drawable settings = VectorDrawable.create(context, R.drawable.ic_settings);

        Drawable[] drawables = {
                search,
                feed,
                record,
                settings
        };
        String[] strings = {
                resources.getString(R.string.search),
                resources.getString(R.string.feed),
                resources.getString(R.string.record),
                resources.getString(R.string.settings)
        };

        mDrawerListView.setData(drawables, strings);
        mDrawerListView.setOnItemClickListener(this);

        mDarkTheme.setText(Settings.getDarkTheme() ? R.string.let_there_light : R.string.let_there_dark);
        mDarkTheme.setOnClickListener(this);
        RippleSalon.addRipple(mDarkTheme, ResourcesUtils.getAttrBoolean(context, R.attr.dark));
    }

    public void loadHeaderImageView() {
        mHeader.load();
    }

    public void unloadHeaderImageView() {
        mHeader.unload();
    }

    @Override
    public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
        // Avoid qiuck click action
        long now = System.currentTimeMillis();
        if (now - mHit > 500) {
            switch (position) {
                case INDEX_SEARCH:
                    if (mHelper != null) {
                        mHelper.onClickSearch();
                    }
                    break;
                case INDEX_FEED:
                    if (mHelper != null) {
                        mHelper.onClickFeed();
                    }
                    break;
                case INDEX_RECORD:
                    if (mHelper != null) {
                        mHelper.onClickRecord();
                    }
                    break;
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

    @Override
    public boolean onLongClickImage(File imageFile) {
        if (mHelper != null) {
            mHelper.OnLongClickImage(imageFile);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        if (mDarkTheme == v) {
            if (!mHitDarkTheme) {
                // Analysis
                Analysis.action(getContext(), "dark_theme");

                mHitDarkTheme = true;
                boolean darkTheme = !Settings.getDarkTheme();
                Settings.putDarkTheme(darkTheme);
                Messenger.getInstance().notify(Constants.MESSENGER_ID_CHANGE_THEME, darkTheme);
            }
        }
    }

    public interface Helper {

        void OnLongClickImage(File imageFile);

        void onClickSearch();

        void onClickFeed();

        void onClickRecord();

        void onClickSettings();
    }
}
