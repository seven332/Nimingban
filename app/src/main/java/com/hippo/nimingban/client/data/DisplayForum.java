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

package com.hippo.nimingban.client.data;

import com.hippo.text.Html;

public class DisplayForum extends Forum {

    public Site site;
    public String id;
    public String displayname;
    public int priority;
    public boolean visibility;
    public String msg;
    public boolean official;

    private CharSequence name;

    @Override
    public String toString() {
        return "site = " + site + ", id = " + id + ", displayname = " + displayname +
                ", priority = " + priority + ", visibility = " + visibility;
    }

    @Override
    public Site getNMBSite() {
        return site;
    }

    @Override
    public String getNMBId() {
        return id;
    }

    @Override
    public CharSequence getNMBDisplayname() {
        if (name == null) {
            if (displayname == null) {
                name = "Forum";
            } else {
                name = Html.fromHtml(displayname);
            }
        }
        return name;
    }

    @Override
    public String getNMBMsg() {
        return msg;
    }

    public boolean getVisibility() {
        return visibility;
    }
}
