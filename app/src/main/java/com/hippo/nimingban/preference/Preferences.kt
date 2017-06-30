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

package com.hippo.nimingban.preference

import android.content.SharedPreferences
import android.util.Log

/*
 * Created by Hippo on 6/29/2017.
 */

abstract class Preferences(protected val pref: SharedPreferences) {

  private val LOG_TAG = Preferences::class.java.simpleName

  protected fun setBoolean(key: String, value: Boolean) {
    pref.edit().putBoolean(key, value).apply()
  }

  protected fun getBoolean(key: String, defValue: Boolean): Boolean {
    try {
      return pref.getBoolean(key, defValue)
    } catch (e: ClassCastException) {
      Log.e(LOG_TAG, "The value of $key is not a boolean.", e)
      return defValue
    }
  }

  protected fun setInt(key: String, value: Int) {
    pref.edit().putInt(key, value).apply()
  }

  protected fun getInt(key: String, defValue: Int): Int {
    try {
      return pref.getInt(key, defValue)
    } catch (e: ClassCastException) {
      Log.e(LOG_TAG, "The value of $key is not a int.", e)
      return defValue
    }
  }

  protected fun setLong(key: String, value: Long) {
    pref.edit().putLong(key, value).apply()
  }

  protected fun getLong(key: String, defValue: Long): Long {
    try {
      return pref.getLong(key, defValue)
    } catch (e: ClassCastException) {
      Log.e(LOG_TAG, "The value of $key is not a long.", e)
      return defValue
    }
  }

  protected fun setFloat(key: String, value: Float) {
    pref.edit().putFloat(key, value).apply()
  }

  protected fun getFloat(key: String, defValue: Float): Float {
    try {
      return pref.getFloat(key, defValue)
    } catch (e: ClassCastException) {
      Log.e(LOG_TAG, "The value of $key is not a float.", e)
      return defValue
    }
  }

  protected fun setString(key: String, value: String) {
    pref.edit().putString(key, value).apply()
  }

  protected fun getString(key: String, defValue: String): String {
    try {
      return pref.getString(key, defValue)
    } catch (e: ClassCastException) {
      Log.e(LOG_TAG, "The value of $key is not a String.", e)
      return defValue
    }
  }

  protected fun setDecimalInt(key: String, value: Int) {
    pref.edit().putString(key, Integer.toString(value)).apply()
  }

  protected fun getDecimalInt(key: String, defValue: Int): Int {
    try {
      try {
        return pref.getString(key, Integer.toString(defValue)).toInt()
      } catch (e: NumberFormatException) {
        Log.e(LOG_TAG, "The value of $key is not a decimal int.", e)
        return defValue
      }
    } catch (e: ClassCastException) {
      Log.e(LOG_TAG, "The value of $key is not a String.", e)
      return defValue
    }
  }

  protected fun edit(): SharedPreferences.Editor {
    return pref.edit()
  }
}
