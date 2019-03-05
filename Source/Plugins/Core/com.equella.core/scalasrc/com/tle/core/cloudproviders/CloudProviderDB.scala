package com.tle.core.cloudproviders
import java.util.UUID

import cats.data.ValidatedNec
import com.tle.core.db._
import com.tle.core.db.dao.{EntityDB, EntityDBExt}
import com.tle.core.db.tables.OEQEntity
import com.tle.core.validation.EntityValidation
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.doolse.simpledba.Iso
import io.doolse.simpledba.circe.circeJsonUnsafe

case class CloudOAuthCredentials(clientId: String, clientSecret: String)

case class Viewer(name: String, serviceId: String)

case class ServiceUri(authenticated: Boolean, uri: String)

case class CloudProviderData(baseUrl: String,
                             iconUrl: Option[String],
                             providerAuth: CloudOAuthCredentials,
                             oeqAuth: CloudOAuthCredentials,
                             serviceUris: Map[String, ServiceUri],
                             viewers: Map[String, Map[String, Viewer]])

object CloudProviderData {
  import io.circe.generic.auto._
  implicit val decoder = deriveDecoder[CloudProviderData]
  implicit val encoder = deriveEncoder[CloudProviderData]
}

case class CloudProviderDB(entity: OEQEntity, data: CloudProviderData)

object CloudProviderDB {

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

  def proofOfConcept: DB[Unit] =
    for {
      oeq <- EntityDB.newEntity(UUID.randomUUID())
      valid <- flushDB(
        EntityDB.create(CloudProviderDB(
          oeq,
          CloudProviderData("http://mybaseuri/",
                            None,
                            CloudOAuthCredentials("oeq", "secret"),
                            CloudOAuthCredentials("myprovider", "secret"),
                            Map.empty,
                            Map.empty)
        )))
    } yield valid
}
