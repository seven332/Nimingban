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
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbPen
import com.hippo.nimingban.util.INVALID_INDEX
import io.reactivex.android.schedulers.AndroidSchedulers

/*
 * Created by Hippo on 2017/7/15.
 */

abstract class ForumListPen : NmbPen<ForumListUi>(), ForumListLogic {

  init {
    ForumListUiState().also { view = it; state = it }
  }

  private var forums: List<Forum> = emptyList()
  private var selectedForum: Forum? = null
  private var selectedIndex: Int = -1

  override fun onCreate(args: Bundle) {
    super.onCreate(args)

    NMB_DB.liveForums.observable
        .map { it.toList() }
        .observeOn(AndroidSchedulers.mainThread())
        .register({ onUpdateForums(it) }, { /* Ignore error */ })
  }

  private fun onUpdateForums(forums: List<Forum>) {
    this.forums = forums

    val oldSelectedForum = selectedForum

    selectedForum = null
    selectedIndex = INVALID_INDEX

    // Try to find the same selected forum in the new forums
    if (oldSelectedForum != null) {
      for ((index, item) in forums.withIndex()) {
        if (item.id == oldSelectedForum.id) {
          selectedForum = item
          selectedIndex = index
          break
        }
      }
    }

    // Select the first forum
    if (selectedForum == null && forums.isNotEmpty()) {
      selectedForum = forums[0]
      selectedIndex = 0
    }

    view.updateForums(forums)
    onSelectForum(selectedForum, false)
  }

  override fun onClickForum(forum: Forum, index: Int) {
    val oldSelectedIndex = selectedIndex
    selectedForum = forum
    selectedIndex = index
    if (oldSelectedIndex != index) {
      view.updateSelectedIndex(oldSelectedIndex, index)
      onSelectForum(forum, true)
    }
  }

  override fun getSelectedIndex() = selectedIndex

  abstract fun onSelectForum(forum: Forum?, byUser: Boolean)
}
