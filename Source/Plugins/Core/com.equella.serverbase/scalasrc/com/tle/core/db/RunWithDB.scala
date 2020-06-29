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

import java.sql.Connection
import java.util.Locale

import com.tle.beans.Institution
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.UserState
import com.tle.web.DebugSettings
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import zio._

object RunWithDB {

  val logger = LoggerFactory.getLogger(getClass)

  case class DBContext(connection: Reservation[Any, Throwable, Connection],
                       inst: Institution,
                       user: UserState,
                       locale: Locale,
                       datasource: DataSource)
      extends UserContext
      with JDBCConnection

  def getSessionHolder() = {
    TransactionSynchronizationManager.getResource(defaultSessionFactory).asInstanceOf[SessionHolder]
  }

  def executeWithHibernate[A](jdbc: DB[A]): A = {
    val sessionHolder = getSessionHolder()
    if (sessionHolder == null) {
      sys.error("There is no hibernate session - make sure it's inside @Transactional")
    }
    val con = sessionHolder.getSession().connection()
    val uc  = UserContext.fromThreadLocals()
    dbRuntime.unsafeRun(
      jdbc.provide(
        DBContext(Reservation(ZIO.succeed(con), _ => ZIO.unit),
                  uc.inst,
                  uc.user,
                  uc.locale,
                  uc.datasource)))
  }

  def executeTransaction[A](ds: DataSource, jdbc: JDBCIO[A]): A = {
    val sessionHolder = getSessionHolder()
    if (sessionHolder != null && Option(sessionHolder.getTransaction).exists(_.isActive)) {
      val msg =
        "Hibernate transaction is available on this thread - should be using executeWithHibernate"
      if (DebugSettings.isDebuggingMode) sys.error(msg)
      else logger.error(msg)
    }
    dbRuntime.unsafeRun(
      managedConnection(ds).use { con =>
        jdbc
          .tapBoth(_ => ZIO.effect(con.rollback()).ignore, _ => ZIO.effect(con.commit()).ignore)
          .provide(new JDBCConnection {
            override val connection = Reservation(ZIO.succeed(con), _ => ZIO.unit)
          })
      }
    )
  }

  def executeIfInInstitution[A](db: DB[Option[A]]): Option[A] = {
    Option(CurrentInstitution.get).flatMap(_ => execute(db))
  }

  def execute[A](db: DB[A]): A = {
    val uc = UserContext.fromThreadLocals()
    executeTransaction(
      uc.datasource,
      db.provideSome[JDBCConnection](c =>
        DBContext(c.connection, uc.inst, uc.user, uc.locale, uc.datasource))
    )
  }

  def executeWithPostCommit(db: DB[Task[Unit]]): Unit =
    dbRuntime.unsafeRun(execute(db))

}
