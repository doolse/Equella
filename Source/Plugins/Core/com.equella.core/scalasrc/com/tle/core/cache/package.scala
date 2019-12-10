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
