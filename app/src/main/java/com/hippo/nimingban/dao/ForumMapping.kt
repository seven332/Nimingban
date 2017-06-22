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

package com.hippo.nimingban.dao

import android.content.ContentValues
import android.database.Cursor
import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.database.MSQLiteBuilder
import com.hippo.nimingban.util.getBoolean
import com.hippo.nimingban.util.getInt
import com.hippo.nimingban.util.getString
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery

/*
 * Created by Hippo on 6/17/2017.
 */

const val TABLE_FORUM = "forum"

private const val COLUMN_ID = Forum.ID
private const val COLUMN_FGROUP = Forum.FGROUP
private const val COLUMN_SORT = Forum.SORT
private const val COLUMN_NAME = Forum.NAME
private const val COLUMN_SHOW_NAME = Forum.SHOW_NAME
private const val COLUMN_MSG = Forum.MSG
private const val COLUMN_INTERVAL = Forum.INTERVAL
private const val COLUMN_CREATED_AT = Forum.CREATED_AT
private const val COLUMN_UPDATE_AT = Forum.UPDATE_AT
private const val COLUMN_STATUS = Forum.STATUS
private const val COLUMN_OFFICIAL = "official"
private const val COLUMN_VISIBLE = "visible"
private const val COLUMN_WEIGHT = "weight"

const val FORUM_COLUMN_ID = COLUMN_ID
const val FORUM_COLUMN_WEIGHT = COLUMN_WEIGHT

fun forumVersion1(sqlBuilder: MSQLiteBuilder) {
  sqlBuilder
      .createTable(TABLE_FORUM, COLUMN_ID, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_FGROUP, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_SORT, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_NAME, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_SHOW_NAME, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_MSG, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_INTERVAL, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_CREATED_AT, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_UPDATE_AT, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_STATUS, String::class)
      .insertColumn(TABLE_FORUM, COLUMN_OFFICIAL, Boolean::class)
      .insertColumn(TABLE_FORUM, COLUMN_VISIBLE, Boolean::class)
      .insertColumn(TABLE_FORUM, COLUMN_WEIGHT, Int::class)
}

class ForumMapping : SQLiteTypeMapping<Forum>(ForumPutResolver(), ForumGetResolver(), ForumDeleteResolver())

private class ForumPutResolver : DefaultPutResolver<Forum>() {

  override fun mapToUpdateQuery(item: Forum) = UpdateQuery.builder()
      .table(TABLE_FORUM)
      .where("$COLUMN_ID = ?")
      .whereArgs(item.id)
      .build()

  override fun mapToInsertQuery(item: Forum) = InsertQuery.builder()
      .table(TABLE_FORUM)
      .build()

  override fun mapToContentValues(item: Forum) = ContentValues(13)
      .also {
        it.put(COLUMN_ID, item._id)
        it.put(COLUMN_FGROUP, item._fgroup)
        it.put(COLUMN_SORT, item._sort)
        it.put(COLUMN_NAME, item._name)
        it.put(COLUMN_SHOW_NAME, item._showName)
        it.put(COLUMN_MSG, item._msg)
        it.put(COLUMN_INTERVAL, item._interval)
        it.put(COLUMN_CREATED_AT, item._createdAt)
        it.put(COLUMN_UPDATE_AT, item._updateAt)
        it.put(COLUMN_STATUS, item._status)
        it.put(COLUMN_OFFICIAL, item.official)
        it.put(COLUMN_VISIBLE, item.visible)
        it.put(COLUMN_WEIGHT, item.weight)
      }
}

private class ForumGetResolver : DefaultGetResolver<Forum>() {

  override fun mapFromCursor(cursor: Cursor) = Forum(
      cursor.getString(COLUMN_ID, null),
      cursor.getString(COLUMN_FGROUP, null),
      cursor.getString(COLUMN_SORT, null),
      cursor.getString(COLUMN_NAME, null),
      cursor.getString(COLUMN_SHOW_NAME, null),
      cursor.getString(COLUMN_MSG, null),
      cursor.getString(COLUMN_INTERVAL, null),
      cursor.getString(COLUMN_CREATED_AT, null),
      cursor.getString(COLUMN_UPDATE_AT, null),
      cursor.getString(COLUMN_STATUS, null))
      .also {
        it.init
        it.official = cursor.getBoolean(COLUMN_OFFICIAL, false)
        it.visible = cursor.getBoolean(COLUMN_VISIBLE, true)
        it.weight = cursor.getInt(COLUMN_WEIGHT, 0)
      }
}

private class ForumDeleteResolver : DefaultDeleteResolver<Forum>() {

  override fun mapToDeleteQuery(item: Forum) = DeleteQuery.builder()
      .table(TABLE_FORUM)
      .where("$COLUMN_ID = ?")
      .whereArgs(item.id)
      .build()
}
