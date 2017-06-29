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

/*
 * Created by Hippo on 6/16/2017.
 */

/**
 * Returns itself if it is `MutableList`, or convert it to a `MutableList`.
 */
fun <T> List<T>.asMutableList(): MutableList<T> = this as? MutableList<T> ?: this.toMutableList()

/**
 * Removes and returns the first element matching the given [predicate].
 */
inline fun <T> MutableIterable<T>.removeFirst(predicate: (T) -> Boolean): T? {
  val each = iterator()
  while (each.hasNext()) {
    val next = each.next()
    if (predicate(next)) {
      each.remove()
      return next
    }
  }
  return null
}

/**
 * Removes all of the elements of this collection that satisfy the given [predicate].
 * Returns `true` if any elements were removed.
 */
inline fun <T> MutableIterable<T>.filterNot(predicate: (T) -> Boolean): Boolean {
  var removed = false
  val each = iterator()
  while (each.hasNext()) {
    if (predicate(each.next())) {
      each.remove()
      removed = true
    }
  }
  return removed
}

/**
 * Performs the given [action] on each entry. Remove the entry if return `true`.
 */
inline fun <K, V> MutableMap<K, V>.foreachRemoved(action: (K, V) -> Boolean ) {
  val iterator = entries.iterator()
  while (iterator.hasNext()) {
    val entry = iterator.next()
    if (action(entry.key, entry.value)) {
      iterator.remove()
    }
  }
}
