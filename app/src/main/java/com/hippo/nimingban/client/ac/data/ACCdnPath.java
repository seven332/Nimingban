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

package com.hippo.nimingban.client.ac.data;

public class ACCdnPath {

    public static final String DEFAULT_CDN_PATH = "https://nmbimg.fastmirror.org/";
    public static final String DEFAULT_CDN_HOST = "nmbimg.fastmirror.org";
    public static final float DEFAULT_CDN_RATE = 0.5f;

    public String url;
    public float rate;

    public ACCdnPath() {
        this(DEFAULT_CDN_PATH, DEFAULT_CDN_RATE);
    }

    public ACCdnPath(String url, float rate) {
        this.url = url;
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "url = " + url + ", rate = " + rate;
    }
}
