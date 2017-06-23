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

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/*
 * Created by Hippo on 6/10/2017.
 */

class MathTest {

  @Test
  fun testFloorDiv() {
    assertEquals(0, floorDiv(6, 7))
    assertEquals(1, floorDiv(6, 6))
    assertEquals(1, floorDiv(6, 5))
    assertEquals(1, floorDiv(6, 4))
    assertEquals(2, floorDiv(6, 3))
    assertEquals(3, floorDiv(6, 2))
    assertEquals(6, floorDiv(6, 1))
    assertEquals(-6, floorDiv(6, -1))
    assertEquals(-3, floorDiv(6, -2))
    assertEquals(-2, floorDiv(6, -3))
    assertEquals(-2, floorDiv(6, -4))
    assertEquals(-2, floorDiv(6, -5))
    assertEquals(-1, floorDiv(6, -6))
    assertEquals(-1, floorDiv(6, -7))

    assertEquals(-1, floorDiv(-6, 7))
    assertEquals(-1, floorDiv(-6, 6))
    assertEquals(-2, floorDiv(-6, 5))
    assertEquals(-2, floorDiv(-6, 4))
    assertEquals(-2, floorDiv(-6, 3))
    assertEquals(-3, floorDiv(-6, 2))
    assertEquals(-6, floorDiv(-6, 1))
    assertEquals(6, floorDiv(-6, -1))
    assertEquals(3, floorDiv(-6, -2))
    assertEquals(2, floorDiv(-6, -3))
    assertEquals(1, floorDiv(-6, -4))
    assertEquals(1, floorDiv(-6, -5))
    assertEquals(1, floorDiv(-6, -6))
    assertEquals(0, floorDiv(-6, -7))

    assertEquals(0L, floorDiv(6L, 7L))
    assertEquals(1L, floorDiv(6L, 6L))
    assertEquals(1L, floorDiv(6L, 5L))
    assertEquals(1L, floorDiv(6L, 4L))
    assertEquals(2L, floorDiv(6L, 3L))
    assertEquals(3L, floorDiv(6L, 2L))
    assertEquals(6L, floorDiv(6L, 1L))
    assertEquals(-6L, floorDiv(6L, -1L))
    assertEquals(-3L, floorDiv(6L, -2L))
    assertEquals(-2L, floorDiv(6L, -3L))
    assertEquals(-2L, floorDiv(6L, -4L))
    assertEquals(-2L, floorDiv(6L, -5L))
    assertEquals(-1L, floorDiv(6L, -6L))
    assertEquals(-1L, floorDiv(6L, -7L))

    assertEquals(-1L, floorDiv(-6L, 7L))
    assertEquals(-1L, floorDiv(-6L, 6L))
    assertEquals(-2L, floorDiv(-6L, 5L))
    assertEquals(-2L, floorDiv(-6L, 4L))
    assertEquals(-2L, floorDiv(-6L, 3L))
    assertEquals(-3L, floorDiv(-6L, 2L))
    assertEquals(-6L, floorDiv(-6L, 1L))
    assertEquals(6L, floorDiv(-6L, -1L))
    assertEquals(3L, floorDiv(-6L, -2L))
    assertEquals(2L, floorDiv(-6L, -3L))
    assertEquals(1L, floorDiv(-6L, -4L))
    assertEquals(1L, floorDiv(-6L, -5L))
    assertEquals(1L, floorDiv(-6L, -6L))
    assertEquals(0L, floorDiv(-6L, -7L))
  }

  @Test
  fun testCeilDiv() {
    assertEquals(1, ceilDiv(6, 7))
    assertEquals(1, ceilDiv(6, 6))
    assertEquals(2, ceilDiv(6, 5))
    assertEquals(2, ceilDiv(6, 4))
    assertEquals(2, ceilDiv(6, 3))
    assertEquals(3, ceilDiv(6, 2))
    assertEquals(6, ceilDiv(6, 1))
    assertEquals(-6, ceilDiv(6, -1))
    assertEquals(-3, ceilDiv(6, -2))
    assertEquals(-2, ceilDiv(6, -3))
    assertEquals(-1, ceilDiv(6, -4))
    assertEquals(-1, ceilDiv(6, -5))
    assertEquals(-1, ceilDiv(6, -6))
    assertEquals(0, ceilDiv(6, -7))

    assertEquals(0, ceilDiv(-6, 7))
    assertEquals(-1, ceilDiv(-6, 6))
    assertEquals(-1, ceilDiv(-6, 5))
    assertEquals(-1, ceilDiv(-6, 4))
    assertEquals(-2, ceilDiv(-6, 3))
    assertEquals(-3, ceilDiv(-6, 2))
    assertEquals(-6, ceilDiv(-6, 1))
    assertEquals(6, ceilDiv(-6, -1))
    assertEquals(3, ceilDiv(-6, -2))
    assertEquals(2, ceilDiv(-6, -3))
    assertEquals(2, ceilDiv(-6, -4))
    assertEquals(2, ceilDiv(-6, -5))
    assertEquals(1, ceilDiv(-6, -6))
    assertEquals(1, ceilDiv(-6, -7))

    assertEquals(1L, ceilDiv(6L, 7L))
    assertEquals(1L, ceilDiv(6L, 6L))
    assertEquals(2L, ceilDiv(6L, 5L))
    assertEquals(2L, ceilDiv(6L, 4L))
    assertEquals(2L, ceilDiv(6L, 3L))
    assertEquals(3L, ceilDiv(6L, 2L))
    assertEquals(6L, ceilDiv(6L, 1L))
    assertEquals(-6L, ceilDiv(6L, -1L))
    assertEquals(-3L, ceilDiv(6L, -2L))
    assertEquals(-2L, ceilDiv(6L, -3L))
    assertEquals(-1L, ceilDiv(6L, -4L))
    assertEquals(-1L, ceilDiv(6L, -5L))
    assertEquals(-1L, ceilDiv(6L, -6L))
    assertEquals(0L, ceilDiv(6L, -7L))

    assertEquals(0L, ceilDiv(-6L, 7L))
    assertEquals(-1L, ceilDiv(-6L, 6L))
    assertEquals(-1L, ceilDiv(-6L, 5L))
    assertEquals(-1L, ceilDiv(-6L, 4L))
    assertEquals(-2L, ceilDiv(-6L, 3L))
    assertEquals(-3L, ceilDiv(-6L, 2L))
    assertEquals(-6L, ceilDiv(-6L, 1L))
    assertEquals(6L, ceilDiv(-6L, -1L))
    assertEquals(3L, ceilDiv(-6L, -2L))
    assertEquals(2L, ceilDiv(-6L, -3L))
    assertEquals(2L, ceilDiv(-6L, -4L))
    assertEquals(2L, ceilDiv(-6L, -5L))
    assertEquals(1L, ceilDiv(-6L, -6L))
    assertEquals(1L, ceilDiv(-6L, -7L))
  }

  @Test
  fun testFloorMod() {
    assertEquals(6, floorMod(6, 7))
    assertEquals(0, floorMod(6, 6))
    assertEquals(1, floorMod(6, 5))
    assertEquals(2, floorMod(6, 4))
    assertEquals(0, floorMod(6, 3))
    assertEquals(0, floorMod(6, 2))
    assertEquals(0, floorMod(6, 1))
    assertEquals(0, floorMod(6, -1))
    assertEquals(0, floorMod(6, -2))
    assertEquals(0, floorMod(6, -3))
    assertEquals(-2, floorMod(6, -4))
    assertEquals(-4, floorMod(6, -5))
    assertEquals(0, floorMod(6, -6))
    assertEquals(-1, floorMod(6, -7))

    assertEquals(1, floorMod(-6, 7))
    assertEquals(0, floorMod(-6, 6))
    assertEquals(4, floorMod(-6, 5))
    assertEquals(2, floorMod(-6, 4))
    assertEquals(0, floorMod(-6, 3))
    assertEquals(0, floorMod(-6, 2))
    assertEquals(0, floorMod(-6, 1))
    assertEquals(0, floorMod(-6, -1))
    assertEquals(0, floorMod(-6, -2))
    assertEquals(0, floorMod(-6, -3))
    assertEquals(-2, floorMod(-6, -4))
    assertEquals(-1, floorMod(-6, -5))
    assertEquals(0, floorMod(-6, -6))
    assertEquals(-6, floorMod(-6, -7))

    assertEquals(6L, floorMod(6L, 7L))
    assertEquals(0L, floorMod(6L, 6L))
    assertEquals(1L, floorMod(6L, 5L))
    assertEquals(2L, floorMod(6L, 4L))
    assertEquals(0L, floorMod(6L, 3L))
    assertEquals(0L, floorMod(6L, 2L))
    assertEquals(0L, floorMod(6L, 1L))
    assertEquals(0L, floorMod(6L, -1L))
    assertEquals(0L, floorMod(6L, -2L))
    assertEquals(0L, floorMod(6L, -3L))
    assertEquals(-2L, floorMod(6L, -4L))
    assertEquals(-4L, floorMod(6L, -5L))
    assertEquals(0L, floorMod(6L, -6L))
    assertEquals(-1L, floorMod(6L, -7L))

    assertEquals(1L, floorMod(-6L, 7L))
    assertEquals(0L, floorMod(-6L, 6L))
    assertEquals(4L, floorMod(-6L, 5L))
    assertEquals(2L, floorMod(-6L, 4L))
    assertEquals(0L, floorMod(-6L, 3L))
    assertEquals(0L, floorMod(-6L, 2L))
    assertEquals(0L, floorMod(-6L, 1L))
    assertEquals(0L, floorMod(-6L, -1L))
    assertEquals(0L, floorMod(-6L, -2L))
    assertEquals(0L, floorMod(-6L, -3L))
    assertEquals(-2L, floorMod(-6L, -4L))
    assertEquals(-1L, floorMod(-6L, -5L))
    assertEquals(0L, floorMod(-6L, -6L))
    assertEquals(-6L, floorMod(-6L, -7L))
  }

  @Test
  fun testCeilMod() {
    assertEquals(-1, ceilMod(6, 7))
    assertEquals(0, ceilMod(6, 6))
    assertEquals(-4, ceilMod(6, 5))
    assertEquals(-2, ceilMod(6, 4))
    assertEquals(0, ceilMod(6, 3))
    assertEquals(0, ceilMod(6, 2))
    assertEquals(0, ceilMod(6, 1))
    assertEquals(0, ceilMod(6, -1))
    assertEquals(0, ceilMod(6, -2))
    assertEquals(0, ceilMod(6, -3))
    assertEquals(2, ceilMod(6, -4))
    assertEquals(1, ceilMod(6, -5))
    assertEquals(0, ceilMod(6, -6))
    assertEquals(6, ceilMod(6, -7))

    assertEquals(-6, ceilMod(-6, 7))
    assertEquals(0, ceilMod(-6, 6))
    assertEquals(-1, ceilMod(-6, 5))
    assertEquals(-2, ceilMod(-6, 4))
    assertEquals(0, ceilMod(-6, 3))
    assertEquals(0, ceilMod(-6, 2))
    assertEquals(0, ceilMod(-6, 1))
    assertEquals(0, ceilMod(-6, -1))
    assertEquals(0, ceilMod(-6, -2))
    assertEquals(0, ceilMod(-6, -3))
    assertEquals(2, ceilMod(-6, -4))
    assertEquals(4, ceilMod(-6, -5))
    assertEquals(0, ceilMod(-6, -6))
    assertEquals(1, ceilMod(-6, -7))

    assertEquals(-1L, ceilMod(6L, 7L))
    assertEquals(0L, ceilMod(6L, 6L))
    assertEquals(-4L, ceilMod(6L, 5L))
    assertEquals(-2L, ceilMod(6L, 4L))
    assertEquals(0L, ceilMod(6L, 3L))
    assertEquals(0L, ceilMod(6L, 2L))
    assertEquals(0L, ceilMod(6L, 1L))
    assertEquals(0L, ceilMod(6L, -1L))
    assertEquals(0L, ceilMod(6L, -2L))
    assertEquals(0L, ceilMod(6L, -3L))
    assertEquals(2L, ceilMod(6L, -4L))
    assertEquals(1L, ceilMod(6L, -5L))
    assertEquals(0L, ceilMod(6L, -6L))
    assertEquals(6L, ceilMod(6L, -7L))

    assertEquals(-6L, ceilMod(-6L, 7L))
    assertEquals(0L, ceilMod(-6L, 6L))
    assertEquals(-1L, ceilMod(-6L, 5L))
    assertEquals(-2L, ceilMod(-6L, 4L))
    assertEquals(0L, ceilMod(-6L, 3L))
    assertEquals(0L, ceilMod(-6L, 2L))
    assertEquals(0L, ceilMod(-6L, 1L))
    assertEquals(0L, ceilMod(-6L, -1L))
    assertEquals(0L, ceilMod(-6L, -2L))
    assertEquals(0L, ceilMod(-6L, -3L))
    assertEquals(2L, ceilMod(-6L, -4L))
    assertEquals(4L, ceilMod(-6L, -5L))
    assertEquals(0L, ceilMod(-6L, -6L))
    assertEquals(1L, ceilMod(-6L, -7L))
  }

  @Test
  fun testRandomInt() {
    testRandomIntInternal(1)
    testRandomIntInternal(0, 1)
    testRandomIntInternal(1, 2)
    testRandomIntInternal(-1, 0)
    testRandomIntInternal(-455, 213)
    testRandomIntInternal(Integer.MIN_VALUE, 435)
    testRandomIntInternal(-324, Integer.MAX_VALUE - 111)
    try {
      testRandomIntInternal(0)
      fail()
    } catch (e: IllegalArgumentException) {}
    try {
      testRandomIntInternal(-100)
      fail()
    } catch (e: IllegalArgumentException) {}
  }

  fun testRandomIntInternal(howbig: Int) {
    for (i in 0 until 10000) {
      val random = random(howbig)
      assertTrue(random < howbig)
    }
  }

  fun testRandomIntInternal(howsmall: Int, howbig: Int) {
    for (i in 0 until 10000) {
      val random = random(howsmall, howbig)
      assertTrue(random >= howsmall)
      assertTrue(random < howbig)
    }
  }
}
