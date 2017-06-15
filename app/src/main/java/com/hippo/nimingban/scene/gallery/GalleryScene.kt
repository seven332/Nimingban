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

package com.hippo.nimingban.scene.gallery

import android.os.Bundle
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.scene.NmbScene
import com.hippo.nimingban.scene.ui.GalleryUi
import com.hippo.nimingban.scene.ui.SceneUi
import com.hippo.nimingban.scene.ui.wrapInSwipeBack
import com.hippo.nimingban.scene.ui.wrapInToolbar

/*
 * Created by Hippo on 6/15/2017.
 */

class GalleryScene : NmbScene(), GallerySceneLogic {

  companion object {
    const val KEY_REPLY = "GalleryScene:reply"
  }

  private var reply: Reply? = null

  override fun createUi(): SceneUi = GalleryUi(reply, this, context!!, activity as NmbActivity)
      .wrapInToolbar(this).wrapInSwipeBack(this)

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)

    opacity = TRANSLUCENT

    reply = args?.getParcelable(KEY_REPLY)
  }

  override fun onFinishUi() { pop() }
}

fun Reply.galleryScene(): GalleryScene {
  val args = Bundle()
  args.putParcelable(GalleryScene.KEY_REPLY, this)
  val scene = GalleryScene()
  scene.args = args
  return scene
}
