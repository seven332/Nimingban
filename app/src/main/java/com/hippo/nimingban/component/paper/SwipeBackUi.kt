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
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.util.find
import com.hippo.swipeback.SwipeBackLayout

/*
 * Created by Hippo on 6/21/2017.
 */

class SwipeBackUi(
    val logic: SwipeBackLogic,
    override val inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi() {

  companion object {
    const val CONTAINER_ID = R.id.swipe_back_container
  }

  override val view: View
  private val swipeBack: SwipeBackLayout

  init {
    view = inflater.inflate(R.layout.ui_swipe_back, container, false)
    swipeBack = view.find<SwipeBackLayout>(R.id.swipe_back)

    swipeBack.addSwipeListener(object : SwipeBackLayout.SwipeListener {
      override fun onFinish() {
        logic.onFinishUi()
      }
      override fun onSwipe(percent: Float) {}
      override fun onSwipeOverThreshold() {}
      override fun onStateChange(edge: Int, state: Int) {}
    })

    // Bind the ui to logic
    logic.swipeBackUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    // Unbind the ui from logic
    logic.swipeBackUi = null
  }

  fun setSwipeEdge(edge: Int) {
    swipeBack.swipeEdge = edge
  }
}
