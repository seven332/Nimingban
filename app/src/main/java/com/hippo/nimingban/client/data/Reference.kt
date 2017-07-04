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

package com.hippo.nimingban.client.data

/*
 * Created by Hippo on 2017/7/4.
 */

data class Reference(
    val id: String,
    val date: Long,
    val user: String?,
    val title: String?,
    val name: String?,
    val email: String?,
    val content: String?,
    val image: String?,
    val sage: Boolean,
    val admin: Boolean,
    val threadId: String?
) {

  fun toReply() = Reply(
      id = id,
      date = date,
      user = user,
      title = title,
      name = name,
      email = email,
      content = content,
      image = image,
      sage = sage,
      admin = admin)
}
