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

/*
 * Created by Hippo on 6/16/2017.
 */

class CollectionTest {

  @Test
  fun testAsMutableList() {
    val list1 = mutableListOf("324", "abc")
    assertTrue { list1 === list1.asMutableList() }

    val list2 = listOf("324", "abc")
    assertTrue { list2 === list2.asMutableList() }
  }

  @Test
  fun testRemoveFirst() {
    val list1 = mutableListOf("123", "1234", "4321", "1")
    assertEquals("1234", list1.removeFirst { it.length == 4 })
    assertEquals(mutableListOf("123", "4321", "1"), list1)

    val list2 = mutableListOf("123", "1234", "4321", "1")
    assertEquals(null, list2.removeFirst { it.length == 10 })
    assertEquals(mutableListOf("123", "1234", "4321", "1"), list2)
  }
}
