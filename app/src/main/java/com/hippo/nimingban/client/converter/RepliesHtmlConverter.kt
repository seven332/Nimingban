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

import com.hippo.nimingban.client.data.RepliesHtml
import org.jsoup.Jsoup

/*
 * Created by Hippo on 6/23/2017.
 */

class RepliesHtmlConverter : NmbConverter<RepliesHtml>() {

  override fun doConvert(body: String): RepliesHtml {
    val document = Jsoup.parse(body)

    // Forum
    val top = document.getElementById("h-content-top-nav").children().first().children()
    val forum = if (top?.size ?: 0 >= 2) top[1].text() else ""

    // Page size
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

    return RepliesHtml(forum, href.lastInt(Int.MAX_VALUE))
  }
}
