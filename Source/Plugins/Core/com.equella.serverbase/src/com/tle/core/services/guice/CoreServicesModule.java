/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.services.guice;

import com.tle.common.Check;
import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.ClusteredTaskServiceImpl;
import com.tle.core.services.impl.LocalTaskServiceImpl;
import javax.inject.Singleton;

@SuppressWarnings("nls")
public class CoreServicesModule extends MandatoryConfigModule {
  @Override
  protected void configure() {
    bindURL("admin.url");
    install(new CoreServicesOptionalModule());
    install(new CoreServicesTrackerModule());
  }

  public static class CoreServicesOptionalModule extends OptionalConfigModule {
    @Override
    protected void configure() {
      bindInt("taskService.maxConcurrentTasks", 26);
      bindBoolean("institution.ignorehost", false);

      if (Check.isEmpty(getProperty("zookeeper.instances"))) {
        bind(TaskService.class).to(LocalTaskServiceImpl.class).in(Singleton.class);
      } else {
        bind(TaskService.class).to(ClusteredTaskServiceImpl.class).in(Singleton.class);
      }
    }
  }

  public static class CoreServicesTrackerModule extends PluginTrackerModule {

    @Override
    protected String getPluginId() {
      return "com.tle.core.services";
    }

    @Override
    protected void configure() {
      bindTracker(Object.class, "coreTasks", null).setIdParam("id");
    }
  }
}
