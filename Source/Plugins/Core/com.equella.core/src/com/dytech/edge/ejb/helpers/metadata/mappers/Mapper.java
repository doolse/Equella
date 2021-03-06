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

package com.dytech.edge.ejb.helpers.metadata.mappers;

import com.dytech.edge.ejb.helpers.metadata.mapping.Mapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping.MappingType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/** Abstract class for mappers. */
public abstract class Mapper extends HashMap<String, Collection<Mapping>> {
  public Mapper() {
    super();
  }

  public String process(String data) {
    return data;
  }

  protected void setValue(String key, String value, MappingType type, boolean repeat) {
    Collection<Mapping> col = get(key);
    if (col == null) {
      col = new HashSet<Mapping>();
      put(key, col);
    }
    col.add(new Mapping(key, value, type, repeat));
  }
}
