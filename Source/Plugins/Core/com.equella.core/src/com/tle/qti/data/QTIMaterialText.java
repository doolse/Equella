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

package com.tle.qti.data;

import java.io.Serializable;

/**
 * Represents some text to make up a QTIMaterial
 *
 * @author will
 */
public class QTIMaterialText implements QTIMaterialElement, Serializable {
  private static final long serialVersionUID = 1L;
  private String text;
  private boolean bold = false;

  public QTIMaterialText(String text, boolean bold) {
    this.text = text;
    this.bold = bold;
  }

  public QTIMaterialText(String text) {
    this(text, false);
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  @SuppressWarnings("nls")
  @Override
  public String getHtml() {
    if (bold) {
      return "<strong>" + text + "</strong>";
    } else {
      return text;
    }
  }

  public void setBold(boolean bold) {
    this.bold = bold;
  }

  public boolean isBold() {
    return bold;
  }
}
