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

package com.hippo.nimingban.client

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import com.hippo.nimingban.util.fromHtml
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

/*
 * Created by Hippo on 6/4/2017.
 */

const val REPLY_PAGE_SIZE = 19

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.getDefault())

// Kotlin need a way to initialize package-level val
// https://youtrack.jetbrains.com/issue/KT-13486
private val INIT = run {
  DATE_FORMAT.timeZone = TimeZone.getTimeZone("GMT+08:00")
}

// Remove all brackets in string
private fun String.removeBrackets(): String {
  val sb = StringBuilder(this.length - 3)
  var brackets = false
  for (c in this) {
    if (brackets) {
      if (c == ')') brackets = false
    } else {
      if (c == '(') brackets = true else sb.append(c)
    }
  }
  return sb.toString()
}

fun String?.toNmbDate(): Long {
  if (this == null) return 0

  try {
    return DATE_FORMAT.parse(this.removeBrackets())?.time ?: 0
  } catch (e: ParseException) {
    return 0
  }
}


private const val DEFAULT_USER = "无名氏"

fun String?.toNmbUser(admin: Boolean): CharSequence {
  val user = this?.fromHtml() ?: DEFAULT_USER
  if (admin) {
    val spannable = user as? Spannable ?: SpannableString(user)
    spannable.setSpan(ForegroundColorSpan(Color.RED), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannable
  } else {
    return user
  }
}


private const val NO_TITLE = "无标题"
private const val NO_NAME = "无名氏"
private const val DEFAULT_CONTENT = "无文本"

private val URL_PATTERN = Pattern.compile("(http|https)://[a-z0-9A-Z%-]+(\\.[a-z0-9A-Z%-]+)+(:\\d{1,5})?(/[a-zA-Z0-9-_~:#@!&',;=%/\\*\\.\\?\\+\\$\\[\\]\\(\\)]+)?/?")
private val REFERENCE_PATTERN = Pattern.compile(">>?(?:No.)?(\\d+)")

private fun SpannableStringBuilder.resolveUrl(): SpannableStringBuilder {
  val matcher = URL_PATTERN.matcher(this)

  while (matcher.find()) {
    val start = matcher.start()
    val end = matcher.end()

    val links = this.getSpans(start, end, URLSpan::class.java)
    if (links.isNotEmpty()) {
      // There has been URLSpan already, leave it alone
      continue
    }

    val urlSpan = URLSpan(matcher.group(0))
    this.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }

  return this
}

private fun SpannableStringBuilder.resolveReference(): SpannableStringBuilder {
  val matcher = REFERENCE_PATTERN.matcher(this)

  while (matcher.find()) {
    val start = matcher.start()
    val end = matcher.end()

    val span = NmbReferenceSpan(matcher.group(1))
    this.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }

  return this
}

fun String?.toNmbContent(sage: Boolean, title: String?, name: String?, email: String?): CharSequence {
  val sb = StringBuilder()
  if (sage) {
    sb.append("<font color=\"red\"><b>SAGE</b></font><br><br>")
  }
  if (!title.isNullOrEmpty() && NO_TITLE != title) {
    sb.append("<b>").append(title).append("</b><br>")
  }
  if (!name.isNullOrEmpty() && NO_NAME != name) {
    sb.append("<b>").append(name).append("</b><br>")
  }
  if (!email.isNullOrEmpty()) {
    sb.append("<b>").append(email).append("</b><br>")
  }
  sb.append(if (!this.isNullOrEmpty()) this else DEFAULT_CONTENT)
  return (sb.toString().fromHtml() as SpannableStringBuilder).resolveUrl().resolveReference()
}
