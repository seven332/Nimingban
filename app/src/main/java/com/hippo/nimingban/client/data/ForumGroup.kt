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
import com.hippo.nimingban.util.readTypedList

/*
 * Created by Hippo on 6/12/2017.
 */

data class ForumGroup(
    @Expose @SerializedName("id") private val _id: String?,
    @Expose @SerializedName("sort") private val _sort: String?,
    @Expose @SerializedName("name") private val _name: String?,
    @Expose @SerializedName("status") private val _status: String?,
    @Expose @SerializedName("forums") private val _forums: List<Forum>?
) : Parcelable {

  val init by lazy {
    forums = _forums ?: emptyList()
    forums.forEach { it.init }
  }

  var forums: List<Forum> = emptyList()
    private set


  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(_id)
    dest.writeString(_sort)
    dest.writeString(_name)
    dest.writeString(_status)
    dest.writeTypedList(_forums)
  }

  constructor(source: Parcel) : this(
      source.readString(),
      source.readString(),
      source.readString(),
      source.readString(),
      source.readTypedList(Forum.CREATOR))


  companion object {
    @JvmField val CREATOR: Parcelable.Creator<ForumGroup> = object : Parcelable.Creator<ForumGroup> {
      override fun createFromParcel(source: Parcel): ForumGroup {
        val forumGroup = ForumGroup(source)
        forumGroup.init
        return forumGroup
      }

      override fun newArray(size: Int): Array<ForumGroup?> {
        return arrayOfNulls(size)
      }
    }
  }
}
