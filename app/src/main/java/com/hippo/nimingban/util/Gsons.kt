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
import com.hippo.nimingban.exception.GsonException

/*
 * Created by Hippo on 7/1/2017.
 */

private fun error(key: String): Nothing = throw GsonException("Invalid value: $key")

fun JsonObject.element(key: String): JsonElement {
  try {
    return get(key) ?: error(key)
  } catch (e: Throwable) { error(key) }
}

fun JsonObject.int(key: String): Int {
  try {
    return get(key).asInt
  } catch (e: Throwable) { error(key) }
}

fun JsonObject.string(key: String): String {
  try {
    return get(key).asString ?: error(key)
  } catch (e: Throwable) { error(key) }
}

fun JsonObject.stringOrNull(key: String): String? {
  try {
    return get(key).asString
  } catch (e: Throwable) { error(key) }
}

fun JsonObject.stringNotBlank(key: String): String {
  try {
    return get(key).asString.also { if (it.isNullOrBlank()) error(key) }
  } catch (e: Throwable) { error(key) }
}

fun JsonObject.stringNotBlankOrNull(key: String): String? {
  try {
    return get(key).asString.let { if (it.isNullOrBlank()) null else it }
  } catch (e: Throwable) { error(key) }
}
