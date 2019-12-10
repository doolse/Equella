package com.tle.core.cache

import com.tle.core.events.ApplicationEvent
import com.tle.core.events.ApplicationEvent.PostTo
import com.tle.core.events.listeners.ApplicationListener

trait CacheInvalidation extends ApplicationListener {
  def invalidateKey(cacheId: String, key: String): Unit
}

case class CacheInvalidationEvent(cacheId: String, key: String)
    extends ApplicationEvent[CacheInvalidation](PostTo.POST_TO_OTHER_CLUSTER_NODES) {
  override def getListener: Class[CacheInvalidation] = classOf[CacheInvalidation]

  override def postEvent(listener: CacheInvalidation): Unit = listener.invalidateKey(cacheId, key)
}
