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

import java.util.Locale

import com.tle.beans.Institution
import com.tle.common.i18n.CurrentLocale
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.{CurrentUser, UserState}
import com.tle.core.hibernate.CurrentDataSource
import javax.sql.DataSource

trait Institutional {
  def inst: Institution
}

trait UserContext extends Institutional {
  def user: UserState
  def locale: Locale
  def datasource: DataSource
}

object UserContext {

  def fromThreadLocals(): UserContext = {
    new UserContext {
      val inst       = CurrentInstitution.get()
      val user       = CurrentUser.getUserState
      val locale     = CurrentLocale.getLocale
      val datasource = Option(CurrentDataSource.get()).map(_.getDataSource).orNull
    }
  }

}
