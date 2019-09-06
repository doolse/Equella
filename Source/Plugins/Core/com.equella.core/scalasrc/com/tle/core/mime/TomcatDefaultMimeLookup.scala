package com.tle.core.mime

import com.tle.beans.mime.MimeEntry
import com.tle.core.mimetypes.MimeTypeService.DefaultMimeLookup
import com.tle.core.mimetypes.institution.MimeMigrator
import org.apache.catalina.Context

import scala.collection.JavaConverters._

class TomcatDefaultMimeLookup(context: Context) extends DefaultMimeLookup {

  val defaultMap = MimeMigrator.getDefaultMimeEntries.asScala.map(me => (me.getType, me)).toMap

  override def byMimeType(mimeType: String): MimeEntry = {
    defaultMap.getOrElse(mimeType, {
      val me = new MimeEntry
      me.setType(mimeType)
      me
    })
  }

  override def byExtension(extension: String): MimeEntry =
    Option(context.findMimeMapping(extension)).map(byMimeType).orNull
}
