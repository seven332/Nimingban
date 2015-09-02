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

package com.hippo.nimingban.network;

import com.hippo.yorozuya.MathUtils;

import java.net.HttpCookie;

public class HttpCookieWithId {

    public long id;
    public HttpCookie httpCookie;
    private long mMaxAge;
    private long mWhenCreated;

    public HttpCookieWithId(long id, HttpCookie httpCookie) {
        this.id = id;
        this.httpCookie = httpCookie;
        mMaxAge = httpCookie.getMaxAge();
        mWhenCreated = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HttpCookieWithId) {
            return id == ((HttpCookieWithId) o).id;
        } else {
            return false;
        }
    }

    /**
     * @return 0 - Long.MAX_VALUE for the actual max age, -1 for eternal life
     */
    public long getMaxAge() {
        if (mMaxAge == -1l) {
            return -1l;
        } else {
            return MathUtils.clamp(mMaxAge - ((System.currentTimeMillis() - mWhenCreated) / 1000), 0l, Long.MAX_VALUE);
        }
    }

    /**
     * Returns true if this cookie's Max-Age is 0.
     */
    public boolean hasExpired() {
        long maxAge = getMaxAge();

        if (maxAge == -1l) {
            return false;
        }

        boolean expired = false;
        if (maxAge <= 0l) {
            expired = true;
        }
        return expired;
    }
}
