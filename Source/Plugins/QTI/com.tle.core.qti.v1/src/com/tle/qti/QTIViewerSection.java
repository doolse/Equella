package com.tle.qti;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.qti.data.QTIQuiz;
import com.tle.qti.render.QTIQuizRenderer;
import com.tle.qti.service.QTIService;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.template.Decorations;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ViewItemResource;

@Bind
public class QTIViewerSection extends AbstractViewerSection<QTIViewerSection.QTIViewerModel>
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private QTIService qtiService;

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource) throws IOException
	{
		Decorations.getDecorations(info).clearAllDecorations();

		try( InputStream in = fileSystemService
			.read(resource.getViewableItem().getFileHandle(), resource.getFilepath()) )
		{
			PropBagEx quizXml = new PropBagEx(in);

			String path = resource.getViewableItem().getItemdir() + resource.getFileDirectoryPath();
			QTIQuiz quiz = qtiService.parseQuizXml(quizXml, path);

			QTIViewerModel model = getModel(info);
			model.setQuiz(new QTIQuizRenderer(quiz));

			return viewFactory.createResult("qtiviewer.ftl", this);
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "qti"; //$NON-NLS-1$
	}

	@Override
	public Class<QTIViewerModel> getModelClass()
	{
		return QTIViewerModel.class;
	}

	public static class QTIViewerModel
	{
		private QTIQuizRenderer quiz;

		public void setQuiz(QTIQuizRenderer quiz)
		{
			this.quiz = quiz;
		}

		public QTIQuizRenderer getQuiz()
		{
			return quiz;
		}
	}

}