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

package com.tle.core.portal.service;

import java.util.List;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.common.portal.entity.Portlet;

public interface PortletServiceExtension
{
	void doValidation(PortletEditingBean newPortlet, List<ValidationError> errors);

	void deleteExtra(Portlet portlet);

	void edit(Portlet oldPortlet, PortletEditingBean newPortlet);

	void add(Portlet portlet);

	void loadExtra(Portlet portlet);

	void changeUserId(String fromUserId, String toUserId);
}