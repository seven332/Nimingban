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

import com.hippo.nimingban.architecture.Logic
import com.hippo.nimingban.client.data.Forum

/*
 * Created by Hippo on 6/20/2017.
 */

interface ForumListLogic : Logic {

  /**
   * Called when user click a forum.
   */
  fun onSelectForum(forum: Forum, index: Int)

  /**
   * Returns the selected forum index.
   */
  fun getSelectedIndex(): Int
}
