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
) : Parcelable {

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

  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readLong(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readByte() != 0.toByte(),
      parcel.readByte() != 0.toByte(),
      parcel.readString())

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(id)
    parcel.writeLong(date)
    parcel.writeString(user)
    parcel.writeString(title)
    parcel.writeString(name)
    parcel.writeString(email)
    parcel.writeString(content)
    parcel.writeString(image)
    parcel.writeByte(if (sage) 1 else 0)
    parcel.writeByte(if (admin) 1 else 0)
    parcel.writeString(threadId)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<Reference> {
    override fun createFromParcel(parcel: Parcel): Reference {
      return Reference(parcel)
    }

    override fun newArray(size: Int): Array<Reference?> {
      return arrayOfNulls(size)
    }
  }
}
