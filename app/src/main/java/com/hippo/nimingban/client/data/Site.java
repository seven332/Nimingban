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

import android.content.Context;

import com.hippo.yorozuya.MathUtils;

public abstract class Site {

    public static final int RANDOM = 0;
    public static final int AC = 1;
    public static final int KUKUKU = 2;
    public static final int SITE_MIN = AC;
    public static final int SITE_MAX = AC;

    public static Site fromId(int id) {
        switch (id) {
            case RANDOM:
                return fromId(MathUtils.random(SITE_MIN, SITE_MAX + 1));
            case AC:
                return ACSite.getInstance();
            default:
                throw new IllegalStateException("Unknown site " + id);
        }
    }

    public static boolean isValid(int id) {
        return id >= SITE_MIN && id <= SITE_MAX;
    }

    public abstract int getId();

    public abstract String getReadableName(Context context);

    /**
     * @return 0 or negative for none
     */
    public abstract long getCookieMaxAge(Context context);

    public abstract String getUserId(Context context);

    public abstract String getPostTitle(Context context, String postId);
}
