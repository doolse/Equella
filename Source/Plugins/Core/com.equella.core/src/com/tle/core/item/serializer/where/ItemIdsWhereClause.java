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

package com.tle.core.item.serializer.where;

import com.google.common.base.Preconditions;
import com.tle.common.Check;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.ItemSerializerWhere;
import java.util.Collection;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

@SuppressWarnings("nls")
public class ItemIdsWhereClause implements ItemSerializerWhere {
  private Collection<Long> itemIds;

  public ItemIdsWhereClause(Collection<Long> itemIds) {
    // Don't try executing this query if we have no itemIds, as it's very
    // expensive for large numbers of items
    Preconditions.checkArgument(!Check.isEmpty(itemIds), "You must supply one or more item IDs");

    this.itemIds = itemIds;
  }

  @Override
  public void addWhere(ItemSerializerState state) {
    DetachedCriteria itemQuery = state.getItemQuery();
    itemQuery.add(Restrictions.in("id", itemIds));
  }
}
