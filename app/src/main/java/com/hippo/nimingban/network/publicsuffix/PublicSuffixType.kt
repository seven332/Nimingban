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

package com.hippo.nimingban.network.publicsuffix

/*
 * Created by Hippo on 6/29/2017.
 */

// https://github.com/google/guava/blob/v22.0/guava/src/com/google/thirdparty/publicsuffix/PublicSuffixType.java

/**
 * Specifies the type of a top-level domain definition.
 */
enum class PublicSuffixType(val innerNodeCode: Char, val leafNodeCode: Char) {
  /** private definition of a top-level domain  */
  PRIVATE(':', ','),
  /** ICANN definition of a top-level domain  */
  ICANN('!', '?');
}

/** Returns a PublicSuffixType of the right type according to the given code */
fun publicSuffixTypeOf(code: Char) = PublicSuffixType.values().first { it.innerNodeCode == code || it.leafNodeCode == code }
