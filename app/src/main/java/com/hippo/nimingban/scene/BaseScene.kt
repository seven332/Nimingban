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
import com.hippo.stage.Scene

/*
 * Created by Hippo on 6/5/2017.
 */

abstract class BaseScene<P: ScenePresenter<P, V>, V: SceneView<V, P>> : Scene() {

  var presenter: P? = null
    private set

  var view: V? = null
    private set

  /**
   * Create a presenter.
   */
  protected abstract fun createPresenter(): P

  /**
   * Create a view.
   */
  protected abstract fun createView(): V

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
    presenter = createPresenter()
    onCreatePresenter(presenter!!)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val presenter: P = this.presenter!!

    val view = createView()
    this.view = view
    onCreateView(view)
    view.create(presenter, inflater, container)

    presenter.view = view

    view.restoring = true
    presenter.restore(view)
    view.restoring = false

    return view.view!!
  }

  override fun onAttachView(view: View) {
    super.onAttachView(view)
    this.view!!.attach()
  }

  override fun onStart() {
    super.onStart()
    this.view!!.start()
  }

  override fun onResume() {
    super.onResume()
    this.view!!.resume()
  }

  override fun onPause() {
    super.onPause()
    this.view!!.pause()
  }

  override fun onStop() {
    super.onStop()
    this.view!!.stop()
  }

  override fun onDetachView(view: View) {
    super.onDetachView(view)
    this.view!!.detach()
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    this.view!!.destroy()
    this.view = null
    this.presenter!!.view = null
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter!!.destroy()
    presenter = null
  }

  /**
   * Called after the presenter created.
   */
  @CallSuper
  protected fun onCreatePresenter(presenter: P) {}

  /**
   * Called after the SceneView created.
   */
  @CallSuper
  protected fun onCreateView(view: V) {}
}
