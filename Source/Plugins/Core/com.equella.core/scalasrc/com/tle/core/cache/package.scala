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

import com.tle.core.db.Institutional
import zio.{RIO, ZIO}

package object cache {

  trait Cacheable[R, K, V] {
    def cacheId: String

    def key(userContext: R, v: K): String

    def query: K => RIO[R, V]
  }

  case class InstCacheable[R <: Institutional, V](cacheId: String, query: Unit => RIO[R, V])
      extends Cacheable[R, Unit, V] {
    def key(userContext: R, v: Unit): String = userContext.inst.getUniqueId.toString
  }

  trait Cache[R, K, V] {
    def invalidate: K => RIO[R, Unit]
    def get: K => RIO[R, V]
    def getIfValid(k: K, valid: V => Boolean): RIO[R, V] = get(k).flatMap { value =>
      if (valid(value)) ZIO.succeed(value)
      else invalidate(k).flatMap(_ => get(k))
    }
  }

}
