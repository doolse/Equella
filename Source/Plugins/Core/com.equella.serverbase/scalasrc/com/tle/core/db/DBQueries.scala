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

import java.time.Instant

import com.tle.core.db.tables._
import com.tle.core.db.types.{DbUUID, InstId, String20, String255, UserId}
import fs2.Stream
import io.doolse.simpledba.jdbc.JDBCWriteOp

case class AuditLogQueries(
    insertNew: (Long => AuditLogEntry) => JDBCIO[AuditLogEntry],
    deleteForUser: ((UserId, InstId)) => Stream[JDBCIO, JDBCWriteOp],
    listForUser: ((UserId, InstId)) => Stream[JDBCIO, AuditLogEntry],
    deleteForInst: InstId => Stream[JDBCIO, JDBCWriteOp],
    deleteBefore: Instant => Stream[JDBCIO, JDBCWriteOp],
    countForInst: InstId => Stream[JDBCIO, Int],
    listForInst: InstId => Stream[JDBCIO, AuditLogEntry]
)

case class ViewCountQueries(
    writeItemCounts: Writes[ItemViewCount],
    writeAttachmentCounts: Writes[AttachmentViewCount],
    itemCount: ((InstId, DbUUID, Int)) => Stream[JDBCIO, ItemViewCount],
    allItemCount: InstId => Stream[JDBCIO, ItemViewCount],
    attachmentCount: (
        (InstId, DbUUID, Int, DbUUID)
    ) => Stream[JDBCIO, AttachmentViewCount],
    allAttachmentCount: (
        (InstId, DbUUID, Int)
    ) => Stream[JDBCIO, AttachmentViewCount],
    countForCollectionId: Long => Stream[JDBCIO, Int],
    attachmentCountForCollectionId: Long => Stream[JDBCIO, Int],
    deleteForItemId: ((InstId, DbUUID, Int)) => Stream[JDBCIO, JDBCWriteOp]
)

case class SettingsQueries(
    write: Writes[Setting],
    query: ((InstId, String)) => Stream[JDBCIO, Setting],
    prefixQuery: ((InstId, String)) => Stream[JDBCIO, Setting],
    prefixAnyInst: String => Stream[JDBCIO, Setting]
)

case class EntityQueries(
    write: Writes[OEQEntity],
    allByType: ((InstId, String20)) => Stream[JDBCIO, OEQEntity],
    byId: ((InstId, DbUUID)) => Stream[JDBCIO, OEQEntity],
    allByInst: InstId => Stream[JDBCIO, OEQEntity]
)

case class CachedValueQueries(
    insertNew: (Long => CachedValue) => JDBCIO[CachedValue],
    writes: Writes[CachedValue],
    getForKey: ((String255, String255, InstId)) => Stream[JDBCIO, CachedValue],
    getForValue: ((String255, String, InstId)) => Stream[JDBCIO, CachedValue])

trait DBQueries {

  def auditLogQueries: AuditLogQueries

  def viewCountQueries: ViewCountQueries

  def settingsQueries: SettingsQueries

  def entityQueries: EntityQueries

  def cachedValueQueries: CachedValueQueries

  def flush(s: Stream[JDBCIO, JDBCWriteOp]): JDBCIO[Unit]
}
