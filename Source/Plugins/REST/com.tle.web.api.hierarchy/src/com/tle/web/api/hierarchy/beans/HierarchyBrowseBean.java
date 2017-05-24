package com.tle.web.api.hierarchy.beans;

import java.util.List;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;

/**
 * @author larry
 */
public class HierarchyBrowseBean extends AbstractExtendableBean
{
	private String uuid;
	private String name;
	private String shortDescription;
	private String longDescription;
	private String subTopicsSectionName;
	private String resultsSectionName;
	private String powerSearchUuid;
	private HierarchyBrowseBean parent;
	private List<ItemBean> keyResources;
	private SearchBean<ItemBean> searchResults;
	private List<HierarchyBrowseBean> subTopics;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getShortDescription()
	{
		return shortDescription;
	}

	public void setShortDescription(String shortDescription)
	{
		this.shortDescription = shortDescription;
	}

	public String getLongDescription()
	{
		return longDescription;
	}

	public void setLongDescription(String longDescription)
	{
		this.longDescription = longDescription;
	}

	public String getSubTopicsSectionName()
	{
		return subTopicsSectionName;
	}

	public void setSubTopicsSectionName(String subTopicsSectionName)
	{
		this.subTopicsSectionName = subTopicsSectionName;
	}

	public String getResultsSectionName()
	{
		return resultsSectionName;
	}

	public void setResultsSectionName(String resultsSectionName)
	{
		this.resultsSectionName = resultsSectionName;
	}

	public String getPowerSearchUuid()
	{
		return powerSearchUuid;
	}

	public void setPowerSearchUuid(String powerSearchUuid)
	{
		this.powerSearchUuid = powerSearchUuid;
	}

	public HierarchyBrowseBean getParent()
	{
		return parent;
	}

	public void setParent(HierarchyBrowseBean parent)
	{
		this.parent = parent;
	}

	public List<ItemBean> getKeyResources()
	{
		return keyResources;
	}

	public void setKeyResources(List<ItemBean> keyResources)
	{
		this.keyResources = keyResources;
	}

	public SearchBean<ItemBean> getSearchResults()
	{
		return searchResults;
	}

	public void setSearchResults(SearchBean<ItemBean> searchResults)
	{
		this.searchResults = searchResults;
	}

	public List<HierarchyBrowseBean> getSubTopics()
	{
		return subTopics;
	}

	public void setSubTopics(List<HierarchyBrowseBean> subTopics)
	{
		this.subTopics = subTopics;
	}
}