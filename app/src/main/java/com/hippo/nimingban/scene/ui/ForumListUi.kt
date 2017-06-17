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

package com.hippo.nimingban.scene.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.util.INVALID_INDEX
import io.reactivex.android.schedulers.AndroidSchedulers

/*
 * Created by Hippo on 6/17/2017.
 */

class ForumListUi(
    val logic: ForumListLogic,
    context: Context,
    activity: NmbActivity
) : NmbUi(context, activity) {

  companion object {
    private const val KEY_SELECTED_FORUM = "ForumListUi:selected_forum"
    private const val KEY_SELECTED_INDEX = "ForumListUi:selected_index"
  }

  private var selectedForum: Forum? = null
  private var selectedIndex: Int = INVALID_INDEX

  private var forums: List<Forum> = emptyList()

  private var adapter: ForumAdapter? = null

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup): View {
    val view = inflater.inflate(R.layout.ui_forum_list, container, false)

    val adapter = ForumAdapter(inflater)
    val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = adapter

    this.adapter = adapter

    // Observe forums in db
    NMB_DB.liveForums.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { onUpdateForums(it) }
        .register()

    return view
  }

  private fun onUpdateForums(forums: List<Forum>) {
    this.forums = forums

    val selectedForum = this.selectedForum

    this.selectedForum = null
    this.selectedIndex = INVALID_INDEX

    // Try to find the same selected forum in the new forums
    if (selectedForum != null) {
      for ((index, item) in forums.withIndex()) {
        if (item.id == selectedForum.id) {
          this.selectedForum = item
          this.selectedIndex = index
          break
        }
      }
    }

    // Select the first forum
    if (this.selectedForum == null && forums.isNotEmpty()) {
      this.selectedForum = forums[0]
      this.selectedIndex = 0
    }

    adapter?.notifyDataSetChanged()

    this.selectedForum.let {
      if (it != null && it.id != selectedForum?.id) {
        logic.onSelectForum(it)
      } else if (it == null) {
        logic.onNoForum()
      }
    }
  }

  private fun onSelectForum(forum: Forum, index: Int) {
    if (selectedIndex == index) return

    val oldSelectedIndex = selectedIndex
    selectedForum = forum
    selectedIndex = index

    adapter?.let {
      if (oldSelectedIndex != INVALID_INDEX) it.notifyItemChanged(oldSelectedIndex)
      if (index != INVALID_INDEX) it.notifyItemChanged(index)
    }

    logic.onSelectForum(forum)
  }

  override fun onSaveState(outState: Bundle) {
    super.onSaveState(outState)
    outState.putParcelable(KEY_SELECTED_FORUM, selectedForum)
    outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
  }

  override fun onRestoreState(savedViewState: Bundle) {
    super.onRestoreState(savedViewState)
    selectedForum = savedViewState.getParcelable(KEY_SELECTED_FORUM)
    selectedIndex = savedViewState.getInt(KEY_SELECTED_FORUM)
  }

  private inner class ForumHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text = itemView.findViewById(R.id.text) as TextView

    val item: Forum? get() = adapterPosition.takeIf { it in 0 until forums.size }?.let { forums[it] }

    init {
      itemView.setOnClickListener { item?.let { onSelectForum(it, adapterPosition) } }
    }
  }

  private inner class ForumAdapter(val inflater: LayoutInflater) : RecyclerView.Adapter<ForumHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ForumHolder(inflater.inflate(R.layout.forum_list_item, parent, false))

    override fun onBindViewHolder(holder: ForumHolder, position: Int) {
      holder.text.text = forums[position].displayName
      holder.itemView.isSelected = position == selectedIndex
    }

    override fun getItemCount() = forums.size
  }
}
