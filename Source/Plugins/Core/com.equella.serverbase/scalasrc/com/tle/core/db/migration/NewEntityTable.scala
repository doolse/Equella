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
import java.time.Instant
import java.util.{Date, UUID}

import com.tle.core.db._
import com.tle.core.i18n.ServerStrings
import com.tle.core.migration.MigrationResult
import cats.syntax.apply._
import com.tle.core.db.tables.OEQEntity
import com.tle.core.db.types.{DbUUID, LocaleStrings, UserId}
import io.circe.Json
import io.circe.syntax._
import io.doolse.simpledba.syntax._
import io.doolse.simpledba.jdbc._

object NewEntityTable
    extends SimpleMigration(
      "NewEntityTable",
      2019,
      2,
      5,
      ServerStrings.lookup.prefix("mig.newentities")
    ) {
  def migration(progress: MigrationResult, schemaMigration: DBSchemaMigration): JDBCIO[Unit] = {
    schemaMigration.addTablesAndIndexes(
      schemaMigration.newEntityTables,
      schemaMigration.newEntityIndexes,
      progress
    ) *> convertOldSearchConfigs
  }

  def convertOldSearchConfigs: JDBCIO[Unit] = {
    val SearchConfigPFX = "searchconfig."
    val queries         = DBSchema.queries
    val settingsQueries = queries.settingsQueries
    queries
      .flush(
        settingsQueries
          .prefixAnyInst(s"$SearchConfigPFX%")
          .flatMap { setting =>
            val uuid = UUID.fromString(setting.property.substring(SearchConfigPFX.length))

            val configJson = io.circe.parser
              .parse(setting.value)
              .getOrElse(Json.obj())
              .withObject(_.remove("id").asJson)

            val oeq = OEQEntity(
              uuid = DbUUID(uuid),
              inst_id = setting.institution_id,
              typeid = "searchconfig",
              s"Converted search configuration $uuid",
              LocaleStrings.empty,
              None,
              LocaleStrings.empty,
              UserId("system"),
              Instant.now(),
              Instant.now(),
              configJson
            )
            queries.entityQueries.write.insert(oeq) ++ settingsQueries.write.delete(setting)
          })
  }
}
