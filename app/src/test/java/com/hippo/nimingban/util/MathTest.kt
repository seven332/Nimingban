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

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/*
 * Created by Hippo on 6/10/2017.
 */

class MathTest {

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
