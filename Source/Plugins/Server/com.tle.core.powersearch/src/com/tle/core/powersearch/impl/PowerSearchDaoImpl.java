/*
 * Created on Oct 26, 2005
 */
package com.tle.core.powersearch.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.powersearch.PowerSearchDao;
import com.tle.core.remoting.RemotePowerSearchService;

/**
 * @author Nicholas Read
 */
@Bind(PowerSearchDao.class)
@Singleton
public class PowerSearchDaoImpl extends AbstractEntityDaoImpl<PowerSearch> implements PowerSearchDao
{
	public PowerSearchDaoImpl()
	{
		super(PowerSearch.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.PowerSearchDao#enumerateItemdefIds(long)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Long> enumerateItemdefIds(long powerSearchId)
	{
		return getHibernateTemplate().findByNamedParam(
			"select i.id from PowerSearch p inner join p.itemdefs i where p.id = :id", "id", powerSearchId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.PowerSearchDao#listAllForSchema(long)
	 */
	@Override
	public List<BaseEntityLabel> listAllForSchema(final long schemaID)
	{
		return listAll(RemotePowerSearchService.ENTITY_TYPE, new ListCallback()
		{
			@Override
			public String getAdditionalWhere()
			{
				return "be.schema.id = :schemaID"; //$NON-NLS-1$
			}

			@Override
			public void processQuery(Query query)
			{
				query.setParameter("schemaID", schemaID); //$NON-NLS-1$
			}

			@Override
			public String getAdditionalJoins()
			{
				return null;
			}

			@Override
			public boolean isDistinct()
			{
				return false;
			}

			@Override
			public String getOrderBy()
			{
				return null;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.PowerSearchDao#getPowerSearchesReferencingItemDefinition
	 * (com.tle.beans.entity.itemdef.ItemDefinition)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<PowerSearch> getPowerSearchesReferencingItemDefinition(ItemDefinition itemDefinition)
	{
		return getHibernateTemplate().findByNamedParam(
			"from PowerSearch p where :itemDefinition in elements(p.itemdefs)", "itemDefinition", itemDefinition);
	}
}