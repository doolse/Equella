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

package com.tle.core.schema.impl;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.guice.Bind;
import com.tle.core.schema.SchemaReferences;
import com.tle.core.services.entity.ItemDefinitionService;

@Bind
@Singleton
public class TempCollectionRefs implements SchemaReferences
{

	@Inject
	private ItemDefinitionService collectionService;

	@Override
	public List<BaseEntityLabel> getSchemaUses(long id)
	{
		return collectionService.listAllForSchema(id);
	}
}