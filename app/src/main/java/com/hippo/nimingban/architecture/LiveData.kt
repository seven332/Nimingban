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

package com.hippo.nimingban.architecture

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

/*
 * Created by Hippo on 6/16/2017.
 */

/**
 * LiveData is a observable. Observe it to catch change.
 */
class LiveData<T>(data: T) {

  var data: T = data
    set(value) {
      field = value
      observable.onNext(value)
    }

  val observable: Subject<T> = BehaviorSubject.createDefault(data).toSerialized()
}
