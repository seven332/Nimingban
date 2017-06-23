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

package com.hippo.nimingban.client.converter

import org.junit.Test
import kotlin.test.assertEquals

/*
 * Created by Hippo on 6/23/2017.
 */

class ThreadsHtmlConverterTest {

  @Test
  fun testLastInt() {
    assertEquals(123, "123".lastInt(456))
    assertEquals(123, "dsfds123".lastInt(456))
    assertEquals(123, "dsfds123fgasdfas".lastInt(456))
    assertEquals(456, "fsfdsadas".lastInt(456))
  }
}
