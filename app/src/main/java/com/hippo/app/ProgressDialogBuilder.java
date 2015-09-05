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

package com.hippo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hippo.nimingban.R;

public class ProgressDialogBuilder extends AlertDialog.Builder {

    private TextView mMessageView;

    public ProgressDialogBuilder(Context context) {
        super(context);
        init();
    }

    public ProgressDialogBuilder(Context context, int theme) {
        super(context, theme);
        init();
    }

    private void init() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_progress, null);
        mMessageView = (TextView) view.findViewById(R.id.message);
        super.setView(view);
    }

    @Override
    public ProgressDialogBuilder setView(int layoutResId) {
        throw new IllegalStateException("Can't setView");
    }

    @Override
    public ProgressDialogBuilder setView(View view) {
        throw new IllegalStateException("Can't setView");
    }

    @Override
    public ProgressDialogBuilder setView(View view, int viewSpacingLeft,
            int viewSpacingTop,int viewSpacingRight, int viewSpacingBottom) {
        throw new IllegalStateException("Can't setView");
    }

    @Override
    public ProgressDialogBuilder setMessage(CharSequence message) {
        mMessageView.setText(message);
        return this;
    }

    @Override
    public ProgressDialogBuilder setMessage(int messageId) {
        mMessageView.setText(messageId);
        return this;
    }
}
