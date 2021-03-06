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

package com.tle.web.favourites;

import com.tle.common.search.DefaultSearch;
import com.tle.core.guice.Bind;
import com.tle.web.favourites.itemlist.FavouritesItemList;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.searching.StandardSearchResultType;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import javax.inject.Inject;

@Bind
public class FavouritesSearchResults extends AbstractPrototypeSection<SearchResultsModel>
    implements StandardSearchResultType {
  private static PluginResourceHelper helper =
      ResourcesService.getResourceHelper(FavouritesSearchResults.class);

  @Inject private FavouritesItemList favouritesItemList;

  @Override
  public AbstractItemList<StandardItemListEntry, ?> getCustomItemList() {
    return favouritesItemList;
  }

  @Override
  public String getKey() {
    return helper.key("result.type.standard");
  }

  @Override
  public String getValue() {
    return "standard";
  }

  @Override
  public void register(SectionTree tree, String parentId) {
    tree.registerInnerSection(this, parentId);
    tree.registerInnerSection(favouritesItemList, parentId);
  }

  @Override
  public void addResultTypeDefaultRestrictions(DefaultSearch defaultSearch) {
    // nothing
  }

  @Override
  public boolean isDisabled() {
    return false;
  }
}
