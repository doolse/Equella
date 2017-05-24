package com.tle.common.scripting.types.impl;

import com.dytech.edge.common.valuebean.DefaultGroupBean;
import com.dytech.edge.common.valuebean.GroupBean;
import com.tle.common.scripting.types.GroupScriptType;

/**
 * @author aholland
 */
public class GroupScriptTypeImpl extends DefaultGroupBean implements GroupScriptType
{
	private static final long serialVersionUID = 1L;

	public GroupScriptTypeImpl(GroupBean group)
	{
		super(group.getUniqueID(), group.getName());
	}
}