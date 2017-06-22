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

import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.scripting.ScriptEvaluator;
import com.tle.core.scripting.WorkflowScriptObjectContributor;

/**
 * @author aholland
 */
public interface WorkflowOperationService extends ScriptEvaluator, WorkflowScriptObjectContributor
{
	ScriptContext createScriptContext(ItemPack itemPack, FileHandle fileHandle,
		Map<String, Object> attributes, Map<String, Object> objects);

	boolean isAnOwner(Item item, String userUuid);

	UserBean getOwner(Item item);

	void updateMetadataBasedSecurity(PropBagEx itemxml, Item item);

}