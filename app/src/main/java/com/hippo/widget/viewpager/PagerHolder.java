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

package com.hippo.widget.viewpager;

import android.view.View;

public class PagerHolder {

    int position = RecyclerPagerAdapter.INVALID_POSITION;

    int oldPosition = RecyclerPagerAdapter.INVALID_POSITION;

    public View itemView;

    public PagerHolder(View itemView) {
        this.itemView = itemView;
    }
}
