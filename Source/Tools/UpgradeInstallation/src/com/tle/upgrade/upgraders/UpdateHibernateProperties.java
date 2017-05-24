package com.tle.upgrade.upgraders;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;

public class UpdateHibernateProperties extends AbstractUpgrader
{
	private static final String HIBERNATE_DIALECT = "hibernate.dialect"; //$NON-NLS-1$

	@Override
	public String getId()
	{
		return "UpdateHibernateProperties"; //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		PropertyFileModifier modifier = new PropertyFileModifier(new File(new File(tleInstallDir, CONFIG_FOLDER),
			PropertyFileModifier.HIBERNATE_CONFIG))
		{
			@Override
			protected boolean modifyProperties(PropertiesConfiguration props)
			{
				String dialect = props.getString(HIBERNATE_DIALECT);
				String newdialect = null;
				if( dialect.equals("org.hibernate.dialect.PostgreSQLDialect") )
				{
					newdialect = "com.tle.hibernate.dialect.ExtendedPostgresDialect";
				}
				else if( dialect.equals("org.hibernate.dialect.Oracle10gDialect") )
				{
					newdialect = "com.tle.hibernate.dialect.ExtendedOracle10gDialect";
				}
				else if( dialect.equals("org.hibernate.dialect.Oracle9iDialect")
					|| dialect.equals("org.hibernate.dialect.Oracle9Dialect") )
				{
					newdialect = "com.tle.hibernate.dialect.ExtendedOracle9iDialect";
				}
				if( newdialect != null )
				{
					props.setProperty(HIBERNATE_DIALECT, newdialect);
					return true;
				}
				return false;
			}
		};
		modifier.updateProperties();
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}
}