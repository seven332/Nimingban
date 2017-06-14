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
    @Expose @SerializedName("id") private val _id: String?,
    @Expose @SerializedName("fgroup") private val _fgroup: String?,
    @Expose @SerializedName("sort") private val _sort: String?,
    @Expose @SerializedName("name") private val _name: String?,
    @Expose @SerializedName("showName") private val _showName: String?,
    @Expose @SerializedName("msg") private val _msg: String?,
    @Expose @SerializedName("interval") private val _interval: String?,
    @Expose @SerializedName("createdAt") private val _createdAt: String?,
    @Expose @SerializedName("updateAt") private val _updateAt: String?,
    @Expose @SerializedName("status") private val _status: String?
) : Parcelable {

  val init by lazy {
    id = _id
    displayName = _showName?.fromHtml() ?: _name ?: DEFAULT_FORUM
    displayMessage = _msg?.fromHtml() ?: DEFAULT_MESSAGE
  }

  var id: String? = null
    private set
  var displayName: CharSequence = ""
    private set
  var displayMessage: CharSequence = ""
    private set


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
      source.readString())

  companion object {

    private const val DEFAULT_FORUM = "板块丁"
    private const val DEFAULT_MESSAGE = "略"

    @JvmField val CREATOR: Parcelable.Creator<Forum> = object : Parcelable.Creator<Forum> {
      override fun createFromParcel(source: Parcel): Forum {
        val forum = Forum(source)
        forum.init
        return forum
      }

      override fun newArray(size: Int): Array<Forum?> {
        return arrayOfNulls(size)
      }
    }
  }
}
