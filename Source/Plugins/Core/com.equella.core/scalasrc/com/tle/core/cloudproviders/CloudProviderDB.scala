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

package com.tle.core.cloudproviders

import java.util.concurrent.TimeUnit
import java.util.{Locale, UUID}

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import cats.effect.{IO, LiftIO}
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.validated._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.tle.core.db._
import com.tle.core.db.dao.{EntityDB, EntityDBExt}
import com.tle.core.db.tables.OEQEntity
import com.tle.core.validation.{EntityValidation, OEQEntityEdits}
import com.tle.legacy.LegacyGuice
import com.tle.web.DebugSettings
import fs2.Stream
import io.circe.generic.semiauto._
import io.doolse.simpledba.Iso
import io.doolse.simpledba.circe.circeJsonUnsafe
import org.slf4j.LoggerFactory
import zio.ZIO
import zio.interop.catz._

case class CloudProviderData(baseUrl: String,
                             iconUrl: Option[String],
                             vendorId: String,
                             providerAuth: CloudOAuthCredentials,
                             oeqAuth: CloudOAuthCredentials,
                             serviceUrls: Map[String, ServiceUrl],
                             viewers: Map[String, Map[String, Viewer]])

object CloudProviderData {

  implicit val decoderV = deriveDecoder[Viewer]
  implicit val encoderV = deriveEncoder[Viewer]
  implicit val decoderS = deriveDecoder[ServiceUrl]
  implicit val encoderS = deriveEncoder[ServiceUrl]
  implicit val decoderC = deriveDecoder[CloudOAuthCredentials]
  implicit val encoderC = deriveEncoder[CloudOAuthCredentials]

  implicit val decoder = deriveDecoder[CloudProviderData]
  implicit val encoder = deriveEncoder[CloudProviderData]
}

case class CloudProviderDB(entity: OEQEntity, data: CloudProviderData)

object CloudProviderDB {

  val FieldVendorId    = "vendorId"
  val RefreshServiceId = "refresh"
  val Logger           = LoggerFactory.getLogger(getClass)

  val tokenCache =
    LegacyGuice.replicatedCacheService.getCache[String]("cloudRegTokens", 100, 1, TimeUnit.HOURS)

  type CloudProviderVal[A] = ValidatedNec[EntityValidation, A]

  implicit val dbExt: EntityDBExt[CloudProviderDB] =
    new EntityDBExt[CloudProviderDB] {
      val dataIso = circeJsonUnsafe[CloudProviderData]
      val iso = Iso(
        oeq => CloudProviderDB(oeq, dataIso.from(oeq.data)),
        scdb => scdb.entity.copy(data = dataIso.to(scdb.data))
      )

      override def typeId: String = "cloudprovider"
    }

  def toInstance(db: CloudProviderDB): CloudProviderInstance = {
    val oeq  = db.entity
    val data = db.data
    CloudProviderInstance(
      id = oeq.uuid.id,
      name = oeq.name,
      description = oeq.description,
      vendorId = data.vendorId,
      baseUrl = data.baseUrl,
      iconUrl = data.iconUrl,
      providerAuth = data.providerAuth,
      oeqAuth = data.oeqAuth,
      serviceUrls = data.serviceUrls,
      viewers = data.viewers
    )
  }

  case class ProviderStdEdits(reg: CloudProviderRegistration) extends OEQEntityEdits {
    override def name               = reg.name
    override def nameStrings        = None
    override def description        = reg.description
    override def descriptionStrings = None
  }

  def validateRegistrationFields(oeq: OEQEntity,
                                 reg: CloudProviderRegistration,
                                 oeqAuth: CloudOAuthCredentials,
                                 locale: Locale): CloudProviderVal[CloudProviderDB] = {
    EntityValidation.nonBlankString(FieldVendorId, reg.vendorId) *>
      EntityValidation.standardValidation(ProviderStdEdits(reg), oeq, locale).map { newOeq =>
        val data = CloudProviderData(
          baseUrl = reg.baseUrl,
          iconUrl = reg.iconUrl,
          vendorId = reg.vendorId,
          providerAuth = reg.providerAuth,
          oeqAuth = oeqAuth,
          serviceUrls = reg.serviceUrls,
          viewers = reg.viewers
        )
        CloudProviderDB(newOeq, data)
      }
  }

  def validToken(regToken: String): DB[CloudProviderVal[Unit]] = {
    ZIO.effect {
      if (!tokenCache.get(regToken).isPresent)
        EntityValidation("token", "invalid").invalidNec
      else {
        tokenCache.invalidate(regToken)
        ().validNec
      }
    }
  }

  def register(
      regToken: String,
      registration: CloudProviderRegistration): DB[CloudProviderVal[CloudProviderInstance]] =
    validToken(regToken).flatMap {
      case Valid(_) =>
        for {
          oeq    <- EntityDB.newEntity(UUID.randomUUID())
          locale <- getContext.map(_.locale)
          validated = validateRegistrationFields(oeq,
                                                 registration,
                                                 CloudOAuthCredentials.random(),
                                                 locale)
          _ <- validated.traverse(cdb => flushDB(EntityDB.create(cdb)))
        } yield validated.map(toInstance)
      case Invalid(e) => e.invalid[CloudProviderInstance].pure[DB]
    }

  def editRegistered(id: UUID, registration: CloudProviderRegistration)
    : OptionT[DBR, CloudProviderVal[CloudProviderInstance]] =
    EntityDB.readOne(id).flatMap { oeq =>
      doEdit(oeq, registration).mapError(Some(_))
    }

  private def doEdit(
      oeq: CloudProviderDB,
      registration: CloudProviderRegistration): DB[CloudProviderVal[CloudProviderInstance]] =
    for {
      locale <- getContext.map(_.locale)
      validated = validateRegistrationFields(oeq.entity, registration, oeq.data.oeqAuth, locale)
      _ <- validated.traverse(cdb => flushDB(EntityDB.update[CloudProviderDB](oeq.entity, cdb)))
    } yield validated.map(toInstance)

  def refreshRegistration(id: UUID): OptionT[DBR, CloudProviderVal[CloudProviderInstance]] =
    for {
      oeqProvider <- EntityDB.readOne(id)
      provider = toInstance(oeqProvider)
      refreshService <- ZIO.succeed(provider.serviceUrls.get(RefreshServiceId)).some
      validated <- CloudProviderService
        .serviceRequest(refreshService,
                        provider,
                        Map.empty,
                        uri =>
                          sttp
                            .post(uri)
                            .body(CloudProviderRefreshRequest(id))
                            .response(asJson[CloudProviderRegistration]))
        .flatMap { response =>
          response.body match {
            case Right(Right(registration)) => doEdit(oeqProvider, registration).map(Option(_))
            case err =>
              ZIO.effect {
                Logger.warn(s"Failed to refresh provider - $err")
                Option.empty[CloudProviderVal[CloudProviderInstance]]
              }
          }
        }
        .some
    } yield validated

  val createRegistrationToken: DB[String] = {
    LiftIO[DB].liftIO(IO {
      val newToken = UUID.randomUUID().toString
      tokenCache.put(newToken, newToken)
      newToken
    })
  }

  val readAll: Stream[DB, CloudProviderInstance] = {
    EntityDB.readAll[CloudProviderDB].map(toInstance)
  }

  val allProviders: Stream[DB, CloudProviderDetails] = {
    EntityDB.readAll[CloudProviderDB].map { cp =>
      val oeq = cp.entity
      CloudProviderDetails(
        id = oeq.uuid.id,
        name = oeq.name,
        description = oeq.description,
        vendorId = cp.data.vendorId,
        iconUrl = cp.data.iconUrl,
        canRefresh = DebugSettings.isDevMode && cp.data.serviceUrls.contains(RefreshServiceId)
      )
    }
  }

  def deleteRegistration(id: UUID): InstDB[Unit] =
    EntityDB.delete(id).compile.drain

  def get(id: UUID): OptionT[InstDBR, CloudProviderInstance] = {
    EntityDB.readOne(id).map(toInstance)
  }

  def getStream(id: UUID): Stream[InstDB, CloudProviderInstance] = {
    EntityDB.readOneStream(id).map(toInstance)
  }
}
