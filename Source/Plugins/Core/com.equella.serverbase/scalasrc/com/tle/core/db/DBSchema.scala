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

package com.tle.core.db

import java.util

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables._
import com.tle.core.db.types._
import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import fs2.Stream
import io.circe.Json
import io.doolse.simpledba._
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import shapeless._
import zio.RIO

import scala.collection.JavaConverters._
import scala.collection.mutable

trait DBSchema extends StdColumns {

  def mapper: JDBCMapper[C]
  val queries: JDBCQueries[C, JDBCStream, JDBCIO] = mapper.queries(jdbcEffect)
  def flush(s: Stream[JDBCIO, JDBCWriteOp])       = queries.flush(s)

  implicit def dbUuidCol: C[DbUUID]

  val schemaSQL: SQLDialect = queries.dialect

  val allTables: mutable.Buffer[TableDefinition]         = mutable.Buffer()
  val allIndexes: mutable.Buffer[(TableColumns, String)] = mutable.Buffer()

  def indexEach(cols: TableColumns, name: NamedColumn => String): Seq[(TableColumns, String)] =
    cols.columns.map { cb =>
      TableColumns(cols.name, Seq(cb)) -> name(cb)
    }

  implicit def dbJsonCol(implicit scol: C[String]): C[Json] =
    wrap[String, Json](scol, _.isoMap(JsonColumn.jsonStringIso), jsonColumnMod)

  def jsonColumnMod(ct: ColumnType): ColumnType = ct

  implicit def jsonColumns[A <: JsonColumn](
      implicit c: Iso[A, Option[String]],
      col: C[Option[String]]
  ): C[A] =
    wrap[Option[String], A](col, _.isoMap[A](c), jsonColumnMod)

  def autoIdCol: C[Long]

  val auditLog = mapper.mapped[AuditLogEntry].table("audit_log_entry").edit('id, autoIdCol).key('id)

  def insertAuditLog: (Long => AuditLogEntry) => JDBCIO[AuditLogEntry]

  val userAndInst = Cols('user_id, 'institution_id)

  val auditLogQueries = {
    val deleteLogs = queries.deleteFrom(auditLog)
    AuditLogQueries(
      insertAuditLog,
      deleteLogs.where(userAndInst, BinOp.EQ).build[(UserId, InstId)],
      queries.query(auditLog).where(userAndInst, BinOp.EQ).build[(UserId, InstId)],
      deleteLogs.where('institution_id, BinOp.EQ).build,
      deleteLogs.where('timestamp, BinOp.LT).build,
      queries.selectFrom(auditLog).count.where('institution_id, BinOp.EQ).buildAs[InstId, Int],
      queries.query(auditLog).where('institution_id, BinOp.EQ).build
    )
  }

  val auditLogTable = auditLog.definition

  val auditLogIndexColumns: TableColumns = auditLog.subset(
    Cols('institution_id, 'timestamp, 'event_category, 'event_type, 'user_id) ++ Cols(
      'session_id,
      'data1,
      'data2,
      'data3
    )
  )

  allTables += auditLogTable
  allIndexes ++= indexEach(auditLogIndexColumns, "audit_" + _.name)

  val auditLogNewColumns = auditLog.subset(Cols('meta))

  val itemViewId    = Cols('inst, 'item_uuid, 'item_version)
  val itemViewCount = mapper.mapped[ItemViewCount].table("viewcount_item").keys(itemViewId)
  val attachmentViewCount = mapper
    .mapped[AttachmentViewCount]
    .table("viewcount_attachment")
    .keys(itemViewId ++ Cols('attachment))

  val viewCountTables = Seq(itemViewCount.definition, attachmentViewCount.definition)

  allTables ++= viewCountTables

  val countByCol = queries.sqlRecord[Tuple1[Long], Tuple1[Option[Int]]](
    "select sum(\"count\") from viewcount_item vci " +
      "inner join item i on vci.item_uuid = i.uuid and vci.item_version = i.version " +
      "inner join base_entity be on be.id = i.item_definition_id where be.id = ?"
  )

  val attachmentViewCountByCol = queries.sqlRecord[Tuple1[Long], Tuple1[Option[Int]]](
    "select sum(\"count\") from viewcount_attachment vca " +
      "inner join attachment a on vca.attachment = a.uuid " +
      "inner join item i on a.item_id = i.id " +
      "inner join base_entity be on be.id = i.item_definition_id where be.id = ?"
  )

  val viewCountQueries = {
    val del1 =
      queries.deleteFrom(itemViewCount).where(itemViewId, BinOp.EQ).build[(InstId, DbUUID, Int)]
    val del2 = queries
      .deleteFrom(attachmentViewCount)
      .where(itemViewId, BinOp.EQ)
      .build[(InstId, DbUUID, Int)]
    ViewCountQueries(
      queries.writes(itemViewCount),
      queries.writes(attachmentViewCount),
      queries.byPK(itemViewCount).build,
      queries.query(itemViewCount).where(Cols('inst), BinOp.EQ).build,
      queries.byPK(attachmentViewCount).build,
      queries
        .query(attachmentViewCount)
        .where(Cols('inst, 'item_uuid, 'item_version), BinOp.EQ)
        .build,
      countByCol.as[Long => Stream[JDBCIO, Option[Int]]].andThen(_.map(_.getOrElse(0))),
      attachmentViewCountByCol
        .as[Long => Stream[JDBCIO, Option[Int]]]
        .andThen(_.map(_.getOrElse(0))),
      id => del1(id) ++ del2(id)
    )
  }

  val settingsRel =
    mapper.mapped[Setting].table("configuration_property").keys(Cols('institution_id, 'property))

  val settingsQueries = SettingsQueries(
    queries.writes(settingsRel),
    queries.byPK(settingsRel).build,
    queries
      .query(settingsRel)
      .where(Cols('institution_id), BinOp.EQ)
      .where(Cols('property), BinOp.LIKE)
      .build,
    queries
      .query(settingsRel)
      .where(Cols('property), BinOp.LIKE)
      .build
  )

  val entityTable = mapper.mapped[OEQEntity].table("entities").keys(Cols('inst_id, 'uuid))

  val entityTypeIdx = (entityTable.subset(Cols('inst_id, 'typeid)), "entityTypeIdx")

  val newEntityTables  = Seq(entityTable.definition)
  val newEntityIndexes = Seq(entityTypeIdx)

  val entityQueries = EntityQueries(
    queries.writes(entityTable),
    queries.query(entityTable).where(Cols('inst_id, 'typeid), BinOp.EQ).build,
    queries.byPK(entityTable).build,
    queries.query(entityTable).where(Cols('inst_id), BinOp.EQ).build
  )

  def cachedValueByValue: ((String255, String, InstId)) => Stream[JDBCIO, CachedValue] =
    queries.query(cachedValues).where(Cols('cache_id, 'value, 'institution_id), BinOp.EQ).build

  val cachedValues = mapper.mapped[CachedValue].table("cached_value").edit('id, autoIdCol).key('id)

  def insertCachedValue: (Long => CachedValue) => JDBCIO[CachedValue]

  val cachedValueQueries = CachedValueQueries(
    insertCachedValue,
    queries.writes(cachedValues),
    queries.query(cachedValues).where(Cols('cache_id, 'key, 'institution_id), BinOp.EQ).build,
    cachedValueByValue
  )

  allTables ++= newEntityTables
  allIndexes ++= newEntityIndexes

  def creationSQL: util.Collection[String] = {
    allTables.map(schemaSQL.createTable) ++
      allIndexes.map(i => schemaSQL.createIndex(i._1, i._2))
  }.asJava

}

object DBSchema {
  lazy private val schemaForDBType: DBQueries with DBSchemaMigration = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver"                        => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver"              => OracleSchema
    }
  }

  def schemaMigration: DBSchemaMigration = schemaForDBType

  def queries: DBQueries = schemaForDBType
}
