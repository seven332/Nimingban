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
import android.net.Uri
import android.util.AttributeSet
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.GenericDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.hippo.nimingban.R
import com.hippo.nimingban.drawable.TextDrawable
import com.hippo.nimingban.util.attrColor

/*
 * Created by Hippo on 6/14/2017.
 */

class NmbCover : GenericDraweeView {

  companion object {
    private const val COVER_URL = "http://cover.acfunwiki.org/cover.php"
  }

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

  init {
    val failure = TextDrawable("(;´Д`)", 0.8f)
    failure.backgroundColor = context.attrColor(R.attr.backgroundColorStatusBar)
    failure.textColor = context.attrColor(android.R.attr.textColorTertiary)
    hierarchy.setFailureImage(failure)
    hierarchy.setRetryImage(failure.constantState.newDrawable())

    val imageRequest = ImageRequestBuilder
        .newBuilderWithSource(Uri.parse(COVER_URL))
        .disableDiskCache()
        .build()

    val controller = Fresco.newDraweeControllerBuilder()
        .setOldController(controller)
        .setTapToRetryEnabled(true)
        .setAutoPlayAnimations(true)
        .setImageRequest(imageRequest)
        .build()
    setController(controller)
  }
}
