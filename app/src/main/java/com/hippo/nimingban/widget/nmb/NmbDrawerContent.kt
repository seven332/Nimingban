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
import android.util.AttributeSet
import com.hippo.drawerlayout.DrawerLayoutChild
import com.hippo.stage.StageLayout

/*
 * Created by Hippo on 6/4/2017.
 */

class NmbDrawerContent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : StageLayout(context, attrs, defStyleAttr), DrawerLayoutChild {

  private var windowPaddingTop: Int = 0
  private var windowPaddingBottom: Int = 0
  private val listeners = mutableListOf<OnGetWindowPaddingTopListener>()

  override fun onGetWindowPadding(top: Int, bottom: Int) {
    windowPaddingTop = top
    windowPaddingBottom = bottom

    for (listener in listeners.toList()) {
      listener.onGetWindowPaddingTop(top)
    }
  }

  override fun getAdditionalBottomMargin(): Int {
    return 0
  }

  override fun getAdditionalTopMargin(): Int {
    return windowPaddingBottom
  }

  /**
   * Register a [OnGetWindowPaddingTopListener].
   * The [OnGetWindowPaddingTopListener.onGetWindowPaddingTop] will be called at once.
   */
  fun addOnGetWindowPaddingTopListener(listener: OnGetWindowPaddingTopListener?) {
    if (listener != null) {
      listener.onGetWindowPaddingTop(windowPaddingTop)
      listeners.add(listener)
    }
  }

  /**
   * Remove a [OnGetWindowPaddingTopListener].
   */
  fun removeOnGetWindowPaddingTopListener(listener: OnGetWindowPaddingTopListener?) {
    if (listener != null) {
      listeners.remove(listener)
    }
  }

  /**
   * The callback to get window padding top.
   */
  interface OnGetWindowPaddingTopListener {
    fun onGetWindowPaddingTop(top: Int)
  }
}
