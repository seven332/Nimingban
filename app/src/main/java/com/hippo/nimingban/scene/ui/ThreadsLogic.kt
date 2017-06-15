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

package com.hippo.nimingban.scene.ui

import com.hippo.nimingban.architecture.Logic
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout

/*
 * Created by Hippo on 6/15/2017.
 */

interface ThreadsLogic : Logic {

  /** Set data for adapter **/
  fun initializeAdapter(adapter: ContentDataAdapter<Thread, *>)

  /** Unset data for adapter **/
  fun terminateAdapter(adapter: ContentDataAdapter<Thread, *>)

  /** Set presenter for ContentLayout **/
  fun initializeContentLayout(contentLayout: ContentLayout)

  /** Unset presenter for ContentLayout **/
  fun terminateContentLayout(contentLayout: ContentLayout)

  /** Called when user click a thread **/
  fun onClickThread(thread: Thread)

  /** Called when user click a thread **/
  fun onClickThumb(reply: Reply)
}
