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

import android.os.Bundle
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.scene.NmbScene
import com.hippo.nimingban.scene.gallery.galleryScene
import com.hippo.nimingban.scene.ui.RepliesUi
import com.hippo.nimingban.scene.ui.SceneUi
import com.hippo.nimingban.scene.ui.wrapInSwipeBack
import com.hippo.nimingban.scene.ui.wrapInToolbar
import com.hippo.nimingban.widget.content.ContentData
import com.hippo.nimingban.widget.content.ContentDataAdapter
import com.hippo.nimingban.widget.content.ContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/11/2017.
 */

class RepliesScene: NmbScene(), RepliesSceneLogic {

  companion object {
    const val KEY_ID = "RepliesScene:id"
    const val KEY_THREAD = "RepliesScene:thread"
  }

  private var id: String? = null
  private var thread: Thread? = null

  private val data = RepliesData()

  override fun createUi(): SceneUi {
    return RepliesUi(this, context!!, activity as NmbActivity).wrapInToolbar(this).wrapInSwipeBack(this)
  }

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)

    opacity = TRANSLUCENT

    if (args != null) {
      id = args.getString(KEY_ID)
      thread = args.getParcelable(KEY_THREAD)
    }

    data.restore()
  }

  override fun initializeAdapter(adapter: ContentDataAdapter<Reply, *>) { adapter.data = data }

  override fun terminateAdapter(adapter: ContentDataAdapter<Reply, *>) { adapter.data = null }

  override fun initializeContentLayout(contentLayout: ContentLayout) { data.view = contentLayout }

  override fun terminateContentLayout(contentLayout: ContentLayout) { data.view = null }

  override fun onFinishUi() { pop() }

  override fun onClickThumb(reply: Reply) {
    stage?.pushScene(reply.galleryScene())
  }

  inner class RepliesData : ContentData<Reply>() {

    private val threadId get() = id ?: thread?.id

    override fun onRequireData(id: Int, page: Int) {
      val threadId = this.threadId
      if (threadId != null) {
        NMB_CLIENT.replies(threadId, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
              if (thread == null) thread = it.first
              // TODO need a better way to get max page
              setData(id, it.second, Int.MAX_VALUE)
            }, {
              setError(id, it)
            })
      } else {
        // TODO i18n
        schedule { setError(id, Exception("No thread id")) }
      }
    }

    override fun onRestoreData(id: Int) {
      val thread = this@RepliesScene.thread
      if (thread != null) {
        setData(id, listOf(thread.toReply()), 1)
      } else {
        setError(id, ContentData.FAILED_TO_RESTORE_EXCEPTION)
      }
    }

    override fun onBackupData(data: List<Reply>) {}
  }
}

fun Thread.repliesScene(): RepliesScene {
  val args = Bundle()
  args.putParcelable(RepliesScene.KEY_THREAD, this)
  val scene = RepliesScene()
  scene.args = args
  return scene
}
