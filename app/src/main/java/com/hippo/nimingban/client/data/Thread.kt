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
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.hippo.nimingban.client.NO_NAME
import com.hippo.nimingban.client.NO_TITLE
import com.hippo.nimingban.client.toNmbContent
import com.hippo.nimingban.client.fromNmbDate
import com.hippo.nimingban.client.toNmbUser
import com.hippo.nimingban.exception.GsonException
import com.hippo.nimingban.util.element
import com.hippo.nimingban.util.int
import com.hippo.nimingban.util.stringNotEmpty
import java.lang.reflect.Type

/*
 * Created by Hippo on 6/4/2017.
 */

class ThreadApiGson : JsonDeserializer<Thread> {

  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): Thread {
    val jo = json.asJsonObject

    return Thread(
        id = jo.stringNotEmpty("id") ?: throw GsonException("Invalid thread id"),
        date = jo.stringNotEmpty("now").fromNmbDate(),
        user = jo.stringNotEmpty("userid"),
        title = jo.stringNotEmpty("title")?.let { if (it == NO_TITLE) null else it },
        name = jo.stringNotEmpty("name")?.let { if (it == NO_NAME) null else it },
        email = jo.stringNotEmpty("email"),
        content = jo.stringNotEmpty("content"),
        image = run {
          val img = jo.stringNotEmpty("img")
          val ext = jo.stringNotEmpty("ext")
          if (img != null && ext != null) img + ext else null
        },
        sage = jo.int("sage", 0) == 1,
        admin = jo.int("admin", 0) == 1,
        replyCount = jo.int("replyCount", 0),
        replies = jo.element("replys")?.let {
          context.deserialize<List<Reply>>(it, object : TypeToken<List<Reply>>(){}.type)
        } ?: emptyList()
    )
  }
}


data class Thread(
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
    val replyCount: Int,
    val replies: List<Reply>
) : Parcelable {

  var forum: String? = null

  val displayId = "No." + id
  val displayUser = user.toNmbUser(admin)
  val displayContent = content.toNmbContent(sage, title, name, email)

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
      parcel.readInt(),
      parcel.createTypedArrayList(Reply)) {
    forum = parcel.readString()
  }

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
    parcel.writeInt(replyCount)
    parcel.writeTypedList(replies)
    parcel.writeString(forum)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<Thread> {
    override fun createFromParcel(parcel: Parcel): Thread {
      return Thread(parcel)
    }

    override fun newArray(size: Int): Array<Thread?> {
      return arrayOfNulls(size)
    }
  }
}
