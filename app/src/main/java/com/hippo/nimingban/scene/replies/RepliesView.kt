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

package com.hippo.nimingban.scene.replies

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hippo.android.resource.AttrResources
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.scene.NmbScene
import com.hippo.nimingban.scene.ToolbarView
import com.hippo.nimingban.util.dp2pix
import com.hippo.nimingban.util.prettyTime
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import com.hippo.nimingban.widget.nmb.NmbThumb
import com.hippo.recyclerview.addons.LinearDividerItemDecoration

/*
 * Created by Hippo on 6/11/2017.
 */

class RepliesView(
    scene: NmbScene<RepliesPresenter, RepliesView>,
    activity: NmbActivity,
    context: Context
) : ToolbarView<RepliesView, RepliesPresenter>(scene, activity, context),
    RepliesContract.View, ContentLayout.Extension {

  internal var contentLayout: ContentLayout? = null
  internal var recyclerView: RecyclerView? = null
  internal var adapter: RepliesAdapter? = null

  override fun onCreateToolbarContent(inflater: LayoutInflater, parent: ViewGroup): View {
    val view = inflater.inflate(R.layout.scene_replies, parent, false)!!

    val adapter = RepliesAdapter(inflater)

    val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(inflater.context)

    val itemDecoration = LinearDividerItemDecoration(
        LinearDividerItemDecoration.VERTICAL,
        AttrResources.getAttrColor(context, R.attr.dividerColor),
        1.dp2pix(context))
    itemDecoration.setShowLastDivider(true)
    recyclerView.addItemDecoration(itemDecoration)

    val contentLayout = view.findViewById(R.id.content_layout) as ContentLayout
    contentLayout.extension = this

    this.contentLayout = contentLayout
    this.recyclerView = recyclerView
    this.adapter = adapter

    return view
  }

  override fun onDestroy() {
    super.onDestroy()
    recyclerView?.adapter = null
    recyclerView?.layoutManager = null
  }

  override fun showMessage(message: String) {
    activity.snack(message)
  }


  class RepliesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val user = itemView.findViewById(R.id.user) as TextView
    val id = itemView.findViewById(R.id.id) as TextView
    val date = itemView.findViewById(R.id.date) as TextView
    val content = itemView.findViewById(R.id.content) as TextView
    val thumb = itemView.findViewById(R.id.thumb) as NmbThumb
  }


  class RepliesAdapter(val inflater: LayoutInflater) : ContentDataAdapter<Reply, RepliesHolder>() {

    override fun onCreateViewHolder2(parent: ViewGroup?, viewType: Int) =
        RepliesHolder(inflater.inflate(R.layout.replies_item, parent, false))

    override fun onBindViewHolder(holder: RepliesHolder, position: Int) {
      val thread = get(position)
      holder.user.text = thread.displayUser
      holder.id.text = thread.displayId
      holder.date.text = thread.date.prettyTime(inflater.context)
      holder.content.text = thread.displayContent
      holder.thumb.loadThumb(thread.image)
    }
  }
}
