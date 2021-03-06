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

package com.tle.core.harvester.oai.data;

/** */
public class OAIError {
  private String code;
  private String message;

  public OAIError(String code2, String message2) {
    this.code = code2;
    this.message = message2;
    if (message == null) {
      message = ""; // $NON-NLS-1$
    }

    if (code == null) {
      code = ""; // $NON-NLS-1$
    }
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
