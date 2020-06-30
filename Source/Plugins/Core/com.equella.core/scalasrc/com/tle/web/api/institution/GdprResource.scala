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

package com.tle.web.api.institution

import java.io.{OutputStream, PrintStream}
import java.util.zip.{ZipEntry, ZipOutputStream}

import cats.data.Kleisli
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.db._
import com.tle.core.db.tables.AuditLogEntry
import com.tle.core.db.types.UserId
import com.tle.exceptions.AccessDeniedException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.users.UserDetails
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.doolse.simpledba.syntax._
import io.swagger.annotations.{Api, ApiParam}
import javax.ws.rs._
import javax.ws.rs.core.{Response, StreamingOutput}
import zio.interop.catz._

@Produces(value = Array("application/json"))
@Path("userdata/")
@Api("Privacy")
class GdprResource {

  val tleUserDao = LegacyGuice.tleUserDao
  val queries    = DBSchema.queries.auditLogQueries

  case class AuditEntry(category: String,
                        `type`: String,
                        timestamp: String,
                        sessionId: String,
                        data: Map[String, Json])

  object AuditEntry {
    def apply(ale: AuditLogEntry): AuditEntry = {
      val data = Map("1" -> ale.data1.map(_.value.asJson),
                     "2" -> ale.data2.map(_.value.asJson),
                     "3" -> ale.data3.map(_.value.asJson),
                     "4" -> ale.data4.map(_.asJson)).collect {
        case (k, Some(v)) => (k, v)
      } ++ ale.meta.asJson.asObject.map(_.toMap).getOrElse(Map.empty).collect {
        case (k, j) if !j.isNull => (k, j)
      }
      AuditEntry(ale.event_category.value,
                 ale.event_type.value,
                 StdDateFormat.instance.clone().format(ale.timestamp.toEpochMilli),
                 ale.session_id.value,
                 data)
    }
  }

  def checkPriv(): Unit = {
    if (!CurrentUser.getUserState.isSystem)
      throw new AccessDeniedException("Only TLE_ADMINISTRATOR can call this")
  }

  @DELETE
  @Path("{user}")
  def delete(@PathParam("user") @ApiParam(value = "An ID (not a username) of a user",
                                          required = true) user: String): Response = {
    checkPriv()
    RunWithDB.execute(getContext.flatMap { uc =>
      DBSchema.queries.flush(queries.deleteForUser((UserId(user), uc.inst)))
    })
    Response.ok().build()
  }

  @GET
  @Path("{user}")
  @Produces(Array("application/zip"))
  def retrieve(@PathParam("user") @ApiParam(value = "An ID (not a username) of a user",
                                            required = true) user: String): Response = {
    checkPriv()
    Response
      .ok(
        new StreamingOutput {

          override def write(output: OutputStream): Unit = {

            val zip = new ZipOutputStream(output)
            zip.putNextEntry(new ZipEntry("data.json"))
            val print = new PrintStream(zip, true, "UTF8")

            print.println("{")
            def writeUser() = Option(tleUserDao.findByUuid(user)).foreach { tleUser =>
              print.print("\"user\": ")
              print.print(UserDetails.apply(tleUser).asJson.spaces2)
              print.println("\n, ")
            }
            def writeLogs() = {
              print.println("\"auditlog\": [")
              var first = true
              RunWithDB.execute(getContext.flatMap { uc =>
                queries
                  .listForUser((UserId(user), uc.inst))
                  .map { ale =>
                    if (!first) print.print(", ")
                    print.print(AuditEntry(ale).asJson.spaces2)
                    first = false
                  }
                  .compile
                  .drain
              })
              print.print("]")
            }
            writeUser()
            writeLogs()
            print.println("}")
            zip.closeEntry()
            zip.close()
          }
        }
      )
      .build()
  }
}
