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

package com.hippo.nimingban.scene

import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.architecture.ViewInterface

/*
 * Created by Hippo on 6/5/2017.
 */

/**
 * [ViewInterface] for [com.hippo.stage.Scene].
 */
abstract class SceneView<V: SceneView<V, P>, P: ScenePresenter<P, V>> : ViewInterface<V, P> {

  override var presenter: P? = null

  var view: View? = null

  var restoring: Boolean = false
    internal set

  /**
   * Creates the view.
   */
  fun create(presenter: P, inflater: LayoutInflater, parent: ViewGroup) {
    this.presenter = presenter
    this.view = onCreate(inflater, parent)
  }

  /**
   * Attaches this view.
   */
  fun attach() {
    onAttach()
  }

  /**
   * Starts this view.
   */
  fun start() {
    onStart()
  }

  /**
   * Resumes this view.
   */
  fun resume() {
    onResume()
  }

  /**
   * Pauses this view.
   */
  fun pause() {
    onPause()
  }

  /**
   * Stops this view.
   */
  fun stop() {
    onStop()
  }

  /**
   * Detaches this view.
   */
  fun detach() {
    onDetach()
  }

  /**
   * Destroys this view.
   */
  fun destroy() {
    onDestroy()
  }

  /**
   * Saves state for this view.
   */
  fun saveState(outState: Bundle) {
    onSaveState(outState)
  }

  /**
   * Restores state for this view.
   */
  fun restoreState(savedViewState: Bundle) {
    onRestoreState(savedViewState)
  }

  /**
   * Creates this actual `View`.
   */
  protected abstract fun onCreate(inflater: LayoutInflater, parent: ViewGroup): View

  /**
   * Called when the view attached.
   */
  @CallSuper
  protected open fun onAttach() {}

  /**
   * Called when the view started.
   */
  @CallSuper
  protected open fun onStart() {}

  /**
   * Called when the view resumed.
   */
  @CallSuper
  protected open fun onResume() {}

  /**
   * Called when the view paused.
   */
  @CallSuper
  protected open fun onPause() {}

  /**
   * Called when the view stopped.
   */
  @CallSuper
  protected open fun onStop() {}

  /**
   * Called when the view detached.
   */
  @CallSuper
  protected open fun onDetach() {}

  /**
   * Called when the view destroyed.
   */
  @CallSuper
  protected open fun onDestroy() {}

  /**
   * Called when saving state of the view.
   */
  @CallSuper
  protected open fun onSaveState(outState: Bundle) {}

  /**
   * Called when restoring state of the view.
   */
  @CallSuper
  protected open fun onRestoreState(savedViewState: Bundle) {}
}
