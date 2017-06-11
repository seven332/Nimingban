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

import com.hippo.nimingban.util.INVALID_ID

/*
 * Created by Hippo on 6/5/2017.
 */

abstract class ContentData<T> : ContentContract.AbsPresenter<T>() {

  companion object {
    @JvmField val TYPE_RESTORE = 0
    @JvmField val TYPE_GOTO = 1
    @JvmField val TYPE_PREV_PAGE = 2
    @JvmField val TYPE_PREV_PAGE_ADJUST_POSITION = 3
    @JvmField val TYPE_NEXT_PAGE = 4
    @JvmField val TYPE_NEXT_PAGE_ADJUST_POSITION = 5
    @JvmField val TYPE_REFRESH_PAGE = 6

    @JvmField val DEFAULT_FIRST_PAGE_INDEX = 0

    @JvmField val NOT_FOUND_EXCEPTION = Exception("Not Found")
    @JvmField val TAP_TO_LOAD_EXCEPTION = Exception("Tap to Load")
    @JvmField val FAILED_TO_RESTORE_EXCEPTION = Exception("Failed to Restore")
  }

  override var view: ContentContract.View? = null
    get() = field
    set(value) {
      val _field = field
      if (_field != null) {
        _field.presenter = null
      }
      if (value != null) {
        value.presenter = this
        state.restore(value)
      }
      field = value
    }

  override val state: ContentContract.State = ContentState()

  private var idGenerator = INVALID_ID

  private var requirePage = 0
  private var requireType = 0
  private var requireId = INVALID_ID

  private var restored = false

  private val data: MutableList<T> = mutableListOf()
  /** The min page index **/
  private var minPage = DEFAULT_FIRST_PAGE_INDEX
  /** The max page index + 1  */
  private var maxPage = DEFAULT_FIRST_PAGE_INDEX
  /** The first loaded page index **/
  private var beginPage = DEFAULT_FIRST_PAGE_INDEX
  /** The last loaded page index + 1 **/
  private var endPage = DEFAULT_FIRST_PAGE_INDEX
  /**
   * Store the data divider index
   *
   * For example, the data contain page 3, page 4, page 5,
   * page 3 size is 7, page 4 size is 8, page 5 size is 9,
   * so `dataDivider` contain 7, 15, 24.
   */
  private var dataDivider: MutableList<Int> = mutableListOf()

  private var removeDuplicates = true
  // Duplicates checking left and right range
  private var duplicatesCheckRange = 50


  override fun size() = data.size

  override fun get(index: Int) = data[index]

  private fun isMinReached() = beginPage <= minPage

  override fun isMaxReached() = endPage >= maxPage

  fun restore() = requireData(0, TYPE_RESTORE)

  /**
   * Go to target page. It discards all previous data.
   */
  override fun goTo(page: Int) = requireData(page, TYPE_GOTO)

  /**
   * It's different from goTo().
   * switchTo() will only scrollToPosition() if
   * the page is in range.
   */
  override fun switchTo(page: Int) {
    if (page in beginPage until endPage) {
      val beginIndex = if (page == beginPage) 0 else dataDivider[page - beginPage - 1]
      scrollToPosition(beginIndex)
    } else if (page == endPage) {
      nextPage(true)
    } else if (page == beginPage - 1) {
      prevPage(true)
    } else {
      goTo(page)
    }
  }

  internal fun prevPage(adjustPosition: Boolean) =
      requireData(beginPage - 1, if (adjustPosition) TYPE_PREV_PAGE_ADJUST_POSITION else TYPE_PREV_PAGE)

  internal fun nextPage(adjustPosition: Boolean) =
      requireData(endPage, if (adjustPosition) TYPE_NEXT_PAGE_ADJUST_POSITION else TYPE_NEXT_PAGE)

  internal fun refreshPage(page: Int) = requireData(page, TYPE_REFRESH_PAGE)

  override fun onRefreshHeader() {
    if (isMinReached()) {
      goTo(beginPage)
    } else {
      prevPage(false)
    }
    state.setHeaderRefreshing()
  }

  override fun onRefreshFooter() {
    if (beginPage == endPage) {
      // No data is loaded
      stopRefreshing()
      return
    } else if (isMaxReached()) {
      refreshPage(endPage - 1)
    } else {
      nextPage(false)
    }
    state.setFooterRefreshing()
  }

  override fun onClickTip() {
    if (!isMaxReached()) {
      nextPage(true)
    } else if (!isMinReached()) {
      prevPage(true)
    } else {
      goTo(DEFAULT_FIRST_PAGE_INDEX)
    }
    showProgressBar()
  }

  private fun nextId(): Int {
    var id: Int
    do {
      id = ++idGenerator
    } while (id == INVALID_ID)
    return id
  }

  private fun requireData(page: Int, type: Int) {
    requirePage = page
    requireType = type
    requireId = nextId()

    if (data.isEmpty()) {
      showProgressBar()
    } else {
      showContent()
    }

    if (type == TYPE_RESTORE) {
      onRestoreData(requireId)
    } else {
      onRequireData(requireId, page)
    }
  }

  /**
   * Requires data. This method is not blocking.
   * When you get data, call [.setData] or [.setError].
   * If you get data right now, post it.
   */
  protected abstract fun onRequireData(id: Int, page: Int)

  /**
   * Restores data from backed up before. This method is not blocking.
   * When you get data, call [.setData] or [.setError].
   * If you get data right now, post it.
   */
  protected abstract fun onRestoreData(id: Int)

  /**
   * Backs up the data to allow restore later. This method is not blocking.
   */
  protected abstract fun onBackupData(data: List<T>)

  /**
   * Whether remove duplicates. If remove, duplicate item
   * in [.setData] will be ignored.
   *
   *
   * Duplicates in the same page are not ignored.
   *
   * @see .isDuplicate
   */
  fun setRemoveDuplicates(remove: Boolean) {
    this.removeDuplicates = remove
  }

  /**
   * Sets duplicates checking range.
   *
   * @see .setRemoveDuplicates
   */
  fun setDuplicatesCheckRange(range: Int) {
    duplicatesCheckRange = range
  }

  /**
   * Returns `true` if the two items are duplicate.
   *
   * @see .setRemoveDuplicates
   */
  open fun isDuplicate(t1: T, t2: T) = t1 == t2

  /**
   * Got data. Return {@code true} if it affects this {@code ContentData}.
   *
   * Min page index is `0` as default.
   */
  fun setData(id: Int, list: List<T>, max: Int) = setData(id, list, 0, max)

  /**
   * Got data. Return {@code true} if it affects this {@code ContentData}.
   */
  fun setData(id: Int, list: List<T>, min: Int, max: Int): Boolean {
    if (requireId == INVALID_ID || id != requireId) return false

    if (min > max) throw IllegalStateException("min > max")

    requireId = INVALID_ID
    restored = requireType == TYPE_RESTORE
    stopRefreshing()

    when (requireType) {
      TYPE_RESTORE -> onRestore(list)
      TYPE_GOTO -> onGoTo(list, min, max)
      TYPE_PREV_PAGE -> onPrevPage(list, min, max, false)
      TYPE_PREV_PAGE_ADJUST_POSITION -> onPrevPage(list, min, max, true)
      TYPE_NEXT_PAGE -> onNextPage(list, min, max, false)
      TYPE_NEXT_PAGE_ADJUST_POSITION -> onNextPage(list, min, max, true)
      TYPE_REFRESH_PAGE-> onRefreshPage(list, min, max)
      else -> throw IllegalStateException("Unknown type: " + requireType)
    }

    return true
  }

  private fun onRestore(list: List<T>) {
    // Update data
    data.clear()
    data.addAll(list)
    notifyDataSetChanged()

    // Update dataDivider
    dataDivider.clear()
    dataDivider.add(list.size)

    // Update pages, beginPage, endPage
    // Always assume 1 page
    minPage = 0
    maxPage = 1
    beginPage = 0
    endPage = 1

    // Update UI
    if (data.isEmpty()) {
      showProgressBar()
    } else {
      showContent()
      scrollToPosition(0)
      setHeaderRefreshing()
    }

    // Keep loading
    goTo(DEFAULT_FIRST_PAGE_INDEX)
  }

  private fun onGoTo(d: List<T>, min: Int, max: Int) {
    // Update data
    data.clear()
    data.addAll(d)
    notifyDataSetChanged()

    // Update dataDivider
    dataDivider.clear()
    dataDivider.add(d.size)

    // Update pages, beginPage, endPage
    minPage = min
    maxPage = max
    beginPage = requirePage
    endPage = beginPage + 1

    // Update UI
    if (data.isEmpty()) {
      if (isMinReached() && isMaxReached()) {
        showTip(NOT_FOUND_EXCEPTION)
      } else {
        showTip(TAP_TO_LOAD_EXCEPTION)
      }
    } else {
      showContent()
      // Scroll to top
      scrollToPosition(0)
    }

    // Backup data
    if (requirePage == min && !d.isEmpty()) {
      onBackupData(d)
    }
  }

  private fun removeDuplicates(d: List<T>, index: Int): List<T> {
    // Don't check all data, just check the data around the index to insert
    return removeDuplicates(d, index - duplicatesCheckRange, index + duplicatesCheckRange)
  }

  // Start and end will be fixed to fit range [0, data.size())
  private fun removeDuplicates(list: List<T>, start: Int, end: Int): List<T> {
    val from = Math.max(0, start)
    val to = Math.min(data.size, end)
    val control = data.subList(from, to)
    return list.filter { it1 -> control.all { it2 -> !isDuplicate(it1, it2) } }
  }

  private fun onPrevPage(_list: List<T>, min: Int, max: Int, adjustPosition: Boolean) {
    // Remove duplicates
    val list: List<T>
    if (removeDuplicates) {
      list = removeDuplicates(_list, 0)
    } else {
      list = _list
    }

    // Update data
    val size = list.size
    if (size != 0) {
      data.addAll(0, list)
      notifyItemRangeInserted(0, size)
    }

    // Update dataDivider
    if (size != 0) {
      var i = 0
      val n = dataDivider.size
      while (i < n) {
        dataDivider[i] = dataDivider[i] + size
        i++
      }
    }
    dataDivider.add(0, size)

    // Update pages, beginPage, endPage
    minPage = min
    maxPage = max
    --beginPage

    // Update UI
    if (data.isEmpty()) {
      if (isMinReached() && isMaxReached()) {
        showTip(NOT_FOUND_EXCEPTION)
      } else {
        showTip(TAP_TO_LOAD_EXCEPTION)
      }
    } else {
      showContent()
      if (adjustPosition) {
        // Scroll to the first position of require page
        scrollToPosition(0)
      }
    }
  }

  private fun onNextPage(_list: List<T>, min: Int, max: Int, adjustPosition: Boolean) {
    // Remove duplicates
    val list: List<T>
    if (removeDuplicates) {
      list = removeDuplicates(_list, data.size)
    } else {
      list = _list
    }

    // Update data
    val oldSize = data.size
    if (!list.isEmpty()) {
      data.addAll(list)
      notifyItemRangeInserted(oldSize, list.size)
    }

    // Update dataDivider
    dataDivider.add(data.size)

    // Update pages, beginPage, endPage
    minPage = min
    maxPage = max
    ++endPage

    // Update UI
    if (data.isEmpty()) {
      if (isMinReached() && isMaxReached()) {
        showTip(NOT_FOUND_EXCEPTION)
      } else {
        showTip(TAP_TO_LOAD_EXCEPTION)
      }
    } else {
      showContent()
      if (adjustPosition) {
        if (!list.isEmpty()) {
          // Scroll to the first position of require page
          scrollToPosition(oldSize)
        }
      } else {
        scrollDownALittle()
      }
    }
  }

  private fun onRefreshPage(_list: List<T>, min: Int, max: Int) {
    if (requirePage >= endPage || requirePage < beginPage) {
      throw IllegalStateException("TYPE_REFRESH_PAGE requires requirePage in range, "
          + "beginPage=" + beginPage + ", endPage=" + endPage + ", requirePage=" + requirePage)
    }

    val beginIndex = if (requirePage == beginPage) 0 else dataDivider[requirePage - beginPage - 1]
    val oldEndIndex = dataDivider[requirePage - beginPage]

    // Remove duplicates
    val list: List<T>
    if (removeDuplicates) {
      val __list = removeDuplicates(_list, beginIndex - duplicatesCheckRange, beginIndex)
      list = removeDuplicates(__list, oldEndIndex, oldEndIndex + duplicatesCheckRange)
    } else {
      list = _list
    }

    val newEndIndex = beginIndex + list.size

    // Update data
    val oldCount = oldEndIndex - beginIndex
    val newCount = list.size
    val overlapCount = Math.min(oldCount, newCount)
    // Change overlapping data
    if (overlapCount != 0) {
      data.subList(beginIndex, beginIndex + overlapCount).clear()
      data.addAll(beginIndex, list.subList(0, overlapCount))
      notifyItemRangeChanged(beginIndex, overlapCount)
    }
    // Remove remaining data
    if (oldCount > overlapCount) {
      data.subList(beginIndex + overlapCount, beginIndex + oldCount).clear()
      notifyItemRangeRemoved(beginIndex + overlapCount, oldCount - overlapCount)
    }
    // Add remaining data
    if (newCount > overlapCount) {
      data.addAll(beginIndex + overlapCount, list.subList(overlapCount, newCount))
      notifyItemRangeInserted(beginIndex + overlapCount, newCount - overlapCount)
    }

    // Update dataDivider
    if (newEndIndex != oldEndIndex) {
      var i = requirePage - beginPage
      val n = dataDivider.size
      while (i < n) {
        dataDivider[i] = dataDivider[i] - oldEndIndex + newEndIndex
        i++
      }
    }

    // Update pages, beginPage, endPage
    minPage = min
    maxPage = max

    // Update UI
    if (data.isEmpty()) {
      if (isMinReached() && isMaxReached()) {
        showTip(NOT_FOUND_EXCEPTION)
      } else {
        showTip(TAP_TO_LOAD_EXCEPTION)
      }
    } else {
      showContent()
    }
  }

  /**
   * Got exception. Return `true` if it affects this `ContentData`.
   */
  fun setError(id: Int, e: Throwable): Boolean {
    if (requireId == INVALID_ID || id != requireId) return false

    requireId = INVALID_ID
    stopRefreshing()

    if ((requireType == TYPE_GOTO && !restored) || requireType == TYPE_RESTORE || data.isEmpty()) {
      // Clear all data
      if (!data.isEmpty()) {
        data.clear()
        notifyDataSetChanged()
      }
      dataDivider.clear()
      minPage = 0
      maxPage = 0
      beginPage = 0
      endPage = 0

      if (requireType == TYPE_RESTORE) {
        showProgressBar()
        goTo(0)
      } else {
        showTip(e)
      }
    } else {
      // Has some data
      // Only non-interrupting message
      showMessage(e)
    }

    return true
  }
}
