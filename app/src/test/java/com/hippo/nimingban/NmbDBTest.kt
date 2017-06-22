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

import com.hippo.nimingban.client.data.Forum
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/*
 * Created by Hippo on 6/17/2017.
 */

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class NmbDBTest {

  val db = NmbDB(RuntimeEnvironment.application)

  @Test
  fun testForum() {
    val forums = listOf(
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum(),
        randomOfficeForum())

    db.setOfficialForums(listOf(
        randomForumGroup(*forums.slice(0 until forums.size / 2).toTypedArray()),
        randomForumGroup(*forums.slice(forums.size / 2 until forums.size).toTypedArray())
    ))
    var currentForums = forums.toMutableList()
    assertEquals(currentForums, db.forums())

    val customForum = randomCustomForum()
    db.putForum(customForum)
    currentForums.add(customForum)
    assertEquals(currentForums, db.forums())

    db.removeForum(forums[3])
    currentForums.removeAt(3)
    assertEquals(currentForums, db.forums())

    db.orderForum(11, 5)
    currentForums.add(5, currentForums.removeAt(11))
    assertEquals(currentForums, db.forums())

    db.orderForum(7, 10)
    currentForums.add(10, currentForums.removeAt(7))
    assertEquals(currentForums, db.forums())

    db.setOfficialForums(listOf(randomForumGroup(*forums.subList(0, 3).toTypedArray())))
    currentForums = mutableListOf<Forum>().also { it.addAll(forums.subList(0, 3)) }.also { it.add(customForum) }
    assertEquals(currentForums, db.forums())
  }
}
