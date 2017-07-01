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

package com.hippo.nimingban.widget.nmb

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.view.View
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.GenericDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.hippo.nimingban.R
import com.hippo.nimingban.drawable.TextDrawable
import com.hippo.nimingban.util.attrColor

/*
 * Created by Hippo on 6/9/2017.
 */

class NmbThumb : GenericDraweeView {

  private var isStarted = false

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

  init {
    val failure = TextDrawable("(;´Д`)", 0.8f)
    failure.backgroundColor = context.attrColor(R.attr.backgroundColorStatusBar)
    failure.textColor = context.attrColor(android.R.attr.textColorTertiary)
    hierarchy.setFailureImage(failure)
    hierarchy.setRetryImage(failure.constantState.newDrawable())
  }

  fun start() {
    isStarted = true
    controller?.animatable?.start()
  }

  fun stop() {
    isStarted = false
    controller?.animatable?.stop()
  }

  // TODO get image url from api
  fun loadThumb(thumb: String?) =
      load(if (thumb.isNullOrEmpty()) null else "http://img6.nimingban.com/thumb/" + thumb)

  fun load(url: String?) {
    if (url.isNullOrEmpty()) {
      visibility = View.GONE
      return
    } else {
      visibility = View.VISIBLE
    }

    val controller = Fresco.newDraweeControllerBuilder()
        .setOldController(controller)
        .setTapToRetryEnabled(true)
        .setUri(url)
        .setControllerListener(object : BaseControllerListener<ImageInfo>() {
          override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
            if (isStarted) animatable?.start()
          }
        })
        .build()
    setController(controller)
  }
}
