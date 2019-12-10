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

package com.tle.core.institution

import java.io.{InputStream, StringReader}

import cats.effect.{IO, LiftIO}
import com.tle.common.filesystem.FileEntry
import com.tle.common.filesystem.handle.TemporaryFileHandle
import com.tle.legacy.LegacyGuice.fileSystemService
import fs2.{Pipe, Stream}
import io.circe.parser.parse
import io.circe.{Decoder, Encoder}

import scala.collection.JavaConverters._
import cats.syntax.functor._
import zio.{Task, TaskR}

import scala.io.Source

object ExportUtils {

  def asJsonFiles[A, R](
      encoder: Encoder[A],
      path: A => String,
      baseDir: TemporaryFileHandle
  ): Pipe[TaskR[R, ?], A, Unit] = _.evalMap { a =>
    val filepath = path(a)
    Task.effect {
      fileSystemService.write(baseDir, filepath, new StringReader(encoder(a).spaces2), false)
    }.unit
  }

  def fileContentsStream[F[_]](baseDir: TemporaryFileHandle, parentPath: String, fe: FileEntry)(
      implicit L: LiftIO[F]
  ): Stream[F, InputStream] = {
    if (fe.isFolder) {
      val newParentPath = s"$parentPath${fe.getName}/"
      fe.getFiles.asScala
        .foldLeft(Stream.empty: Stream[F, InputStream])(
          (s, fe) => s ++ fileContentsStream(baseDir, newParentPath, fe)
        )
    } else
      Stream.bracket(L.liftIO(IO {
        fileSystemService.read(baseDir, parentPath + fe.getName)
      }))(inp => L.liftIO(IO(inp.close())))
  }

  def jsonFileStream[A, F[_]](
      dec: Decoder[A],
      baseDir: TemporaryFileHandle,
      basePath: String
  )(implicit L: LiftIO[F]): Stream[F, A] = {
    val rootFolder = fileSystemService.enumerateTree(baseDir, basePath, null)

    if (rootFolder.isFolder) {
      fileContentsStream(baseDir, "", rootFolder).map { inp =>
        parse(Source.fromInputStream(inp).mkString)
          .flatMap(dec.decodeJson)
          .fold(throw _, identity)
      }
    } else Stream.empty
  }
}
