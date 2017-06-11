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

/*
 * Created by Hippo on 6/4/2017.
 */

internal interface ReplyInterface {
  val id: String?
  val image: String?
  val date: Long
  val user: String?
  val name: String?
  val email: String?
  val title: String?
  val content: String?
  val sage: Boolean
  val admin: Boolean

  val displayId: CharSequence
  val displayUser: CharSequence
  val displayContent: CharSequence
}


internal open class ReplyImpl(
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
    _admin: String?
) : ReplyInterface {
  final override val id = _id
  final override val image = if (_img.isNullOrEmpty().not() && _ext.isNullOrEmpty().not()) _img + _ext else null
  final override val date = _now.toNmbDate()
  final override val user = _user
  final override val name = _name
  final override val email = _email
  final override val title = _title
  final override val content = _content
  final override val sage = _sage == "1"
  final override val admin = _admin == "1"

  final override val displayId = "No." + (_id ?: "0")
  final override val displayUser = _user.toNmbUser(admin)
  final override val displayContent = _content.toNmbContent(sage, _title, _name, _email)
}


data class Reply(
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
    @Expose @SerializedName("admin") val _admin: String?
) : ReplyInterface, Parcelable {
  private val actuality by lazy { ReplyImpl(_id, _img, _ext, _now, _user, _name, _email, _title, _content, _sage, _admin) }

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

  override val displayId get() = actuality.displayId
  override val displayUser get() = actuality.displayUser
  override val displayContent get() = actuality.displayContent


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
      source.readString())

  companion object {
    @JvmField val CREATOR: Parcelable.Creator<Reply> = object : Parcelable.Creator<Reply> {
      override fun createFromParcel(source: Parcel): Reply {
        return Reply(source)
      }

      override fun newArray(size: Int): Array<Reply?> {
        return arrayOfNulls(size)
      }
    }
  }
}
