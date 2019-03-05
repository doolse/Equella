package com.tle.web.api.cloudprovider
import java.util.UUID

import com.tle.core.cloudproviders.CloudProviderDB
import com.tle.web.api.ApiHelper
import io.swagger.annotations.Api
import javax.ws.rs.core.Response
import javax.ws.rs.{POST, Path, Produces}
import cats.syntax.functor._

@Api("Cloud providers")
@Path("cloudprovider")
@Produces(Array("application/json"))
class CloudProviderApi {

  @POST
  def proofOfConcept(): Response = {
    ApiHelper.runAndBuild {
      CloudProviderDB.proofOfConcept.as(Response.ok())
    }
  }
}
