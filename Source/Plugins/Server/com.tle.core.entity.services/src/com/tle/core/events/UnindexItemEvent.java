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

package com.tle.core.events;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.listeners.UnindexItemListener;

/**
 * @author Nicholas Read
 */
public class UnindexItemEvent extends ApplicationEvent<UnindexItemListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemIdKey itemId;

	public UnindexItemEvent(ItemIdKey itemId, boolean self)
	{
		super(self ? PostTo.POST_TO_SELF_SYNCHRONOUSLY : PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.itemId = itemId;
	}

	public ItemIdKey getItemId()
	{
		return itemId;
	}

	@Override
	public Class<UnindexItemListener> getListener()
	{
		return UnindexItemListener.class;
	}

	@Override
	public void postEvent(UnindexItemListener listener)
	{
		listener.unindexItemEvent(this);
	}
}