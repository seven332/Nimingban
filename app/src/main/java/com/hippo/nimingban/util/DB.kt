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

import android.database.Cursor
import com.pushtorefresh.storio.sqlite.StorIOSQLite

/*
 * Created by Hippo on 6/17/2017.
 */

/**
 * Gets boolean value for special column from the {@code cursor}.
 */
fun Cursor.getBoolean(column: String, defValue: Boolean): Boolean {
  try {
    val index = getColumnIndex(column)
    if (index != -1) {
      return getInt(index) != 0
    }
  } catch (e: Throwable) {}
  return defValue
}

/**
 * Gets int value for special column from the {@code cursor}.
 */
fun Cursor.getInt(column: String, defValue: Int): Int {
  try {
    val index = getColumnIndex(column)
    if (index != -1) {
      return getInt(index)
    }
  } catch (e: Throwable) {}
  return defValue
}

/**
 * Gets long value for special column from the {@code cursor}.
 */
fun Cursor.getLong(column: String, defValue: Long): Long {
  try {
    val index = getColumnIndex(column)
    if (index != -1) {
      return getLong(index)
    }
  } catch (e: Throwable) {}
  return defValue
}

/**
 * Gets float value for special column from the {@code cursor}.
 */
fun Cursor.getFloat(column: String, defValue: Float): Float {
  try {
    val index = getColumnIndex(column)
    if (index != -1) {
      return getFloat(index)
    }
  } catch (e: Throwable) {}
  return defValue
}

/**
 * Gets string value for special column from the {@code cursor}.
 */
fun Cursor.getString(column: String, defValue: String?): String? {
  try {
    val index = getColumnIndex(column)
    if (index != -1) {
      return getString(index)
    }
  } catch (e: Throwable) {}
  return defValue
}

/**
 * Executes the given [block] function in transaction.
 */
inline fun <R> StorIOSQLite.transaction(block: (StorIOSQLite) -> R): R {
  lowLevel().beginTransaction()
  try {
    return block(this).also { lowLevel().setTransactionSuccessful() }
  } finally {
    lowLevel().endTransaction()
  }
}
