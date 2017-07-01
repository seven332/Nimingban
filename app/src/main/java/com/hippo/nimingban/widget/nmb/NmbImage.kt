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
import android.graphics.Color
import android.util.AttributeSet
import com.hippo.fresco.large.FrescoLarge
import com.hippo.fresco.large.LargeDraweeView
import com.hippo.nimingban.R
import com.hippo.nimingban.drawable.ProgressDrawable
import com.hippo.nimingban.drawable.TextDrawable
import com.hippo.nimingban.util.attrColor
import com.hippo.nimingban.util.dp2pix

/*
 * Created by Hippo on 6/15/2017.
 */

class NmbImage : LargeDraweeView {

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

  init {
    val failure = TextDrawable("(;´Д`)", 0.8f)
    failure.backgroundColor = Color.TRANSPARENT
    failure.textColor = context.attrColor(android.R.attr.textColorTertiary)

    val progress = ProgressDrawable()
    progress.color = context.attrColor(R.attr.colorAccent)
    progress.size = 48.dp2pix(context)
    progress.indeterminate = true

    hierarchy.setProgressBarImage(progress)
    hierarchy.setFailureImage(failure)
    hierarchy.setRetryImage(failure.constantState.newDrawable())
    hierarchy.fadeDuration = 0
  }

  // TODO get image url from api
  fun loadImage(image: String?) =
      load(if (image.isNullOrEmpty()) null else "http://img6.nimingban.com/image/" + image)

  fun load(url: String?) {
    val controller = FrescoLarge.newDraweeControllerBuilder()
        .setOldController(controller)
        .setTapToRetryEnabled(true)
        .setAutoPlayAnimations(true)
        .setUri(url)
        .build()
    setController(controller)
  }
}
