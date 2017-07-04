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

package com.hippo.nimingban.client.converter

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.hippo.nimingban.GSON
import com.hippo.nimingban.R
import com.hippo.nimingban.exception.GeneralException
import com.hippo.nimingban.exception.PresetException
import org.jsoup.Jsoup

/*
 * Created by Hippo on 2017/7/4.
 */

/**
 * Get the last int in a string.
 */
internal fun String?.lastInt(defValue: Int): Int {
  if (this == null) return defValue

  var result = 0
  var base = 1
  var inInt = false
  for (i in this.length - 1 downTo 0) {
    val ch = this[i]
    val isInt = ch in '0' .. '9'

    if (isInt) {
      inInt = true
      result += (ch - '0') * base
      base *= 10
    } else if (inInt) {
      return result
    }
  }

  return if (inInt) result else defValue
}

/**
 * Parses error from body.
 */
internal fun parseError(body: String): Throwable? {
  // Check body empty
  if (body.isNullOrBlank()) {
    return PresetException("Empty Body", R.string.error_empty_body, R.drawable.emoticon_sad_primary_x64)
  }

  // Check json format
  try {
    val jo = GSON.fromJson(body, JsonObject::class.java)
    if (!jo.get("success").asBoolean) {
      return GeneralException(jo.get("msg").asString)
    }
  } catch (e: JsonSyntaxException) { /* Ignore */ }

  // Sometimes the server may return a string in json format
  if (body.length >= 2 && body.startsWith('\'') && body.endsWith('\'')) {
    try {
      return GeneralException(GSON.fromJson(body, String::class.java))
    } catch (e: JsonSyntaxException) { /* Ignore */ }
  }

  // Check plain text
  if (!body.contains("<") && !body.contains("{") && !body.contains("[") && body.length <= 1024) {
    return GeneralException(body)
  }

  // Check human readable error
  val document = Jsoup.parse(body)
  val dom = document.getElementsByClass("error").first()
  if (dom != null) {
    return GeneralException(dom.text())
  }

  return null
}
