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

package com.hippo.nimingban.scene.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.widget.nmb.NmbImage

/*
 * Created by Hippo on 6/15/2017.
 */

class GalleryUi(
    val reply: Reply?,
    val logic: GalleryLogic,
    context: android.content.Context,
    activity: NmbActivity
) : NmbUi(context, activity) {

  private var image: NmbImage? = null

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.ui_gallery, container, false)

    val image = view.findViewById(R.id.image) as NmbImage
    image.loadImage(reply?.image)

    this.image = image

    return view
  }
}
