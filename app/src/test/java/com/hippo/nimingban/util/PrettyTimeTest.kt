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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/*
 * Created by Hippo on 6/9/2017.
 */

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "src/main/AndroidManifest.xml")
class PrettyTimeTest {

  private val SECOND_MILLIS = 1000L
  private val MINUTE_MILLIS = 60 * SECOND_MILLIS
  private val HOUR_MILLIS = 60 * MINUTE_MILLIS
  private val DAY_MILLIS = 24 * HOUR_MILLIS
  private val WEEK_MILLIS = 7 * DAY_MILLIS

  @Test
  fun testPrettyTime() {
    val context = RuntimeEnvironment.application

    assertEquals("just now", 0L.prettyTime(context, 0))
    assertEquals("2 min", (2 * MINUTE_MILLIS).prettyTime(context, 0))
    assertEquals("1 h", (50 * MINUTE_MILLIS).prettyTime(context, 0))
    assertEquals("12 h", (12 * HOUR_MILLIS).prettyTime(context, 0))
    assertEquals("1 d", (DAY_MILLIS).prettyTime(context, 0))
    assertEquals("13 d", (2 * WEEK_MILLIS - 1).prettyTime(context, 0))
  }
}
