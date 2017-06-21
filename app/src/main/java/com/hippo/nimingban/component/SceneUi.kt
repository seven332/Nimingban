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

import android.support.annotation.CallSuper
import android.view.View
import com.hippo.nimingban.architecture.Ui
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins

/*
 * Created by Hippo on 6/19/2017.
 */

abstract class SceneUi : Ui {

  companion object {
    const val CREATE = 1
    const val ATTACH = 2
    const val START = 3
    const val RESUME = 4
    const val PAUSE = 5
    const val STOP = 6
    const val DETACH = 7
    const val DESTROY = 8
  }

  abstract val view: View

  private var step = CREATE
  private var lifecycleHandler: LifecycleHandler? = null
  val lifecycle by lazy {
    val lifecycleHandler = LifecycleHandler(step)
    this.lifecycleHandler = lifecycleHandler
    lifecycleHandler.observable
  }

  /**
   * Attaches the ui.
   */
  internal fun attach() {
    onAttach()
    step = ATTACH
    lifecycleHandler?.emit(step)
  }

  /**
   * Starts the ui.
   */
  internal fun start() {
    onStart()
    step = START
    lifecycleHandler?.emit(step)
  }

  /**
   * Resumes the ui.
   */
  internal fun resume() {
    onResume()
    step = RESUME
    lifecycleHandler?.emit(step)
  }

  /**
   * Pauses the ui.
   */
  internal fun pause() {
    onPause()
    step = PAUSE
    lifecycleHandler?.emit(step)
  }

  /**
   * Stops the ui.
   */
  internal fun stop() {
    onStop()
    step = STOP
    lifecycleHandler?.emit(step)
  }

  /**
   * Detaches the ui.
   */
  internal fun detach() {
    onDetach()
    step = DETACH
    lifecycleHandler?.emit(step)
  }

  /**
   * Destroys the ui.
   */
  internal fun destroy() {
    onDestroy()
    step = DESTROY
    lifecycleHandler?.emit(step)
  }

  /**
   * Called when the ui attached.
   */
  @CallSuper
  protected open fun onAttach() {}

  /**
   * Called when the ui started.
   */
  @CallSuper
  protected open fun onStart() {}

  /**
   * Called when the ui resumed.
   */
  @CallSuper
  protected open fun onResume() {}

  /**
   * Called when the ui paused.
   */
  @CallSuper
  protected open fun onPause() {}

  /**
   * Called when the ui stopped.
   */
  @CallSuper
  protected open fun onStop() {}

  /**
   * Called when the ui detached.
   */
  @CallSuper
  protected open fun onDetach() {}

  /**
   * Called when the ui destroyed.
   */
  @CallSuper
  protected open fun onDestroy() {}


  private class LifecycleHandler(var step: Int) {

    private val list = mutableListOf<ObservableEmitter<Int>>()

    val observable : Observable<Int> by lazy {
      Observable.create<Int> {
        list.add(it)
        it.setCancellable { list.remove(it) }
        emitMissingStep(it)
      }
    }

    private fun emitMissingStep(emitter: ObservableEmitter<Int>) {
      if (step in CREATE until DESTROY) emit(emitter, CREATE)
      if (step in ATTACH until DETACH) emit(emitter, ATTACH)
      if (step in START until STOP) emit(emitter, START)
      if (step in RESUME until PAUSE) emit(emitter, RESUME)
    }

    fun emit(step: Int) {
      this.step = step
      list.forEach { emit(it, step) }
    }

    private fun emit(emitter: ObservableEmitter<Int>, step: Int) {
      if (!emitter.isDisposed) {
        try {
          emitter.onNext(step)
        } catch (t: Throwable) {
          Exceptions.throwIfFatal(t)
          RxJavaPlugins.onError(t)
        }
      }
    }
  }
}
