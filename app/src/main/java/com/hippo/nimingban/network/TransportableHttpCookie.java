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

import android.support.annotation.Nullable;

import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransportableHttpCookie {

    public String name;
    public String value;
    public String comment;
    public String commentURL;
    public boolean discard;
    public String domain;
    public long maxAge;
    public String path;
    public String portList;
    public boolean secure;
    public int version;
    public String url;

    public static List<TransportableHttpCookie> from(URL url, List<HttpCookieWithId> list) {
        List<TransportableHttpCookie> result = new ArrayList<>(list.size());
        for (HttpCookieWithId hcwi : list) {
            HttpCookie cookie = hcwi.httpCookie;
            TransportableHttpCookie thc = new TransportableHttpCookie();
            thc.name = cookie.getName();
            thc.value = cookie.getValue();
            thc.comment = cookie.getComment();
            thc.commentURL = cookie.getCommentURL();
            thc.discard = cookie.getDiscard();
            thc.domain = cookie.getDomain();
            thc.maxAge = cookie.getMaxAge();
            thc.path = cookie.getPath();
            thc.portList = cookie.getPortlist();
            thc.secure = cookie.getSecure();
            thc.version = cookie.getVersion();
            thc.url = url.toString();
            result.add(thc);
        }
        return result;
    }

    @Nullable
    public HttpCookie to() {
        if (name == null || value == null) {
            return null;
        }

        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setComment(comment);
        cookie.setCommentURL(commentURL);
        cookie.setDiscard(discard);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        cookie.setPortlist(portList);
        cookie.setSecure(secure);
        cookie.setVersion(version);

        return cookie;
    }
}
