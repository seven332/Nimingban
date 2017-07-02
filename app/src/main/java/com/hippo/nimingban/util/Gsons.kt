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

package com.hippo.nimingban.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/*
 * Created by Hippo on 7/1/2017.
 */

fun JsonObject.element(key: String): JsonElement? {
  return try {
    get(key)
  } catch (e: Throwable) {
    null
  }
}

fun JsonObject.int(key: String, defValue: Int): Int {
  return try {
    get(key).asInt
  } catch (e: Throwable) {
    defValue
  }
}

fun JsonObject.string(key: String, defValue: String?): String? {
  return try {
    get(key).asString
  } catch (e: Throwable) {
    defValue
  }
}

fun JsonObject.stringNotEmpty(key: String): String? {
  return string(key, null).let {
    if (it.isNullOrEmpty()) null else it
  }
}
