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
import com.hippo.nimingban.component.GroupUi
import com.hippo.nimingban.component.SceneUi
import com.hippo.nimingban.component.paper.SwipeBackLogic
import com.hippo.nimingban.component.paper.SwipeBackUi
import com.hippo.nimingban.util.find
import com.hippo.swipeback.SwipeBackLayout

/*
 * Created by Hippo on 6/21/2017.
 */

class DefaultSwipeBackUi(
    val logic: SwipeBackLogic,
    inflater: LayoutInflater,
    container: ViewGroup
) : GroupUi(), SwipeBackUi {

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
  }

  override fun setSwipeEdge(edge: Int) {
    swipeBack.swipeEdge = edge
  }

  fun <T : SceneUi> addChild(creator: (ViewGroup) -> T) =
      inflateChild(creator, R.id.swipe_back_container, 0)
}

inline fun defaultSwipeBackUi(
    logic: SwipeBackLogic,
    inflater: LayoutInflater,
    container: ViewGroup,
    init: DefaultSwipeBackUi.() -> Unit
): DefaultSwipeBackUi {
  val defaultSwipeBackUi = DefaultSwipeBackUi(logic, inflater, container)
  defaultSwipeBackUi.init()
  return defaultSwipeBackUi
}
