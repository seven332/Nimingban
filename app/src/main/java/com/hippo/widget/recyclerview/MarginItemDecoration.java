/*
 * Copyright (C) 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.widget.recyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class MarginItemDecoration extends RecyclerView.ItemDecoration {

    private int mMarginLeft;
    private int mMarginTop;
    private int mMarginRight;
    private int mMarginBottom;

    public MarginItemDecoration(int margin) {
        setMargin(margin);
    }

    public MarginItemDecoration(int marginLeft, int marginTop, int marginRight, int marginBottom) {
        setMargin(marginLeft, marginTop, marginRight, marginBottom);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
            RecyclerView parent, RecyclerView.State state) {
        outRect.set(mMarginLeft, mMarginTop, mMarginRight, mMarginBottom);
    }

    public void setMargin(int margin) {
        mMarginLeft = mMarginTop = mMarginRight = mMarginBottom = margin;
    }

    public void setMargin(int marginLeft, int marginTop, int marginRight, int marginBottom) {
        mMarginLeft = marginLeft;
        mMarginTop = marginTop;
        mMarginRight = marginRight;
        mMarginBottom = marginBottom;
    }
}
