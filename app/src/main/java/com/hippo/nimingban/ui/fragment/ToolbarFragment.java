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

package com.hippo.nimingban.ui.fragment;

/*
 * Created by Hippo on 10/7/2016.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hippo.nimingban.R;

public class ToolbarFragment extends BaseFragment {

    private Toolbar mToolbar;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_toolbar, container, false);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        // Add content to contentPanel
        final View content = onCreateView2(inflater, container, savedInstanceState);
        if (content != null) {
            final ViewGroup contentPanel = (ViewGroup) view.findViewById(R.id.content_panel);
            contentPanel.addView(content);
        }

        return view;
    }

    @Nullable
    public View onCreateView2(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return null;
    }


    public void setTitle(@StringRes int resId) {
        mToolbar.setTitle(resId);
    }

    public void setTitle(CharSequence title) {
        mToolbar.setTitle(title);
    }
}
