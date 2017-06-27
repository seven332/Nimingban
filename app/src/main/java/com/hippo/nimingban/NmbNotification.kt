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

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import java.util.concurrent.atomic.AtomicInteger

/*
 * Created by Hippo on 6/27/2017.
 */

class NmbNotification(context: Context) {

  private val manager= context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  private val id = AtomicInteger()

  private fun newId() = id.getAndIncrement()

  fun notify(tag: String, notification: Notification) =
      newId().also { manager.notify(tag, it, notification) }

  fun notify(tag: String, id: Int, notification: Notification) {
    manager.notify(tag, id, notification)
  }

  fun cancel(tag: String, id: Int) {
    manager.cancel(tag, id)
  }
}
