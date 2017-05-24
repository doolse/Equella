package com.tle.web.cloud.search.filters;

import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlListModel;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
public class CloudFilterByPublisherSection extends AbstractCloudFilter
{
	@PlugKey("filter.bypublisher.title")
	private static Label LABEL_TITLE;
	@PlugKey("filter.bypublisher.top")
	private static String DEFAULT_OPTION;

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public void prepareSearch(SectionInfo info, CloudSearchEvent event) throws Exception
	{
		String publisher = list.getSelectedValueAsString(info);
		if( !Check.isEmpty(publisher) )
		{
			event.getCloudSearch().setPublisher(publisher);
		}
	}

	@Override
	public HtmlListModel<NameValue> buildListModel()
	{
		return new DynamicHtmlListModel<NameValue>()
		{
			@Override
			protected Option<NameValue> getTopOption()
			{
				return new KeyOption<NameValue>(DEFAULT_OPTION, "", null);
			}

			@Override
			protected Iterable<NameValue> populateModel(SectionInfo info)
			{
				return cloudService.getCloudFilterInfo().getPublishers();
			}
		};
	}

	@Override
	protected String getPublicParam()
	{
		return "pub";
	}
}