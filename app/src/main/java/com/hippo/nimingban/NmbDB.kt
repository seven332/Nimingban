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

import android.content.Context
import com.hippo.nimingban.architecture.LiveData
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.ForumGroup
import com.hippo.nimingban.dao.FORUM_COLUMN_WEIGHT
import com.hippo.nimingban.dao.ForumMapping
import com.hippo.nimingban.dao.TABLE_FORUM
import com.hippo.nimingban.dao.forumVersion1
import com.hippo.nimingban.database.MSQLiteBuilder
import com.hippo.nimingban.util.asMutableList
import com.hippo.nimingban.util.removeFirst
import com.hippo.nimingban.util.transaction
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.Query

/*
 * Created by Hippo on 6/17/2017.
 */

class NmbDB(context: Context) {

  companion object {
    private const val DB_NAME = "nmb2"
    private const val DB_VERSION = 1
  }

  private val sql = DefaultStorIOSQLite.builder()
      .sqliteOpenHelper(
          MSQLiteBuilder()
              .also { it.version(1) }
              .also { forumVersion1(it) }
              .build(context, DB_NAME, DB_VERSION))
      .addTypeMapping(Forum::class.java, ForumMapping())
      .build()

  val liveForums: LiveData<List<Forum>> = LiveData(forums())

  /**
   * Sets official forums. Keeps origin order. Appends new official forums at end.
   * Removes old official forums which don't exist. Keeps custom forums.
   */
  fun setOfficialForums(forums: List<ForumGroup>) {
    sql.transaction {
      val newForums = forums
          .flatMap { it.forums }
          .asMutableList()

      val oldForums = forums()

      val currentForums = oldForums
          // Replace old forum with new forum, with origin order, keep custom forum
          .map {forum -> newForums.removeFirst { forum.id == it.id } ?: if (forum.official) null else forum }
          .filterNotNull()
          .asMutableList()
          // Add remain new forums
          .also { it.addAll(newForums) }

      // Fill weight with list order
      var weight = 0
      currentForums.forEach { it.weight = weight++ }

      // Delete all old forums
      sql.delete()
          .byQuery(DeleteQuery.builder()
              .table(TABLE_FORUM)
              .build())
          .prepare()
          .executeAsBlocking()

      // Put all new forums
      sql.put()
          .objects(currentForums)
          .prepare()
          .executeAsBlocking()

      // Update live data
      liveForums.data = currentForums
    }
  }

  /**
   * Add a forum.
   */
  fun addForum(forum: Forum) {
    sql.transaction {
      // Get the forum with the max weight
      val lastForum = sql.get()
          .`object`(Forum::class.java)
          .withQuery(Query.builder()
              .table(TABLE_FORUM)
              .orderBy("$FORUM_COLUMN_WEIGHT DESC")
              .limit(1)
              .build())
          .prepare()
          .executeAsBlocking()

      forum.weight = (lastForum?.weight ?: -1) + 1

      sql.put()
          .`object`(forum)
          .prepare()
          .executeAsBlocking()

      // Update live data
      liveForums.data = forums()
    }
  }

  /**
   * Remove a forum.
   */
  fun removeForum(forum: Forum) {
    sql.transaction {
      sql.delete()
          .`object`(forum)
          .prepare()
          .executeAsBlocking()

      // Update live data
      liveForums.data = forums()
    }
  }

  /**
   * Order a forum.
   */
  fun orderForum(oldIndex: Int, newIndex: Int) {
    if (oldIndex == newIndex) return
    sql.transaction {
      val forums = forums()
      if (oldIndex !in 0 until forums.size) return
      if (newIndex !in 0 until forums.size) return

      val forumsToUpdate: List<Forum>
      val range: IntProgression
      if (oldIndex < newIndex) {
        forumsToUpdate = forums.subList(oldIndex, newIndex + 1)
        range = oldIndex .. newIndex
      } else {
        forumsToUpdate = forums.subList(newIndex, oldIndex + 1)
        range = oldIndex downTo newIndex
      }

      var oldWeight = forums[newIndex].weight
      for (index in range) {
        val weight = oldWeight
        oldWeight = forums[index].weight
        forums[index].weight = weight
      }

      sql.put()
          .objects(forumsToUpdate)
          .prepare()
          .executeAsBlocking()

      // Update live data
      liveForums.data = forums.sortedBy { it.weight }
    }
  }

  /**
   * Get all forums.
   */
  internal fun forums(): List<Forum> = sql.get()
      .listOfObjects(Forum::class.java)
      .withQuery(Query.builder()
          .table(TABLE_FORUM)
          .orderBy("$FORUM_COLUMN_WEIGHT ASC")
          .build())
      .prepare()
      .executeAsBlocking()
}
