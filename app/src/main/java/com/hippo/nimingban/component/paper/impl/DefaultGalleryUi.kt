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

package com.hippo.nimingban.component.paper.impl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.component.NmbUi
import com.hippo.nimingban.component.paper.GalleryLogic
import com.hippo.nimingban.component.paper.GalleryUi
import com.hippo.nimingban.util.find
import com.hippo.nimingban.widget.nmb.NmbImage

/*
 * Created by Hippo on 6/21/2017.
 */

class DefaultGalleryUi(
    val logic: GalleryLogic,
    val reply: Reply?,
    inflater: LayoutInflater,
    container: ViewGroup
) : NmbUi(), GalleryUi {

  override val view: View
  private val image: NmbImage

  init {
    view = inflater.inflate(R.layout.ui_gallery, container, false)
    image = view.find(R.id.image)
    image.loadImage(reply?.image)
  }
}
