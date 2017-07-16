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

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbPaper
import com.hippo.nimingban.util.INVALID_INDEX
import com.hippo.nimingban.util.find

/*
 * Created by Hippo on 2017/7/15.
 */

class ForumListPaper(
    private val logic: ForumListLogic
) : NmbPaper<ForumListUi>(logic), ForumListUi {

  private var forums: List<Forum> = emptyList()

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: ForumAdapter

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)

    view = inflater.inflate(R.layout.paper_forum_list, container, false)
    recyclerView = view.find(R.id.forum_list)

    adapter = ForumAdapter()
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(context)
  }

  override fun onDestroy() {
    super.onDestroy()
    recyclerView.adapter = null
    recyclerView.layoutManager = null
  }

  override fun updateForums(forums: List<Forum>) {
    this.forums = forums
    adapter.notifyDataSetChanged()
  }

  override fun updateSelectedIndex(oldIndex: Int, newIndex: Int) {
    if (oldIndex != INVALID_INDEX) {
      adapter.notifyItemChanged(oldIndex)
    }
    if (newIndex != INVALID_INDEX) {
      adapter.notifyItemChanged(newIndex)
    }
  }


  private inner class ForumHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val text = itemView.findViewById(R.id.text) as TextView

    val item: Forum? get() = adapterPosition.takeIf { it in 0 until forums.size }?.let { forums[it] }

    init {
      itemView.setOnClickListener { item?.let { logic.onClickForum(it, adapterPosition); } }
    }
  }


  private inner class ForumAdapter : RecyclerView.Adapter<ForumHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ForumHolder(inflater.inflate(R.layout.forum_list_item, parent, false))

    override fun onBindViewHolder(holder: ForumHolder, position: Int) {
      holder.text.text = forums[position].displayedVividName
      holder.itemView.isActivated = position == logic.getSelectedIndex()
    }

    override fun getItemCount() = forums.size
  }
}
