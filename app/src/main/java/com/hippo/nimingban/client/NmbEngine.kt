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
import com.hippo.nimingban.client.data.Thread
import io.reactivex.Single
import retrofit2.http.GET
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
  fun replies(
      @Url url: String
  ): Single<Thread>
}
