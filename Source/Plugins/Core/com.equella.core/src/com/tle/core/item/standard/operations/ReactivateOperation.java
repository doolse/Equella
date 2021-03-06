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

package com.tle.core.item.standard.operations;

import com.tle.beans.item.ItemStatus;
import com.tle.common.security.SecurityConstants;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = SecurityConstants.ARCHIVE_ITEM)
@SecureItemStatus(ItemStatus.ARCHIVED)
public class ReactivateOperation extends TaskOperation {
  @Override
  public boolean execute() {
    setState(ItemStatus.LIVE);
    getItem().setModerating(getModerationStatus().isUnarchiveModerating());
    restoreTasksForItem();
    return true;
  }
}
