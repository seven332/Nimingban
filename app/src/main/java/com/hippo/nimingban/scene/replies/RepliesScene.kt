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

package com.hippo.nimingban.scene.replies

import android.os.Bundle
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.scene.NmbScene

/*
 * Created by Hippo on 6/11/2017.
 */

class RepliesScene: NmbScene<RepliesPresenter, RepliesView>() {

  companion object {
    const val KEY_ID = "RepliesScene:id"
    const val KEY_THREAD = "RepliesScene:thread"
  }


  override fun createPresenter(): RepliesPresenter {
    val args = this.args
    val id: String?
    val thread: Thread?

    if (args != null) {
      id = args.getString(KEY_ID)
      thread = args.getParcelable(KEY_THREAD)
    } else {
      id = null
      thread = null
    }

    return RepliesPresenter(id, thread)
  }

  override fun createView(): RepliesView {
    return RepliesView(this, activity as NmbActivity, context!!)
  }
}


fun newRepliesScene(thread: Thread): RepliesScene {
  val args = Bundle()
  args.putParcelable(RepliesScene.KEY_THREAD, thread)
  val scene = RepliesScene()
  scene.args = args
  return scene
}
