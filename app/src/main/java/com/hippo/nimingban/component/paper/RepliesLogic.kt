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

import android.text.style.ClickableSpan
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.component.MvpLogic
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout

/*
 * Created by Hippo on 2017/7/17.
 */

interface RepliesLogic : MvpLogic<RepliesUi> {

  fun initAdapter(adapter: ContentDataAdapter<Reply, *>)

  fun termAdapter(adapter: ContentDataAdapter<Reply, *>)

  fun initContentLayout(layout: ContentLayout)

  fun termContentLayout(layout: ContentLayout)

  fun showMessage(message: String)

  fun registerUserColor(user: String, color: Int)

  fun unregisterUserColor(user: String)

  fun getUserColor(user: String): Int?

  fun onClickThumb(reply: Reply)

  fun onClickSpan(span: ClickableSpan)

  fun onClickReply(reply: Reply)
}
