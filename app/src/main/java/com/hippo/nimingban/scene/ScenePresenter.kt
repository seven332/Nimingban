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

/*
 * Created by Hippo on 5/11/2017.
 */

import android.support.annotation.CallSuper
import com.hippo.nimingban.architecture.PresenterInterface

/**
 * [PresenterInterface] for [com.hippo.stage.Scene].
 */
abstract class ScenePresenter<P: ScenePresenter<P, V>, V: SceneView<V, P>> : PresenterInterface<P, V> {

  override var view: V? = null

  /**
   * Creates this presenter.
   */
  fun create() {
    onCreate()
  }

  /**
   * Destroys this presenter.
   */
  fun destroy() {
    onDestroy()
  }

  /**
   * Called when the presenter created.
   */
  @CallSuper
  protected open fun onCreate() {}

  /**
   * Called when the presenter destroyed.
   */
  @CallSuper
  protected open fun onDestroy() {}

  open fun onCreateView(view: V) {}

  open fun onDestroyView(view: V) {}
}
