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

import android.view.MenuItem
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.component.GroupLogic
import com.hippo.nimingban.component.paper.BottomToolLogic
import com.hippo.nimingban.component.paper.SendLogic
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.post
import com.hippo.nimingban.component.reply
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/24/2017.
 */

class SendSceneLogic(
    private val scene: Scene,
    forum: Forum?,
    val thread: Thread?
) : GroupLogic() {

  val toolbarLogic: ToolbarLogic = SendToolbarLogic().also { addChild(it) }
  val bottomToolLogic: BottomToolLogic = SendBottomToolLogic().also { addChild(it) }
  val sendLogic: SendLogic = SendLogic(scene, forum).also { addChild(it) }

  fun onSelectForum(forum: Forum) {
    sendLogic.onSelectForum(forum)
  }

  private fun send() {
    val forum = sendLogic.getForum()
    val title = sendLogic.getTitle()
    val name = sendLogic.getName()
    val email = sendLogic.getEmail()
    val content = sendLogic.getContent()

    if (forum != null) {
      post(title, name, email, content, forum.id, false)
    } else if (thread != null) {
      reply(title, name, email, content, thread.id, false)
    }

    scene.pop()
  }


  private inner class SendToolbarLogic: ToolbarLogic() {

    init {
      setNavigationIcon(R.drawable.arrow_left_white_x24)
      inflateMenu(R.menu.send_top)
    }

    override fun onClickNavigationIcon() {
      scene.pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_send -> {
          send()
          return true
        }
        else -> return false
      }
    }
  }


  private inner class SendBottomToolLogic: BottomToolLogic() {

    init {
      inflateMenu(R.menu.send_bottom)
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_image -> {
          // TODO
          return true
        }
        else -> return false
      }
    }
  }
}
