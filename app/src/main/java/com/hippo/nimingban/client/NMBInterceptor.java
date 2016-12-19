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

package com.hippo.nimingban.client;

/*
 * Created by Hippo on 11/21/2016.
 */

import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.yorozuya.Utilities;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NMBInterceptor implements Interceptor {

    private static final String USER_AGENT_DEFAULT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";
    private static final String USER_AGENT_AC = "havfun-nimingban";

    private static String getUserAgent(HttpUrl url) {
        String host = url.host();
        if (host.equals(ACUrl.DOMAIN) || Utilities.contain(ACSite.getInstance().getCdnHosts(), host)) {
            return USER_AGENT_AC;
        } else {
            return USER_AGENT_DEFAULT;
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        Request.Builder requestBuilder = userRequest.newBuilder();
        requestBuilder.header("User-Agent", getUserAgent(userRequest.url()));
        return chain.proceed(requestBuilder.build());
    }
}
