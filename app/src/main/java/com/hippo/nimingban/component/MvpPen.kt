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
import android.support.annotation.CallSuper
import android.util.Log
import com.hippo.viewstate.ViewState

/*
 * Created by Hippo on 2017/7/14.
 */

abstract class MvpPen<Ui : Any> : MvpLogic<Ui> {

  lateinit var view: Ui
    protected set

  protected lateinit var state: ViewState<Ui>

  override fun attach(ui: Ui) {
    state.attach(ui)
  }

  /**
   * Detaches the paper of the pen.
   */
  override fun detach() {
    state.detach()
  }

  internal open fun print() {
    Log.d("TAG", this::class.java.name)
  }

  /**
   * Creates the pen.
   */
  internal fun create(args: Bundle) {
    onCreate(args)
  }

  /**
   * Destroys the pen.
   */
  internal fun destroy() {
    onDestroy()
  }

  /**
   * Updates the arguments of the pen.
   */
  internal fun updateArgs(args: Bundle) {
    onUpdateArgs(args)
  }

  /**
   * Called when the pen created.
   */
  @CallSuper
  protected open fun onCreate(args: Bundle) {}

  /**
   * Called when the pen destroyed.
   */
  @CallSuper
  protected open fun onDestroy() {}

  /**
   * Called when updating the arguments of the pen.
   */
  @CallSuper
  protected open fun onUpdateArgs(args: Bundle) {}
}
