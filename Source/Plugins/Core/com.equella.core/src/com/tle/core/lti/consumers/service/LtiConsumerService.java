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

package com.tle.core.lti.consumers.service;

import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingBean;

public interface LtiConsumerService
    extends AbstractEntityService<LtiConsumerEditingBean, LtiConsumer> {
  @SuppressWarnings("nls")
  public static final String ENTITY_TYPE = "LTI_CONSUMER";

  LtiConsumer findByConsumerKey(String consumerKey);
}
