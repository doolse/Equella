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

import java.util.concurrent.{Executors, LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import cats.effect.Blocker
import sttp.client.{NothingT, SttpBackend, SttpBackendOptions}
import sttp.client.SttpBackendOptions.{Proxy, ProxyType}
import sttp.client.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import com.tle.legacy.LegacyGuice
import sttp.client.asynchttpclient.WebSocketHandler
import zio._
import zio.internal.Executor
import zio.interop.catz._

package object httpclient {
  val httpClientThreadpool =
    new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable])
  implicit val httpRuntime = new BootstrapRuntime {
    override val platform = super.platform
      .withExecutor(Executor.fromThreadPoolExecutor(_ => Int.MaxValue)(httpClientThreadpool))

  }

  implicit lazy val sttpBackend = {
    val proxy     = LegacyGuice.configService.getProxyDetails
    val sttpProxy = Option(proxy.getHost).map(h => Proxy(h, proxy.getPort, ProxyType.Http))
    httpRuntime.unsafeRun(
      AsyncHttpClientFs2Backend[Task](SttpBackendOptions.Default.copy(proxy = sttpProxy)))
  }
}
