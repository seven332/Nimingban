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

import com.hippo.nimingban.client.NO_NAME
import com.hippo.nimingban.client.NO_TITLE
import com.hippo.nimingban.client.data.Reference
import com.hippo.nimingban.client.fromNmbDate
import com.hippo.nimingban.exception.ParseException
import com.hippo.nimingban.util.substringAfter
import org.jsoup.Jsoup

/*
 * Created by Hippo on 2017/7/4.
 */

class ReferenceHtmlConverter : NmbConverter<Reference>() {

  override fun doConvert(body: String): Reference {
    val document = Jsoup.parse(body)

    val id = document
        .getElementsByClass("h-threads-item-reply h-threads-item-ref").first()
        ?.attr("data-threads-id")
        .apply { if (isNullOrEmpty()) throw ParseException() }!!
    val image = document
        .getElementsByClass("h-threads-img").first()
        ?.attr("href")
        .run { getImage(this) }
    val title = document
        .getElementsByClass("h-threads-info-title").first()
        ?.text()
        .run { if (this == null || this.isEmpty() || this == NO_TITLE) null else this}
    val name = document
        .getElementsByClass("h-threads-info-email").first()
        ?.text().
        run { if (this == null || this.isEmpty() || this == NO_NAME) null else this}
    val date = document
        .getElementsByClass("h-threads-info-createdat").first()
        ?.text()
        .fromNmbDate()
    val user = document
        .getElementsByClass("h-threads-info-uid").first()
        ?.text()
        ?.run { if (this.startsWith("ID:")) substring(3) else this }
    val admin = document
        .getElementsByClass("h-threads-info-uid").first()
        ?.childNodeSize() ?: 0 > 1
    val content = document
        .getElementsByClass("h-threads-content").first()
        ?.html()
    val threadId = document
        .getElementsByClass("h-threads-info-id").first()
        ?.attr("href")
        ?.substringAfter("/t/")?.substringBefore('?')

    return Reference(
        id = id,
        date = date,
        user = user,
        title = title,
        name = name,
        email = null,
        content = content,
        image = image,
        sage = false,
        admin = admin,
        threadId = threadId
    )
  }

  private fun getImage(url: String?) = url?.substringAfter("/image/") ?: url?.substringAfter("/thumb/")
}
