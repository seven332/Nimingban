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
import com.hippo.nimingban.util.fromHtml

/*
 * Created by Hippo on 6/12/2017.
 */

data class Forum(
    @Expose @SerializedName(ID) val _id: String?,
    @Expose @SerializedName(FGROUP) val _fgroup: String?,
    @Expose @SerializedName(SORT) val _sort: String?,
    @Expose @SerializedName(NAME) val _name: String?,
    @Expose @SerializedName(SHOW_NAME) val _showName: String?,
    @Expose @SerializedName(MSG) val _msg: String?,
    @Expose @SerializedName(INTERVAL) val _interval: String?,
    @Expose @SerializedName(CREATED_AT) val _createdAt: String?,
    @Expose @SerializedName(UPDATE_AT) val _updateAt: String?,
    @Expose @SerializedName(STATUS) val _status: String?
) : Parcelable {

  val init by lazy {
    id = _id ?: ""
    displayName = (_showName?.fromHtml() ?: "")
        .let { if (it.isNullOrBlank()) _name ?: "" else it }
        .let { if (it.isNullOrBlank()) DEFAULT_FORUM else it }
    displayMessage = (_msg?.fromHtml() ?: "")
        .let { if (it.isNullOrBlank()) DEFAULT_MESSAGE else it }
  }

  var id: String = ""
    private set
  var displayName: CharSequence = ""
    private set
  var displayMessage: CharSequence = ""
    private set

  /** Whether the forum is from api **/
  var official = false

  /** Only useful to save it to db **/
  var weight = 0

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(_id)
    dest.writeString(_fgroup)
    dest.writeString(_sort)
    dest.writeString(_name)
    dest.writeString(_showName)
    dest.writeString(_msg)
    dest.writeString(_interval)
    dest.writeString(_createdAt)
    dest.writeString(_updateAt)
    dest.writeString(_status)
    dest.writeInt(if (official) 1 else 0)
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
      source.readString()) {
    official = source.readInt() != 0
  }

  companion object {

    const val ID = "id"
    const val FGROUP = "fgroup"
    const val SORT = "sort"
    const val NAME = "name"
    const val SHOW_NAME = "showName"
    const val MSG = "msg"
    const val INTERVAL = "interval"
    const val CREATED_AT = "createdAt"
    const val UPDATE_AT = "updateAt"
    const val STATUS = "status"

    private const val DEFAULT_FORUM = "板块丁"
    private const val DEFAULT_MESSAGE = "略"

    @JvmField val CREATOR: Parcelable.Creator<Forum> = object : Parcelable.Creator<Forum> {
      override fun createFromParcel(source: Parcel) = Forum(source).also { it.init }
      override fun newArray(size: Int) = arrayOfNulls<Forum?>(size)
    }
  }
}
