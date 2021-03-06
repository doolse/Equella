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

package com.dytech.installer;

public class InstallerException extends RuntimeException {
  public InstallerException() {
    super();
  }

  public InstallerException(String message) {
    super(message);
  }

  public InstallerException(Throwable cause) {
    super(cause);
  }

  public InstallerException(String message, Throwable cause) {
    super(message, cause);
  }
}
