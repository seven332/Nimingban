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
import com.hippo.nimingban.NMB_APP
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupLogic
import com.hippo.nimingban.component.paper.ForumsLogic
import com.hippo.nimingban.component.paper.ToolbarLogic
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/22/2017.
 */

class ForumsSceneLogic(
    val scene: Scene
) : GroupLogic() {

  val toolbarLogic: ToolbarLogic = ForumsToolbarLogic().also { addChild(it) }
  val forumsLogic: ForumsLogic = ForumsLogic().also { addChild(it) }


  private inner class ForumsToolbarLogic : ToolbarLogic() {

    init {
      setTitle(NMB_APP.getString(R.string.forums_title))
      setNavigationIcon(R.drawable.arrow_left_white_x24)
    }

    override fun onClickNavigationIcon() {
      scene.pop()
    }

    override fun onClickMenuItem(item: MenuItem): Boolean {
      // TODO("not implemented")
      return false
    }
  }
}
