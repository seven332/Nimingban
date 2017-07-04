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
import com.hippo.nimingban.util.Quad
import com.hippo.nimingban.util.ceilDiv
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/4/2017.
 */

class NmbClient(private val engine: NmbEngine) {

  companion object {
    private const val MAX_THREADS_PAGES = 100
    private const val MAX_REPLY_PAGE_SIZE = 19
  }

  fun forums() = engine.forums()

  fun threads(forum: Forum, page: Int) =
      Singles.zip(
          engine.threads(threadsApiUrl(forum.id, page))
              .subscribeOn(Schedulers.io()),
          engine.threadsHtml(threadsHtmlUrl(forum.htmlName, page))
              .onErrorReturn { ThreadsHtml(MAX_THREADS_PAGES, emptyList()) }
              .subscribeOn(Schedulers.io()),
          { list, (pages, threads) ->
            if (forum.isVirtual()) {
              // For virtual forum, use forum from html
              list.forEach { thread ->
                threads.find { it.id == thread.id }
                    ?.let { thread.forum = it.forum }
              }
            } else {
              // For actual forum, use requested forum
              list.forEach { it.forum = forum.displayedName }
            }

            // Return a pair of list and pages
            Pair(list, minOf(pages, MAX_THREADS_PAGES))
          })

  fun replies(id: String, page: Int) =
      Singles.zip(
          engine.replies(repliesApiUrl(id, page))
              .subscribeOn(Schedulers.io()),
          engine.repliesHtml(repliesHtmlUrl(id, page))
              .subscribeOn(Schedulers.io()),
          { thread, (forum, _) ->
            val replies = thread.replies.toMutableList()
            if (page == 0) {
              replies.add(0, thread.toReply())
            }

            val pages: Int
            if (thread.replies.size < MAX_REPLY_PAGE_SIZE) {
              pages = page + 1
            } else {
              pages = ceilDiv(thread.replyCount, MAX_REPLY_PAGE_SIZE)
            }

            Quad(thread, replies, forum, pages)
          }
      )

  fun reference(
      id: String
  ) = engine.reference(referenceHtmlUrl(id))

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
