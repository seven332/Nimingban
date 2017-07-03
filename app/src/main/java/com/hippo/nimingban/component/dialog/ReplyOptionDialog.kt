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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hippo.android.dialog.base.DialogView
import com.hippo.android.dialog.base.DialogViewBuilder
import com.hippo.nimingban.R
import com.hippo.nimingban.client.data.Reply
import com.hippo.nimingban.client.data.Thread
import com.hippo.nimingban.client.referenceText
import com.hippo.nimingban.component.scene.sendScene
import com.hippo.nimingban.component.share
import com.hippo.nimingban.tip
import com.hippo.nimingban.util.string

/*
 * Created by Hippo on 7/3/2017.
 */

class ReplyOptionDialog : NmbDialog() {

  companion object {
    internal const val KEY_THREAD = "ReplyOptionDialog:thread"
    internal const val KEY_REPLY = "ReplyOptionDialog:reply"
  }

  private var thread: Thread? = null
  private var reply: Reply? = null

  override fun onCreate(args: Bundle?) {
    super.onCreate(args)
    if (args != null) {
      thread = args.getParcelable(KEY_THREAD)
      reply = args.getParcelable(KEY_REPLY)
    }
  }

  override fun onCreateDialogView(inflater: LayoutInflater, container: ViewGroup): DialogView {
    val context = inflater.context
    val pm = context.packageManager

    // Get all text processor
    val resolveInfos: List<ResolveInfo>
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      resolveInfos = pm.queryIntentActivities(Intent()
          .setAction(Intent.ACTION_PROCESS_TEXT)
          .setType("text/plain"), 0) ?: emptyList()
    } else {
      resolveInfos = emptyList()
    }

    val items = arrayOf(
        context.string(R.string.reply_option_reply),
        context.string(R.string.reply_option_copy),
        context.string(R.string.reply_option_share),
        context.string(R.string.reply_option_report),
        *resolveInfos.map { it.loadLabel(pm) }.toTypedArray()
    )

    return DialogViewBuilder()
        .items(items) { dialog, which ->
          when(which) {
            // Open send scene
            0 -> { thread?.let { thread ->
              stage?.pushScene(sendScene(thread, reply?.referenceText()))
            } }
            // Copy reply content
            1 -> { reply?.let { reply ->
              val cbm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              cbm.primaryClip = ClipData.newPlainText(null, reply.displayedContent)
              tip(R.string.reply_option_copy_done)
            } }
            // Share reply content
            2 -> { reply?.let { reply ->
              share(reply.displayedContent.toString())
            } }
            // Report the reply
            3 -> {
              // TODO
            }
            //  Text processor applications
            else -> { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { reply?.let { reply ->
              val index = which - 4
              if (index in 0 until resolveInfos.size) {
                val info = resolveInfos[index]
                val intent = Intent()
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .setAction(Intent.ACTION_PROCESS_TEXT)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                    .putExtra(Intent.EXTRA_PROCESS_TEXT, reply.displayedContent.toString())
                startActivity(intent)
              }
            } } }
          }
          dialog.dismiss()
        }
        .build(inflater, container)
  }
}
