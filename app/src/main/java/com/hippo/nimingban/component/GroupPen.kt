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

/*
 * Created by Hippo on 6/20/2017.
 */

abstract class GroupPen<P : Any> : NmbPen<P>() {

  private val children = mutableListOf<MvpPen<*>>()

  fun addChild(pen: MvpPen<*>) {
    children.add(pen)
  }

  override fun print() {
    super.print()
    children.forEach { it.print() }
  }

  override fun onCreate(args: Bundle) {
    super.onCreate(args)
    children.forEach { it.create(args) }
  }

  override fun onDestroy() {
    super.onDestroy()
    children.forEach { it.destroy() }
  }

  override fun onUpdateArgs(args: Bundle) {
    super.onUpdateArgs(args)
    children.forEach { it.updateArgs(args) }
  }
}
