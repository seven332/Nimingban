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
import com.hippo.nimingban.client.NmbEngine
import com.hippo.nimingban.client.toNmbMessage
import com.hippo.nimingban.client.toNmbName
import com.hippo.nimingban.client.toNmbVividName
import com.hippo.nimingban.exception.GsonException
import com.hippo.nimingban.util.stringNotEmpty
import java.lang.reflect.Type
import java.net.URLEncoder

/*
 * Created by Hippo on 6/12/2017.
 */


class ForumApiGson : JsonDeserializer<Forum> {

  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Forum {
    val jo = json.asJsonObject

    return Forum(
        id = jo.stringNotEmpty("id") ?: throw GsonException("Invalid forum id"),
        group = jo.stringNotEmpty("fgroup"),
        sort = jo.stringNotEmpty("sort"),
        name = jo.stringNotEmpty("name"),
        shownName = jo.stringNotEmpty("showName"),
        message = jo.stringNotEmpty("msg"),
        interval = jo.stringNotEmpty("interval"),
        createdAt = jo.stringNotEmpty("createdAt"),
        updateAt = jo.stringNotEmpty("updateAt"),
        status = jo.stringNotEmpty("status"))
        .also { it.official = true }
  }
}


data class Forum(
    val id: String,
    val group: String?,
    val sort: String?,
    val name: String?,
    val shownName: String?,
    val message: String?,
    val interval: String?,
    val createdAt: String?,
    val updateAt: String?,
    val status: String?
) : Parcelable {

  val displayedName = name.toNmbName()
  val displayedVividName = name.toNmbVividName(shownName)
  val displayedMessage = message.toNmbMessage()

  /**
   * Used for [NmbEngine.threadsHtml].
   */
  val htmlName = run {
    try {
      URLEncoder.encode(name, "UTF-8")!!
    } catch (e: Throwable) { throw GsonException("Can't encode forum name") }
  }

  /**
   * Whether the forum is from api.
   */
  var official = false

  /**
   * Whether the forum is visible for user.
   */
  var visible = true

  /**
   * Only useful to save it to db.
   */
  var weight = 0

  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString()) {
    official = parcel.readByte() != 0.toByte()
    visible = parcel.readByte() != 0.toByte()
    weight = parcel.readInt()
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(id)
    parcel.writeString(group)
    parcel.writeString(sort)
    parcel.writeString(name)
    parcel.writeString(shownName)
    parcel.writeString(message)
    parcel.writeString(interval)
    parcel.writeString(createdAt)
    parcel.writeString(updateAt)
    parcel.writeString(status)
    parcel.writeByte(if (official) 1 else 0)
    parcel.writeByte(if (visible) 1 else 0)
    parcel.writeInt(weight)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<Forum> {
    override fun createFromParcel(parcel: Parcel): Forum {
      return Forum(parcel)
    }

    override fun newArray(size: Int): Array<Forum?> {
      return arrayOfNulls(size)
    }
  }
}
