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

package com.hippo.nimingban.component

import android.support.v7.app.NotificationCompat
import com.hippo.nimingban.NMB_APP
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.NMB_NOTIFICATION
import com.hippo.nimingban.R
import com.hippo.nimingban.string
import com.hippo.nimingban.tip
import com.hippo.nimingban.util.explain
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/26/2017.
 */

private const val TAG_POST = "OPERATIONS:post"
private const val TAG_REPLY = "OPERATIONS:reply"

fun post(
    title: String,
    name: String,
    email: String,
    content: String,
    fid: String,
    water: Boolean
) {
  val notification = NotificationCompat.Builder(NMB_APP)
      .setContentText(string(R.string.post_notification_title))
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .setCategory(NotificationCompat.CATEGORY_SOCIAL)
      .setOngoing(false)
      .build()
  val id = NMB_NOTIFICATION.notify(TAG_POST, notification)

  NMB_CLIENT.post(title, name, email, content, fid, water)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        NMB_NOTIFICATION.cancel(TAG_POST, id)
        tip(R.string.post_success)
      }, {
        NMB_NOTIFICATION.cancel(TAG_POST, id)
        tip("${string(R.string.post_failure)}: ${explain(it)}")
      })
}

fun reply(
    title: String,
    name: String,
    email: String,
    content: String,
    resto: String,
    water: Boolean
) {
  val notification = NotificationCompat.Builder(NMB_APP)
      .setContentText(string(R.string.reply_notification_title))
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .setCategory(NotificationCompat.CATEGORY_SOCIAL)
      .setOngoing(false)
      .build()
  val id = NMB_NOTIFICATION.notify(TAG_REPLY, notification)

  NMB_CLIENT.reply(title, name, email, content, resto, water)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        NMB_NOTIFICATION.cancel(TAG_REPLY, id)
        tip(R.string.reply_success)
      }, {
        NMB_NOTIFICATION.cancel(TAG_REPLY, id)
        tip("${string(R.string.reply_failure)}: ${explain(it)}")
      })
}
