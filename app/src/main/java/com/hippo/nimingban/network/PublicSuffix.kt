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

import com.hippo.nimingban.network.publicsuffix.PUBLIC_SUFFIX_EXACT
import com.hippo.nimingban.network.publicsuffix.PUBLIC_SUFFIX_EXCLUDED
import com.hippo.nimingban.network.publicsuffix.PUBLIC_SUFFIX_UNDER

/*
 * Created by Hippo on 6/29/2017.
 */

// https://github.com/google/guava/blob/v22.0/guava/src/com/google/common/net/InternetDomainName.java

private val DOT_REGEX = "\\."

/**
 * Indicates whether this domain name represents a *public suffix*, as defined by the Mozilla
 * Foundation's [Public Suffix List](http://publicsuffix.org/) (PSL). A public suffix
 * is one under which Internet users can directly register names, such as `com`,
 * `co.uk` or `pvt.k12.wy.us`. Examples of domain names that are *not* public
 * suffixes include `ehviewer`, `ehviewer.com` and `foo.co.uk`.
 */
fun isPublicSuffix(domain: String?): Boolean {
  if (domain == null) return false
  if (PUBLIC_SUFFIX_EXACT.containsKey(domain)) return true
  if (PUBLIC_SUFFIX_EXCLUDED.containsKey(domain)) return false
  if (matchesWildcardPublicSuffix(domain)) return true
  return false
}

/**
 * Does the domain name match one of the "wildcard" patterns (e.g. `"*.ar"`)?
 */
private fun matchesWildcardPublicSuffix(domain: String): Boolean {
  val pieces = domain.split(DOT_REGEX.toRegex(), 2).toTypedArray()
  return pieces.size == 2 && PUBLIC_SUFFIX_UNDER.containsKey(pieces[1])
}
