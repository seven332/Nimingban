/*
 * Copyright 2016 Hippo Seven
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

/*
 * Created by Hippo on 10/7/2016.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.hippo.drawerlayout.DrawerLayout;
import com.hippo.nimingban.R;
import com.hippo.nimingban.ui.fragment.ForumFragment;
import com.hippo.yorozuya.ResourcesUtils;

public class ForumActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT_FORUM = "fragment_forum";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        // Set status bar color
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));

        // Add fragment
        if (savedInstanceState == null) {
            final ForumFragment forumFragment = new ForumFragment();
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, forumFragment, TAG_FRAGMENT_FORUM);
            transaction.commitAllowingStateLoss();
        }
    }
}
