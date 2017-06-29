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

import okhttp3.HttpUrl
import java.util.regex.Pattern

/*
 * Created by Hippo on 6/29/2017.
 */

/**
 * Quick and dirty pattern to differentiate IP addresses from hostnames. This is an approximation
 * of Android's private InetAddress#isNumeric API.
 *
 * This matches IPv6 addresses as a hex string containing at least one colon, and possibly
 * including dots after the first colon. It matches IPv4 addresses as strings containing only
 * decimal digits and dots. This pattern matches strings like "a:.23" and "54" that are neither IP
 * addresses nor hostnames; they will be verified as IP addresses (which is a more strict
 * verification).
 */
private val VERIFY_AS_IP_ADDRESS = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)")

/** Returns true if `host` is not a host name and might be an IP address.  */
private fun verifyAsIpAddress(host: String): Boolean {
  return VERIFY_AS_IP_ADDRESS.matcher(host).matches()
}

// okhttp3.Cookie.domainMatch(HttpUrl, String)
internal fun domainMatch(url: HttpUrl, domain: String): Boolean {
  val urlHost = url.host()

  if (urlHost == domain) {
    return true // As in 'example.com' matching 'example.com'.
  }

  if (urlHost.endsWith(domain)
      && urlHost[urlHost.length - domain.length - 1] == '.'
      && !verifyAsIpAddress(urlHost)) {
    return true // As in 'example.com' matching 'www.example.com'.
  }

  return false
}
