package com.tle.web.controls.flickr.filter;

import java.util.Date;

import com.tle.web.controls.flickr.FlickrSearchEvent;
import com.tle.web.search.filter.AbstractFilterByDateRangeSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
@TreeIndexed
public class FlickrFilterByDateRangeSection extends AbstractFilterByDateRangeSection<FlickrSearchEvent>
{
	@PlugKey("filter.bydate.title")
	private static Label LABEL_TITLE;

	@Override
	protected String[] getParameterNames()
	{
		final String id = getSectionId();
		return new String[]{id + "_dp", id + "_ds", id + "_dr"};
	}

	@Override
	public String getAjaxDiv()
	{
		return "date-range-filter";
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public void prepareSearch(SectionInfo info, FlickrSearchEvent event) throws Exception
	{
		Date[] dateRange = getDateRange(info);
		if( dateRange != null )
		{
			event.filterByDateRange(dateRange);
		}
	}
}