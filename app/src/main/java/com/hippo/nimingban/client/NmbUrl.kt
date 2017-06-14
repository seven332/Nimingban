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

import okhttp3.HttpUrl

/*
 * Created by Hippo on 6/4/2017.
 */

const val NMB_APP_ID = "nimingban"

const val NMB_DOMAIN = "h.nimingban.com"

const val NMB_HOST = "https://" + NMB_DOMAIN

const val NMB_FORUM_TIMELINE = "-1"

const val NMB_API_FORUMS = NMB_HOST + "/Api/getForumList?appid=" + NMB_APP_ID

const val NMB_API_TIMELINE = NMB_HOST + "/Api/timeline?appid=" + NMB_APP_ID

const val NMB_API_THREADS = NMB_HOST + "/Api/showf?appid=" + NMB_APP_ID

const val NMB_API_REPLIES = NMB_HOST + "/Api/thread?appid=" + NMB_APP_ID


fun forumsUrl() = NMB_API_FORUMS

fun timelineUrl(page: Int) =  NMB_API_TIMELINE + "&page=" + (page + 1)

fun threadsUrl(forum: String, page: Int): String {
  return when (forum) {
    NMB_FORUM_TIMELINE -> timelineUrl(page)
    else -> NMB_API_THREADS + "&id=" + forum + "&page=" + (page + 1)
  }
}

fun repliesUrl(id: String, page: Int) = NMB_API_REPLIES + "&id=" + id + "&page=" + (page + 1)

// TODO check NMB_DOMAIN, and image host
fun isNmbUrl(url: HttpUrl): Boolean = true
