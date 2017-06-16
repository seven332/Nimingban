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
import com.hippo.nimingban.database.parser.ForumParser
import io.reactivex.Single
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.select

/*
 * Created by Hippo on 6/16/2017.
 */

class NmbDB(val content: Context) {

  companion object {
    val FORUM_PARSER = ForumParser()
  }

  private val sql = NmbSQLite(content)
  private val db = sql.writableDatabase

  fun forums() = db.select(NmbSQLite.TABLE_FORUM)
      .orderBy(NmbSQLite.FORUM_WEIGHT, SqlOrderDirection.ASC)
      .parseList(FORUM_PARSER)

  fun forumsAsSingle() = Single.fromCallable { forums() }
}
