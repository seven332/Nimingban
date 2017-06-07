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
 * Created by Hippo on 6/5/2017.
 */

internal class ContentState : ContentContract.State() {

  override var presenter: ContentContract.Presenter? = null

  private var showContent: Boolean = false
  private var showProgressBar: Boolean = false
  private var throwable: Throwable? = null
  private var headerRefreshing: Boolean = false
  private var footerRefreshing: Boolean = false

  override fun restore(view: ContentContract.View) {
    if (showContent) {
      view.showContent()
    } else if (throwable != null) {
      view.showTip(throwable!!)
    } else if (showProgressBar) {
      view.showProgressBar()
    }

    if (headerRefreshing) {
      view.setHeaderRefreshing()
    } else if (footerRefreshing) {
      view.setFooterRefreshing()
    }
  }

  override fun showContent() {
    showContent = true
    throwable = null
    showProgressBar = false
  }

  override fun showTip(t: Throwable) {
    showContent = false
    throwable = t
    showProgressBar = false
  }

  override fun showProgressBar() {
    showContent = false
    throwable = null
    showProgressBar = true
  }

  override fun showMessage(t: Throwable) {}

  override fun stopRefreshing() {
    headerRefreshing = false
    footerRefreshing = false
  }

  override fun setHeaderRefreshing() {
    headerRefreshing = true
    footerRefreshing = false
  }

  override fun setFooterRefreshing() {
    headerRefreshing = false
    footerRefreshing = true
  }

  override fun scrollToPosition(position: Int) {}

  override fun scrollDownALittle() {}

  override fun notifyDataSetChanged() {}

  override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {}

  override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {}

  override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {}
}
