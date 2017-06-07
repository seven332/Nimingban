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

package com.hippo.nimingban.scene.threads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.scene.SceneView
import com.hippo.nimingban.widget.ProgressView

/*
 * Created by Hippo on 6/5/2017.
 */

class ThreadsView : SceneView<ThreadsView, ThreadsPresenter>(), ThreadsContract.View {

  override fun onCreate(inflater: LayoutInflater, parent: ViewGroup): View {
    val view = ProgressView(inflater.context, null)
    view.indeterminate = true
    return view
  }
}
