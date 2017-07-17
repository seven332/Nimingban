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
import com.hippo.nimingban.client.data.Reference
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread

/*
 * Created by Hippo on 2017/7/17.
 */

fun replies(thread: Thread, forum: String?): RepliesScene {
  val args = Bundle()
  args.putParcelable(RepliesScene.KEY_THREAD, thread)
  args.putString(RepliesScene.KEY_REPLY_ID, thread.id)
  args.putString(RepliesScene.KEY_FORUM, thread.forum ?: forum)
  val scene = RepliesScene()
  scene.setArgs(args)
  return scene
}

fun gallery(image: String?): GalleryScene {
  val args = Bundle()
  args.putString(GalleryScene.KEY_IMAGE, image)
  val scene = GalleryScene()
  scene.setArgs(args)
  return scene
}

fun gallery(thread: Thread) = galleryNmb(thread.image)

fun gallery(reply: Reply)= galleryNmb(reply.image)

fun gallery(reference: Reference) = galleryNmb(reference.image)

private fun galleryNmb(nmbImage: String?): GalleryScene {
  val args = Bundle()
  args.putString(GalleryScene.KEY_NMB_IMAGE, nmbImage)
  val scene = GalleryScene()
  scene.setArgs(args)
  return scene
}
