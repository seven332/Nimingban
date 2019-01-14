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

package com.hippo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hippo.nimingban.R;

public class CheckBoxDialogBuilder extends AlertDialog.Builder {

    private final boolean mShowCheckbox;
    private final CheckBox mCheckBox;

    @SuppressLint("InflateParams")
    public CheckBoxDialogBuilder(Context context, CharSequence message, String checkText, boolean showCheckbox, boolean checked) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_checkbox_builder, null);
        setView(view);
        TextView messageView = (TextView) view.findViewById(R.id.message);
        mShowCheckbox = showCheckbox;
        mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
        messageView.setText(message);
        mCheckBox.setText(checkText);
        mCheckBox.setChecked(checked);
        if (!showCheckbox) mCheckBox.setVisibility(View.GONE);
    }

    public boolean isShowCheckbox() {
        return mShowCheckbox;
    }

    public boolean isChecked() {
        return mCheckBox.isChecked();
    }
}
