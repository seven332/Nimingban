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

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/*
 * Created by Hippo on 6/4/2017.
 */

internal interface ThreadInterface : ReplyInterface {
  val replyCount: Int
  val replies: List<Reply>
}

internal class ThreadImpl(
    _id: String?,
    _img: String?,
    _ext: String?,
    _now: String?,
    _user: String?,
    _name: String?,
    _email: String?,
    _title: String?,
    _content: String?,
    _sage: String?,
    _admin: String?,
    _replyCount: String?,
    _replies: List<Reply>?
) : ReplyImpl(_id, _img, _ext, _now, _user, _name, _email, _title, _content, _sage, _admin), ThreadInterface {
  override val replyCount = _replyCount?.toInt() ?: 0
  override val replies = _replies ?: emptyList()
}

// Thread should extend Reply, but data class can't be open
// I hope a property can be class delegation, but it can't
data class Thread(
    @Expose @SerializedName("id") val _id: String?,
    @Expose @SerializedName("img") val _img: String?,
    @Expose @SerializedName("ext") val _ext: String?,
    @Expose @SerializedName("now") val _now: String?,
    @Expose @SerializedName("userid") val _user: String?,
    @Expose @SerializedName("name") val _name: String?,
    @Expose @SerializedName("email") val _email: String?,
    @Expose @SerializedName("title") val _title: String?,
    @Expose @SerializedName("content") val _content: String?,
    @Expose @SerializedName("sage") val _sage: String?,
    @Expose @SerializedName("admin") val _admin: String?,
    @Expose @SerializedName("replyCount") val _replyCount: String?,
    @Expose @SerializedName("replys") val _replies: List<Reply>?
) : ThreadInterface {
  private val actuality by lazy { ThreadImpl(_id, _img, _ext, _now, _user, _name, _email, _title, _content, _sage, _admin, _replyCount, _replies) }

  override val id get() = actuality.id
  override val image get() = actuality.image
  override val date get() = actuality.date
  override val user get() = actuality.user
  override val name get() = actuality.name
  override val email get() = actuality.email
  override val title get() = actuality.title
  override val content get() = actuality.content
  override val sage get() = actuality.sage
  override val admin get() = actuality.admin
  override val replyCount get() = actuality.replyCount
  override val replies get() = actuality.replies

  override val displayId get() = actuality.displayId
  override val displayUser get() = actuality.displayUser
  override val displayContent get() = actuality.displayContent
}
