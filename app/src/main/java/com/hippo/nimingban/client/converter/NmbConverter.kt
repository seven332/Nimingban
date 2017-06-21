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
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter

/*
 * Created by Hippo on 6/21/2017.
 */

abstract class NmbConverter<T> : Converter<ResponseBody, T> {

  override final fun convert(value: ResponseBody): T {
    val body = value.string()
    try {
      return doConvert(body)
    } catch (e: Throwable) {
      throw parseError(body) ?: e
    }
  }

  /**
   * Converts the body to value.
   */
  abstract fun doConvert(body: String): T
}

/**
 * Parses error from body.
 */
fun parseError(body: String): Throwable? {
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
