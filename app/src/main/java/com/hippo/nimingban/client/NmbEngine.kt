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

import com.hippo.nimingban.client.data.ForumGroup
import com.hippo.nimingban.client.data.RepliesHtml
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.client.data.ThreadsHtml
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/*
 * Created by Hippo on 6/4/2017.
 */

interface NmbEngine {

  @GET(NMB_API_FORUMS)
  fun forums(): Single<List<ForumGroup>>

  @GET()
  fun threads(
      @Url url: String
  ): Single<List<Thread>>

  @GET()
  fun threadsHtml(
      @Url url: String
  ): Single<ThreadsHtml>

  @GET()
  fun replies(
      @Url url: String
  ): Single<Thread>

  @GET()
  fun repliesHtml(
      @Url url: String
  ): Single<RepliesHtml>

  @FormUrlEncoded
  @POST(NMB_HTML_POST)
  fun post(
      @Field("name") name: String,
      @Field("email") email: String,
      @Field("title") title: String,
      @Field("content") content: String,
      @Field("fid") resto: String,
      @Field("water") water: String,
      @Field("image") image: RequestBody?
  ): Single<Unit>

  @FormUrlEncoded
  @POST(NMB_HTML_REPLY)
  fun reply(
      @Field("name") name: String,
      @Field("email") email: String,
      @Field("title") title: String,
      @Field("content") content: String,
      @Field("resto") resto: String,
      @Field("water") water: String,
      @Field("image") image: RequestBody?
  ): Single<Unit>
}
