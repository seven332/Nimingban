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

package com.hippo.nimingban;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.hippo.nimingban.widget.GuideView;
import com.hippo.yorozuya.ViewUtils;

public class GuideHelper {

    private static ViewGroup getParent(Activity activity) {
        ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView();
        View view = ViewUtils.getChild(parent, android.R.id.content);
        if (view instanceof ViewGroup) {
            parent = (ViewGroup) view;
        }
        return parent;
    }

    public static class Builder {

        private Activity mActivity;
        private int mPadding;
        private int mPaddingTop;
        private int mPaddingBottom;
        private int mColor;
        @GuideView.MessagePosition
        private int mMessagePosition = Gravity.TOP;
        private int mBackgroundColor;
        private CharSequence mMessage;
        private CharSequence mButton;
        private View.OnClickListener mOnDissmisListener;

        public Builder(@NonNull Activity activity) {
            mActivity = activity;
        }

        public Builder setPadding(int padding) {
            mPadding = padding;
            mPaddingTop = padding;
            mPaddingBottom = padding;
            return this;
        }

        public Builder setPaddingTop(int paddingTop) {
            mPaddingTop = paddingTop;
            return this;
        }

        public Builder setPaddingBottom(int paddingTop) {
            mPaddingBottom = paddingTop;
            return this;
        }

        public Builder setColor(int color) {
            mColor = color;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            mBackgroundColor = backgroundColor;
            return this;
        }

        public Builder setMessagePosition(@GuideView.MessagePosition int position) {
            mMessagePosition = position;
            return this;
        }

        public Builder setMessage(CharSequence text) {
            mMessage = text;
            return this;
        }

        public Builder setButton(CharSequence text) {
            mButton = text;
            return this;
        }

        public Builder setOnDissmisListener(View.OnClickListener listener) {
            mOnDissmisListener = listener;
            return this;
        }

        public void show() {
            ViewGroup parent = getParent(mActivity);
            GuideView guideView = new GuideView(mActivity);
            guideView.setColor(mColor);
            guideView.setPadding(mPadding, mPaddingTop, mPadding, mPaddingBottom);
            guideView.setBackgroundColor(mBackgroundColor);
            guideView.setMessagePosition(mMessagePosition);
            guideView.setMessage(mMessage);
            guideView.setButton(mButton);
            guideView.setOnDissmisListener(mOnDissmisListener);
            parent.addView(guideView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }
}
