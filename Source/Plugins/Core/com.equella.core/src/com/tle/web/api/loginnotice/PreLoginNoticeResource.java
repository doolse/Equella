/*
 * Copyright 2017 Apereo
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

package com.tle.web.api.loginnotice;

import io.swagger.annotations.Api;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/** @author Samantha Fisher */
@Path("preloginnotice/")
@Api("Pre Login Notice")
public interface PreLoginNoticeResource {
  @GET
  @Produces("text/plain")
  Response retrievePreLoginNotice();

  @PUT
  Response setPreLoginNotice(String loginNotice);

  @DELETE
  Response deletePreLoginNotice();
}