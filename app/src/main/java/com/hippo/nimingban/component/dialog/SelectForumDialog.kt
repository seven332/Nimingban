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

package com.hippo.nimingban.component.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hippo.android.dialog.base.DialogView
import com.hippo.android.dialog.base.DialogViewBuilder
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.util.find
import io.reactivex.android.schedulers.AndroidSchedulers

/*
 * Created by Hippo on 6/26/2017.
 */

class SelectForumDialog : NmbDialog() {

  private var adapter: ForumsAdapter? = null

  init {
    setCancelledOnTouchOutside(false)
  }

  override fun onCreateDialogView(inflater: LayoutInflater, container: ViewGroup): DialogView {
    adapter = ForumsAdapter(inflater)
    return DialogViewBuilder()
        .adapter(adapter) { dialog, index ->
          val target = this.target
          val adapter = this.adapter
          if (target is OnSelectForumListener && adapter != null) {
            target.onSelectForum(adapter.getItem(index))
          }
          dialog.dismiss()
        }
        .build(inflater, container)
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    adapter?.destroy()
    adapter = null
  }

  private class ForumsAdapter(val inflater: LayoutInflater) : BaseAdapter() {

    private var forums: List<Forum> = emptyList()

    private val disposable = NMB_DB.liveForums.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          forums = it.toList()
          notifyDataSetChanged()
        }, { /* Ignore error */ })

    fun destroy() {
      disposable.dispose()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
      var view = convertView
      if (view == null) {
        view = inflater.inflate(R.layout.list_single_item, parent, false)!!
      }
      view.find<TextView>(android.R.id.text1).text = forums[position].displayedName
      return view
    }

    override fun getItem(position: Int) = forums[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = forums.size
  }


  interface OnSelectForumListener {
    fun onSelectForum(forum: Forum)
  }
}
