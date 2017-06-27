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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.component.NmbUi
import com.hippo.nimingban.util.addState
import com.hippo.nimingban.util.asMutableList
import com.hippo.nimingban.util.find
import com.hippo.nimingban.util.isUnder
import com.hippo.nimingban.util.drawable

/*
 * Created by Hippo on 6/22/2017.
 */

class ForumsUi(
    val logic: ForumsLogic,
    val inflater: LayoutInflater,
    container: ViewGroup
) : NmbUi() {

  override val view: View
  private val context = inflater.context
  private val recyclerView: RecyclerView
  private val touchActionGuardManager: RecyclerViewTouchActionGuardManager
  private val dragDropManager: RecyclerViewDragDropManager
  private val adapter: ForumAdapter
  private val wrappedAdapter: RecyclerView.Adapter<*>

  private var forums: MutableList<Forum> = mutableListOf()

  init {
    view = inflater.inflate(R.layout.ui_forums, container, false)
    recyclerView = view.find(R.id.recycler_view)

    // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
    touchActionGuardManager = RecyclerViewTouchActionGuardManager()
    touchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
    touchActionGuardManager.isEnabled = true

    // drag & drop manager
    dragDropManager = RecyclerViewDragDropManager()

    adapter = ForumAdapter()
    adapter.setHasStableIds(true)
    wrappedAdapter = dragDropManager.createWrappedAdapter(adapter)

    recyclerView.hasFixedSize()
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = wrappedAdapter
    recyclerView.itemAnimator = DraggableItemAnimator()

    touchActionGuardManager.attachRecyclerView(recyclerView)
    dragDropManager.attachRecyclerView(recyclerView)

    // Bind the ui to logic
    logic.forumsUi = this
  }

  override fun onDestroy() {
    super.onDestroy()
    dragDropManager.release()
    touchActionGuardManager.release()
    recyclerView.adapter = null
    WrapperAdapterUtils.releaseAll(wrappedAdapter)

    // Unbind the ui from logic
    logic.forumsUi = null
  }

  fun onUpdateForums(forums: List<Forum>) {
    this.forums = forums.asMutableList()
    wrappedAdapter.notifyDataSetChanged()
  }


  private inner class ForumHolder(itemView: View) : AbstractDraggableItemViewHolder(itemView) {
    val delete = itemView.find<ImageView>(R.id.delete)
    val hide = itemView.find<ImageView>(R.id.hide)
    val name = itemView.find<TextView>(R.id.name)
    val sort = itemView.find<ImageView>(R.id.sort)

    val item: Forum? get() = adapterPosition.takeIf { it in 0 until forums.size }?.let { forums[it] }

    init {
      delete.setImageDrawable(context.drawable(R.drawable.delete_primary_x24))
      val eye = StateListDrawable().apply {
        addState(context.drawable(R.drawable.eye_primary_x24), android.R.attr.state_activated)
        addState(context.drawable(R.drawable.eye_off_primary_x24))
      }
      hide.setImageDrawable(eye)
      sort.setImageDrawable(context.drawable(R.drawable.reorder_primary_x24))

      hide.setOnClickListener {
        val item = this.item
        if (item != null) {
          // Toggle visible
          item.visible = !item.visible
          adapter.notifyItemChanged(adapterPosition)
          // Sync it in database
          schedule { NMB_DB.putForum(item) }
        }
      }
    }
  }


  private inner class ForumAdapter : RecyclerView.Adapter<ForumHolder>(), DraggableItemAdapter<ForumHolder> {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        ForumHolder(inflater.inflate(R.layout.forums_item, parent, false))

    override fun onBindViewHolder(holder: ForumHolder, position: Int) {
      val forum = forums[position]
      holder.name.text = forum.displayName
      holder.delete.visibility = if (forum.official) View.GONE else View.VISIBLE
      holder.hide.visibility = if (forum.official) View.VISIBLE else View.GONE
      holder.hide.isActivated = forum.visible
    }

    override fun getItemCount() = forums.size

    override fun getItemId(position: Int) = forums[position].id.hashCode().toLong()

    override fun onCheckCanStartDrag(holder: ForumHolder, position: Int, x: Int, y: Int) =
        holder.sort.isUnder(x.toFloat(), y.toFloat())

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
      // Modify data right now
      forums.add(toPosition, forums.removeAt(fromPosition))
      notifyItemMoved(fromPosition, toPosition)
      // Sync it in database
      schedule { NMB_DB.orderForum(fromPosition, toPosition) }
    }

    override fun onGetItemDraggableRange(holder: ForumHolder?, position: Int) = null

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int) = true
  }
}
