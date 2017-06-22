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

package com.tle.core.tasks;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.filters.FilterFactory;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class RemoveDeletedItems implements ScheduledTask
{
	@Inject
	private ItemService itemService;
	@Inject
	private FilterFactory filterFactory;
	@Inject(optional = true)
	// can be overrode by the optional-config.properties
	@Named("com.tle.core.tasks.RemoveDeletedItems.daysBeforeRemoval")
	private int daysBeforeRemoval = 7;

	@Override
	public void execute()
	{
		itemService.operateAll(filterFactory.removeDeleted(daysBeforeRemoval));
	}
}