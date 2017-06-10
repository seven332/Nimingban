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

import com.hippo.nimingban.architecture.PresenterInterface
import com.hippo.nimingban.architecture.ViewInterface

interface ContentContract {

  /**
   * `Presenter` of ContentLayout.
   */
  interface Presenter : PresenterInterface<Presenter, View> {

    fun onRefreshHeader()

    fun onRefreshFooter()

    fun onClickTip()

    fun goTo(page: Int)

    fun switchTo(page: Int)

    fun size(): Int

    fun isMaxReached(): Boolean
  }

  /**
   * `View` of ContentLayout.
   */
  interface View : ViewInterface<View, Presenter> {

    fun showContent()

    fun showTip(t: Throwable)

    fun showProgressBar()

    fun showMessage(t: Throwable)

    fun stopRefreshing()

    fun setHeaderRefreshing()

    fun setFooterRefreshing()

    fun scrollToPosition(position: Int)

    fun scrollDownALittle()

    fun notifyDataSetChanged()

    fun notifyItemRangeInserted(positionStart: Int, itemCount: Int)

    fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int)

    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int)
  }

  interface DataPresenter<out T> : Presenter {

    fun get(index: Int): T
  }

  abstract class State : View {

    abstract fun restore(view: View)
  }

  abstract class AbsPresenter<out T> : DataPresenter<T>, View {

    override var presenter: ContentContract.Presenter?
      get() = error("Never touch ContentContract.AbsPresenter's presenter")
      set(value) { error("Never touch ContentContract.AbsPresenter's presenter") }

    abstract val state: State

    override fun showContent() {
      view?.showContent()
      state.showContent()
    }

    override fun showTip(t: Throwable) {
      view?.showTip(t)
      state.showTip(t)
    }

    override fun showProgressBar() {
      view?.showProgressBar()
      state.showProgressBar()
    }

    override fun showMessage(t: Throwable) {
      view?.showMessage(t)
      state.showMessage(t)
    }

    override fun stopRefreshing() {
      view?.stopRefreshing()
      state.stopRefreshing()
    }

    override fun setHeaderRefreshing() {
      view?.setHeaderRefreshing()
      state.setHeaderRefreshing()
    }

    override fun setFooterRefreshing() {
      view?.setFooterRefreshing()
      state.setFooterRefreshing()
    }

    override fun scrollToPosition(position: Int) {
      view?.scrollToPosition(position)
      state.scrollToPosition(position)
    }

    override fun scrollDownALittle() {
      view?.scrollDownALittle()
      state.scrollDownALittle()
    }

    override fun notifyDataSetChanged() {
      view?.notifyDataSetChanged()
      state.notifyDataSetChanged()
    }

    override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
      view?.notifyItemRangeInserted(positionStart, itemCount)
      state.notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
      view?.notifyItemRangeRemoved(positionStart, itemCount)
      state.notifyItemRangeRemoved(positionStart, itemCount)
    }

    override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
      view?.notifyItemRangeChanged(positionStart, itemCount)
      state.notifyItemRangeChanged(positionStart, itemCount)
    }
  }
}
