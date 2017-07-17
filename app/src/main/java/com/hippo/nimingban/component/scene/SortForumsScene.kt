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

import android.os.Bundle
import android.view.MenuItem
import com.hippo.nimingban.R
import com.hippo.nimingban.component.MvpPaper
import com.hippo.nimingban.component.MvpPen
import com.hippo.nimingban.component.NmbScene
import com.hippo.nimingban.component.paper.SortForumsPen
import com.hippo.nimingban.component.paper.ToolbarPaper
import com.hippo.nimingban.component.paper.ToolbarPen
import com.hippo.nimingban.component.paper.papers
import com.hippo.nimingban.component.paper.pens
import com.hippo.nimingban.component.paper.sortForums
import com.hippo.nimingban.component.paper.toolbar

/*
 * Created by Hippo on 2017/7/16.
 */

class SortForumsScene : NmbScene() {

  private val toolbar: ToolbarPen = object : ToolbarPen() {

    override fun onCreate(args: Bundle) {
      super.onCreate(args)
      view.setTitle(R.string.sort_forums_title)
      view.setNavigationIcon(R.drawable.arrow_left_white_x24)
    }

    override fun onClickNavigationIcon() {
      pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      return false
    }

    override fun onDoubleClick() {}
  }

  private val sortForums: SortForumsPen = object : SortForumsPen() {}

  private val pen = pens(toolbar, sortForums)

  override fun createPen(): MvpPen<*> = pen

  override fun createPaper(): MvpPaper<*> = papers(pen) {
    toolbar(toolbar, it) {
      sortForums(sortForums, ToolbarPaper.CONTAINER_ID)
    }
  }
}
