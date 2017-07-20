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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbPaper
import com.hippo.nimingban.drawable.ChipDrawable
import com.hippo.nimingban.util.addState
import com.hippo.nimingban.util.attrColor
import com.hippo.nimingban.util.drawable
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 2017/7/18.
 */

class SendPaper(
    private val logic: SendLogic
) : NmbPaper<SendPaper>(logic), SendUi {

  private lateinit var moreText: View
  private lateinit var forumText: TextView
  private lateinit var forumView: TextView
  private lateinit var moreAction: ImageView
  private lateinit var moreView: View
  private lateinit var title: EditText
  private lateinit var name: EditText
  private lateinit var email: EditText
  private lateinit var content: EditText

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)

    view = inflater.inflate(R.layout.paper_send, container, false)
    moreText = view.find(R.id.more_text)
    forumText = view.find(R.id.forum_text)
    forumView = view.find(R.id.forum_view)
    moreAction = view.find(R.id.more_action)
    moreView = view.find(R.id.more)
    title = view.find(R.id.title)
    name = view.find(R.id.name)
    email = view.find(R.id.email)
    content = view.find(R.id.content)

    val moreDrawable = StateListDrawable()
    moreDrawable.addState(context.drawable(R.drawable.chevron_up_primary_x24), android.R.attr.state_activated)
    moreDrawable.addState(context.drawable(R.drawable.chevron_down_primary_x24))
    moreAction.setImageDrawable(moreDrawable)
    moreAction.setOnClickListener { logic.onClickMoreAction() }

    forumView.setOnClickListener { logic.onClickForum() }
    @Suppress("DEPRECATION")
    forumView.setBackgroundDrawable(ChipDrawable(context.attrColor(R.attr.backgroundColorStatusBar)))
  }

  override fun onAttach() {
    super.onAttach()
    content.requestFocus()
  }

  override fun asPost() {
    moreText.visibility = View.GONE
    forumText.visibility = View.VISIBLE
    forumView.visibility = View.VISIBLE
  }

  override fun asReply() {
    moreText.visibility = View.VISIBLE
    forumText.visibility = View.GONE
    forumView.visibility = View.GONE
  }

  override fun setPresetContent(text: String) {
    content.setText(text)
  }

  override fun setMoreActionsVisibility(show: Boolean) {
    moreAction.isActivated = show
    moreView.visibility = if (show) View.VISIBLE else View.GONE
  }

  override fun setForum(forum: Forum) {
    forumView.text = forum.displayedName
  }

  override fun requestInput() {
    logic.feedbackInput(
        title = title.text.toString(),
        name = name.text.toString(),
        email = email.text.toString(),
        content = content.toString()
    )
  }
}
