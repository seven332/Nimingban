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

package com.hippo.nimingban.component.paper

import android.os.Bundle
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbPen

/*
 * Created by Hippo on 2017/7/18.
 */

open class SendPen : NmbPen<SendUi>(), SendLogic {

  init {
    SendUiState().also { view = it; state = it }
  }

  private var forum: Forum? = null
  private var threadId: String? = null

  private var showMoreActions = false

  override fun onCreate(args: Bundle) {
    super.onCreate(args)
    view.setMoreActionsVisibility(showMoreActions)
  }

  fun init(forum: Forum) {
    this.forum = forum
    view.asPost()
    view.setForum(forum)
  }

  fun init(threadId: String) {
    this.threadId = threadId
    view.asReply()
  }

  override fun onFeedbackInput(title: String, name: String, email: String, content: String) {}

  override fun onClickForum() {}

  override fun onClickMoreAction() {
    showMoreActions = !showMoreActions
    view.setMoreActionsVisibility(showMoreActions)
  }
}
