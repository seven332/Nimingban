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

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hippo.nimingban.component.GroupPaper
import com.hippo.nimingban.component.MvpPaper

/*
 * Created by Hippo on 2017/7/14.
 */

@Suppress("NAME_SHADOWING")
fun papers(
    logic: DumpLogic,
    onCreate: GroupPaper<DumpUi>.(ViewGroup) -> MvpPaper<*>
) = object : DumpPaper(logic) {
  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)
    val paper = onCreate(container)
    addChild(paper)
    view = paper.view
  }
}

fun GroupPaper<*>.drawer(
    logic: DrawerLogic,
    container: ViewGroup,
    init: DrawerPaper.() -> Unit
) = DrawerPaper(logic).also { it.create(inflater, container); it.init() }

fun GroupPaper<*>.drawer(
    logic: DrawerLogic,
    containerId: Int,
    init: DrawerPaper.() -> Unit
) = inflateChild(containerId) { container -> DrawerPaper(logic).also { it.create(inflater, container) } }.apply { init() }

fun GroupPaper<*>.swipeBack(
    logic: SwipeBackLogic,
    container: ViewGroup,
    init: SwipeBackPaper.() -> Unit
) = SwipeBackPaper(logic).also { it.create(inflater, container); it.init() }

fun GroupPaper<*>.swipeBack(
    logic: SwipeBackLogic,
    containerId: Int,
    init: SwipeBackPaper.() -> Unit
) = inflateChild(containerId) { container -> SwipeBackPaper(logic).also { it.create(inflater, container) } }.apply { init() }

fun GroupPaper<*>.toolbar(
    logic: ToolbarLogic,
    container: ViewGroup,
    init: ToolbarPaper.() -> Unit
) = ToolbarPaper(logic).also { it.create(inflater, container); it.init() }

fun GroupPaper<*>.toolbar(
    logic: ToolbarLogic,
    containerId: Int,
    init: ToolbarPaper.() -> Unit
) : ToolbarPaper {

  Log.d("TAG", "Papers.toolbar this = $this")

  return inflateChild(containerId) { container -> ToolbarPaper(logic).also { it.create(inflater, container) } }.apply { init() }
}

fun GroupPaper<*>.threads(
    logic: ThreadsLogic,
    containerId: Int
) = inflateChild(containerId) { container -> ThreadsPaper(logic).also { it.create(inflater, container) } }

fun GroupPaper<*>.forumList(
    logic: ForumListLogic,
    containerId: Int
) = inflateChild(containerId) { container -> ForumListPaper(logic).also { it.create(inflater, container) } }

fun GroupPaper<*>.sortForums(
    logic: SortForumsLogic,
    containerId: Int
) = inflateChild(containerId) { container -> SortForumsPaper(logic).also { it.create(inflater, container) } }

fun GroupPaper<*>.replies(
    logic: RepliesLogic,
    containerId: Int
) = inflateChild(containerId) { container -> RepliesPaper(logic).also { it.create(inflater, container) } }

fun GroupPaper<*>.gallery(
    logic: GalleryLogic,
    containerId: Int
) = inflateChild(containerId) { container -> GalleryPaper(logic).also { it.create(inflater, container) } }
