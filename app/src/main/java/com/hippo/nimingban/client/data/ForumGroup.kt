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
import com.hippo.nimingban.exception.GsonException
import com.hippo.nimingban.util.element
import com.hippo.nimingban.util.stringNotEmpty
import java.lang.reflect.Type

/*
 * Created by Hippo on 6/12/2017.
 */

class ForumGroupApiGson : JsonDeserializer<ForumGroup> {

  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): ForumGroup {
    val jo = json.asJsonObject

    return ForumGroup(
        id = jo.stringNotEmpty("id") ?: throw GsonException("Invalid forum group id"),
        sort = jo.stringNotEmpty("sort"),
        name = jo.stringNotEmpty("name"),
        status = jo.stringNotEmpty("status"),
        forums = jo.element("forums")?.let {
          context.deserialize<List<Forum>>(it, object : TypeToken<List<Forum>>(){}.type)
        } ?: emptyList()
    )
  }
}


data class ForumGroup(
    val id: String,
    val sort: String?,
    val name: String?,
    val status: String?,
    val forums: List<Forum>
) : Parcelable {

  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.createTypedArrayList(Forum))

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(id)
    parcel.writeString(sort)
    parcel.writeString(name)
    parcel.writeString(status)
    parcel.writeTypedList(forums)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ForumGroup> {
    override fun createFromParcel(parcel: Parcel): ForumGroup {
      return ForumGroup(parcel)
    }

    override fun newArray(size: Int): Array<ForumGroup?> {
      return arrayOfNulls(size)
    }
  }
}
