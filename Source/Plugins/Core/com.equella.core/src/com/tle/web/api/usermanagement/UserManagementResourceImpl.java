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

package com.tle.web.api.usermanagement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.usermanagement.standard.service.TLEUserService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.interfaces.beans.UserExportBean;
import com.tle.web.remoting.rest.service.RestImportExportHelper;
import com.tle.web.remoting.rest.service.UrlLinkService;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;

/** See the interface class for the @Path annotations. */
@SuppressWarnings("nls")
@Bind(EquellaUserResource.class)
@Singleton
public class UserManagementResourceImpl implements EquellaUserResource {
  private static final Logger LOGGER = Logger.getLogger(UserManagementResourceImpl.class);

  @SuppressWarnings("unused")
  private static final String APIDOC_USERLOGNAME = "The login name of the user";

  @Inject private TLEAclManager aclManager;
  @Inject private TLEUserService tleUserService;
  @Inject private UrlLinkService urlLinkService;

  @Override
  public SearchBean<UserBean> list(
      UriInfo uriInfo, String query, String parentGroupId, Integer length, boolean recursive) {
    ensurePriv();
    SearchBean<UserBean> result = new SearchBean<UserBean>();

    // if the query is null, leave it as a non-null empty string, otherwise
    // wrap it as a wildcard
    String q = (query == null ? "" : tleUserService.prepareQuery(query));

    List<TLEUser> rawResults =
        tleUserService.searchUsers(
            q, parentGroupId, length != null ? length : Integer.MAX_VALUE, recursive);
    List<UserBean> resultsOfBeans = Lists.newArrayList();

    for (TLEUser tleUser : rawResults) {
      UserBean newB = apiUserBeanFromTLEUser(tleUser, uriInfo);
      resultsOfBeans.add(newB);
    }

    result.setStart(0);
    result.setLength(resultsOfBeans.size());
    result.setResults(resultsOfBeans);
    result.setAvailable(rawResults.size());

    return result;
  }

  private boolean setPassword(UserBean userBean, TLEUser tleUser, boolean forcePassword) {
    UserExportBean exportDetails = userBean.getExportDetails();
    final String newHashedPassword =
        (exportDetails == null ? null : exportDetails.getPasswordHash());
    if (newHashedPassword != null) {
      tleUser.setPassword(newHashedPassword);
      return false;
    }
    Object password = userBean.get("password");
    if (password != null) {
      String stringPass = (String) password;
      tleUser.setPassword(stringPass);
    } else if (forcePassword) {
      tleUser.setPassword("");
    } else return false;

    return true;
  }

  /**
   * generate a new UUID if the caller hasn't provided one.
   *
   * @param userBean
   * @return
   */
  @Override
  public Response addUser(UserBean userBean) {
    try {
      if (Check.isEmpty(userBean.getId())) {
        userBean.setId(UUID.randomUUID().toString());
      }
      TLEUser tleUser = populateTLEUser(userBean);
      boolean passwordNotHashed = setPassword(userBean, tleUser, true);

      String surelythesameuuid = tleUserService.add(tleUser, passwordNotHashed);
      return Response.status(Status.CREATED)
          .header("X-UUID", surelythesameuuid)
          .location(getSelfLink(surelythesameuuid))
          .build();
    } catch (InvalidDataException ide) {
      return Response.status(Status.BAD_REQUEST).entity(ide.getErrorsAsMap()).build();
    } catch (Throwable t) {
      LOGGER.error("Error adding user", t);
      throw t;
    }
  }

  @Override
  public Response editUser(String uuid, UserBean userBean) {
    String userId = userBean.getId();
    if (userId != null && !uuid.equals(userId)) {
      return Response.status(Status.BAD_REQUEST).entity(ImmutableMap.of("id", "different")).build();
    }

    try {
      TLEUser uneditedUser = tleUserService.get(uuid);
      if (userId == null) {
        userBean.setId(uuid);
      }
      TLEUser editedUser = populateTLEUser(userBean, uneditedUser);
      boolean passwordNotHashed = setPassword(userBean, editedUser, false);
      String postFactoUuid = tleUserService.edit(editedUser, passwordNotHashed);

      return Response.ok(postFactoUuid).build();
    } catch (InvalidDataException ide) {
      return Response.status(Status.BAD_REQUEST).entity(ide.getErrorsAsMap()).build();
    } catch (Throwable t) {
      LOGGER.error("Error editing user", t);
      throw t;
    }
  }

  /**
   * @param uuid
   * @return userBean in Response body, or Response.404
   */
  @Override
  public UserBean getUser(UriInfo uriInfo, String uuid) {
    ensurePriv();
    TLEUser tleUser = tleUserService.get(uuid);
    return userResponse(tleUser, uriInfo);
  }

  @Override
  public UserBean getUserByUsername(UriInfo uriInfo, String username) {
    ensurePriv();
    TLEUser tleUser = tleUserService.getByUsername(username);
    return userResponse(tleUser, uriInfo);
  }

  private UserBean userResponse(TLEUser tleUser, UriInfo uriInfo) {
    if (tleUser != null) {
      UserBean userBean = apiUserBeanFromTLEUser(tleUser, uriInfo);
      return userBean;
    }
    throw new NotFoundException();
  }

  @Override
  public Response deleteUser(String uuid) {
    tleUserService.delete(uuid);
    return Response.status(Status.NO_CONTENT).build();
  }

  private UserBean apiUserBeanFromTLEUser(TLEUser tleUser, UriInfo uriInfo) {
    UserBean newB = new UserBean(tleUser.getUniqueID());
    newB.setEmailAddress(tleUser.getEmailAddress());
    newB.setFirstName(tleUser.getFirstName());
    newB.setLastName(tleUser.getLastName());
    newB.setUsername(tleUser.getUsername());
    newB.setId(tleUser.getUniqueID());
    if (RestImportExportHelper.isExport(uriInfo)) {
      UserExportBean exportBean = new UserExportBean();
      exportBean.setExportVersion("1.0");
      exportBean.setPasswordHash(tleUser.getPassword());
      newB.setExportDetails(exportBean);
    }

    Map<String, String> links =
        Collections.singletonMap("self", getSelfLink(newB.getId()).toString());
    newB.set("links", links);

    return newB;
  }

  public TLEUser populateTLEUser(UserBean userBean) {
    TLEUser tleUser = new TLEUser();
    return populateTLEUser(userBean, tleUser);
  }

  public TLEUser populateTLEUser(UserBean userBean, TLEUser tleUser) {
    tleUser.setEmailAddress(userBean.getEmailAddress());
    tleUser.setFirstName(userBean.getFirstName());
    tleUser.setLastName(userBean.getLastName());
    tleUser.setUsername(userBean.getUsername());
    tleUser.setUuid(userBean.getId());
    return tleUser;
  }

  private void ensurePriv() {
    final Set<String> stillThere = aclManager.filterNonGrantedPrivileges("EDIT_USER_MANAGEMENT");
    if (stillThere.isEmpty()) {
      throw new AccessDeniedException("EDIT_USER_MANAGEMENT not granted");
    }
  }

  private URI getSelfLink(String userUuid) {
    return urlLinkService.getMethodUriBuilder(EquellaUserResource.class, "getUser").build(userUuid);
  }
}
