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

import android.graphics.drawable.Drawable
import com.hippo.nimingban.component.MvpUi
import com.hippo.viewstate.GenerateViewState
import com.hippo.viewstate.strategy.SingleByMethod
import com.hippo.viewstate.strategy.SingleByTag
import com.hippo.viewstate.strategy.StrategyType

/*
 * Created by Hippo on 2017/7/13.
 */

@GenerateViewState
interface ToolbarUi : MvpUi {

  companion object {
    private const val TAG_SET_TITLE = "ToolbarUi:set_title"
    private const val TAG_SET_SUBTITLE = "ToolbarUi:set_subtitle"
    private const val TAG_SET_NAVIGATION_ICON = "ToolbarUi:set_navigation_icon"
  }

  @StrategyType(value = SingleByTag::class, tag = TAG_SET_TITLE)
  fun setTitle(title: CharSequence)

  @StrategyType(value = SingleByTag::class, tag = TAG_SET_TITLE)
  fun setTitle(resId: Int)

  @StrategyType(value = SingleByTag::class, tag = TAG_SET_SUBTITLE)
  fun setSubtitle(subtitle: CharSequence)

  @StrategyType(value = SingleByTag::class, tag = TAG_SET_SUBTITLE)
  fun setSubtitle(resId: Int)

  @StrategyType(value = SingleByTag::class, tag = TAG_SET_NAVIGATION_ICON)
  fun setNavigationIcon(icon: Drawable)

  @StrategyType(value = SingleByTag::class, tag = TAG_SET_NAVIGATION_ICON)
  fun setNavigationIcon(resId: Int)

  @StrategyType(value = SingleByMethod::class)
  fun inflateMenu(resId: Int)

  @StrategyType(value = SingleByMethod::class)
  fun enableDoubleClick()
}
