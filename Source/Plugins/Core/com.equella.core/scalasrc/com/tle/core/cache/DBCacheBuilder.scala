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

package com.tle.core.cache

import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import com.tle.core.db._
import com.tle.legacy.LegacyGuice
import zio.{RIO, Task, ZIO}

import scala.collection.mutable

object DBCacheBuilder extends CacheInvalidation {

  val globalCaches: mutable.Map[String, ConcurrentMap[String, AnyRef]] = mutable.Map()

  def buildCache[R, K, V](cacheable: Cacheable[R, K, V],
                          backingMap: ConcurrentMap[String, AnyRef] =
                            new ConcurrentHashMap[String, AnyRef]()): Cache[R, K, V] = {
    globalCaches.put(cacheable.cacheId, backingMap)
    new Cache[R, K, V] {
      def invalidate: K => RIO[R, Unit] =
        (k: K) =>
          ZIO.environment[R].flatMap { uc =>
            Task {
              val key = cacheable.key(uc, k)
              backingMap.remove(key)
              LegacyGuice.eventService.publishApplicationEvent(
                CacheInvalidationEvent(cacheable.cacheId, key))
            }
        }

      def get: K => RIO[R, V] =
        (k: K) =>
          ZIO.environment[R].flatMap { uc =>
            val key = cacheable.key(uc, k)
            def compute(s: String): Task[V] =
              dbRuntime.unsafeRun(cacheable.query(k).provide(uc).memoize)
            backingMap.computeIfAbsent(key, compute).asInstanceOf[RIO[R, V]]
        }

    }
  }

  override def invalidateKey(cacheId: String, key: String): Unit = {
    globalCaches.get(cacheId).foreach(_.remove(key))
  }
}
