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

package com.hippo.preference;

import android.content.Context;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;

import com.hippo.nimingban.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FixedSwitchPreference extends SwitchPreference {

    private static Method sSyncSummaryViewMethod;

    static {
        try {
            sSyncSummaryViewMethod = TwoStatePreference.class.getDeclaredMethod("syncSummaryView", View.class);
            sSyncSummaryViewMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            sSyncSummaryViewMethod = null;
        }
    }

    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            FixedSwitchPreference.this.setChecked(isChecked);
        }
    }

    public FixedSwitchPreference(Context context) {
        super(context);
    }

    public FixedSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            if (checkableView instanceof SwitchCompat) {
                final SwitchCompat switchView = (SwitchCompat) checkableView;
                switchView.setOnCheckedChangeListener(null);
            }

            ((Checkable) checkableView).setChecked(isChecked());

            if (checkableView instanceof SwitchCompat) {
                final SwitchCompat switchView = (SwitchCompat) checkableView;
                switchView.setTextOn(getSummaryOn());
                switchView.setTextOff(getSummaryOff());
                switchView.setOnCheckedChangeListener(mListener);
            }
        }

        if (sSyncSummaryViewMethod != null) {
            try {
                sSyncSummaryViewMethod.invoke(this, view);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
