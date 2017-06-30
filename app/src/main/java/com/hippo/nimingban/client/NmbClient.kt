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

import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.ThreadsHtml
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/4/2017.
 */

class NmbClient(private val engine: NmbEngine) {

  companion object {
    private const val MAX_THREADS_PAGES = 100
  }

  fun forums() =
      engine.forums()
          .map {
            // Init all forums
            it.forEach {
              it.init
              // All forums is official
              it.forums.forEach { it.official = true }
            }
            // Return it self
            it
          } !!

  fun threads(forum: Forum, page: Int) =
      Singles.zip(
          engine.threads(threadsApiUrl(forum.id, page))
              .map { it.also { it.forEach { it.init } } }
              .subscribeOn(Schedulers.io()),
          engine.threadsHtml(threadsHtmlUrl(forum.htmlName, page))
              .onErrorReturn { ThreadsHtml(MAX_THREADS_PAGES, emptyList()) }
              .subscribeOn(Schedulers.io()),
          { list, (pages, threads) ->
            // Pass forum from html to list
            list.forEach { thread ->
              threads.find { it.id == thread.id }
                  ?.let { thread.forum = it.forum }
            }
            // Return a pair of list and pages
            Pair(list, minOf(pages, MAX_THREADS_PAGES))
          })

  fun replies(id: String, page: Int) =
      engine.replies(repliesApiUrl(id, page))
          .map {
            // Init the thread
            it.init
            // The reply list
            val replies = it.replies.toMutableList()
            if (page == 0) {
              // It's the first, add thread itself to the header
              replies.add(0, it.toReply())
            }
            // Pack thread and reply list
            Pair(it, replies)
          } !!

  fun repliesHtml(id: String, page: Int) = engine.repliesHtml(repliesHtmlUrl(id, page))

  fun post(
      title: String,
      name: String,
      email: String,
      content: String,
      fid: String,
      water: Boolean
  ) = engine.post(name, email, title, content, fid, water.toString(), null)

  fun reply(
      title: String,
      name: String,
      email: String,
      content: String,
      resto: String,
      water: Boolean
  ) = engine.reply(name, email, title, content, resto, water.toString(), null)
}
