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

import android.content.Context
import com.hippo.nimingban.activity.NmbActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/*
 * Created by Hippo on 6/7/2017.
 */

abstract class NmbView<V: SceneView<V, P>, P: ScenePresenter<P, V>>(val activity: NmbActivity, val context: Context) :
    SceneView<V, P>() {

  private val worker = AndroidSchedulers.mainThread().createWorker()

  override fun onDestroy() {
    super.onDestroy()
    worker.dispose()
  }

  /**
   * Schedules an action for execution in UI thread.
   * The action will be cancelled after the view destroyed.
   * Returns `Disposables.disposed()` if the view is already destroyed.
   */
  fun schedule(action: () -> Unit): Disposable {
    return worker.schedule(action)
  }

  /**
   * Schedules an action for execution at some point in the future
   * and in UI thread.
   * The action will be cancelled after the view detached.
   * Returns `Disposables.disposed()` if the view is already destroyed.
   */
  fun schedule(action: () -> Unit, delayMillis: Long): Disposable {
    return worker.schedule(action, delayMillis, TimeUnit.MILLISECONDS)
  }
}
