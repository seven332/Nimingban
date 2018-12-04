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

import java.util.LinkedHashMap;

public class UpdateStatus {

    public int versionCode;
    public String versionName;
    public String info;
    public long size;
    public String apkUrl;
    public LinkedHashMap<String, String> discUrls;
    public String failedUrl;

    @Override
    public String toString() {
        return "versionCode = " + versionCode + ", versionName = " + versionName + ", info = " + info +
                ", size = " + size + ", apkUrl = " + apkUrl + ", discUrls = " + discUrls + ", failedUrl = " + failedUrl;
    }
}
