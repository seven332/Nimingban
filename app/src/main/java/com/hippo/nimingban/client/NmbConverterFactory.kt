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

package com.hippo.nimingban.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hippo.nimingban.client.converter.GsonConverter
import com.hippo.nimingban.client.converter.ThreadsHtmlConverter
import com.hippo.nimingban.client.converter.UnitConverter
import com.hippo.nimingban.client.data.ThreadsHtml
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/*
 * Created by Hippo on 6/21/2017.
 */

class NmbConverterFactory(val gson: Gson) : Converter.Factory() {

  override fun responseBodyConverter(
      type: Type,
      annotations: Array<out Annotation>?,
      retrofit: Retrofit?
  ): Converter<ResponseBody, *>? = when(type) {
    Unit::class.java -> UnitConverter()
    ThreadsHtml::class.java -> ThreadsHtmlConverter()
    else -> {
      val adapter = gson.getAdapter(TypeToken.get(type))
      GsonConverter(adapter)
    }
  }
}
