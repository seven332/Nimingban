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

package com.hippo.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hippo.nimingban.R;
import com.hippo.yorozuya.LayoutUtils;

public class IconListPreference extends ListPreference {

    private int[] mEntryIcons;
    private int mClickedDialogEntryIndex;

    public IconListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    private String getNameFromText(String text) {
        int index1 = text.lastIndexOf('/');
        int index2 = text.lastIndexOf('.');
        if (index1 >= 0 && index2 >= 0 && index2 >= index1) {
            return text.substring(index1+1, index2);
        } else {
            return null;
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconListPreference, defStyleAttr, defStyleRes);
        CharSequence[] entryIconTexts = a.getTextArray(R.styleable.IconListPreference_entryIcons);
        if (entryIconTexts != null) {
            mEntryIcons = new int[entryIconTexts.length];
            Resources resources = context.getResources();
            String packageName = context.getPackageName();
            for (int i = 0, size = entryIconTexts.length; i < size; i++) {
                mEntryIcons[i] = resources.getIdentifier(getNameFromText(entryIconTexts[i].toString()), "mipmap", packageName);
            }
        }
        a.recycle();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View iconView = view.findViewById(R.id.iconWidget);
        if (iconView instanceof ImageView) {
            final ImageView imageView = (ImageView) iconView;
            imageView.setImageResource(getIconResId());
        }
    }

    @DrawableRes
    public int getIconResId() {
        int index = findIndexOfValue(getValue());
        return index >= 0 && mEntryIcons != null ? mEntryIcons[index] : 0;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        if (getEntries() == null || getEntryValues() == null || mEntryIcons == null) {
            throw new IllegalStateException(
                    "IconListPreference requires an entries array and an entryValues array and entryIcons array");
        }

        mClickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setSingleChoiceItems(new IconAdapter(), mClickedDialogEntryIndex,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedDialogEntryIndex = which;

                        /*
                         * Clicking on an item simulates the positive button
                         * click, and dismisses the dialog.
                         */
                        IconListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                });

        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    private class IconAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return getEntries().length;
        }

        @Override
        public Object getItem(int position) {
            return getEntries()[position];
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_single_choice, parent, false);
            }

            TextView textView = (TextView) convertView;
            textView.setText(getEntries()[position]);
            Drawable icon = getContext().getResources().getDrawable(mEntryIcons[position]);
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            textView.setCompoundDrawablePadding(LayoutUtils.dp2pix(getContext(), 16));
            textView.setCompoundDrawables(icon, null, null, null);

            return convertView;
        }
    }
}
