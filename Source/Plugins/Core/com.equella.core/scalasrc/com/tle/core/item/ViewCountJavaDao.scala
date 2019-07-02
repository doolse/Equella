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

package com.tle.core.item

import java.time.Instant
import java.util.Date

import cats.data.Kleisli
import com.tle.beans.Institution
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.item.ItemKey
import com.tle.core.db.tables.{AttachmentViewCount, ItemViewCount}
import com.tle.core.db._
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import scala.collection.JavaConverters._
import zio.interop.catz._

object ViewCountJavaDao {

  val queries                                        = DBSchema.queries.viewCountQueries
  def flush(writes: fs2.Stream[JDBCIO, JDBCWriteOp]) = DBSchema.queries.flush(writes)

  def incrementSummaryViews(itemKey: ItemKey): Unit = RunWithDB.executeWithHibernate {
    getContext.flatMap { uc =>
      flush {
        queries
          .itemCount((uc.inst, itemKey.getUuid, itemKey.getVersion))
          .last
          .flatMap {
            case Some(c) =>
              queries.writeItemCounts
                .update(c, c.copy(count = c.count + 1, last_viewed = Instant.now()))
            case _ =>
              val newCount =
                ItemViewCount(uc.inst, itemKey.getUuid, itemKey.getVersion, 1, Instant.now())
              queries.writeItemCounts.insert(newCount)
          }
      }
    }
  }

  def setSummaryViews(itemKey: ItemKey, views: Int, lastViewed: Instant): Unit =
    RunWithDB.executeWithHibernate {
      getContext.flatMap { uc =>
        flush {
          queries
            .itemCount((uc.inst, itemKey.getUuid, itemKey.getVersion))
            .last
            .flatMap {
              case Some(c) =>
                queries.writeItemCounts.update(c, c.copy(count = views, last_viewed = lastViewed))
              case _ =>
                val newCount =
                  ItemViewCount(uc.inst, itemKey.getUuid, itemKey.getVersion, views, lastViewed)
                queries.writeItemCounts.insert(newCount)
            }
        }
      }
    }

  def incrementAttachmentViews(itemKey: ItemKey, attachment: String): Unit =
    RunWithDB.executeWithHibernate {
      getContext.flatMap { uc =>
        flush {
          queries
            .attachmentCount((uc.inst, itemKey.getUuid, itemKey.getVersion, attachment))
            .last
            .flatMap {
              case Some(c) =>
                queries.writeAttachmentCounts
                  .update(c, c.copy(count = c.count + 1, last_viewed = Instant.now()))
              case _ =>
                val newCount = AttachmentViewCount(uc.inst,
                                                   itemKey.getUuid,
                                                   itemKey.getVersion,
                                                   attachment,
                                                   1,
                                                   Instant.now())
                queries.writeAttachmentCounts.insert(newCount)
            }
        }
      }
    }

  def setAttachmentViews(itemKey: ItemKey,
                         attachment: String,
                         views: Int,
                         lastViewed: Instant): Unit = RunWithDB.executeWithHibernate {
    getContext.flatMap { uc =>
      flush {
        queries
          .attachmentCount((uc.inst, itemKey.getUuid, itemKey.getVersion, attachment))
          .last
          .flatMap {
            case Some(c) =>
              queries.writeAttachmentCounts.update(c,
                                                   c.copy(count = views, last_viewed = lastViewed))
            case _ =>
              val newCount = AttachmentViewCount(uc.inst,
                                                 itemKey.getUuid,
                                                 itemKey.getVersion,
                                                 attachment,
                                                 views,
                                                 lastViewed)
              queries.writeAttachmentCounts.insert(newCount)
          }
      }
    }
  }

  def getAllSummaryViewCount(inst: Institution): java.util.List[ItemViewCount] =
    RunWithDB.executeWithHibernate {
      queries.allItemCount(inst).compile.toVector.map(_.asJava)
    }

  def getSummaryViewCount(itemKey: ItemKey): Int = RunWithDB.executeWithHibernate {
    getContext
      .flatMap { uc =>
        queries.itemCount((uc.inst, itemKey.getUuid, itemKey.getVersion)).map(_.count).compile.last
      }
      .map(_.getOrElse(0))
  }

  def getAttachmentViewCount(itemKey: ItemKey, attachment: String): Int =
    RunWithDB.executeWithHibernate {
      getContext
        .flatMap { uc =>
          queries
            .attachmentCount((uc.inst, itemKey.getUuid, itemKey.getVersion, attachment))
            .map(_.count)
            .compile
            .last
        }
        .map(_.getOrElse(0))
    }

  def getAllAttachmentViewCount(inst: Institution,
                                itemKey: ItemKey): java.util.List[AttachmentViewCount] =
    RunWithDB.executeWithHibernate {
      queries
        .allAttachmentCount(inst, itemKey.getUuid, itemKey.getVersion)
        .compile
        .toVector
        .map(_.asJava)
    }

  def getSummaryViewsForCollection(col: ItemDefinition): Int = RunWithDB.executeWithHibernate {
    queries.countForCollectionId(col.getId).compile.last.map(_.getOrElse(0))
  }

  def getAttachmentViewsForCollection(col: ItemDefinition): Int = RunWithDB.executeWithHibernate {
    queries.attachmentCountForCollectionId(col.getId).compile.last.map(_.getOrElse(0))
  }

  def deleteForItem(item: ItemKey): Unit = RunWithDB.executeWithHibernate {
    getContext.flatMap { uc =>
      flush(queries.deleteForItemId(uc.inst, item.getUuid, item.getVersion))
    }
  }
}
