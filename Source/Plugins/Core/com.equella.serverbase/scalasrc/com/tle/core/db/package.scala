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

package com.tle.core

import java.sql.Connection

import cats.~>
import com.tle.beans.Institution
import com.tle.common.institution.CurrentInstitution
import com.tle.core.hibernate.{CurrentDataSource, DataSourceHolder}
import com.tle.core.hibernate.impl.HibernateServiceImpl
import fs2.Stream
import io.doolse.simpledba.WriteQueries
import io.doolse.simpledba.fs2._
import io.doolse.simpledba.interop.zio._
import io.doolse.simpledba.jdbc._
import javax.sql.DataSource
import org.hibernate.{SessionFactory, Transaction}
import org.hibernate.classic.Session
import org.slf4j.LoggerFactory
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import zio.Exit.{Failure, Success}
import zio.interop.catz._
import zio._

import scala.util.Try

package object db {

  val logSQL = LoggerFactory.getLogger("org.hibernate.SQL")

  lazy val defaultSessionFactory =
    HibernateServiceImpl.getInstance().getTransactionAwareSessionFactory("main", false)

  val dbRuntime = new BootstrapRuntime {}

  trait JDBCConnection {
    val connection: Reservation[Any, Throwable, Connection]
  }

  type OptionT[R, A] = ZIO[R, Option[Throwable], A]
  type InstDBR       = Institutional with JDBCConnection
  type DBR           = UserContext with JDBCConnection
  type InstDB[A]     = RIO[InstDBR, A]
  type DB[A]         = RIO[DBR, A]
  type JDBCIO[A]     = RIO[JDBCConnection, A]
  type JDBCStream[A] = Stream[JDBCIO, A]

  type Writes[T] = WriteQueries[JDBCStream, JDBCIO, JDBCWriteOp, T]

  def managedConnection(ds: DataSource): Managed[Throwable, Connection] =
    Managed.fromAutoCloseable(ZIO.effect(ds.getConnection()))

  object JDBCIOConnection extends WithJDBCConnection[JDBCStream] {
    override def apply[A](f: Connection => Stream[JDBCIO, A]): Stream[JDBCIO, A] =
      for {
        managedConnection <- Stream.eval(ZIO.access[JDBCConnection](_.connection))
        connection <- Stream.bracket(managedConnection.acquire)(c =>
          managedConnection.release(Exit.succeed(c)).unit)
        res <- f(connection)
      } yield res
  }

  val jdbcEffect = JDBCEffect.withLogger[JDBCStream, JDBCIO](
    JDBCIOConnection,
    new JDBCLogger[JDBCIO] {
      override def logPrepare(sql: String): JDBCIO[Unit]                = ZIO.effect(logSQL.debug(sql))
      override def logBind(sql: String, values: Seq[Any]): JDBCIO[Unit] = ZIO.unit
    }
  )

  val getContext: URIO[UserContext, UserContext] = ZIO.environment
  val getDBContext: URIO[UserContext with JDBCConnection, UserContext with JDBCConnection] =
    ZIO.environment
  val getInst: URIO[Institutional, Institution] = ZIO.access[Institutional](_.inst)

  def dbAttempt[A](db: DB[A]): DB[Either[Throwable, A]] = db.either

  val instStream = Stream.eval(getInst)

  def dbStream[R <: UserContext, A](f: UserContext => Stream[RIO[R, *], A]): Stream[RIO[R, *], A] =
    Stream.eval(getContext).flatMap(f)

  def toJDBCStream[R <: JDBCConnection, A](
      stream: Stream[RIO[R, *], A]): RIO[R, Stream[JDBCIO, A]] =
    ZIO.environment[R].map { ctx =>
      stream.translate(new (RIO[R, *] ~> JDBCIO) {
        override def apply[A](fa: RIO[R, A]): JDBCIO[A] = fa.provide(ctx)
      })
    }
  def flushDB(writes: Stream[JDBCIO, JDBCWriteOp]): DB[Unit] = jdbcEffect.flush(writes)

}
