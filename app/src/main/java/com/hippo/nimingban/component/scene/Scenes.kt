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
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread

/*
 * Created by Hippo on 6/24/2017.
 */

fun Thread.repliesScene(): RepliesScene {
  val args = Bundle()
  args.putParcelable(RepliesScene.KEY_THREAD, this)
  args.putString(RepliesScene.KEY_FORUM, forum)
  val scene = RepliesScene()
  scene.args = args
  return scene
}

fun Reply.galleryScene(): GalleryScene {
  val args = Bundle()
  args.putParcelable(GalleryScene.KEY_REPLY, this)
  val scene = GalleryScene()
  scene.args = args
  return scene
}

fun sendScene(thread: Thread, presetContent: String?): SendScene {
  val args = Bundle()
  args.putParcelable(SendScene.KEY_THREAD, thread)
  args.putString(SendScene.KEY_PRESET_CONTENT, presetContent)
  val scene = SendScene()
  scene.args = args
  return scene
}

fun Forum.sendScene(): SendScene {
  val args = Bundle()
  args.putParcelable(SendScene.KEY_FORUM, this)
  val scene = SendScene()
  scene.args = args
  return scene
}
