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
import com.hippo.nimingban.client.toNmbDate
import com.hippo.nimingban.util.fromHtml

/*
 * Created by Hippo on 6/4/2017.
 */

// Thread should extend Reply, but data class can't be open
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
) {
  val id by lazy { _id }
  val image by lazy { if (_img.isNullOrEmpty().not() && _ext.isNullOrEmpty().not()) _img + _ext else null }
  val date by lazy { _now.toNmbDate() }
  val user by lazy { _user }
  val name by lazy { _name }
  val email by lazy { _email }
  val title by lazy { _title }
  val content by lazy { _content?.fromHtml() }
  val sage by lazy { _sage == "1" }
  val admin by lazy { _admin == "1" }
  val replyCount by lazy { _replyCount?.toInt() ?: 0 }
  val replies by lazy { _replies ?: emptyList() }
}
