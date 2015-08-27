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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class Post {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
    private static final Object sDateFormatLock = new Object();

    abstract public String getNMBId();

    abstract public CharSequence getNMBTime();

    abstract public CharSequence getNMBUser();

    abstract public CharSequence getNMBReplyCount();

    abstract public CharSequence getNMBContent();

    abstract public String getNMBThumbUrl();

    abstract public String getNMBImageUrl();

    public static String generateTimeString(Date date) {
        synchronized (sDateFormatLock) {
            return DATE_FORMAT.format(date);
        }
    }
}
