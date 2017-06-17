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

import android.database.sqlite.SQLiteDatabase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
 * Created by Hippo on 6/17/2017.
 */

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class MSQLiteTest {

  companion object {
    val DB_NAME = "test.db"

    val VERSION_1_TABLE_A = "A"
    val VERSION_1_COLUMN_BOOLEAN1 = "boolean1"
    val VERSION_1_COLUMN_BYTE1 = "byte1"
    val VERSION_1_COLUMN_SHORT1 = "short1"
    val VERSION_1_COLUMN_INT1 = "int1"
    val VERSION_2_TABLE_B = "B"
    val VERSION_2_COLUMN_LONG1 = "long1"
    val VERSION_2_COLUMN_FLOAT1 = "float1"
    val VERSION_3_COLUMN_DOUBLE1 = "double1"
    val VERSION_3_COLUMN_STRING1 = "string1"

    val TABLE_PRIMARY_KEY_INT = "primary_key_int"
    val PRIMARY_KEY_INT_COLUMN = "int1"
    val TABLE_PRIMARY_KEY_FLOAT = "primary_key_float"
    val PRIMARY_KEY_FLOAT_COLUMN = "float1"
    val TABLE_PRIMARY_KEY_STRING = "primary_key_string"
    val PRIMARY_KEY_STRING_COLUMN = "string1"
  }

  private val sqlBuilder: MSQLiteBuilder = MSQLiteBuilder()
      .version(1)
      .createTable(VERSION_1_TABLE_A)
      .insertColumn(VERSION_1_TABLE_A, VERSION_1_COLUMN_BOOLEAN1, Boolean::class)
      .insertColumn(VERSION_1_TABLE_A, VERSION_1_COLUMN_BYTE1, Byte::class)
      .insertColumn(VERSION_1_TABLE_A, VERSION_1_COLUMN_SHORT1, Short::class)
      .insertColumn(VERSION_1_TABLE_A, VERSION_1_COLUMN_INT1, Int::class)
      .version(2)
      .createTable(VERSION_2_TABLE_B)
      .insertColumn(VERSION_2_TABLE_B, VERSION_2_COLUMN_LONG1, Long::class)
      .insertColumn(VERSION_2_TABLE_B, VERSION_2_COLUMN_FLOAT1, Float::class)
      .version(3)
      .dropTable(VERSION_1_TABLE_A)
      .insertColumn(VERSION_2_TABLE_B, VERSION_3_COLUMN_DOUBLE1, Double::class)
      .insertColumn(VERSION_2_TABLE_B, VERSION_3_COLUMN_STRING1, String::class)
      .version(4)
      .createTable(TABLE_PRIMARY_KEY_INT, PRIMARY_KEY_INT_COLUMN, Int::class)
      .createTable(TABLE_PRIMARY_KEY_FLOAT, PRIMARY_KEY_FLOAT_COLUMN, Float::class)
      .createTable(TABLE_PRIMARY_KEY_STRING, PRIMARY_KEY_STRING_COLUMN, String::class)

  @Test
  fun testCustomPrimaryKey() {
    val app = RuntimeEnvironment.application
    val db = sqlBuilder.build(app, DB_NAME, 4).readableDatabase

    columnsEquals(db, TABLE_PRIMARY_KEY_INT, PRIMARY_KEY_INT_COLUMN)
    columnsEquals(db, TABLE_PRIMARY_KEY_FLOAT, PRIMARY_KEY_FLOAT_COLUMN)
    columnsEquals(db, TABLE_PRIMARY_KEY_STRING, PRIMARY_KEY_STRING_COLUMN)
  }

  @Test
  fun testUpdate() {
    val app = RuntimeEnvironment.application

    val helper1 = sqlBuilder.build(app, DB_NAME, 1)
    val db1 = helper1.readableDatabase
    assertTrue { existsTable(db1, VERSION_1_TABLE_A) }
    assertFalse { existsTable(db1, VERSION_2_TABLE_B) }
    columnsEquals(db1, VERSION_1_TABLE_A,
        MSQLiteBuilder.COLUMN_ID,
        VERSION_1_COLUMN_BOOLEAN1,
        VERSION_1_COLUMN_BYTE1,
        VERSION_1_COLUMN_SHORT1,
        VERSION_1_COLUMN_INT1)
    db1.close()
    helper1.close()

    val helper2 = sqlBuilder.build(app, DB_NAME, 2)
    val db2 = helper2.readableDatabase
    assertTrue { existsTable(db2, VERSION_1_TABLE_A) }
    assertTrue { existsTable(db2, VERSION_2_TABLE_B) }
    columnsEquals(db2, VERSION_2_TABLE_B,
        MSQLiteBuilder.COLUMN_ID,
        VERSION_2_COLUMN_LONG1,
        VERSION_2_COLUMN_FLOAT1)
    db2.close()
    helper2.close()
  }

  private fun existsTable(db: SQLiteDatabase, table: String): Boolean {
    db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$table';", null).use {
      return it.moveToNext()
    }
  }

  private fun columnsEquals(db: SQLiteDatabase, table: String, vararg columns: String) {
    db.query(table, null, null, null, null, null, null).use {
      val columnList = it.columnNames.asList()
      assertEquals(columnList.size, columns.size)
      for (column in columns) {
        columnList.contains(column)
      }
    }
  }
}
