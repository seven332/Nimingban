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

package com.hippo.nimingban

import android.app.Application
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.hippo.fresco.large.FrescoLarge
import com.hippo.nimingban.client.NMB_HOST
import com.hippo.nimingban.client.NmbClient
import com.hippo.nimingban.client.NmbEngine
import com.hippo.nimingban.client.NmbInterceptor
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.ForumGroup
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/*
 * Created by Hippo on 6/4/2017.
 */

private var _NMB_APP: NmbApp? = null

val NMB_APP: NmbApp by lazy { _NMB_APP!! }

val NMB_CLIENT: NmbClient by lazy {
  val retrofit = Retrofit.Builder()
      // Base url is useless, but it makes Retrofit happy
      .baseUrl(NMB_HOST)
      .client(OK_HTTP_CLIENT)
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create(GSON))
      .build()
  val nmbEngine = retrofit.create(NmbEngine::class.java)
  NmbClient(nmbEngine)
}

val NMB_DB: NmbDB by lazy { NmbDB(NMB_APP) }

val OK_HTTP_CLIENT: OkHttpClient by lazy {
  OkHttpClient.Builder()
      .addInterceptor(NmbInterceptor())
      .build()
}

val GSON: Gson by lazy {
  GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .registerTypeAdapter(Forum::class.java, InstanceCreator {
        Forum(null, null, null, null, null, null, null, null, null, null)
      })
      .registerTypeAdapter(ForumGroup::class.java, InstanceCreator {
        ForumGroup(null, null, null, null, null)
      })
      .registerTypeAdapter(Reply::class.java, InstanceCreator {
        Reply(null, null, null, null, null, null, null, null, null, null, null)
      })
      .registerTypeAdapter(Thread::class.java, InstanceCreator {
        Thread(null, null, null, null, null, null, null, null, null, null, null, null, null)
      })
      .create()
}

private var _REF_WATCHER: RefWatcher? = null

val REF_WATCHER: RefWatcher by lazy { _REF_WATCHER!! }

/**
 * Updates forums in database
 */
fun updateForums() {
  NMB_CLIENT.forums()
      .subscribeOn(Schedulers.io())
      .subscribe({ NMB_DB.setOfficialForums(it) }, { /* Ignore error */ })
}

class NmbApp : Application() {

  override fun onCreate() {
    _NMB_APP = this
    super.onCreate()
    _REF_WATCHER = LeakCanary.install(this)

    val imagePipelineConfigBuilder = OkHttpImagePipelineConfigFactory.newBuilder(this, OK_HTTP_CLIENT)
    FrescoLarge.initialize(this, null, null, imagePipelineConfigBuilder)
  }
}
