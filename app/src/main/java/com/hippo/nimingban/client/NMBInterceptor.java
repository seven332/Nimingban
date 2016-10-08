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
 * Created by Hippo on 10/7/2016.
 */

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NMBInterceptor implements Interceptor {

    private static final String DEFAULT_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36";

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request userRequest = chain.request();
        final Request.Builder requestBuilder = userRequest.newBuilder();

        // TODO Add different UserAgent
        if (userRequest.header("User-Agent") == null) {
            requestBuilder.header("User-Agent", DEFAULT_UA);
        }

        return chain.proceed(requestBuilder.build());
    }
}
