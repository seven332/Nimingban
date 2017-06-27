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

import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbUi
import com.hippo.nimingban.drawable.ChipDrawable
import com.hippo.nimingban.util.addState
import com.hippo.nimingban.util.attrColor
import com.hippo.nimingban.util.find
import com.hippo.nimingban.util.drawable

/*
 * Created by Hippo on 6/24/2017.
 */

class SendUi(
    private val logic: SendLogic,
    inflater: LayoutInflater,
    container: ViewGroup
) : NmbUi() {

  companion object {
    private const val KEY_SHOW_MORE = "SendScene:show_more"
  }

  override val view: View
  private val context = inflater.context
  private val moreText: View
  private val forumText: TextView
  private val forumView: TextView
  private val moreAction: ImageView
  private val moreView: View
  private val title: EditText
  private val name: EditText
  private val email: EditText
  private val content: EditText

  private var forum: Forum? = null
  private var showMore: Boolean = false

  init {
    view = inflater.inflate(R.layout.ui_send, container, false)
    moreText = view.find(R.id.more_text)
    forumText = view.find(R.id.forum_text)
    forumView = view.find(R.id.forum_view)
    moreAction = view.find(R.id.more_action)
    moreView = view.find(R.id.more)
    title = view.find(R.id.title)
    name = view.find(R.id.name)
    email = view.find(R.id.email)
    content = view.find(R.id.content)

    val moreDrawable = StateListDrawable().apply {
      addState(context.drawable(R.drawable.chevron_up_primary_x24), android.R.attr.state_activated)
      addState(context.drawable(R.drawable.chevron_down_primary_x24))
    }
    moreAction.setImageDrawable(moreDrawable)
    moreAction.setOnClickListener { showMore(!showMore) }

    forumView.setOnClickListener { logic.onClickForum() }

    ViewCompat.setBackground(forumView, ChipDrawable(context.attrColor(R.attr.backgroundColorStatusBar)))

    showMore(showMore)

    logic.sendUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    logic.sendUi = null
  }

  private fun showMore(show: Boolean) {
    showMore = show
    moreAction.isActivated = show
    moreView.visibility = if (show) View.VISIBLE else View.GONE
  }

  fun onSelectForum(forum: Forum?) {
    this.forum = forum
    if (forum != null) {
      moreText.visibility = View.GONE
      forumText.visibility = View.VISIBLE
      forumView.visibility = View.VISIBLE
      forumView.text = forum.name
    } else {
      moreText.visibility = View.VISIBLE
      forumText.visibility = View.GONE
      forumView.visibility = View.GONE
    }
  }

  fun getTitle() = title.text.toString()

  fun getName() = name.text.toString()

  fun getEmail() = email.text.toString()

  fun getContent() = content.text.toString()

  override fun onSaveState(outState: Bundle) {
    super.onSaveState(outState)
    outState.putBoolean(KEY_SHOW_MORE, showMore)
  }

  override fun onRestoreState(savedViewState: Bundle) {
    super.onRestoreState(savedViewState)
    showMore = savedViewState.getBoolean(KEY_SHOW_MORE)
    showMore(showMore)
  }
}
