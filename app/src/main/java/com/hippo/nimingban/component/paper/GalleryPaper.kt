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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.component.NmbPaper
import com.hippo.nimingban.util.find
import com.hippo.nimingban.widget.nmb.NmbImage

/*
 * Created by Hippo on 2017/7/18.
 */

class GalleryPaper(
    logic: GalleryLogic
) : NmbPaper<GalleryPaper>(logic), GalleryUi {

  private lateinit var image: NmbImage

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)

    view = inflater.inflate(R.layout.paper_gallery, container, false)
    image = view.find(R.id.image)
  }

  override fun setImage(url: String?) {
    image.loadImage(url)
  }

  override fun setNmbImage(nmbImage: String?) {
    image.loadNmbImage(nmbImage)
  }
}
