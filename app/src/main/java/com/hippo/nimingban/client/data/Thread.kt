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

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hippo.nimingban.client.toNmbContent
import com.hippo.nimingban.client.toNmbDate
import com.hippo.nimingban.client.toNmbUser
import com.hippo.nimingban.util.readTypedList

/*
 * Created by Hippo on 6/4/2017.
 */

// Thread should extend Reply, but data class can't be open
// I hope a property can be class delegation, but it can't
data class Thread(
    @Expose @SerializedName("id") private val _id: String?,
    @Expose @SerializedName("img") private val _img: String?,
    @Expose @SerializedName("ext") private val _ext: String?,
    @Expose @SerializedName("now") private val _now: String?,
    @Expose @SerializedName("userid") private val _user: String?,
    @Expose @SerializedName("name") private val _name: String?,
    @Expose @SerializedName("email") private val _email: String?,
    @Expose @SerializedName("title") private val _title: String?,
    @Expose @SerializedName("content") private val _content: String?,
    @Expose @SerializedName("sage") private val _sage: String?,
    @Expose @SerializedName("admin") private val _admin: String?,
    @Expose @SerializedName("replyCount") private val _replyCount: String?,
    @Expose @SerializedName("replys") private val _replies: List<Reply>?
) : Parcelable {

  val init by lazy {
    id = _id
    image = if (_img.isNullOrEmpty().not() && _ext.isNullOrEmpty().not()) _img + _ext else null
    date = _now.toNmbDate()
    user = _user
    name = _name
    email = _email
    title = _title
    content = _content
    sage = _sage == "1"
    admin = _admin == "1"
    replyCount = _replyCount?.toInt() ?: 0
    replies = _replies ?: emptyList()
    replies.forEach { it.init }

    displayId = "No." + (_id ?: "0")
    displayUser = _user.toNmbUser(admin)
    displayContent = _content.toNmbContent(sage, _title, _name, _email)
  }

  var id: String? = null
    private set
  var image: String? = null
    private set
  var date: Long = 0
    private set
  var user: String? = null
    private set
  var name: String? = null
    private set
  var email: String? = null
    private set
  var title: String? = null
    private set
  var content: String? = null
    private set
  var sage: Boolean = false
    private set
  var admin: Boolean = false
    private set
  var replyCount: Int = 0
    private set
  var replies: List<Reply> = emptyList()
    private set

  var displayId: CharSequence = ""
    private set
  var displayUser: CharSequence = ""
    private set
  var displayContent: CharSequence = ""
    private set

  fun toReply(): Reply = Reply(_id, _img, _ext, _now, _user, _name, _email, _title, _content, _sage, _admin).apply { init }

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(_id)
    dest.writeString(_img)
    dest.writeString(_ext)
    dest.writeString(_now)
    dest.writeString(_user)
    dest.writeString(_name)
    dest.writeString(_email)
    dest.writeString(_title)
    dest.writeString(_content)
    dest.writeString(_sage)
    dest.writeString(_admin)
    dest.writeString(_replyCount)
    dest.writeTypedList(_replies)
  }

  constructor(source: Parcel) : this(
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readTypedList(Reply.CREATOR))

  companion object {
    @JvmField val CREATOR: Parcelable.Creator<Thread> = object : Parcelable.Creator<Thread> {
      override fun createFromParcel(source: Parcel): Thread {
        val thread = Thread(source)
        thread.init
        return thread
      }

      override fun newArray(size: Int): Array<Thread?> {
        return arrayOfNulls(size)
      }
    }
  }
}
