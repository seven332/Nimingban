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
import android.os.Bundle
import com.hippo.nimingban.activity.NmbActivity

/*
 * Created by Hippo on 6/15/2017.
 */

abstract class GroupUi(
    context: Context,
    activity: NmbActivity
) : NmbUi(context, activity) {

  private val children = mutableListOf<SceneUi>()

  /**
   * Add a child to this group. The group will handle all lifecycle of children except 'create'.
   */
  fun addChild(ui: SceneUi) {
    children.add(ui)
  }

  override fun onAttach() {
    super.onAttach()
    children.forEach { it.attach() }
  }

  override fun onStart() {
    super.onStart()
    children.forEach { it.start() }
  }

  override fun onResume() {
    super.onResume()
    children.forEach { it.resume() }
  }

  override fun onPause() {
    super.onPause()
    children.forEach { it.pause() }
  }

  override fun onStop() {
    super.onStop()
    children.forEach { it.stop() }
  }

  override fun onDetach() {
    super.onDetach()
    children.forEach { it.detach() }
  }

  override fun onDestroy() {
    super.onDestroy()
    children.forEach { it.destroy() }
  }

  override fun onSaveState(outState: Bundle) {
    super.onSaveState(outState)
    children.forEach { it.saveState(outState) }
  }

  override fun onRestoreState(savedViewState: Bundle) {
    super.onRestoreState(savedViewState)
    children.forEach { it.restoreState(savedViewState) }
  }
}
