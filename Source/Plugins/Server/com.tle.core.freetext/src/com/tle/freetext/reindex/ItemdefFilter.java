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

package com.tle.freetext.reindex;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.freetext.reindexing.ReindexFilter;

@SuppressWarnings("nls")
public class ItemdefFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"itemdef"};

	private Object[] values;

	public ItemdefFilter(ItemDefinition itemdef)
	{
		values = new Object[]{itemdef.getId()};
	}

	@Override
	protected String getWhereClause()
	{
		return "where itemDefinition.id = :itemdef";
	}

	@Override
	protected String[] getNames()
	{
		return NAMES;
	}

	@Override
	protected Object[] getValues()
	{
		return values;
	}
}