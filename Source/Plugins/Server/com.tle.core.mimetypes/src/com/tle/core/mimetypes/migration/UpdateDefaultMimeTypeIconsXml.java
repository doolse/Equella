/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.mimetypes.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
public class UpdateDefaultMimeTypeIconsXml extends XmlMigrator
{
	@Inject
	private XmlService xmlService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		TemporaryFileHandle mimeFolder = new SubTemporaryFile(staging, "mimetypes"); //$NON-NLS-1$

		for( String entry : xmlHelper.getXmlFileList(mimeFolder) )
		{
			MimeEntry mimeEntry = xmlHelper.readXmlFile(mimeFolder, entry);
			changeDefaultIconPath(mimeEntry);
			xmlHelper.writeFile(mimeFolder, entry, xmlService.serialiseToXml(mimeEntry));
		}
	}

	public static void changeDefaultIconPath(MimeEntry mimeEntry)
	{
		final String iconPath = "PluginIconPath";

		String attr = mimeEntry.getAttribute(iconPath);
		if( !Check.isEmpty(attr) && attr.contains(".gif") )
		{
			mimeEntry.setAttribute(iconPath, attr.replaceAll(".gif", ".png"));
		}
	}
}