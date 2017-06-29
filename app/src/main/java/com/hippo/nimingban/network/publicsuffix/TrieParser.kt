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

import java.util.LinkedList

/*
 * Created by Hippo on 6/29/2017.
 */

// https://github.com/google/guava/blob/v22.0/guava/src/com/google/thirdparty/publicsuffix/TrieParser.java

/**
 * Parses a serialized trie representation of a map of reversed public suffixes into an immutable
 * map of public suffixes.
 */
fun parseTrie(encoded: CharSequence): MutableMap<String, PublicSuffixType> {
  val map = mutableMapOf<String, PublicSuffixType>()
  val encodedLen = encoded.length
  var idx = 0
  while (idx < encodedLen) {
    idx += doParseTrieToBuilder(LinkedList<CharSequence>(), encoded.subSequence(idx, encodedLen), map)
  }
  return map
}

/**
 * Parses a trie node and returns the number of characters consumed.
 *
 * @param stack The prefixes that precede the characters represented by this node. Each entry of
 *              the stack is in reverse order.
 * @param encoded The serialized trie.
 * @param builder A map builder to which all entries will be added.
 * @return The number of characters consumed from `encoded`.
 */
private fun doParseTrieToBuilder(
    stack: MutableList<CharSequence>,
    encoded: CharSequence,
    builder: MutableMap<String, PublicSuffixType>): Int {

  val encodedLen = encoded.length
  var idx = 0
  var c = '\u0000'

  // Read all of the characters for this node.
  while (idx < encodedLen) {
    c = encoded[idx]
    if (c == '&' || c == '?' || c == '!' || c == ':' || c == ',') {
      break
    }
    idx++
  }

  stack.add(0, encoded.subSequence(0, idx).reversed())

  if (c == '!' || c == '?' || c == ':' || c == ',') {
    // '!' represents an interior node that represents an ICANN entry in the map.
    // '?' represents a leaf node, which represents an ICANN entry in map.
    // ':' represents an interior node that represents a private entry in the map
    // ',' represents a leaf node, which represents a private entry in the map.
    val domain = stack.joinToString("")
    if (domain.isNotEmpty()) {
      builder.put(domain, publicSuffixTypeOf(c))
    }
  }
  idx++

  if (c != '?' && c != ',') {
    while (idx < encodedLen) {
      // Read all the children
      idx += doParseTrieToBuilder(stack, encoded.subSequence(idx, encodedLen), builder)
      if (encoded[idx] == '?' || encoded[idx] == ',') {
        // An extra '?' or ',' after a child node indicates the end of all children of this node.
        idx++
        break
      }
    }
  }
  stack.removeAt(0)
  return idx
}
