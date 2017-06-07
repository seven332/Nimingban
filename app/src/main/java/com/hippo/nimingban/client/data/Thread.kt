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

import com.google.gson.annotations.SerializedName
import com.hippo.nimingban.client.toNmbDate

/*
 * Created by Hippo on 6/4/2017.
 */

// Thread should extend Reply, but data class can't be open
data class Thread(
    @SerializedName("id")
    val _id: String?,
    @SerializedName("img")
    val _img: String?,
    @SerializedName("ext")
    val _ext: String?,
    @SerializedName("now")
    val _now: String?,
    @SerializedName("userid")
    val _user: String?,
    @SerializedName("name")
    val _name: String?,
    @SerializedName("email")
    val _email: String?,
    @SerializedName("title")
    val _title: String?,
    @SerializedName("content")
    val _content: String?,
    @SerializedName("sage")
    val _sage: String?,
    @SerializedName("admin")
    val _admin: String?,
    @SerializedName("replyCount")
    val _replyCount: String?,
    @SerializedName("replys")
    val _replies: List<Reply>?
) {
  val id by lazy { _id }
  // TODO get image url prefix
  val thumb by lazy { if (_img.isNullOrEmpty().not() && _ext.isNullOrEmpty().not()) _img + _ext else null }
  // TODO get image url prefix
  val image by lazy { if (_img.isNullOrEmpty().not() && _ext.isNullOrEmpty().not()) _img + _ext else null }
  val date by lazy { _now.toNmbDate() }
  val user by lazy { _user }
  val name by lazy { _name }
  val email by lazy { _email }
  val title by lazy { _title }
  val content by lazy { _content }
  val sage by lazy { _sage == "1" }
  val admin by lazy { _admin == "1" }
  val replyCount by lazy { _replyCount?.toInt() ?: 0 }
  val replies by lazy { _replies ?: emptyList() }
}
