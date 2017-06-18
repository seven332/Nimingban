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

package com.hippo.nimingban.scene.threads

import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.R
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.exception.PresetException
import com.hippo.nimingban.scene.NmbScene
import com.hippo.nimingban.scene.gallery.galleryScene
import com.hippo.nimingban.scene.replies.repliesScene
import com.hippo.nimingban.widget.content.ContentData
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/14/2017.
 */

class ThreadsScene : NmbScene(), ThreadsSceneLogic {

  companion object {
    /** Assigning it to [forum] means no forum available **/
    private val NO_FORUM = Forum(null, null, null, null, null, null, null, null, null, null)
  }

  private var ui: ThreadsSceneUi? = null

  private val data = ThreadsData()

  /** Forum id, null for no forum **/
  private var forum: Forum = NO_FORUM
    set(value) {
      val oldValue = field
      field = value
      if (value.id != oldValue.id) {
        data.goTo(0)
      }
    }

  init {
    // Show progress at first, let ForumListUi to trigger
    data.forceProgress()

    // Update forums for API
    NMB_CLIENT.forums()
        .subscribeOn(Schedulers.io())
        .subscribe({ it -> NMB_DB.setOfficialForums(it) })
        .register()
  }

  override fun createUi() = ThreadsSceneUi(this, context!!, activity as NmbActivity).also { ui = it }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val view = super.onCreateView(inflater, container)

    ui?.let { ui ->
      ui.setTitle(if (forum !== NO_FORUM) forum.displayName else context?.getString(R.string.app_name))
      ui.setNavigationIcon(AppCompatResources.getDrawable(context!!, R.drawable.menu_white_x24))
      ui.setNavigationOnClickListener { this.ui?.toogleLeftDrawer() }
    }

    return view
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    ui = null
  }

  override fun initializeAdapter(adapter: ContentDataAdapter<Thread, *>) { adapter.data = data }

  override fun terminateAdapter(adapter: ContentDataAdapter<Thread, *>) { adapter.data = null }

  override fun initializeContentLayout(contentLayout: ContentLayout) { data.ui = contentLayout }

  override fun terminateContentLayout(contentLayout: ContentLayout) { data.ui = null }

  override fun onClickThread(thread: Thread) { stage?.pushScene(thread.repliesScene()) }

  override fun onClickThumb(reply: Reply) { stage?.pushScene(reply.galleryScene()) }

  override fun onSelectForum(forum: Forum, byUser: Boolean) {
    this.forum = forum
    ui?.setTitle(forum.displayName)
    if (byUser) {
      ui?.closeDrawers()
    }
  }

  override fun onNoForum() {
    this.forum = NO_FORUM
    ui?.setTitle(context?.getString(R.string.app_name))
  }

  inner class ThreadsData : ContentData<Thread>() {

    override fun onRequireData(id: Int, page: Int) {
      if (forum === NO_FORUM) {
        schedule { setError(id, PresetException("No forum", R.string.error_no_forum, R.drawable.emoticon_sad_primary_x64)) }
      } else {
        NMB_CLIENT.threads(forum.id, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ setData(id, it, Int.MAX_VALUE) }, { setError(id, it) })
            .register()
      }
    }

    override fun onRestoreData(id: Int) {
      // TODO("not implemented")
    }

    override fun onBackupData(data: List<Thread>) {
      // TODO("not implemented")
    }

    override fun isDuplicate(t1: Thread, t2: Thread) = t1.id == t2.id
  }
}
