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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.Toast
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hippo.fresco.large.FrescoLarge
import com.hippo.nimingban.activity.NmbActivity
import com.hippo.nimingban.client.NMB_HOST
import com.hippo.nimingban.client.NmbClient
import com.hippo.nimingban.client.NmbConverterFactory
import com.hippo.nimingban.client.NmbEngine
import com.hippo.nimingban.client.NmbInterceptor
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.ForumApiGson
import com.hippo.nimingban.client.data.ForumGroup
import com.hippo.nimingban.client.data.ForumGroupApiGson
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.ReplyApiGson
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.client.data.ThreadApiGson
import com.hippo.nimingban.network.CookieRepository
import com.hippo.nimingban.util.filterNot
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.ref.WeakReference

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
      .addConverterFactory(NmbConverterFactory(GSON))
      .build()
  val nmbEngine = retrofit.create(NmbEngine::class.java)
  NmbClient(nmbEngine)
}

val NMB_DB: NmbDB by lazy { NmbDB(NMB_APP) }

val NMB_NOTIFICATION: NmbNotification by lazy { NmbNotification(NMB_APP) }

val COOKIE_JAR: CookieRepository by lazy {
  CookieRepository(NMB_APP)
}

val OK_HTTP_CLIENT: OkHttpClient by lazy {
  OkHttpClient.Builder()
      .addInterceptor(NmbInterceptor())
      .addNetworkInterceptor(HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BASIC })
      .cookieJar(COOKIE_JAR)
      .build()
}

val GSON: Gson by lazy {
  GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .registerTypeAdapter(Forum::class.java, ForumApiGson())
      .registerTypeAdapter(ForumGroup::class.java, ForumGroupApiGson())
      .registerTypeAdapter(Reply::class.java, ReplyApiGson())
      .registerTypeAdapter(Thread::class.java, ThreadApiGson())
      .create()
}

private var _REF_WATCHER: RefWatcher? = null

val REF_WATCHER: RefWatcher by lazy { _REF_WATCHER!! }

/**
 * Read s string resource.
 */
fun string(resId: Int) = NMB_APP.getString(resId)!!

/**
 * Read s string resource.
 */
fun string(resId: Int, vararg formatArgs: Any) = NMB_APP.getString(resId, *formatArgs)!!

/**
 * Show a tip.
 * First, try to show it as a snack. If can't, show it as a toast.
 */
fun tip(resId: Int) {
  if (!snack(resId)) {
    toast(resId)
  }
}

/**
 * Show a tip.
 * First, try to show it as a snack. If can't, show it as a toast.
 */
fun tip(text: CharSequence) {
  if (!snack(text)) {
    toast(text)
  }
}

/**
 * Show a snack on the top activity if it's NmbActivity.
 * Return `true` if the snack can be shown.
 */
fun snack(resId: Int): Boolean {
  val activity = NMB_APP.getNmbActivity()
  if (activity != null) {
    activity.snack(resId)
    return true
  } else {
    return false
  }
}

/**
 * Show a snack on the top activity if it's NmbActivity.
 * Return `true` if the snack can be shown.
 */
fun snack(text: CharSequence): Boolean {
  val activity = NMB_APP.getNmbActivity()
  if (activity != null) {
    activity.snack(text)
    return true
  } else {
    return false
  }
}

fun toast(resId: Int) {
  Toast.makeText(NMB_APP, resId, Toast.LENGTH_SHORT).show()
}

fun toast(text: CharSequence) {
  Toast.makeText(NMB_APP, text, Toast.LENGTH_SHORT).show()
}

/**
 * Updates forums in database
 */
fun updateForums() {
  NMB_CLIENT.forums()
      .subscribeOn(Schedulers.io())
      .subscribe({ NMB_DB.setOfficialForums(it) }, { /* Ignore error */ })
}

open class NmbApp : Application() {

  private val activities = mutableListOf<WeakReference<Activity>>()

  override fun onCreate() {
    _NMB_APP = this
    super.onCreate()
    _REF_WATCHER = LeakCanary.install(this)

    val imagePipelineConfigBuilder = OkHttpImagePipelineConfigFactory.newBuilder(this, OK_HTTP_CLIENT)
    FrescoLarge.initialize(this, null, null, imagePipelineConfigBuilder)

    registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activities.add(WeakReference(activity))
      }
      override fun onActivityDestroyed(activity: Activity) {
        activities.filterNot { it.get().let { it == null || it == activity } }
      }
      override fun onActivityStarted(activity: Activity) {}
      override fun onActivityResumed(activity: Activity) {}
      override fun onActivityPaused(activity: Activity) {}
      override fun onActivityStopped(activity: Activity) {}
      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
    })
  }

  /**
   * If the top activity is [NmbActivity], returns it.
   */
  fun getNmbActivity(): NmbActivity? {
    try {
      val activity = activities.last { it.get() != null }.get()
      if (activity is NmbActivity) {
        return activity
      }
    } catch (e: NoSuchElementException) {}
    return null
  }
}
