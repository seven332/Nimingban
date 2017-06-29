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

package com.hippo.nimingban.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.Collections

/*
 * Created by Hippo on 6/29/2017.
 */
/**
 * A Persistent `CookieJar` which store cookies to database.
 */
class CookieRepository(context: Context) : CookieJar {

  companion object {
    private const val DB_NAME = "nmb_cookie2"
  }

  private val db = CookieDatabase(context, DB_NAME)
  internal val map = db.getAllCookies()

  @Synchronized private fun addCookie(cookie: Cookie) {
    // For cookie database
    var toAdd: Cookie? = null
    var toUpdate: Cookie? = null
    var toRemove: Cookie? = null

    var set: CookieSet? = map[cookie.domain()]
    if (set == null) {
      set = CookieSet()
      map.put(cookie.domain(), set)
    }

    if (cookie.expiresAt() <= System.currentTimeMillis()) {
      toRemove = set.remove(cookie)
      // If the cookie is not persistent, it's not in database
      if (toRemove != null && !toRemove.persistent()) {
        toRemove = null
      }
    } else {
      toAdd = cookie
      toUpdate = set.add(cookie)
      // If the cookie is not persistent, it's not in database
      if (!toAdd.persistent()) toAdd = null
      if (toUpdate != null && !toUpdate.persistent()) toUpdate = null
      // Remove the cookie if it updates to null
      if (toAdd == null && toUpdate != null) {
        toRemove = toUpdate
        toUpdate = null
      }
    }

    if (toRemove != null) {
      db.remove(toRemove)
    }
    if (toAdd != null) {
      if (toUpdate != null) {
        db.update(toUpdate, toAdd)
      } else {
        db.add(toAdd)
      }
    }
  }

  @Synchronized private fun getCookies(url: HttpUrl): List<Cookie> {
    val accepted = mutableListOf<Cookie>()
    val expired = mutableListOf<Cookie>()

    for ((key, value) in map) {
      if (domainMatch(url, key)) {
        value.get(url, accepted, expired)
      }
    }

    expired.forEach {
      if (it.persistent()) {
        db.remove(it)
      }
    }

    // RFC 6265 Section-5.4 step 2, sort the cookie-list
    // Cookies with longer paths are listed before cookies with shorter paths.
    // Ignore creation-time, we don't store them.
    Collections.sort(accepted) { o1, o2 -> o2.path().length - o1.path().length }

    return accepted
  }

  @Synchronized fun clear() {
    map.clear()
    db.clear()
  }

  @Synchronized fun close() {
    db.close()
  }

  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
    for (cookie in cookies) {
      if (isPublicSuffix(cookie.domain())) {
        // RFC 6265 Section-5.3, step 5 and step 6
        // If the domain of the cookie is a public suffix
        // and is identical to the canonicalized request-host,
        // set the cookie's host-only-flag to true,
        // otherwise ignore the cookie entirely.
        if (cookie.domain() == url.host()) {
          if (!cookie.hostOnly()) {
            val builder = Cookie.Builder()
            builder.name(cookie.name())
            builder.value(cookie.value())
            builder.hostOnlyDomain(cookie.domain())
            builder.path(cookie.path())
            if (cookie.persistent()) builder.expiresAt(cookie.expiresAt())
            if (cookie.secure()) builder.secure()
            if (cookie.httpOnly()) builder.httpOnly()
            addCookie(builder.build())
          }
        }
      } else {
        addCookie(cookie)
      }
    }
  }

  override fun loadForRequest(url: HttpUrl) = getCookies(url)
}
