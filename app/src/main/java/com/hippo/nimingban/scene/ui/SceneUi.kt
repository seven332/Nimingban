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

import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.architecture.Ui
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins

/*
 * Created by Hippo on 6/12/2017.
 */

abstract class SceneUi : Ui {

  companion object {
    private const val INIT = 0
    const val CREATE = 1
    const val ATTACH = 2
    const val START = 3
    const val RESUME = 4
    const val PAUSE = 5
    const val STOP = 6
    const val DETACH = 7
    const val DESTROY = 8
  }

  private var step = INIT

  private var lifecycleInternal: Lifecycle? = null

  val lifecycle by lazy {
    val lifecycle = Lifecycle(step)
    lifecycleInternal = lifecycle
    lifecycle.observable
  }

  private var view: View? = null

  fun create(inflater: LayoutInflater, container: ViewGroup): View {
    val view = onCreate(inflater, container)
    this.view = view

    step = CREATE
    lifecycleInternal?.emit(step)

    return view
  }

  fun attach() {
    onAttach()

    step = ATTACH
    lifecycleInternal?.emit(step)
  }

  fun start() {
    onStart()

    step = START
    lifecycleInternal?.emit(step)
  }

  fun resume() {
    onResume()

    step = RESUME
    lifecycleInternal?.emit(step)
  }

  fun pause() {
    onPause()

    step = PAUSE
    lifecycleInternal?.emit(step)
  }

  fun stop() {
    onStop()

    step = STOP
    lifecycleInternal?.emit(step)
  }

  fun detach() {
    onDetach()

    step = DETACH
    lifecycleInternal?.emit(step)
  }

  fun destroy() {
    onDestroy()

    step = DESTROY
    lifecycleInternal?.emit(step)

    view = null
  }

  abstract fun onCreate(inflater: LayoutInflater, container: ViewGroup): View

  @CallSuper
  open fun onAttach() {}

  @CallSuper
  open fun onStart() {}

  @CallSuper
  open fun onResume() {}

  @CallSuper
  open fun onPause() {}

  @CallSuper
  open fun onStop() {}

  @CallSuper
  open fun onDetach() {}

  @CallSuper
  open fun onDestroy() {}


  private class Lifecycle(var step: Int) {

    private val list = mutableListOf<ObservableEmitter<Int>>()

    val observable by lazy {
      Observable.create<Int> {
        list.add(it)
        it.setCancellable { list.remove(it) }
        emitMissingStep(it)
      } !!
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
