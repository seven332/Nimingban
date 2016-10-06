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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.hippo.nimingban.GuideHelper;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.ui.fragment.BaseFragment;
import com.hippo.nimingban.ui.fragment.FragmentHost;
import com.hippo.nimingban.ui.fragment.PostFragment;
import com.hippo.nimingban.ui.fragment.TypeSendFragment;
import com.hippo.nimingban.util.Settings;
import com.hippo.nimingban.widget.PostLayout;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;

public final class PostActivity extends SwipeBackActivity
        implements FragmentHost, PostFragment.Callback, TypeSendFragment.Callback {

    public static final String ACTION_POST = "com.hippo.nimingban.ui.PostActivity.action.POST";
    public static final String ACTION_SITE_ID = "com.hippo.nimingban.ui.PostActivity.action.SITE_ID";
    public static final String ACTION_SITE_REPLY_ID = "com.hippo.nimingban.ui.PostActivity.action.SITE_REPLY_ID";

    public static final String KEY_POST = "post";
    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";

    public static final String TAG_FRAGMENT_POST = "post";
    public static final String TAG_FRAGMENT_TYPE_SEND = "type_send";

    private PostLayout mPostLayout;

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
            bundle.putString(PostFragment.KEY_ACTION, intent.getAction());
            bundle.putParcelable(PostFragment.KEY_DATA, intent.getData());
            bundle.putInt(PostFragment.KEY_SITE, intent.getIntExtra(KEY_SITE, -1));
            bundle.putString(PostFragment.KEY_ID, intent.getStringExtra(KEY_ID));
            bundle.putParcelable(PostFragment.KEY_POST, intent.getParcelableExtra(KEY_POST));
        }
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimaryDark));
        setContentView(R.layout.activity_post);

        mPostLayout = (PostLayout) findViewById(R.id.fragment_container);

        if (mPostLayout != null) {
            if (savedInstanceState == null) {
                PostFragment postFragment = new PostFragment();
                postFragment.setArguments(createArgs());
                postFragment.setFragmentHost(this);
                postFragment.setCallback(this);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_container, postFragment, TAG_FRAGMENT_POST);
                transaction.commitAllowingStateLoss();
            } else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                PostFragment postFragment = (PostFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_POST);
                if (postFragment != null) {
                    postFragment.setFragmentHost(this);
                    postFragment.setCallback(this);
                }
                TypeSendFragment typeSendFragment = (TypeSendFragment)
                        fragmentManager.findFragmentByTag(TAG_FRAGMENT_TYPE_SEND);
                if (typeSendFragment != null) {
                    typeSendFragment.setFragmentHost(this);
                    typeSendFragment.setCallback(this);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_TYPE_SEND);

        if (fragment != null && fragment instanceof TypeSendFragment) {
            TypeSendFragment typeSendFragment = (TypeSendFragment) fragment;
            if (mPostLayout.getTypeSendState() == PostLayout.STATE_HIDE) {
                mPostLayout.showTypeSend();
            } else if (typeSendFragment.checkBeforeFinish()) {
                typeSendFragment.getFragmentHost().finishFragment(typeSendFragment);
            }
        } else {
            super.onBackPressed();
        }
    }

    private void showSwipeGuide() {
        new GuideHelper.Builder(this)
                .setColor(ResourcesUtils.getAttrColor(this, R.attr.colorPrimary))
                .setPadding(LayoutUtils.dp2pix(this, 16))
                .setMessagePosition(Gravity.TOP)
                .setMessage(getString(R.string.swipe_toolbar_hide_show))
                .setButton(getString(R.string.get_it))
                .setBackgroundColor(0x73000000)
                .setOnDissmisListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Settings.putGuideTypeSend(false);
                    }
                }).show();
    }

    @Override
    public void reply(Site site, String id, String presetText, boolean report) {
        if (Settings.getGuideTypeSend()) {
            showSwipeGuide();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_TYPE_SEND);

        if (fragment == null && !TextUtils.isEmpty(id)) {
            Bundle args = new Bundle();
            args.putString(TypeSendFragment.KEY_ACTION,
                    report ? TypeSendFragment.ACTION_REPORT : TypeSendFragment.ACTION_REPLY);
            args.putInt(TypeSendFragment.KEY_SITE, site.getId());
            args.putString(TypeSendFragment.KEY_ID, id);
            args.putString(TypeSendFragment.KEY_TEXT, presetText);

            TypeSendFragment typeSendFragment = new TypeSendFragment();
            typeSendFragment.setArguments(args);
            typeSendFragment.setFragmentHost(this);
            typeSendFragment.setCallback(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_translate_in, R.anim.fragment_translate_out);
            transaction.add(R.id.fragment_container, typeSendFragment, TAG_FRAGMENT_TYPE_SEND);
            transaction.commitAllowingStateLoss();

            getSwipeBackLayout().setSwipeEnabled(false);
        }
    }

    @Override
    public void onClickBack(TypeSendFragment fragment) {
        if (fragment.checkBeforeFinish()) {
            fragment.getFragmentHost().finishFragment(fragment);
        } else {
            mPostLayout.showTypeSend();
        }
    }

    @Override
    public void finishFragment(BaseFragment fragment) {
        if (fragment instanceof PostFragment) {
            finish();
        } else if (fragment instanceof TypeSendFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_translate_in, R.anim.fragment_translate_out);
            transaction.remove(fragment);
            transaction.commitAllowingStateLoss();

            getSwipeBackLayout().setSwipeEnabled(true);

            mPostLayout.onRemoveTypeSend();
        }
    }
}
