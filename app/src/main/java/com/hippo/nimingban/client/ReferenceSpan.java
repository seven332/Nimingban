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

package com.hippo.nimingban.client;

import android.text.style.ClickableSpan;
import android.view.View;

import com.hippo.nimingban.client.data.Site;

public class ReferenceSpan extends ClickableSpan {

    private Site mSite;
    private String mId;

    public ReferenceSpan(Site site, String id) {
        mSite = site;
        mId = id;
    }

    public Site getSite() {
        return mSite;
    }

    public String getId() {
        return mId;
    }

    @Override
    public void onClick(View widget) {
    }
}
