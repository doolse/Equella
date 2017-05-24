package com.tle.web.viewitem;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.name.Named;
import com.tle.beans.item.ItemKey;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.equella.SectionAuditable.AuditLevel;
import com.tle.web.viewurl.ViewAuditEntry;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ViewItemAuditor
{
	private static final Log LOGGER = LogFactory.getLog(ViewItemAuditor.class);
	private static final String KEY_VIEWED = "$VIEWED$-";
	private static final String KEY_SUMMARY = "S";
	private static final String KEY_CONTENT = "C";

	@Inject
	private UserSessionService sessionService;
	@Inject
	private AuditLogService auditService;

	private AuditLevel auditLevel;

	@Inject
	public void setAuditLevelString(@Named("audit.level") String auditLevelString)
	{
		try
		{
			this.auditLevel = AuditLevel.valueOf(auditLevelString.toUpperCase());
		}
		catch( IllegalArgumentException e )
		{
			LOGGER.error("Could not parse audit.level property.  Setting to NONE");
			this.auditLevel = AuditLevel.NONE;
		}
	}

	public void audit(ViewAuditEntry auditEntry, ItemKey itemId)
	{
		if( auditLevel != AuditLevel.NONE && auditEntry != null )
		{
			try
			{
				if( auditLevel == AuditLevel.NORMAL )
				{
					logViewed(itemId, auditEntry);
				}
				else if( auditLevel == AuditLevel.SMART )
				{
					// log it if it hasn't been already
					if( !isAlreadyViewed(itemId, auditEntry) )
					{
						logViewed(itemId, auditEntry);
						registerViewed(itemId, auditEntry);
					}
				}
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	private boolean isAlreadyViewed(ItemKey itemId, ViewAuditEntry auditEntry)
	{
		Boolean val = sessionService.getAttribute(key(itemId, auditEntry));
		return (val != null && val);
	}

	private void registerViewed(ItemKey itemId, ViewAuditEntry auditEntry)
	{
		sessionService.setAttribute(key(itemId, auditEntry), Boolean.TRUE);
	}

	private String key(ItemKey itemId, ViewAuditEntry auditEntry)
	{
		final boolean summary = auditEntry.isSummary();
		return KEY_VIEWED + (summary ? KEY_SUMMARY : KEY_CONTENT) + (summary ? "" : auditEntry.getPath()) + itemId;
	}

	private void logViewed(ItemKey itemId, ViewAuditEntry entry)
	{
		if( entry.isSummary() )
		{
			auditService.logItemSummaryViewed(itemId);
		}
		else
		{
			auditService.logItemContentViewed(itemId, entry.getContentType(), entry.getPath());
		}
	}
}