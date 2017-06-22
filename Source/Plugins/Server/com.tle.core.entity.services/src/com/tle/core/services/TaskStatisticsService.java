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

package com.tle.core.services;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.Item;
import com.tle.common.workflow.node.WorkflowItem;

public interface TaskStatisticsService
{
	public enum Trend
	{
		WEEK(7), MONTH(30);

		private final int days;

		Trend(int days)
		{
			this.days = days;
		}

		public int getDays()
		{
			return days;
		}
	}

	List<TaskTrend> getWaitingTasks(Trend trend);

	List<TaskTrend> getWaitingTasksForWorkflow(String uuid, Trend trend);

	void enterTask(Item item, WorkflowItem task, Date entry);

	void exitTask(Item item, WorkflowItem task, Date entry);

	void exitAllTasksForItem(Item item, Date end);

	void restoreTasksForItem(Item item);
}