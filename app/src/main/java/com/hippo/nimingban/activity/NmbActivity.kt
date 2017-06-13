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

package com.hippo.nimingban.activity

import android.support.design.widget.Snackbar
import android.view.ViewGroup
import com.hippo.nimingban.R
import com.hippo.nimingban.scene.threads.ThreadsScene
import com.hippo.stage.Scene
import kotlinx.android.synthetic.main.activity_nmb.*

/*
 * Created by Hippo on 6/4/2017.
 */

class NmbActivity : StageActivity() {

  private val coordinatorLayout by lazy { coordinator_layout!! }
  private val stageLayout by lazy { stage_layout!! }

  override fun onSetContentView() {
    setContentView(R.layout.activity_nmb)
  }

  override fun onGetStageLayout(): ViewGroup {
    return stageLayout
  }

  override fun onCreateRootScene(): Scene {
    return ThreadsScene()
  }

  fun snack(message: CharSequence?) {
    if (message != null && message.isNotEmpty()) {
      Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
    }
  }
}
