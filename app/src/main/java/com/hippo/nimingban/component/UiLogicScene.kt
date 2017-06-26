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

package com.hippo.nimingban.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/19/2017.
 */

abstract class UiLogicScene : Scene() {

  protected var logic: SceneLogic? = null
    private set
  protected var ui: SceneUi? = null
    private set

  private var savedInstanceState: Bundle? = null

  /**
   * Create a logic for this scene.
   */
  abstract fun createLogic(args: Bundle?): SceneLogic

  /**
   * Create a ui for the scene.
   */
  abstract fun createUi(inflater: LayoutInflater, container: ViewGroup): SceneUi

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
    this.logic = createLogic(args)
    if (savedInstanceState != null) {
      this.logic!!.restoreState(savedInstanceState!!)
      savedInstanceState = null
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val ui = createUi(inflater, container)
    this.ui = ui
    return ui.view
  }

  override fun onAttachView(view: View) {
    super.onAttachView(view)
    this.ui?.attach() ?: error("Ui is null in onAttachView(). It should not be null.")
  }

  override fun onStart() {
    super.onStart()
    this.ui?.start() ?: error("Ui is null in onStart(). It should not be null.")
  }

  override fun onResume() {
    super.onResume()
    this.ui?.resume() ?: error("Ui is null in onResume(). It should not be null.")
  }

  override fun onPause() {
    super.onPause()
    this.ui?.pause() ?: error("Ui is null in onPause(). It should not be null.")
  }

  override fun onStop() {
    super.onStop()
    this.ui?.stop() ?: error("Ui is null in onStop(). It should not be null.")
  }

  override fun onDetachView(view: View) {
    super.onDetachView(view)
    this.ui?.detach() ?: error("Ui is null in onDetachView(). It should not be null.")
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    this.ui?.destroy() ?: error("Ui is null in onDestroy(). It should not be null.")
    this.ui = null
  }

  override fun onDestroy() {
    super.onDestroy()
    this.logic?.destroy() ?: error("Logic is null in onDestroy(). It should not be null.")
    this.logic = null
  }

  override fun onSaveViewState(view: View, outState: Bundle) {
    super.onSaveViewState(view, outState)
    this.ui?.saveState(outState) ?: error("Ui is null in onSaveViewState(). It should not be null.")
  }

  override fun onRestoreViewState(view: View, savedViewState: Bundle) {
    super.onRestoreViewState(view, savedViewState)
    this.ui?.restoreState(savedViewState) ?: error("Ui is null in onRestoreViewState(). It should not be null.")
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    this.logic?.saveState(outState) ?: error("Logic is null in onSaveInstanceState(). It should not be null.")
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    this.savedInstanceState = savedInstanceState
  }
}
