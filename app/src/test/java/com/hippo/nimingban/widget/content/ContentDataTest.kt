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

import org.junit.Assert.assertEquals
import org.junit.Test

/*
 * Created by Hippo on 6/6/2017.
 */

class ContentDataTest {

  @Test
  fun testRestore() {
    val data = TestData()
    val view = TestView()
    val dataState = DataState()
    val viewState = ViewState()

    data.view = view
    view.data = data

    data.restore()
    dataState.onRestoreData++
    data.assertState(dataState, 0)
    viewState.showProgressBar++
    view.assertState(viewState)

    data.setData(data.id, listOf(0, 1, 2, 3), 0, 10)
    dataState.onRequireData++
    data.assertState(dataState, 4)
    viewState.stopRefreshing++
    viewState.showContent++
    viewState.notifyDataSetChanged++
    viewState.scrollToPosition++
    viewState.showContent++
    viewState.setHeaderRefreshing++
    view.assertState(viewState)
  }

  @Test
  fun testGoTo() {
    val data = TestData()
    val view = TestView()
    val dataState = DataState()
    val viewState = ViewState()

    data.view = view
    view.data = data

    data.goTo(0)
    dataState.onRequireData++
    data.assertState(dataState, 0)
    viewState.showProgressBar++
    view.assertState(viewState)

    data.setData(data.id, listOf(0, 1, 2, 3), 0, 10)
    dataState.onBackupData++
    data.assertState(dataState, 4)
    viewState.stopRefreshing++
    viewState.showContent++
    viewState.notifyDataSetChanged++
    viewState.scrollToPosition++
    view.assertState(viewState)

    data.goTo(100)
    dataState.onRequireData++
    data.assertState(dataState, 0)
    viewState.notifyDataSetChanged++
    viewState.showProgressBar++
    view.assertState(viewState)

    data.setError(data.id, Exception())
    data.assertState(dataState, 0)
    viewState.stopRefreshing++
    viewState.showTip++
    view.assertState(viewState)
  }

  fun testOnRefreshFooter() {

  }

  data class DataState(
      var onRequireData: Int = 0,
      var onRestoreData: Int = 0,
      var onBackupData: Int = 0
  )

  class TestData : ContentData<Int>() {
    val dataState = DataState()
    var id = 0
    var page = 0

    override fun onRequireData(id: Int, page: Int) {
      dataState.onRequireData++
      this.id = id
      this.page = page
    }
    override fun onRestoreData(id: Int) { dataState.onRestoreData++ }
    override fun onBackupData(data: List<Int>) { dataState.onBackupData++ }

    fun assertState(state: DataState, size: Int) {
      assertEquals(state, dataState)
      assertEquals(size, size())
    }
  }

  data class ViewState(
      var showContent: Int = 0,
      var showTip: Int = 0,
      var showProgressBar: Int = 0,
      var showMessage: Int = 0,
      var stopRefreshing: Int = 0,
      var setHeaderRefreshing: Int = 0,
      var setFooterRefreshing: Int = 0,
      var scrollToPosition: Int = 0,
      var scrollDownALittle: Int = 0,
      var notifyDataSetChanged: Int = 0,
      var notifyItemRangeInserted: Int = 0,
      var notifyItemRangeRemoved: Int = 0,
      var notifyItemRangeChanged: Int = 0
  )

  class TestView : ContentContract.View {
    override var presenter: ContentContract.Presenter? = null
    val viewState = ViewState()
    var data: ContentData<Int>? = null

    override fun showContent() { viewState.showContent++ }
    override fun showTip(t: Throwable) { viewState.showTip++ }
    override fun showProgressBar() { viewState.showProgressBar++ }
    override fun showMessage(t: Throwable) { viewState.showMessage++ }
    override fun stopRefreshing() { viewState.stopRefreshing++ }
    override fun setHeaderRefreshing() { viewState.setHeaderRefreshing++ }
    override fun setFooterRefreshing() { viewState.setFooterRefreshing++ }
    override fun scrollToPosition(position: Int) { viewState.scrollToPosition++ }
    override fun scrollDownALittle() { viewState.scrollDownALittle++ }
    override fun notifyDataSetChanged() { viewState.notifyDataSetChanged++ }
    override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) { viewState.notifyItemRangeInserted++ }
    override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) { viewState.notifyItemRangeRemoved++ }
    override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) { viewState.notifyItemRangeChanged++ }

    fun assertState(state: ViewState) {
      assertEquals(state, viewState)
    }
  }
}
