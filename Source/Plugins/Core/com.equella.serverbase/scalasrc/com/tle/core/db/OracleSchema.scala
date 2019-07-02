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

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables.CachedValue
import com.tle.core.db.types.{DbUUID, InstId, String255}
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.oracle._
import io.doolse.simpledba.syntax._
import shapeless.{::, Generic, HNil}

object OracleSchema extends DBSchemaMigration with DBSchema with DBQueries with StdOracleColumns {

  lazy val mapper = oracleMapper
  lazy val hibSeq = Sequence[Long]("hibernate_sequence")

  def autoIdCol = longCol

  lazy val oracleQueries = new OracleQueries(mapper.dialect, queries.effect)

  override def insertAuditLog = oracleQueries.insertWith(auditLog, hibSeq)

  override def insertCachedValue = oracleQueries.insertWith(cachedValues, hibSeq)

  def dbUuidCol =
    wrap[String, DbUUID](stringCol,
                         _.isoMap(Iso(_.id.toString, DbUUID.fromString)),
                         _.copy(typeName = "VARCHAR(36)"))

  override def cachedValueByValue
    : ((String255, String, InstId)) => fs2.Stream[JDBCIO, CachedValue] = {
    queries
      .sqlRecord[(String255, String, InstId), CachedValue](
        "SELECT id,cache_id,\"key\",ttl,value,institution_id FROM cached_value WHERE cache_id = ? AND to_char(value) = ? AND institution_id = ?",
      )
  }
}
