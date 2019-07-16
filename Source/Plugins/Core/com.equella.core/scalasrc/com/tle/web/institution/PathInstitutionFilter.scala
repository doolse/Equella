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

package com.tle.web.institution

import java.net.URI
import java.util

import com.dytech.edge.web.WebConstants
import com.tle.beans.Institution
import com.tle.common.Pair
import com.tle.common.institution.CurrentInstitution
import com.tle.core.hibernate.CurrentDataSource
import com.tle.core.institution.InstitutionStatus
import com.tle.legacy.LegacyGuice
import com.tle.web.dispatcher.{FilterResult, RemappedRequest}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object PathInstitutionFilter {

  var previousInsts: util.Collection[Pair[String, InstitutionStatus]] = _
  var matches: Iterable[(String, InstitutionStatus)]                  = _

  def filter(request: HttpServletRequest,
             response: HttpServletResponse,
             insts: util.Collection[Pair[String, InstitutionStatus]]): FilterResult = {
    if (previousInsts ne insts) {
      matches = insts.asScala
        .map(p => (URI.create(s"http${p.getFirst}").getPath, p.getSecond))
        .toList
        .sortBy(-_._1.length)
    }

    val servletPath = request.getServletPath
    matches
      .collectFirst {
        case (pfx, instStatus)
            if servletPath.startsWith(pfx) &&
              (servletPath.length == pfx.length || servletPath.charAt(pfx.length) == '/') =>
          val institution = instStatus.getInstitution
          val (defaultPath, checkPath) = if (Institution.FAKE eq institution) {
            CurrentInstitution.remove()
            CurrentDataSource.remove()
            (WebConstants.ADMIN_HOME_PAGE, true)
          } else {
            CurrentInstitution.set(institution)
            CurrentDataSource.set(
              LegacyGuice.schemaDataSourceService.getDataSourceForId(instStatus.getSchemaId))
            (WebConstants.DEFAULT_HOME_PAGE, false)
          }
          val _newPath = servletPath.substring(pfx.length)
          val newPath  = if (_newPath.length < 2) "/" + defaultPath else _newPath
          if (checkPath && !InstitutionFilter.validAdminUrl(newPath.substring(1))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            FilterResult.FILTER_CONTINUE
          } else
            new FilterResult(RemappedRequest.wrap(request, pfx, newPath, request.getPathInfo))
      }
      .getOrElse {
        response.sendError(HttpServletResponse.SC_NOT_FOUND)
        FilterResult.FILTER_CONTINUE
      }
  }
}
