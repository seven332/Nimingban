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

import org.junit.Assert.assertEquals
import org.junit.Test

/*
 * Created by Hippo on 6/7/2017.
 */

class AnimateTest {

  @Test
  fun test() {
    val animate = TestAnimate()

    animate.duration = 10
    animate.repeat = 3
    animate.start()

    assertEquals(true, animate.calculate(0))
    assertEquals(0.0f, animate.progress, 0.0f)

    assertEquals(true, animate.calculate(3))
    assertEquals(0.3f, animate.progress, 0.0f)

    assertEquals(true, animate.calculate(9))
    assertEquals(0.9f, animate.progress, 0.0f)

    assertEquals(true, animate.calculate(17))
    assertEquals(0.7f, animate.progress, 0.0f)

    assertEquals(false, animate.calculate(30))
    assertEquals(1.0f, animate.progress, 0.0f)
  }

  @Test
  fun testCurrentSame() {
    val animate = TestAnimate()

    animate.duration = 10
    animate.repeat = 3
    animate.start()

    assertEquals(true, animate.calculate(0))
    assertEquals(0.0f, animate.progress, 0.0f)

    assertEquals(true, animate.calculate(0))
    assertEquals(0.0f, animate.progress, 0.0f)

    assertEquals(true, animate.calculate(17))
    assertEquals(0.7f, animate.progress, 0.0f)

    assertEquals(true, animate.calculate(17))
    assertEquals(0.7f, animate.progress, 0.0f)
  }

  @Test
  fun testCurrentMeet() {
    val animate = TestAnimate()

    animate.duration = 10
    animate.repeat = 3
    animate.start()

    assertEquals(true, animate.calculate(0))
    assertEquals(0.0f, animate.progress, 0.0f)

    assertEquals(false, animate.calculate(30))
    assertEquals(1.0f, animate.progress, 0.0f)
  }

  @Test
  fun testCurrentOverstep() {
    val animate = TestAnimate()

    animate.duration = 10
    animate.repeat = 3
    animate.start()

    assertEquals(true, animate.calculate(0))
    assertEquals(0.0f, animate.progress, 0.0f)

    assertEquals(false, animate.calculate(50))
    assertEquals(1.0f, animate.progress, 0.0f)
  }

  private class TestAnimate : Animate() {
    var progress = 0.0f
    override fun onCalculate(progress: Float) { this.progress = progress }
  }
}
