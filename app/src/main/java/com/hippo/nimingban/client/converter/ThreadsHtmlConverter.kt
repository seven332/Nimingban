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

import com.hippo.nimingban.client.data.ThreadsHtml
import org.jsoup.Jsoup

/*
 * Created by Hippo on 6/23/2017.
 */

class ThreadsHtmlConverter : NmbConverter<ThreadsHtml>() {

  override fun doConvert(body: String): ThreadsHtml {
    val document = Jsoup.parse(body)
    val pagination = document.getElementsByClass("h-pagination").first()
    val children = pagination?.children()

    var href: String? = null
    val last = children?.last()?.children()?.first()

    if (last?.text() == "末页") {
      href = last.attr("href")
    } else if (last?.text() == "下一页") {
      if (children.size > 1) {
        href = children[children.size - 2]?.children()?.first()?.attr("href")
      }
    }

    return ThreadsHtml(href.lastInt(Int.MAX_VALUE))
  }
}

internal fun String?.lastInt(defValue: Int): Int {
  if (this == null) return defValue

  var result = 0
  var base = 1
  var inInt = false
  for (i in this.length - 1 downTo 0) {
    val ch = this[i]
    val isInt = ch in '0' .. '9'

    if (isInt) {
      inInt = true
      result += (ch - '0') * base
      base *= 10
    } else if (inInt) {
      return result
    }
  }

  return if (inInt) result else defValue
}
