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

import okhttp3.Cookie
import okhttp3.HttpUrl
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/*
 * Created by Hippo on 6/29/2017.
 */

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class CookieRepositoryTest {

  internal fun equals(cookieSet: CookieSet, cookies: List<Cookie>) {
    assertNotNull(cookieSet)
    assertNotNull(cookies)

    val map = cookieSet.map
    assertEquals(cookies.size.toLong(), map.size.toLong())
    for (cookie in cookies) {
      assertEquals(cookie, map[CookieSet.Key(cookie)])
    }
  }

  fun equals(cookies1: List<Cookie>, cookies2: List<Cookie>) {
    assertNotNull(cookies1)
    assertNotNull(cookies2)

    assertEquals(cookies1.size.toLong(), cookies2.size.toLong())
    for (cookie in cookies1) {
      assertTrue(cookies2.contains(cookie))
    }
  }

  @Test
  fun testPersistent() {
    val app = RuntimeEnvironment.application

    val urlEh = HttpUrl.parse("http://www.ehviewer.com/")!!
    val cookieEh1 = Cookie.Builder()
        .name("user")
        .value("1234567890")
        .domain("ehviewer.com")
        .path("/")
        .build()
    val cookieEh2 = Cookie.Builder()
        .name("level")
        .value("999")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 100000)
        .build()
    val cookieEh3 = Cookie.Builder()
        .name("speed")
        .value("10")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 100000)
        .build()

    val urlNMB = HttpUrl.parse("http://h.nimingban.com/")!!
    val cookieNMB = Cookie.Builder()
        .name("hash")
        .value("0987654321")
        .domain("nimingban.com")
        .expiresAt(System.currentTimeMillis() + 100000)
        .path("/")
        .build()

    var repository = CookieRepository(app)
    repository.saveFromResponse(urlEh, listOf(cookieEh1, cookieEh2, cookieEh3))
    repository.saveFromResponse(urlNMB, listOf(cookieNMB))
    var map = repository.map
    assertEquals(3, map.size)
    equals(map["ehviewer.com"]!!, listOf(cookieEh1))
    equals(map["www.ehviewer.com"]!!, listOf(cookieEh2, cookieEh3))
    equals(map["nimingban.com"]!!, listOf(cookieNMB))
    repository.close()

    repository = CookieRepository(app)
    map = repository.map
    assertEquals(2, map.size)
    equals(map["www.ehviewer.com"]!!, listOf(cookieEh2, cookieEh3))
    equals(map["nimingban.com"]!!, listOf(cookieNMB))
    repository.close()
  }

  @Test
  fun testUpdate() {
    val app = RuntimeEnvironment.application

    val urlEh = HttpUrl.parse("http://www.ehviewer.com/")!!
    val cookieEh1 = Cookie.Builder()
        .name("level")
        .value("999")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 100000)
        .build()
    val cookieEh2 = Cookie.Builder()
        .name("level")
        .value("0")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 100000)
        .build()

    var repository = CookieRepository(app)
    repository.saveFromResponse(urlEh, listOf(cookieEh1))
    repository.saveFromResponse(urlEh, listOf(cookieEh2))
    var map = repository.map
    assertEquals(1, map.size)
    equals(map["www.ehviewer.com"]!!, listOf(cookieEh2))
    repository.close()


    repository = CookieRepository(app)
    map = repository.map
    assertEquals(1, map.size)
    equals(map["www.ehviewer.com"]!!, listOf(cookieEh2))
    repository.close()
  }

  @Test
  fun testRemoveByExpired() {
    val app = RuntimeEnvironment.application

    val urlEh = HttpUrl.parse("http://www.ehviewer.com/")!!
    val cookieEh1 = Cookie.Builder()
        .name("level")
        .value("999")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 100000)
        .build()
    val cookieEh2 = Cookie.Builder()
        .name("level")
        .value("0")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() - 100000)
        .build()

    var repository = CookieRepository(app)
    repository.saveFromResponse(urlEh, listOf(cookieEh1))
    repository.saveFromResponse(urlEh, listOf(cookieEh2))
    var map = repository.map
    assertEquals(1, map.size)
    equals(map["www.ehviewer.com"]!!, emptyList<Cookie>())
    repository.close()


    repository = CookieRepository(app)
    map = repository.map
    assertEquals(0, map.size)
    repository.close()
  }

  @Test
  fun testRemoveByNonPersistent() {
    val app = RuntimeEnvironment.application

    val urlEh = HttpUrl.parse("http://www.ehviewer.com/")!!
    val cookieEh1 = Cookie.Builder()
        .name("level")
        .value("999")
        .domain("www.ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 100000)
        .build()
    val cookieEh2 = Cookie.Builder()
        .name("level")
        .value("0")
        .domain("www.ehviewer.com")
        .path("/")
        .build()

    var repository = CookieRepository(app)
    repository.saveFromResponse(urlEh, listOf(cookieEh1))
    repository.saveFromResponse(urlEh, listOf(cookieEh2))
    var map = repository.map
    assertEquals(1, map.size)
    equals(map["www.ehviewer.com"]!!, listOf(cookieEh2))
    repository.close()


    repository = CookieRepository(app)
    map = repository.map
    assertEquals(0, map.size)
    repository.close()
  }

  @Test
  fun testGet() {
    val app = RuntimeEnvironment.application

    val urlEh1 = HttpUrl.parse("http://www.ehviewer.com/")!!
    val urlEh2 = HttpUrl.parse("http://ehviewer.com/")!!
    val cookieEh1 = Cookie.Builder()
        .name("user")
        .value("1234567890")
        .domain("ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 3000)
        .build()
    val cookieEh2 = Cookie.Builder()
        .name("level")
        .value("999")
        .domain("www.ehviewer.com")
        .path("/")
        .build()
    val cookieEh3 = Cookie.Builder()
        .name("speed")
        .value("10")
        .domain("ehviewer.com")
        .path("/")
        .build()

    val urlNMB = HttpUrl.parse("http://h.nimingban.com/")!!
    val cookieNMB = Cookie.Builder()
        .name("hash")
        .value("0987654321")
        .domain("nimingban.com")
        .path("/")
        .build()

    val repository = CookieRepository(app)
    repository.saveFromResponse(urlEh1, listOf(cookieEh1, cookieEh2))
    repository.saveFromResponse(urlEh1, listOf(cookieEh3))
    repository.saveFromResponse(urlNMB, listOf(cookieNMB))
    equals(listOf(cookieEh1, cookieEh3), repository.loadForRequest(urlEh2))
    Thread.sleep(3000)
    equals(listOf(cookieEh3), repository.loadForRequest(urlEh2))
    repository.close()
  }

  @Test
  fun testClear() {
    val app = RuntimeEnvironment.application

    val url = HttpUrl.parse("http://www.ehviewer.com/")!!
    val cookie = Cookie.Builder()
        .name("user")
        .value("1234567890")
        .domain("ehviewer.com")
        .path("/")
        .expiresAt(System.currentTimeMillis() + 3000)
        .build()

    var repository = CookieRepository(app)
    repository.saveFromResponse(url, listOf(cookie))
    var map = repository.map
    assertEquals(1, map.size)
    equals(map["ehviewer.com"]!!, listOf(cookie))
    repository.clear()
    map = repository.map
    assertEquals(0, map.size)
    repository.close()

    repository = CookieRepository(app)
    map = repository.map
    assertEquals(0, map.size)
    repository.close()
  }

  @Test
  fun testPublicSuffixCookie() {
    val app = RuntimeEnvironment.application

    val url1 = HttpUrl.parse("http://www.ehviewer.com/")!!
    val url2 = HttpUrl.parse("http://com/")!!
    val cookie1 = Cookie.Builder()
        .name("user")
        .value("1234567890")
        .domain("com")
        .path("/")
        .build()
    val cookie2 = Cookie.Builder()
        .name("user")
        .value("1234567890")
        .hostOnlyDomain("com")
        .path("/")
        .build()

    val repository = CookieRepository(app)
    repository.saveFromResponse(url1, listOf(cookie1))
    val map = repository.map
    assertEquals(0, map.size)
    repository.saveFromResponse(url2, listOf(cookie1))
    assertEquals(1, map.size)
    equals(map["com"]!!, listOf(cookie2))
    repository.close()
  }

  @Test
  fun testSort() {
    val app = RuntimeEnvironment.application

    val url = HttpUrl.parse("http://www.ehviewer.com/long/long/long/")!!
    val cookie1 = Cookie.Builder()
        .name("user")
        .value("1234567890")
        .domain("ehviewer.com")
        .path("/")
        .build()
    val cookie2 = Cookie.Builder()
        .name("supersuperme")
        .value("99999")
        .domain("ehviewer.com")
        .path("/long/")
        .build()
    val cookie3 = Cookie.Builder()
        .name("a")
        .value("b")
        .domain("ehviewer.com")
        .path("/long/long/long/")
        .build()
    val cookie4 = Cookie.Builder()
        .name("speed")
        .value("100")
        .domain("ehviewer.com")
        .path("/long/long/")
        .build()

    val repository = CookieRepository(app)
    repository.saveFromResponse(url, listOf(cookie1, cookie2, cookie3, cookie4))
    val list = repository.loadForRequest(url)
    val expected = listOf(cookie3, cookie4, cookie2, cookie1)
    assertEquals(expected.size, list.size)
    for (i in list.indices) {
      assertEquals(expected[i], list[i])
    }
    repository.close()
  }
}
