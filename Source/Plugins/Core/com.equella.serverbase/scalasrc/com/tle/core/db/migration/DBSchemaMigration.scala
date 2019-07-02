/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.db.migration

import com.tle.core.db._
import com.tle.core.migration.{MigrationResult, MigrationStatusLog}
import fs2.Stream
import io.doolse.simpledba.jdbc._
import zio.{RIO, ZIO}

trait DBSchemaMigration {

  type C[A] <: JDBCColumn[A]
  def schemaSQL: SQLDialect
  def mapper: JDBCMapper[C]

  def withSQLLog(progress: MigrationResult): JDBCQueries[C, JDBCStream, JDBCIO] = {
    val subLogger = jdbcEffect.withLogger(new JDBCLogger[JDBCIO] {
      override def logPrepare(sql: String): JDBCIO[Unit] = ZIO.effect {
        progress.addLogEntry(new MigrationStatusLog(sql, false))
      }

      override def logBind(sql: String, values: Seq[Any]): JDBCIO[Unit] = ZIO.unit
    })
    mapper.queries(subLogger)
  }

  def sqlStmts(progress: MigrationResult, sql: Seq[String]): JDBCIO[Unit] = {
    val queries = withSQLLog(progress)
    queries.flush(Stream.emits(sql.map(queries.sql)).covary[JDBCIO])
  }

  def addColumns(columns: TableColumns, progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    sqlStmts(progress, schemaSQL.addColumns(columns))
  }

  def addTables(tables: Seq[TableDefinition], progress: MigrationResult): JDBCIO[Unit] = {
    addTablesAndIndexes(tables, Seq.empty, progress)
  }

  def addTablesAndIndexes(tables: Seq[TableDefinition],
                          indexes: Seq[(TableColumns, String)],
                          progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    val sql = tables.map(schemaSQL.createTable) ++
      indexes.map(i => schemaSQL.createIndex(i._1, i._2))
    sqlStmts(progress, sql)
  }

  def addIndexes(indexes: Seq[(TableColumns, String)], progress: MigrationResult): JDBCIO[Unit] = {
    addTablesAndIndexes(Seq.empty, indexes, progress)
  }

  def auditLogNewColumns: TableColumns
  def viewCountTables: Seq[TableDefinition]

  def newEntityTables: Seq[TableDefinition]
  def newEntityIndexes: Seq[(TableColumns, String)]

  def creationSQL: java.util.Collection[String]
}
