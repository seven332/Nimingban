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

package com.hippo.nimingban.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.hippo.nimingban.client.data.Forum
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.PRIMARY_KEY
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.UNIQUE
import org.jetbrains.anko.db.createTable

/*
 * Created by Hippo on 6/16/2017.
 */

class NmbSQLite(context: Context) : ManagedSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

  companion object {
    private const val DB_NAME = "nmb2"
    private const val DB_VERSION = 1

    const val TABLE_FORUM = "Forum"
    const val FORUM_WEIGHT = "weight"
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.createTable(TABLE_FORUM, true,
        Forum.ID to TEXT + PRIMARY_KEY + UNIQUE,
        Forum.FGROUP to TEXT,
        Forum.SORT to TEXT,
        Forum.NAME to TEXT,
        Forum.SHOW_NAME to TEXT,
        Forum.MSG to TEXT,
        Forum.INTERVAL to TEXT,
        Forum.CREATED_AT to TEXT,
        Forum.UPDATE_AT to TEXT,
        Forum.STATUS to TEXT,
        FORUM_WEIGHT to INTEGER)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // TODO("not implemented")
  }

  override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // TODO("not implemented")
  }
}
