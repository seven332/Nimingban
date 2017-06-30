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

package com.hippo.nimingban.widget.content

/*
 * Created by Hippo on 2/10/2017.
 */

import com.hippo.nimingban.architecture.Logic
import com.hippo.nimingban.architecture.Ui

interface ContentLogic : Logic {

  fun onRefreshHeader()

  fun onRefreshFooter()

  fun onClickTip()

  fun goTo(page: Int)

  fun switchTo(page: Int)

  fun size(): Int

  fun isMaxReached(): Boolean
}

interface ContentUi : Ui {

  var logic: ContentLogic?

  fun showContent()

  fun showTip(t: Throwable)

  fun showProgressBar()

  fun showMessage(t: Throwable)

  fun stopRefreshing()

  fun setHeaderRefreshing()

  fun setFooterRefreshing()

  fun scrollToPosition(position: Int)

  fun scrollUpALittle()

  fun scrollDownALittle()

  fun notifyDataSetChanged()

  fun notifyItemRangeInserted(positionStart: Int, itemCount: Int)

  fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int)

  fun notifyItemRangeChanged(positionStart: Int, itemCount: Int)
}

interface ContentDataLogic<out T> : ContentLogic {

  fun get(index: Int): T
}

interface ContentState : ContentUi {

  fun restore(ui: ContentUi)
}

abstract class AbsContentData<out T> : ContentDataLogic<T>, ContentUi {

  override var logic: ContentLogic? = null

  abstract var ui: ContentUi?
  abstract val state: ContentState

  override fun showContent() {
    ui?.showContent()
    state.showContent()
  }

  override fun showTip(t: Throwable) {
    ui?.showTip(t)
    state.showTip(t)
  }

  override fun showProgressBar() {
    ui?.showProgressBar()
    state.showProgressBar()
  }

  override fun showMessage(t: Throwable) {
    ui?.showMessage(t)
    state.showMessage(t)
  }

  override fun stopRefreshing() {
    ui?.stopRefreshing()
    state.stopRefreshing()
  }

  override fun setHeaderRefreshing() {
    ui?.setHeaderRefreshing()
    state.setHeaderRefreshing()
  }

  override fun setFooterRefreshing() {
    ui?.setFooterRefreshing()
    state.setFooterRefreshing()
  }

  override fun scrollToPosition(position: Int) {
    ui?.scrollToPosition(position)
    state.scrollToPosition(position)
  }

  override fun scrollUpALittle() {
    ui?.scrollUpALittle()
    state.scrollUpALittle()
  }

  override fun scrollDownALittle() {
    ui?.scrollDownALittle()
    state.scrollDownALittle()
  }

  override fun notifyDataSetChanged() {
    ui?.notifyDataSetChanged()
    state.notifyDataSetChanged()
  }

  override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
    ui?.notifyItemRangeInserted(positionStart, itemCount)
    state.notifyItemRangeInserted(positionStart, itemCount)
  }

  override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
    ui?.notifyItemRangeRemoved(positionStart, itemCount)
    state.notifyItemRangeRemoved(positionStart, itemCount)
  }

  override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
    ui?.notifyItemRangeChanged(positionStart, itemCount)
    state.notifyItemRangeChanged(positionStart, itemCount)
  }
}
