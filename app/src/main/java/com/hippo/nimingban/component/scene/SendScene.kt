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

package com.hippo.nimingban.component.scene

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.MvpPaper
import com.hippo.nimingban.component.MvpPen
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.paper.BottomToolPaper
import com.hippo.nimingban.component.paper.BottomToolPen
import com.hippo.nimingban.component.paper.ScrollPaper
import com.hippo.nimingban.component.paper.ScrollPen
import com.hippo.nimingban.component.paper.SendPen
import com.hippo.nimingban.component.paper.ToolbarPaper
import com.hippo.nimingban.component.paper.ToolbarPen
import com.hippo.nimingban.component.paper.bottomTool
import com.hippo.nimingban.component.paper.papers
import com.hippo.nimingban.component.paper.pens
import com.hippo.nimingban.component.paper.scroll
import com.hippo.nimingban.component.paper.send
import com.hippo.nimingban.component.paper.toolbar

/*
 * Created by Hippo on 2017/7/19.
 */

class SendScene : NmbScene() {

  companion object {
    private const val LOG_TAG = "SendScene"

    internal const val KEY_THREAD_ID = "SendScene:thread_id"
    internal const val KEY_FORUM = "SendScene:forum"
  }

  private val toolbar: ToolbarPen = object : ToolbarPen() {

    override fun onCreate(args: Bundle) {
      super.onCreate(args)
      view.setNavigationIcon(R.drawable.arrow_left_white_x24)
      view.inflateMenu(R.menu.send_top)
    }

    override fun onClickNavigationIcon() {
      super.onClickNavigationIcon()
      pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_reply -> {
          send.view.requestInput()
          return true
        }
        else -> return super.onClickMenuItem(item)
      }
    }
  }

  private val bottomTool: BottomToolPen = object : BottomToolPen() {

    override fun onCreate(args: Bundle) {
      super.onCreate(args)
      view.inflateMenu(R.menu.send_bottom)
    }
  }

  private val scroll: ScrollPen = object : ScrollPen() {}

  private val send: SendPen = object : SendPen() {

    override fun onFeedbackInput(title: String, name: String, email: String, content: String) {
      super.onFeedbackInput(title, name, email, content)
    }
  }

  private val pen = pens(toolbar, bottomTool, send)

  override fun createPen(): MvpPen<*> = pen

  override fun createPaper(): MvpPaper<*> = papers(pen) {
    toolbar(toolbar, it) {
      bottomTool(bottomTool, ToolbarPaper.CONTAINER_ID) {
        scroll(scroll, BottomToolPaper.CONTAINER_ID) {
          send(send, ScrollPaper.CONTAINER_ID)
        }
      }
    }
  }

  override fun onCreate(args: Bundle) {
    super.onCreate(args)

    val forum = args.getParcelable<Forum>(KEY_FORUM)
    val threadId = args.getString(KEY_THREAD_ID)

    if (forum != null) {
      send.init(forum)
      toolbar.view.setTitle(R.string.send_post)
    } else if (threadId != null) {
      send.init(threadId)
      toolbar.view.setTitle(R.string.send_reply)
    } else {
      Log.e(LOG_TAG, "Can't init SendPen")
    }
  }
}
