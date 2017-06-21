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

import android.view.MenuItem
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupLogic
import com.hippo.nimingban.component.paper.GalleryLogic
import com.hippo.nimingban.component.paper.GalleryUi
import com.hippo.nimingban.component.paper.SwipeBackLogic
import com.hippo.nimingban.component.paper.SwipeBackUi
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.nimingban.component.paper.ToolbarUi
import com.hippo.nimingban.component.paper.impl.DefaultGalleryLogic
import com.hippo.nimingban.component.paper.impl.DefaultSwipeBackLogic
import com.hippo.nimingban.component.paper.impl.DefaultToolbarLogic
import com.hippo.stage.Scene
import com.hippo.swipeback.SwipeBackLayout

/*
 * Created by Hippo on 6/21/2017.
 */

class GallerySceneLogicImpl(
    val scene: Scene
) : GroupLogic(), GallerySceneLogic {

  override var gallerySceneUi: GallerySceneUi? = null

  private val galleryLogic = DefaultGalleryLogic().also { addChild(it) }
  override var galleryUi: GalleryUi? = null

  private val toolbarLogic = ToolbarLogicImpl().also { addChild(it) }
  override var toolbarUi: ToolbarUi? = null
    set(value) {
      field = value
      toolbarLogic.toolbarUi = value
    }

  private val swipeBackLogic = DefaultSwipeBackLogic(scene).also { addChild(it) }
  override var swipeBackUi: SwipeBackUi? = null
    set(value) {
      field = value
      swipeBackLogic.swipeBackUi = value
    }

  init {
    swipeBackLogic.setSwipeEdge(SwipeBackLayout.EDGE_LEFT or SwipeBackLayout.EDGE_RIGHT)
  }

  override fun getGalleryLogic(): GalleryLogic = galleryLogic

  override fun getToolbarLogic(): ToolbarLogic = toolbarLogic

  override fun getSwipeBackLogic(): SwipeBackLogic = swipeBackLogic


  private inner class ToolbarLogicImpl: DefaultToolbarLogic() {

    init {
      setNavigationIcon(R.drawable.arrow_left_white_x24)
    }

    override fun onClickNavigationIcon() {
      scene.pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      TODO("not implemented")
    }
  }
}
