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

package com.hippo.nimingban.component.dialog

import android.os.Bundle
import com.hippo.nimingban.R
import com.hippo.stage.Curtain
import com.hippo.stage.SceneInfo
import com.hippo.stage.curtain.FadeCurtain



/*
 * Created by Hippo on 6/26/2017.
 */

abstract class NmbDialog : DebugDialog() {

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
    // TODO
    theme = R.style.ThemeOverlay_AppCompat_Dialog_Alert
  }

  override fun onCreateCurtain(upper: SceneInfo, lower: List<SceneInfo>): Curtain? {
    val curtain = FadeCurtain()
    curtain.setDuration(150L)
    return curtain
  }
}
