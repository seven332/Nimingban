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

import android.view.ViewGroup
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.component.GroupUi

/*
 * Created by Hippo on 6/24/2017.
 */

fun GroupUi.toolbar(
    logic: ToolbarLogic,
    container: ViewGroup,
    init: ToolbarUi.() -> Unit
) = ToolbarUi(logic, inflater, container).also { init(it); addChild(it) }

fun GroupUi.toolbar(
    logic: ToolbarLogic,
    container: Int,
    init: ToolbarUi.() -> Unit
) = inflateChild(container, 0, { ToolbarUi(logic, inflater, it) }, init)

fun GroupUi.swipeBack(
    logic: SwipeBackLogic,
    container: ViewGroup,
    init: SwipeBackUi.() -> Unit
) = SwipeBackUi(logic, inflater, container).also { init(it); addChild(it) }

fun GroupUi.swipeBack(
    logic: SwipeBackLogic,
    container: Int,
    init: SwipeBackUi.() -> Unit
) = inflateChild(container, 0, { SwipeBackUi(logic, inflater, it) }, init)

fun GroupUi.threads(
    logic: ThreadsLogic,
    activity: NmbActivity,
    container: Int
) = inflateChild(container, 0, { ThreadsUi(logic, activity, inflater, it) })

fun GroupUi.navigation(
    logic: NavigationLogic,
    container: Int
) = inflateChild(container, 0, { NavigationUi(logic, inflater, it) })

fun GroupUi.forumList(
    logic: ForumListLogic,
    container: Int
) = inflateChild(container, 0, { ForumListUi(logic, inflater, it) })

fun GroupUi.replies(
    logic: RepliesLogic,
    activity: NmbActivity,
    container: Int
) = inflateChild(container, 0, { RepliesUi(logic, activity, inflater, it) })

fun GroupUi.gallery(
    logic: GalleryLogic,
    reply: Reply?,
    container: Int
) = inflateChild(container, 0, { GalleryUi(logic, reply, inflater, it) })

fun GroupUi.forums(
    logic: ForumsLogic,
    container: Int
) = inflateChild(container, 0, { ForumsUi(logic, inflater, it) })
