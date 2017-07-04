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

package com.hippo.nimingban.widget.nmb

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import com.facebook.common.memory.PooledByteBuffer
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.GenericDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.hippo.nimingban.R
import com.hippo.nimingban.drawable.TextDrawable
import com.hippo.nimingban.util.IO_EXECUTOR
import com.hippo.nimingban.util.attrColor
import com.hippo.nimingban.util.buffer
import com.hippo.nimingban.util.child
import com.hippo.nimingban.util.sink
import com.hippo.nimingban.util.source
import com.hippo.nimingban.util.stream
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/*
 * Created by Hippo on 6/14/2017.
 */

// TODO Click to show gallery scene
// TODO Long click to refresh

class NmbCover : GenericDraweeView {

  companion object {
    private const val COVER_URL = "http://cover.acfunwiki.org/cover.php"
    private const val COVER_FILENAME = "nmb_cover"

    private val HAS_DOWNLOADED = AtomicBoolean()
    private var COVER_FILE: File? = null
    private val COVER_SUBJECT = BehaviorSubject.create<Boolean>().toSerialized()
  }

  private var disposable: Disposable? = null
  private var hasFailureImage = false

  constructor(context: Context): super(context)
  constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

  init {
    // Show image holder at first
    setImageUri(null)

    if (!HAS_DOWNLOADED.getAndSet(true)) {
      COVER_FILE = getCoverFile()
      downloadCover()
    }
  }

  private fun getCoverFile() = context.cacheDir.child(COVER_FILENAME)

  private fun downloadCover() {
    val imageRequest = ImageRequestBuilder
        .newBuilderWithSource(Uri.parse(COVER_URL))
        .disableDiskCache()
        .build()

    Fresco.getImagePipeline()
        .fetchEncodedImage(imageRequest, null)
        .subscribe(object : BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
          override fun onNewResultImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>) {
            if (!dataSource.isFinished) {
              return
            }
            val ref = dataSource.result
            if (ref != null) {
              try {
                // Write encoded image to cover file
                val buffer = ref.get()
                COVER_FILE?.sink()?.buffer()?.apply {
                  writeAll(buffer.stream().source())
                  close()
                }
                // Report
                COVER_SUBJECT.onNext(true)
              } catch (e: Throwable) {
                // Report
                COVER_SUBJECT.onNext(false)
              } finally {
                CloseableReference.closeSafely(ref)
              }
            }
          }

          override fun onFailureImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>?) {
            // Report
            COVER_SUBJECT.onNext(false)
          }
        }, IO_EXECUTOR)
  }

  private fun setImageUri(uri: Uri?) {
    val controller = Fresco.newDraweeControllerBuilder()
        .setOldController(controller)
        .setUri(uri)
        .setAutoPlayAnimations(true)
        .build()
    setController(controller)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    disposable = COVER_SUBJECT
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          // Set up failure image
          if (!hasFailureImage) {
            hasFailureImage = true
            val failure = TextDrawable("(;´Д`)", 0.8f)
            failure.backgroundColor = context.attrColor(R.attr.backgroundColorStatusBar)
            failure.textColor = context.attrColor(android.R.attr.textColorTertiary)
            hierarchy.setFailureImage(failure)
          }

          setImageUri(Uri.fromFile(COVER_FILE))
        }, { /* Ignore error */ })
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    disposable?.dispose()
    disposable = null
  }
}
