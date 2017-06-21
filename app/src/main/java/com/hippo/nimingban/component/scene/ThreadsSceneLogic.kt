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

import com.hippo.nimingban.architecture.Logic
import com.hippo.nimingban.component.paper.ForumListLogic
import com.hippo.nimingban.component.paper.ForumListUi
import com.hippo.nimingban.component.paper.NavigationLogic
import com.hippo.nimingban.component.paper.NavigationUi
import com.hippo.nimingban.component.paper.ThreadsLogic
import com.hippo.nimingban.component.paper.ThreadsUi
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.paper.ToolbarUi

/*
 * Created by Hippo on 6/19/2017.
 */

interface ThreadsSceneLogic : Logic {

  var threadsSceneUi: ThreadsSceneUi?

  fun getThreadsLogic(): ThreadsLogic
  var threadsUi: ThreadsUi?

  fun getThreadsToolbarUiLogic(): ToolbarLogic
  var threadsToolbarUi: ToolbarUi?

  fun getForumListLogic(): ForumListLogic
  var forumListUi: ForumListUi?

  fun getForumListToolbarLogic(): ToolbarLogic
  var forumListToolbarUi: ToolbarUi?

  fun getNavigationLogic(): NavigationLogic
  var navigationUi: NavigationUi?
}
