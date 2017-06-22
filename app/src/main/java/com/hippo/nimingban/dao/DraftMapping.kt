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
import com.hippo.nimingban.client.data.Draft
import com.hippo.nimingban.database.MSQLiteBuilder
import com.hippo.nimingban.util.getInt
import com.hippo.nimingban.util.getLong
import com.hippo.nimingban.util.getString
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery

/*
 * Created by Hippo on 6/22/2017.
 */

const val TABLE_DRAFT = "draft"

private const val COLUMN_ID = MSQLiteBuilder.COLUMN_ID
private const val COLUMN_TYPE = "type"
private const val COLUMN_TO_ID = "to_id"
private const val COLUMN_DATE = "date"
private const val COLUMN_CONTENT = "content"

const val DRAFT_COLUMN_TYPE = COLUMN_TYPE
const val DRAFT_COLUMN_TO_ID = COLUMN_TO_ID

fun draftVersion1(sqlBuilder: MSQLiteBuilder) {
  sqlBuilder
      .createTable(TABLE_DRAFT)
      .insertColumn(TABLE_DRAFT, COLUMN_TYPE, Int::class)
      .insertColumn(TABLE_DRAFT, COLUMN_TO_ID, String::class)
      .insertColumn(TABLE_DRAFT, COLUMN_DATE, Long::class)
      .insertColumn(TABLE_DRAFT, COLUMN_CONTENT, String::class)
}

class DraftMapping : SQLiteTypeMapping<Draft>(DraftPutResolver(), DraftGetResolver(), DraftDeleteResolver())

private class DraftPutResolver : DefaultPutResolver<Draft>() {

  override fun mapToUpdateQuery(item: Draft) = UpdateQuery.builder()
      .table(TABLE_DRAFT)
      .where("$COLUMN_ID = ?")
      .whereArgs(item.id)
      .build()

  override fun mapToInsertQuery(item: Draft) = InsertQuery.builder()
      .table(TABLE_DRAFT)
      .build()

  override fun mapToContentValues(item: Draft) = ContentValues(5)
      .also {
        it.put(COLUMN_TYPE, item.type)
        it.put(COLUMN_TO_ID, item.toId)
        it.put(COLUMN_DATE, item.date)
        it.put(COLUMN_CONTENT, item.content)
      }
}

private class DraftGetResolver : DefaultGetResolver<Draft>() {

  override fun mapFromCursor(cursor: Cursor) = Draft(
      cursor.getLong(COLUMN_ID, 0L),
      cursor.getInt(COLUMN_TYPE, Draft.TYPE_POST),
      cursor.getString(COLUMN_TO_ID, null) ?: "",
      cursor.getLong(COLUMN_DATE, 0L),
      cursor.getString(COLUMN_CONTENT, null) ?: "")
}

private class DraftDeleteResolver : DefaultDeleteResolver<Draft>() {

  override fun mapToDeleteQuery(item: Draft) = DeleteQuery.builder()
      .table(TABLE_FORUM)
      .where("$COLUMN_ID = ?")
      .whereArgs(item.id)
      .build()
}
