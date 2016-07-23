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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.hippo.nimingban.R;
import com.hippo.nimingban.ui.fragment.BaseFragment;
import com.hippo.nimingban.ui.fragment.FragmentHost;
import com.hippo.nimingban.ui.fragment.TypeSendFragment;
import com.hippo.nimingban.util.Settings;
import com.hippo.yorozuya.ResourcesUtils;

public final class TypeSendActivity extends TranslucentActivity
        implements FragmentHost, TypeSendFragment.Callback {

    public static final String TAG_FRAGMENT_TYPE_SEND = "type_send";

    public static final String ACTION_REPLY = TypeSendFragment.ACTION_REPLY;
    public static final String ACTION_CREATE_POST = TypeSendFragment.ACTION_CREATE_POST;
    public static final String ACTION_REPORT = TypeSendFragment.ACTION_REPORT;

    public static final String KEY_SITE = TypeSendFragment.KEY_SITE;
    public static final String KEY_ID = TypeSendFragment.KEY_ID;
    public static final String KEY_TEXT = TypeSendFragment.KEY_TEXT;

    @Override
    protected int getLightThemeResId() {
        return Settings.getColorStatusBar() ? R.style.SwipeActivity : R.style.SwipeActivity_NoStatus;
    }

    @Override
    protected int getDarkThemeResId() {
        return Settings.getColorStatusBar() ? R.style.SwipeActivity_Dark : R.style.SwipeActivity_Dark_NoStatus;
    }

    private Bundle createArgs() {
        Bundle bundle = new Bundle();
        Intent intent = getIntent();
        if (intent != null) {
            bundle.putString(TypeSendFragment.KEY_ACTION, intent.getAction());
            bundle.putString(TypeSendFragment.KEY_TYPE, intent.getType());
            bundle.putInt(TypeSendFragment.KEY_SITE, intent.getIntExtra(KEY_SITE, -1));
            bundle.putString(TypeSendFragment.KEY_ID, intent.getStringExtra(KEY_ID));
            bundle.putString(TypeSendFragment.KEY_TEXT, intent.getStringExtra(KEY_TEXT));
            bundle.putString(TypeSendFragment.KEY_EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
            bundle.putParcelable(TypeSendFragment.KEY_EXTRA_STREAM, intent.getParcelableExtra(Intent.EXTRA_STREAM));
        }
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        setContentView(R.layout.activity_type_send);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                TypeSendFragment fragment = new TypeSendFragment();
                fragment.setArguments(createArgs());
                fragment.setFragmentHost(this);
                fragment.setCallback(this);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_container, fragment, TAG_FRAGMENT_TYPE_SEND);
                transaction.commitAllowingStateLoss();
            } else {
                TypeSendFragment fragment = (TypeSendFragment) getSupportFragmentManager()
                        .findFragmentByTag(TAG_FRAGMENT_TYPE_SEND);
                fragment.setFragmentHost(this);
                fragment.setCallback(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        TypeSendFragment fragment = (TypeSendFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_TYPE_SEND);
        if (fragment.checkBeforeFinish()) {
            finish();
        }
    }

    @Override
    public void finishFragment(BaseFragment fragment) {
        finish();
    }

    @Override
    public void onClickBack(TypeSendFragment fragment) {
        if (fragment.checkBeforeFinish()) {
            fragment.getFragmentHost().finishFragment(fragment);
        }
    }
}
