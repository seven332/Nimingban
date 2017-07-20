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

import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.MvpUi
import com.hippo.nimingban.component.strategy.OnceSingleByMethod
import com.hippo.viewstate.GenerateViewState
import com.hippo.viewstate.strategy.SingleByMethod
import com.hippo.viewstate.strategy.SingleByTag
import com.hippo.viewstate.strategy.StrategyType

/*
 * Created by Hippo on 2017/7/18.
 */

@GenerateViewState
interface SendUi : MvpUi {

  companion object {
    const val TAG_AS = "SendUi:as"
  }

  @StrategyType(value = SingleByTag::class, tag = TAG_AS)
  fun asPost()

  @StrategyType(value = SingleByTag::class, tag = TAG_AS)
  fun asReply()

  @StrategyType(value = OnceSingleByMethod::class)
  fun setPresetContent(text: String)

  @StrategyType(value = SingleByMethod::class)
  fun setMoreActionsVisibility(show: Boolean)

  @StrategyType(value = SingleByMethod::class)
  fun setForum(forum: Forum)

  /**
   * Requests ui to feedback user input. [SendLogic.feedbackInput] should be called.
   */
  fun requestInput()
}
