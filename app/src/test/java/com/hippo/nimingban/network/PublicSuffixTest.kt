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

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
 * Created by Hippo on 6/29/2017.
 */

class PublicSuffixTest {

  @Test
  fun testPublicSuffix() {
    assertTrue(isPublicSuffix("com"))
    assertTrue(isPublicSuffix("uk"))
    assertTrue(isPublicSuffix("cn"))
    assertFalse(isPublicSuffix("bd"))
    assertTrue(isPublicSuffix("hippo.bd"))
    assertTrue(isPublicSuffix("ehviewer.bd"))
    assertTrue(isPublicSuffix("中国"))
    assertFalse(isPublicSuffix("ehviewer"))
    assertFalse(isPublicSuffix("ehviewer.com"))
    assertFalse(isPublicSuffix("www.ck"))
  }
}
