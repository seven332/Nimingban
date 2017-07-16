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

package com.hippo.nimingban.component.paper

import android.support.annotation.DimenRes
import com.hippo.nimingban.component.MvpUi
import com.hippo.viewstate.GenerateViewState
import com.hippo.viewstate.strategy.SingleByMethod
import com.hippo.viewstate.strategy.StrategyType

/*
 * Created by Hippo on 2017/7/15.
 */

@GenerateViewState
interface DrawerUi : MvpUi {

  @StrategyType(value = SingleByMethod::class)
  fun setLeftDrawerWidth(@DimenRes resId: Int)

  @StrategyType(value = SingleByMethod::class)
  fun setRightDrawerWidth(@DimenRes resId: Int)

  @StrategyType(value = SingleByMethod::class)
  fun setLeftDrawerMode(mode: Int)

  @StrategyType(value = SingleByMethod::class)
  fun setRightDrawerMode(mode: Int)

  fun closeDrawers()

  fun openLeftDrawer()

  fun openRightDrawer()
}
