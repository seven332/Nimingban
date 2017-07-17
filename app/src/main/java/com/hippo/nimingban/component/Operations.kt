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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.NotificationCompat
import com.hippo.nimingban.NMB_APP
import com.hippo.nimingban.NMB_CLIENT
import com.hippo.nimingban.NMB_DB
import com.hippo.nimingban.NMB_NOTIFICATION
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.string
import com.hippo.nimingban.tip
import com.hippo.nimingban.util.explain
import com.hippo.nimingban.util.quantityString
import com.hippo.nimingban.util.string
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*
 * Created by Hippo on 6/26/2017.
 */

private const val TAG_POST = "OPERATIONS:post"
private const val TAG_REPLY = "OPERATIONS:reply"

internal fun newForumsTip(forums: List<Forum>, context: Context = NMB_APP): String? {
  if (forums.isNotEmpty() && forums.size <= 3) {
    return context.quantityString(R.plurals.refresh_forums_new, forums.size) +
        forums.map { it.displayedName }
            .joinToString(context.string(R.string.refresh_forums_new_joint))
  } else if (forums.size > 3) {
    return context.string(R.string.refresh_forums_new_a_lot, forums.size)
  } else {
    return null
  }
}

fun refreshForums() {
  NMB_CLIENT.forums()
      .subscribeOn(Schedulers.io())
      .subscribe({
        val forums = NMB_DB.setOfficialForums(it)
        val tip = newForumsTip(forums)
        if (tip != null) {
          tip(tip)
        }
      }, { /* Ignore error */ })
}

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

private fun Intent.startActivity() {
  val context = NMB_APP
  val resolveInfo = context.packageManager.resolveActivity(this, 0)
  if (resolveInfo != null) {
    this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(this)
  }
}

fun openUrl(url: String, handleNmb: Boolean) {
  // TODO handle nmb
  val intent = Intent(Intent.ACTION_VIEW)
  intent.data = Uri.parse(url)
  intent.startActivity()
}

fun share(text: String) {
  val intent = Intent()
  intent.action = Intent.ACTION_SEND
  intent.putExtra(Intent.EXTRA_TEXT, text)
  intent.type = "text/plain"
  intent.startActivity()
}
