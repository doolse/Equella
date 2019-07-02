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

package com.tle.core.db.dao
import java.time.Instant
import java.util.UUID

import com.tle.core.db._
import com.tle.core.db.tables.OEQEntity
import com.tle.core.db.types.{DbUUID, LocaleStrings, UserId}
import fs2.Stream
import io.circe.Json
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import zio.interop.catz._

trait EntityDBExt[A] {
  def iso: Iso[OEQEntity, A]
  def typeId: String
}

object EntityDB {

  def flush(s: Stream[JDBCIO, JDBCWriteOp]) = DBSchema.queries.flush(s)
  val queries                               = DBSchema.queries.entityQueries

  def newEntity[A](uuid: UUID)(implicit ee: EntityDBExt[A]): DB[OEQEntity] =
    getContext.map(
      uc =>
        OEQEntity(
          uuid = DbUUID(uuid),
          inst_id = uc.inst,
          typeid = ee.typeId,
          "",
          LocaleStrings.empty,
          None,
          LocaleStrings.empty,
          UserId(uc.user.getUserBean.getUniqueID),
          Instant.now(),
          Instant.now(),
          Json.obj()
      )
    )

  def readAll[A](implicit ee: EntityDBExt[A]): Stream[InstDB, A] =
    instStream.flatMap { inst =>
      queries.allByType(inst, ee.typeId).map(ee.iso.to)
    }

  def delete(uuid: UUID): Stream[InstDB, Unit] = {
    instStream.flatMap { inst =>
      for {
        oeq <- queries.byId(inst, DbUUID(uuid))
        _   <- Stream.eval(flush(queries.write.delete(oeq)))
      } yield ()
    }
  }

  def readOneStream[A](uuid: UUID)(implicit ee: EntityDBExt[A]): Stream[InstDB, A] =
    instStream.flatMap { inst =>
      queries.byId(inst, DbUUID(uuid)).map(ee.iso.to)
    }

  def readOne[A](uuid: UUID)(implicit ee: EntityDBExt[A]): OptionT[InstDBR, A] =
    readOneStream[A](uuid).compile.last.some

  def update[A](original: OEQEntity, editedData: A)(
      implicit ee: EntityDBExt[A]
  ): Stream[JDBCIO, JDBCWriteOp] = {

    queries.write.update(original, ee.iso.from(editedData))
  }

  def create[A](newEntity: A)(implicit ee: EntityDBExt[A]): Stream[JDBCIO, JDBCWriteOp] = {
    queries.write.insert(ee.iso.from(newEntity))
  }

}
