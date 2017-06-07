/*
 * Copyright 2017 Hippo Seven
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

package com.hippo.nimingban.client

/*
 * Created by Hippo on 1/18/2017.
 */

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

private val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"

private val USER_AGENT_NMB = "havfun-nimingban"

class NmbInterceptor : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val requestBuilder = request.newBuilder()
    val userAgent = if (isNmbUrl(request.url())) USER_AGENT_NMB else USER_AGENT
    requestBuilder.header("User-Agent", userAgent)
    return chain.proceed(requestBuilder.build())
  }
}
