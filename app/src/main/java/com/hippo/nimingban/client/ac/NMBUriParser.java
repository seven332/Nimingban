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

package com.hippo.nimingban.client.ac;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.Site;
import com.hippo.yorozuya.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NMBUriParser {

    private static final String[] AC_HOSTS = {
            "h.nimingban.com",
            "h.acfun.tv",
            "hacfun.tv",
            "adnmb.com",
            "adnmb1.com",
            "adnmb2.com",
    };

    private static final Pattern ID_PATTERN = Pattern.compile("^(?:/m)?/t/(\\d+)");

    @NonNull
    public static PostResult parsePostUri(Uri uri) {
        PostResult result = new PostResult();
        if (uri == null) {
            return result;
        }

        if (Utilities.contain(AC_HOSTS, uri.getHost())) {
            result.site = ACSite.getInstance();
        }

        String path = uri.getPath();
        if (path != null) {
            Matcher matcher = ID_PATTERN.matcher(path);
            if (matcher.find()) {
                result.id = matcher.group(1);
            }
        }

        return result;
    }

    public static class PostResult {
        public Site site;
        public String id;
    }
}
