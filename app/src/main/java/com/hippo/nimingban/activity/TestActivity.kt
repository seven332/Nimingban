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

package com.hippo.nimingban.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

/*
 * Created by Hippo on 2017/7/5.
 */

class TestActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("TAG", "onCreate")
  }

  override fun onStart() {
    super.onStart()
    Log.d("TAG", "onStart")
  }

  override fun onResume() {
    super.onResume()
    Log.d("TAG", "onResume")
  }



  override fun onPause() {
    super.onPause()
    Log.d("TAG", "onPause")
  }

  override fun onStop() {
    super.onStop()
    Log.d("TAG", "onStop")
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d("TAG", "onDestroy")
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
    Log.d("TAG", "onSaveInstanceState")
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    Log.d("TAG", "onRestoreInstanceState")
  }




}
