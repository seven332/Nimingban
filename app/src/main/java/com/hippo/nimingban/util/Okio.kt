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

package com.hippo.nimingban.util

import okio.Okio
import okio.Sink
import okio.Source
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/*
 * Created by Hippo on 2017/7/4.
 */

fun Source.buffer() = Okio.buffer(this)

fun Sink.buffer() = Okio.buffer(this)

fun InputStream.source() = Okio.source(this)

fun File.source() = Okio.source(this)

fun OutputStream.sink() = Okio.sink(this)

fun File.sink() = Okio.sink(this)
