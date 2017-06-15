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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.swipeback.SwipeBackLayout

/*
 * Created by Hippo on 6/15/2017.
 */

class SwipeBackUi(
    val child: SceneUi,
    val logic: SwipeBackLogic,
    context: android.content.Context,
    activity: NmbActivity
) : GroupUi(context, activity) {

  private var swipeBack: SwipeBackLayout? = null

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.ui_swipe_back, container, false)

    val childContainer = view.findViewById(R.id.swipe_back_container) as ViewGroup
    val childView = child.create(inflater, childContainer)
    childContainer.addView(childView, 0)
    addChild(child)

    val swipeBack = view.findViewById(R.id.swipe_back) as SwipeBackLayout
    // TODO Get swipe edge from settings
    swipeBack.swipeEdge = SwipeBackLayout.EDGE_LEFT or SwipeBackLayout.EDGE_RIGHT
    swipeBack.addSwipeListener(object : SwipeBackLayout.SwipeListener {
      override fun onSwipe(percent: Float) {}
      override fun onStateChange(edge: Int, state: Int) {}
      override fun onSwipeOverThreshold() {}
      override fun onFinish() {
        schedule { logic.onFinishUi() }
      }
    })

    this.swipeBack = swipeBack

    return view
  }
}

/** Wrap a ui in a SwipeBackUi **/
fun SceneUi.wrapInSwipeBack(logic: SwipeBackLogic, context: Context, activity: NmbActivity) =
    SwipeBackUi(this, logic, context, activity)

/** Wrap a ui in a SwipeBackUi **/
fun NmbUi.wrapInSwipeBack(logic: SwipeBackLogic) = SwipeBackUi(this, logic, this.context, this.activity)
