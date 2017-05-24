package com.tle.web.qti.viewer.questions.renderer;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;
import com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class SimpleChoiceRenderer extends QtiNodeRenderer
{
	private final SimpleChoice model;
	private final boolean multiple;
	private final String name;

	private boolean checked;

	@AssistedInject
	public SimpleChoiceRenderer(@Assisted SimpleChoice model, @Assisted QtiViewerContext context)
	{
		this(model, context, null, false);
	}

	@AssistedInject
	public SimpleChoiceRenderer(@Assisted SimpleChoice model, @Assisted QtiViewerContext context,
		@Assisted String name, @Assisted boolean multiple)
	{
		super(model, context);
		this.model = model;
		this.multiple = multiple;
		this.name = name;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final String type;
		if( multiple )
		{
			type = "checkbox";
		}
		else
		{
			type = "radio";
		}
		return new DivRenderer("input " + type, null);
	}

	@Override
	protected SectionRenderable createNestedRenderable()
	{
		final QtiViewerContext context = getContext();
		final HtmlBooleanState b = new HtmlBooleanState();

		final String id = id(model.getIdentifier());
		b.setId("opt" + id);
		b.setValue(id);
		b.setName(name);
		b.setChecked(checked);
		final ItemSessionController itemSessionController = context.getItemSessionController();
		final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
		if( itemSessionState.isEnded() )
		{
			b.setDisabled(true);
		}
		else
		{
			b.setClickHandler(new StatementHandler(context.getValueChangedFunction()));
		}

		final NestedRenderable boxRenderer;
		if( multiple )
		{
			final CheckboxRenderer checkboxRenderer = new CheckboxRenderer(b);
			boxRenderer = checkboxRenderer;
		}
		else
		{
			RadioButtonRenderer radioRenderer = new RadioButtonRenderer(b);
			boxRenderer = radioRenderer;
		}
		boxRenderer.setNestedRenderable(super.createNestedRenderable());
		return boxRenderer;
	}

	public boolean isChecked()
	{
		return checked;
	}

	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}
}