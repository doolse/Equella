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

package com.tle.upgrade.upgraders;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.common.util.EquellaConfig;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("nls")
public class AddLibAvConfig extends AbstractUpgrader {

  @Override
  public String getId() {
    return "AddLibAvConfig";
  }

  @Override
  public boolean isBackwardsCompatible() {
    return false;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    EquellaConfig config = new EquellaConfig(tleInstallDir);

    result.addLogMessage("Updating optional-config properties");
    updateOptionalProperties(result, config.getConfigDir());
  }

  private void updateOptionalProperties(final UpgradeResult result, File configDir) {
    try {
      LineFileModifier lineMod =
          new LineFileModifier(new File(configDir, PropertyFileModifier.OPTIONAL_CONFIG), result) {
            @Override
            protected String processLine(String line) {
              // Do nothing
              return line;
            }

            @Override
            protected List<String> addLines() {
              String libavComment = "# LibAv path";
              String libavProp = "#libav.path = /path/to/libav";
              return Lists.newArrayList(Constants.BLANK, libavComment, libavProp);
            }
          };

      lineMod.update();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update config file", e);
    }
  }
}
