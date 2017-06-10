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

package com.hippo.nimingban.widget.nmb

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextSwitcher
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.util.random

/*
 * Created by Hippo on 6/9/2017.
 */

class NmbReplayMarquee @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextSwitcher(context, attrs), Runnable {

  private var started = false
  private var index = 0

  var replies: List<Reply> = emptyList()
    set(value) {
      field = value
      index = -1
      nextReply()
    }


  init {
    setFactory { LayoutInflater.from(context).inflate(
        R.layout.threads_item_reply, this@NmbReplayMarquee, false) }
  }

  fun start() {
    if (!started) {
      started = true
      postDelayed(this, getMarqueeInterval())
    }
  }

  fun stop() {
    if (started) {
      started = false
      removeCallbacks(this)
    }
  }

  private fun nextReply() {
    removeCallbacks(this)

    val replies = this.replies
    if (replies.isEmpty()) {
      setText(null)
    } else {
      index = (++index).let { if (it >= replies.size) 0 else it }.let { if (it < 0) 0 else it}
      setText(replies[index].displayContent)

      if (started) {
        postDelayed(this, getMarqueeInterval())
      }
    }
  }

  private fun getMarqueeInterval() = random(3000, 5001).toLong()

  override fun run() {
    nextReply()
  }
}
