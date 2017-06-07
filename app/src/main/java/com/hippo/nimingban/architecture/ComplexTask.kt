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

package com.hippo.nimingban.architecture

/*
 * Created by Hippo on 6/5/2017.
 */

// TODO Add cancel(). How cancel() affect restarting?

/**
 * `ComplexTask` is an expansive task. It not suitable to run in UI thread.
 */
abstract class ComplexTask<Param, Progress, Result> {

  companion object {
    @JvmField val STATE_NONE = 0
    @JvmField val STATE_RUNNING = 1
    @JvmField val STATE_SUCCESS = 2
    @JvmField val STATE_FAILURE = 3
  }

  var state = STATE_NONE
    private set

  var progress: Progress? = null
    private set

  var result: Result? = null
    private set

  var error: Throwable? = null
    private set

  /**
   * Starts the task. If the task is running, throw [AssertionError].
   */
  fun start(param: Param) {
    assert(state != STATE_RUNNING) { "Can't start a running task" }

    state = STATE_RUNNING
    result = null
    error = null
    onStart(param)
  }

  /**
   * Reports progress of the task. If the task isn't running, throw [AssertionError].
   */
  fun progress(progress: Progress) {
    assert(state == STATE_RUNNING) { "Can only call progress() on running task" }

    this.progress = progress
    onProgress(progress)
  }

  /**
   * Reports success of the task. It ends the task.
   * If the task isn't running, throw [AssertionError].
   */
  fun success(result: Result) {
    assert(state == STATE_RUNNING) { "Can only call success() on running task" }

    this.state = STATE_SUCCESS
    this.progress = null
    this.result = result
    onSuccess(result)
  }

  /**
   * Reports failure of the task. It ends the task.
   * If the task isn't running, throw [AssertionError].
   */
  fun failure(e: Throwable) {
    assert(state == STATE_RUNNING) { "Can only call failure() on running task" }

    this.state = STATE_FAILURE
    this.progress = null
    this.error = e
    onFailure(e)
  }

  /**
   * Called when the task starts.
   */
  open fun onStart(param: Param) {}

  /**
   * Called when reporting the progress of the task.
   */
  open fun onProgress(progress: Progress) {}

  /**
   * Called when the task succeeds.
   */
  open fun onSuccess(result: Result) {}

  /**
   * Called when the task fails.
   */
  open fun onFailure(e: Throwable) {}
}
