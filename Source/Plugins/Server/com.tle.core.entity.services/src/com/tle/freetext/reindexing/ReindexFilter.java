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

package com.tle.freetext.reindexing;

import java.io.Serializable;

import com.tle.core.services.item.ItemService;

/**
 * @author jmaginnis
 */
public abstract class ReindexFilter implements Serializable
{
	public void updateIndexTimes(ItemService itemService)
	{
		itemService.updateIndexTimes(getWhereClause(), getNames(), getValues());
	}

	protected abstract String getWhereClause();

	protected abstract String[] getNames();

	protected abstract Object[] getValues();

}