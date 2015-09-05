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

package com.hippo.nimingban.client;

import com.alibaba.fastjson.JSON;
import com.hippo.httpclient.HttpClient;
import com.hippo.httpclient.HttpRequest;
import com.hippo.httpclient.HttpResponse;
import com.hippo.nimingban.client.data.UpdateStatus;

public final class UpdateEngine {

    private static final String UPDATE_URL = "http://nimingban.herokuapp.com/update?version_code=";

    public static UpdateStatus update(HttpClient httpClient, HttpRequest httpRequest, int versionCode) throws Exception {
        try {
            String url = UPDATE_URL + versionCode;
            httpRequest.setUrl(url);
            HttpResponse response = httpClient.execute(httpRequest);
            String content = response.getString();
            return JSON.parseObject(content, UpdateStatus.class);
        } catch (Exception e) {
            if (httpRequest.isCancelled()) {
                throw new CancelledException();
            } else {
                throw e;
            }
        } finally {
            httpRequest.disconnect();
        }
    }
}
