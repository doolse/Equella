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

package com.tle.web.controls.universal.handlers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.Part;

import com.tle.web.sections.ajax.AjaxEffects;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.function.*;
import com.tle.web.sections.standard.*;
import com.tle.web.sections.standard.model.*;
import com.tle.web.upload.StreamKilledException;
import org.apache.log4j.Logger;

import com.dytech.edge.common.FileInfo;
import com.dytech.edge.exceptions.BannedFileException;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.beans.system.QuotaSettings;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.FileEntry;
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentUser;
import com.tle.core.wizard.LERepository;
import com.tle.mycontent.service.MyContentService;
import com.tle.mycontent.web.selection.MyContentSelectable;
import com.tle.mycontent.web.selection.MyContentSelectionSettings;
import com.tle.web.controls.universal.AbstractAttachmentHandler;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.controls.universal.handlers.fileupload.TypeDetails;
import com.tle.web.controls.universal.handlers.fileupload.TypeOptions;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.controls.universal.handlers.fileupload.details.FileDetails;
import com.tle.web.controls.universal.handlers.fileupload.details.PackageDetails;
import com.tle.web.controls.universal.handlers.fileupload.details.ZipDetails;
import com.tle.web.controls.universal.handlers.fileupload.options.PackageOptions;
import com.tle.web.controls.universal.handlers.fileupload.options.ZipOptions;
import com.tle.web.myresource.MyResourceConstants;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.UnselectLinkRenderer;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.ProgressRenderer;
import com.tle.web.selection.ParentFrameSelectionCallback;
import com.tle.web.selection.SelectedResourceDetails;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.filter.SelectionFilter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class FileUploadHandler extends AbstractAttachmentHandler<FileUploadHandler.FileUploadModel>
{
	public static final String FILE_TYPE_FILE = "FILE";
	public static final String FILE_TYPE_PACKAGE = "PACKAGE";
	public static final String FILE_TYPE_ZIP = "ZIP";

	private static final Logger LOGGER = Logger.getLogger(FileUploadHandler.class);
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(FileUploadHandler.class);

	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/file/fileuploadhandler.js"));
	private static final JSCallAndReference FILE_UPLOAD_HANDLER_CLASS = new ExternallyDefinedFunction(
		"FileUploadHandler", INCLUDE);
	private static final ExternallyDefinedFunction VALIDATE_FUNC = new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "validateFile", 5);

	private static final ExternallyDefinedFunction DONE_UPLOAD = new ExternallyDefinedFunction(
		FILE_UPLOAD_HANDLER_CLASS, "dndUploadFinishedCallback", 0, JQueryProgression.PRERENDER);
	private static final ExternallyDefinedFunction CHECK_UPLOAD = new ExternallyDefinedFunction(
		FILE_UPLOAD_HANDLER_CLASS, "dndCheckUpload", 2);

	/**
	 * Temporary folder for uploaded files
	 */
	private static final String UPLOADS_FOLDER = "_uploads";

	@Inject
	private ConfigurationService configService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private MyContentService myContentService;
	@Inject
	private MyContentSelectable myContentSelectable;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewItemService viewItemService;
	@Inject
	private ItemService itemService;

	// Maybe move these to an extension point? Seems a bit overkill though.
	@Inject
	private PackageOptions packageOptions;
	@Inject
	private ZipOptions zipOptions;
	@Inject
	private FileDetails fileDetails;
	@Inject
	private PackageDetails packageDetails;
	@Inject
	private ZipDetails zipDetails;

	@AjaxFactory
	private AjaxGenerator ajax;

	@PlugKey("handlers.file.name")
	private static Label LABEL_NAME;
	@PlugKey("handlers.file.description")
	private static Label LABEL_DESCRIPTION;
	@PlugKey("handlers.file.link.removeupload")
	private static Label LABEL_REMOVE_UPLOAD;
	@PlugKey("handlers.file.link.cancelupload")
	private static Label LABEL_CANCEL_UPLOAD;
	@PlugKey("handlers.file.title")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("handlers.file.error.notpackage")
	private static String KEY_ERROR_NOTPACKAGE;
	@PlugKey("handlers.file.error.notallowedpackage")
	private static String KEY_ERROR_NOTALLOWEDPACKAGE;
	@PlugKey("handlers.file.error.banned")
	private static String KEY_ERROR_BANNED;
	@PlugKey("handlers.file.error.incorrectmimetype")
	private static String KEY_INCORRECT_MIMETYPE;
	@PlugKey("handlers.file.error.dragndropincorrectmimetype")
	private static String KEY_INCORRECT_MIMETYPE_DND;
	@PlugKey("handlers.file.error.dragndropmaxfilesize")
	private static String KEY_MAX_FILESIZE;
	@PlugKey("handlers.file.viewlink")
	private static Label VIEW_LINK_LABEL;
	@PlugKey("handlers.file.singlefilewarning")
	private static Label LABEL_WARN_SINGLEFILE;
	@PlugKey("handlers.file.replacesinglefilewarning")
	private static Label LABEL_WARN_REPLACESINGLEFILE;
	@PlugKey("handlers.file.oneimsonlywarning")
	private static Label LABEL_WARN_ONLYONEPACKAGE;
	@PlugKey("handlers.file.maxfilesize")
	private static String LABEL_ERROR_MAXFILESIZE;
	@PlugKey("handlers.file.viewer.default")
	private static String KEY_DEFAULT;

	// TODO: Change this to something more suitable! ie. it would be dynamic
	// based on what page
	// you are on
	@PlugKey("handlers.file.title")
	private static Label EDIT_TITLE_LABEL;

	@Component
	private FileUpload fileUpload;
	@PlugKey("handlers.file.link.filesfromscrapbook")
	@Component
	private Link filesFromScrapbookLink;
	@Component
	private FileDrop fileDrop;
	@Component
	@PlugKey("handlers.file.action.next")
	private Button optionsButton;

	@Component(name = "d")
	private Table detailTable;

	private FileUploadSettings fileSettings;
	private JSCallAndReference resultsCallback;

	private String stateKey;
	private UpdateDomFunction updateProgressArea;

	@Override
	public SectionRenderable render(RenderContext context, DialogRenderOptions renderOptions)
	{
		FileUploadModel model = getModel(context);
		if( model.isSelecting() )
		{
			return renderSelection(context, renderOptions);
		}
		if( model.isEditDetails() )
		{
			UploadedFile upload = getDetailsUpload(context);
			if( model.isTypeResolved() )
			{
				renderOptions.setShowSave(true);
				TypeDetails typeDetails = getTypeDetails(upload.getResolvedType());
				return CombinedRenderer.combineResults(
					renderOptionsAndDetailsHeader(context, typeDetails.isShowViewLink()),
					typeDetails.renderDetailsEditor(context, renderOptions, upload));
			}

			return renderTypeOptions(context, renderOptions, upload);
		}

		return renderUpload(context, renderOptions);
	}

	protected SectionRenderable renderUpload(RenderContext context, DialogRenderOptions renderOptions)
	{
		final FileUploadModel model = getModel(context);

		UploadState uploadState = getUploadState(context);
		uploadState.processErrors();
		List<UploadedFile> uploads = uploadState.getOrderedFiles();
		model.setCanScrapbook(/* !settings.isNoScrapbook()&& */myContentService.isMyContentContributionAllowed());


		final BookmarkAndModify uploadUrl = new BookmarkAndModify(context, ajax.getModifier("processUploadOld"));

		final JSAssignable errorCallback = PartiallyApply.partial(events.getSubmitValuesFunction("illegalFile"), 2);
		final JSAssignable doneCallback = PartiallyApply.partial(events.getSubmitValuesFunction("finishedUpload"), 0);

		JSAssignable startedUpload = PartiallyApply.partial(updateProgressArea, 2);
		JSAssignable validateFile = Js.functionValue(Js.call(VALIDATE_FUNC, fileSettings.getMaxFileSize(),
				fileSettings.getMimeTypes(), errorCallback, startedUpload, doneCallback));

		fileUpload.setAjaxUploadUrl(context, uploadUrl);
		fileUpload.setValidateFile(context, validateFile);
		fileDrop.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajax.getModifier("processUploadDND")));
		fileDrop.setValidateFile(context, validateFile);

		model.setProblemLabel(uploadState.getErrorLabel());
		final List<UploadDisplay> uploadDisplays = new ArrayList<UploadDisplay>();
		int uploadsSize = uploads.size();
		boolean canContinue = uploadsSize > 0;
		boolean canEdit = uploadsSize == 1;
		UnmodifiableAttachments umodAtt = new UnmodifiableAttachments(dialogState.getRepository().getItem());
		if( !isMultipleAllowed(context) && uploadsSize > 1 )
		{
			canContinue = false;
			model.setWarningLabel(!isMultiple() ? LABEL_WARN_SINGLEFILE : LABEL_WARN_REPLACESINGLEFILE);
		}

		// TODO: remove this check when multiple IMS packages are supported
		final int uploadedPackageCount = countUploadedPackages(context, uploads);

		int currentlyAttachedPackages = umodAtt.getList(AttachmentType.IMS).size();
		// Also SCORM and QTI
		// TODO: pluginerise this
		final List<CustomAttachment> customs = umodAtt.getList(AttachmentType.CUSTOM);
		for( CustomAttachment custom : customs )
		{
			final String type = custom.getType();
			if( type.equals("qtitest") || type.equals("scorm") )
			{
				currentlyAttachedPackages++;
			}
		}

		if( (!dialogState.isReplacing(context) && currentlyAttachedPackages > 0 && uploadedPackageCount > 0)
			|| uploadedPackageCount > 1 )
		{
			canContinue = false;
			model.setWarningLabel(LABEL_WARN_ONLYONEPACKAGE);
		}

		for( UploadedFile upload : uploads )
		{
			final boolean uploadFinished = upload.isFinished();
			final UploadDisplay updis = new UploadDisplay();
			final HtmlLinkState remove = new HtmlLinkState();

			updis.setRemove(new UnselectLinkRenderer(remove, uploadFinished ? LABEL_REMOVE_UPLOAD
				: LABEL_CANCEL_UPLOAD));
			// The description will be already populated in the case of
			// import scrapbook content
			updis.setFilename(Check.isEmpty(upload.getDescription()) ? upload.getFilename() : upload
				.getDescription());

			final HtmlComponentState progressDivState = new HtmlComponentState();
			final DivRenderer progressDiv = new DivRenderer(progressDivState);
			final String progressBarId = "u" + upload.getUuid();
			progressDivState.setId(progressBarId);

			if( !uploadFinished )
			{
				canContinue = false;
				progressDiv.addClass("progressbar");
			}
			else
			{
				DivRenderer inner = new DivRenderer("");
				inner.addClass("progress-bar-inner");
				inner.addClass("complete");
				progressDiv.setNestedRenderable(inner);

			}
			remove.setClickHandler(events.getNamedHandler("removeUpload", upload.getUuid()));

			updis.setProgressDiv(progressDiv);
			uploadDisplays.add(updis);
		}

		// all downloads finished, multiple files -> add the files and close
		if( canContinue )
		{
			if( canEdit )
			{
				optionsButton.setClickHandler(context, events.getNamedHandler("nextPage"));
				renderOptions.addAction(optionsButton);
			}
			else
			{
				renderOptions.setShowSave(true);
				renderOptions.setShowAddReplace(canContinue);
			}
		}

		model.setUploads(uploadDisplays);
		return viewFactory.createResult("file/file-add.ftl", this);
	}

	private SectionRenderable renderTypeOptions(RenderContext context, DialogRenderOptions renderOptions,
		UploadedFile upload)
	{
		TypeOptions options = getTypeOptions(upload);
		options.loadOptions(context, upload);
		optionsButton.setClickHandler(context, events.getNamedHandler("pickedType"));
		renderOptions.addAction(optionsButton);
		return CombinedRenderer.combineResults(renderOptionsAndDetailsHeader(context, false),
			renderSection(context, options));
	}

	@Override
	public String getHandlerId()
	{
		return "fileHandler";
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? EDIT_TITLE_LABEL : ADD_TITLE_LABEL;
	}

	@Override
	public void createNew(SectionInfo info)
	{
		// nothing
	}

	@Override
	public void cancelled(SectionInfo info)
	{
		FileUploadModel model = getModel(info);
		if( model.isEditDetails() )
		{
			UploadedFile uploaded = getDetailsUpload(info);
			String resolvedType = uploaded.getResolvedType();
			if( resolvedType != null )
			{
				getTypeDetails(resolvedType).cleanup(info, uploaded);
			}
		}
		fileSystemService.removeFile(getStagingFile(), UPLOADS_FOLDER);
		model.setEditDetails(false);
		model.setTypeResolved(false);
		SectionUtils.clearModel(info, this);
		dialogState.setAttribute(info, stateKey, null);
	}

	@Override
	public void saveChanges(SectionInfo info, String replacementUuid)
	{
		FileUploadModel model = getModel(info);
		UploadState uploadState = getUploadState(info);
		if( model.isEditDetails() )
		{
			UploadedFile upload = getDetailsUpload(info);
			TypeDetails typeDetails = getTypeDetails(upload.getResolvedType());
			typeDetails.commitNew(info, upload, replacementUuid);
		}
		else
		{
			// Can only be files
			List<UploadedFile> files = uploadState.getOrderedFiles();
			for( UploadedFile uploadedFile : files )
			{
				fileDetails.initialiseFromUpload(info, uploadedFile, true);
				fileDetails.commitNew(info, uploadedFile, replacementUuid);
				// Huh?
				// replacementUuid = null;
			}
		}
		cancelled(info);
	}

	@Override
	public void saveEdited(SectionInfo info, Attachment attachment)
	{
		UploadedFile upload = getDetailsUpload(info);
		TypeDetails typeDetails = getTypeDetails(upload.getResolvedType());
		typeDetails.commitEdit(info, upload, attachment);
		typeDetails.cleanup(info, upload);
	}

	public SectionRenderable getThumbnailUrlForAttachment(SectionInfo info, Attachment attachment)
	{
		return attachmentResourceService.getViewableResource(info, dialogState.getViewableItem(info), attachment)
			.createStandardThumbnailRenderer(new TextLabel(attachment.getDescription())).addClass("file-thumbnail");
	}

	public SectionRenderable getThumbnailUrlForFile(SectionInfo info, String filename, String mimeType)
	{
		return attachmentResourceService
			.createPathResource(info, dialogState.getViewableItem(info), filename, filename, mimeType, null)
			.createStandardThumbnailRenderer(new TextLabel(mimeType)).addClass("file-thumbnail");
	}

	@Override
	public boolean validate(SectionInfo info)
	{
		FileUploadModel model = getModel(info);
		if( !model.isEditDetails() )
		{
			if( !validateUploads(info) )
			{
				return false;
			}
			UploadState uploadState = getUploadState(info);
			List<UploadedFile> files = uploadState.getOrderedFiles();
			if( files.size() == 1 )
			{
				setupDetailEditing(info);
				return false;
			}
			return true;
		}
		else if( model.isTypeResolved() )
		{
			UploadedFile upload = getDetailsUpload(info);
			TypeDetails typeDetails = getTypeDetails(upload.getResolvedType());
			return typeDetails.validateDetails(info, upload);
		}
		return false;
	}

	@EventHandlerMethod
	public void pickedType(SectionInfo info)
	{
		UploadedFile upload = getDetailsUpload(info);
		getTypeOptions(upload).saveOptions(info, upload);
		String resolvedType = upload.getResolvedType();
		if( resolvedType != null )
		{
			getTypeDetails(resolvedType).initialiseFromUpload(info, upload, true);
			setupDetailEditing(info);
		}
	}

	@EventHandlerMethod
	public void nextPage(SectionInfo info)
	{
		validateUploads(info);
		setupDetailEditing(info);
	}

	protected boolean validateUploads(SectionInfo info)
	{
		UploadState uploadState = getUploadState(info);
		List<UploadedFile> files = uploadState.getOrderedFiles();
		if( files.size() == 1 )
		{
			UploadedFile file = files.get(0);
			String potential = null;
			String resolved = null;
			String resolvedPackageType = null;
			final List<String> packageTypes = getPackageTypes(info, file);
			if( !packageTypes.isEmpty() )
			{
				if( fileSettings.isPackagesOnly() )
				{
					if( fileSettings.isQtiPackagesOnly() || fileSettings.isScormPackagesOnly() )
					{
						resolved = FILE_TYPE_PACKAGE;
						// resolved package type is the most specific package
						// type, which is listed first
						resolvedPackageType = packageTypes.get(0);
					}
					else
					{
						if( packageTypes.size() == 1 )
						{
							resolved = FILE_TYPE_PACKAGE;
							resolvedPackageType = packageTypes.get(0);
						}
						else
						{
							resolved = FILE_TYPE_PACKAGE;
						}
					}
				}
				else
				{
					// it's up to package options to set the resolved type
					potential = FILE_TYPE_PACKAGE;
					if( !isZipFile(file) )
					{
						// it might be package, it's definitely is not a zip
						// file though.
						fileSettings.setNoUnzip(true);
					}
				}
			}
			else if( isZipFile(file) )
			{
				if( fileSettings.isNoUnzip() )
				{
					resolved = FILE_TYPE_FILE;
				}
				else
				{
					// it's up to zip options to set the resolved type
					potential = FILE_TYPE_ZIP;
				}
			}
			else
			{
				resolved = FILE_TYPE_FILE;
			}

			if( resolved != null )
			{
				file.setResolvedType(resolved);
				if( resolvedPackageType != null )
				{
					file.setResolvedSubType(resolvedPackageType);
				}
				getTypeDetails(resolved).initialiseFromUpload(info, file, true);
			}
			else
			{
				file.setPotentialType(potential);
				getTypeDetails(potential).initialiseFromUpload(info, file, false);
			}
		}
		return true;
	}

	protected void setupDetailEditing(SectionInfo info)
	{
		FileUploadModel model = getModel(info);
		model.setEditDetails(true);
		UploadedFile upload = getDetailsUpload(info);
		upload.setDetailEditing(true);
		String resolvedType = upload.getResolvedType();
		if( resolvedType != null )
		{
			if( resolvedType.equals(FILE_TYPE_PACKAGE) && upload.getResolvedSubType() == null )
			{
				return;
			}
			model.setTypeResolved(true);
			getTypeDetails(resolvedType).setupDetailsForEdit(info, upload);
		}
	}

	private UploadedFile getDetailsUpload(SectionInfo info)
	{
		UploadState state = getUploadState(info);
		List<UploadedFile> files = state.getOrderedFiles();
		if( files.size() != 1 )
		{
			throw new Error("Should be exactly one");
		}
		return files.get(0);
	}

	@EventHandlerMethod
	public void finishedUpload(SectionInfo info)
	{

	}

	@EventHandlerMethod
	public void uploadingNow(SectionInfo info, String uploadId, String filename)
	{
		UploadState uploadState = getUploadState(info);
		uploadState.initialiseUpload(uploadId, uniqueName(info, filename));
	}

	@EventHandlerMethod
	public void illegalFile(SectionInfo info, String filename, String reason)
	{
		final UploadState uploadState = getUploadState(info);
		final UploadedFile uploadedFile = new UploadedFile(UUID.randomUUID().toString());
		uploadedFile.setIntendedFilepath(filename);
		uploadState.addUpload(uploadedFile);
		if ("size".equals(reason))
		{
			uploadedFile.setProblemKey(LABEL_ERROR_MAXFILESIZE);
		}
		else
		{
			uploadedFile.setProblemKey(KEY_INCORRECT_MIMETYPE);
		}
	}

	public UploadState getUploadState(SectionInfo info)
	{
		UploadState uploadState = dialogState.getAttribute(info, stateKey);
		if( uploadState == null )
		{
			uploadState = new UploadState();
			dialogState.setAttribute(info, stateKey, uploadState);
		}
		return uploadState;
	}

	@Override
	public void loadForEdit(SectionInfo info, Attachment attachment)
	{
		UploadState uploadState = getUploadState(info);
		UploadedFile upload = new UploadedFile(attachment.getUuid());
		upload.setAttachment((Attachment) attachment.clone());
		TypeDetails typeDetails = getTypeDetailsForAttachment(attachment);
		typeDetails.prepareForEdit(info, upload);
		uploadState.addUpload(upload);
		setupDetailEditing(info);
	}

	private int countUploadedPackages(SectionInfo info, List<UploadedFile> uploads)
	{
		int count = 0;
		for( UploadedFile file : uploads )
		{
			if( file.isFinished() && !getPackageTypes(info, file).isEmpty() )
			{
				++count;
			}
		}
		return count;
	}

	private SectionRenderable renderOptionsAndDetailsHeader(RenderContext context, boolean showViewLink)
	{
		/*
		 * SCORM Packages, at least for the UTI build, and most likely in to the
		 * future, will rely on an Icodeon server to complete the contribution,
		 * hence we don't want a "Download this file" link which would most
		 * often be ineffective. For the first view of a chosen package however,
		 * we may only have gone so far as to recognise the file as
		 * 'application/zip', and not as a SCORM Or IMS Package (even though it
		 * may be). Once the contribution process advances to the summary (prior
		 * to commit) we both fully recognise the file/package type, and refrain
		 * from showing the download link (which is pretty superfluous anyway).
		 */
		detailTable.setFilterable(false);
		final FileUploadModel model = getModel(context);

		UploadedFile fileInfo = getDetailsUpload(context);
		model.setFileInfo(fileInfo);
		model.setEditTitle((Check.isEmpty(fileInfo.getDescription()) ? fileInfo.getFilename() : fileInfo
			.getDescription()));

		ViewableResource viewableResource = getDetailsViewableResource(context);

		// Use generic mimetype thumb
		// model.setThumbnail(getThumbnailUrlForFile(context,
		// fileInfo.getFilename(), fileInfo.getMimeType()));

		// Use custom thumb http://jira.pearsoncmg.com/jira/browse/EQ-389
		ImageRenderer thumbRenderer = viewableResource.createStandardThumbnailRenderer(new TextLabel((Check
			.isEmpty(fileInfo.getDescription()) ? fileInfo.getFilename() : fileInfo.getDescription())));

		model.setThumbnail(thumbRenderer);

		// Get common details (Type, size, filename)
		addAttachmentDetails(context, viewableResource.getCommonAttachmentDetails());

		// Add a view link, conditionally
		if( showViewLink )
		{
			Bookmark resourceBookmark = viewableResource.createCanonicalUrl();
			HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, resourceBookmark);
			linkState.setTarget(HtmlLinkState.TARGET_BLANK);
			model.setViewlink(new LinkRenderer(linkState));
		}

		return viewFactory.createResult("file/file-editheader.ftl", this);
	}

	protected void addAttachmentDetail(SectionInfo info, Label label, Object detail)
	{
		TableState state = detailTable.getState(info);
		addRow(state, label, detail);
	}

	protected void addAttachmentDetails(SectionInfo info, @Nullable List<AttachmentDetail> details)
	{
		TableState state = detailTable.getState(info);
		if( details != null )
		{
			for( AttachmentDetail detail : details )
			{
				addRow(state, detail.getName(), detail.getDescription());
			}
		}
	}

	private void addRow(TableState state, Label label, Object detail)
	{
		TableCell labelCell = new TableCell(label);
		labelCell.addClass("label");
		state.addRow(labelCell, detail);
	}

	private SectionRenderable renderSelection(RenderContext context, DialogRenderOptions renderOptions)
	{
		final SectionInfo forward = selectionService.getSelectionSessionForward(context, initSession(),
			myContentSelectable);

		renderOptions.setFullscreen(true);
		final FileUploadModel model = getModel(context);
		model.setSelectionUrl(new InfoBookmark(forward).getHref());

		return viewFactory.createResult("file/file-selection.ftl", this);
	}

	private SelectionSession initSession()
	{
		final SelectionSession session = new SelectionSession(new ParentFrameSelectionCallback(resultsCallback, false));
		final SelectionFilter mimeFilter = new SelectionFilter();
		final MyContentSelectionSettings settings = new MyContentSelectionSettings();
		settings.setRestrictToHandlerTypes(Arrays.asList(MyResourceConstants.MYRESOURCE_CONTENT_TYPE));
		session.setAttribute(MyContentSelectionSettings.class, settings);
		session.setSelectScrapbook(true);
		session.setSelectItem(true);
		session.setSelectAttachments(false);
		session.setSelectPackage(false);
		session.setSelectMultiple(isMultiple());
		session.setAddToRecentSelections(false);
		if( !isMultiple() )
		{
			session.setSkipCheckoutPage(true);
		}

		if( fileSettings.isRestrictByMime() )
		{
			Set<String> mimeTypes = Sets.newHashSet();
			mimeTypes.addAll(fileSettings.getMimeTypes());
			mimeFilter.setAllowedMimeTypes(mimeTypes);
			session.setAttribute(SelectionFilter.class, mimeFilter);
		}

		return session;
	}

	private TypeDetails getTypeDetailsForAttachment(Attachment attachment)
	{
		if( attachment instanceof ImsAttachment || attachment instanceof CustomAttachment )
		{
			return packageDetails;
		}
		else if( attachment instanceof ZipAttachment )
		{
			return zipDetails;
		}
		else if( attachment instanceof FileAttachment )
		{
			if( !Check.isEmpty((String) attachment.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID)) )
			{
				return zipDetails;
			}
			return fileDetails;
		}
		throw new Error("Not supported");
	}

	@Override
	public void remove(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		getTypeDetailsForAttachment(attachment).removeAttachment(info, attachment, willBeReplaced);
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, UniversalControlState state)
	{
		super.onRegister(tree, parentId, state);
		fileSettings = new FileUploadSettings(state.getControlConfiguration());
		packageOptions.setSettings(fileSettings, this);

		fileDetails.onRegister(tree, parentId, this);
		packageDetails.onRegister(tree, parentId, this);
		zipDetails.onRegister(tree, parentId, this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.registerSubInnerSection(packageOptions, id);
		tree.registerSubInnerSection(zipOptions, id);
		tree.registerSubInnerSection(fileDetails, id);
		tree.registerSubInnerSection(packageDetails, id);
		tree.registerSubInnerSection(zipDetails, id);

		stateKey = id + "_uploads";

		filesFromScrapbookLink.setClickHandler(events.getNamedHandler("startSelection"));

		resultsCallback = new PassThroughFunction("r" + id, events.getSubmitValuesFunction("selectionsMade"));
		updateProgressArea = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("uploadingNow"),
				ajax.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE),"uploads");
	}

	/**
	 * @param info
	 * @param uuid
	 */
	@EventHandlerMethod
	public void removeUpload(SectionInfo info, String uuid)
	{
		UploadState uploadState = getUploadState(info);
		UploadedFile uploadedFile = uploadState.getUploadForUuid(uuid);
		if( uploadedFile != null )
		{
			uploadedFile.setCancelled(true);
			fileSystemService.removeFile(getStagingFile(), uploadedFile.getFilepath());
			// if the filename is null it hasn't even been uploaded to staging
			// eg. the file was over a max file size restriction. don't even try
			// to remove it
			if( uploadedFile.getFilename() != null )
			{
				fileSystemService.removeFile(getStagingFile(), uploadedFile.getFilepath());
			}
		}
		uploadState.removeUpload(uuid);
	}

	@AjaxMethod
	public SectionRenderable processUploadDND(SectionInfo info)
	{
		return processUpload(info, fileDrop);
	}

	@AjaxMethod
	public SectionRenderable processUploadOld(SectionInfo info)
	{
		return processUpload(info, fileUpload);
	}

	public <S extends HtmlFileUploadState> SectionRenderable processUpload(SectionInfo info, AbstractFileUpload<S> uploader)
	{
		Part upload = uploader.getMultipartFile(info);
		String filename = upload.getName();
		String uploadId = info.getRequest().getHeader("X_UUID");
		boolean success = true;
		final UploadState uploadState = getUploadState(info);

		final String uniqueFilename = uniqueName(info, filename);
        final UploadedFile uploadedFile = uploadState.initialiseUpload(uploadId, uniqueFilename);
        if( fileSettings.isRestrictByMime() && !isCorrectMimetype(uploadedFile) )
        {
            String actualPath = UPLOADS_FOLDER + '/' + uploadedFile.getIntendedFilepath();
            uploadedFile.setFilepath(actualPath);
            uploadedFile.setProblemKey(KEY_INCORRECT_MIMETYPE);
            success = false;
        }
        else
        {
            try( InputStream in = new CancellableStream(uploadedFile, upload.getInputStream()) )
            {
                writeStreamToDisk(dialogState.getRepository(), uploadedFile, in);
                validateUpload(info, uploadedFile);
            }
            catch( StreamKilledException k )
            {
                success = false;
                // whatever
            }
            catch( BannedFileException b )
            {
                success = false;
                uploadedFile.setProblemKey(KEY_ERROR_BANNED);
            }
            catch( Exception e )
            {
                success = false;
                SectionUtils.throwRuntime(e);
            }
            finally
            {
                uploadedFile.setFinished(true);
                uploadedFile.setFileUploadUuid(null);
            }
        }
		return new SimpleSectionResult(success);
	}

	private static class CancellableStream extends FilterInputStream {

		private final UploadedFile upload;

		public CancellableStream(UploadedFile upload, InputStream in) {
			super(in);
			this.upload = upload;
		}

		public void checkCancelled() throws IOException {
			if (upload.isCancelled())
			{
				throw new StreamKilledException();
			}
		}

		@Override
		public int read() throws IOException {
			checkCancelled();
			return super.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			checkCancelled();
			return super.read(b, off, len);
		}
	}

	private String uniqueName(SectionInfo info, String filename)
	{
		Set<String> existingNames = Sets.newHashSet();
		addExistingNames(existingNames, "");
		String replacedFilename = getReplacedFilename(info);
		if( replacedFilename != null )
		{
			existingNames.remove(replacedFilename);
		}
		addExistingNames(existingNames, UPLOADS_FOLDER);

		if( !existingNames.contains(filename.toLowerCase()) )
		{
			return filename;
		}

		final Pair<String, String> nameParts = PathUtils.fileParts(filename);
		final String name = nameParts.getFirst();
		final String extension = nameParts.getSecond();
		int upto = 2;
		while( true )
		{
			final String uniqueName = name + '(' + upto + ')' + (Check.isEmpty(extension) ? "" : '.' + extension);
			if( !existingNames.contains(uniqueName.toLowerCase()) )
			{
				return uniqueName;
			}
			upto++;
		}
	}

	@Nullable
	private String getReplacedFilename(SectionInfo info)
	{
		String replacedFilename = null;
		Attachment replaced = dialogState.getReplacedAttachment(info);
		if( replaced != null )
		{
			switch( replaced.getAttachmentType() )
			{
				case FILE:
				case CUSTOM:
				case ZIP:
				case IMS:
					replacedFilename = replaced.getUrl().toLowerCase();
					break;
				default:
					break;
			}
		}
		return replacedFilename;
	}

	private void addExistingNames(Set<String> existingNames, String folder)
	{
		try
		{
			FileEntry[] folderFiles = fileSystemService.enumerate(getStagingFile(), folder, null);
			for( FileEntry fileEntry : folderFiles )
			{
				existingNames.add(fileEntry.getName().toLowerCase());
			}
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	protected void validateUpload(SectionInfo info, UploadedFile file)
	{
		final List<String> allowedTypes = getAllowedTypes();
		final List<String> detectedTypes = getPackageTypes(info, file);
		if( fileSettings.isPackagesOnly() && !Check.isEmpty(detectedTypes) && !Check.isEmpty(allowedTypes) )
		{
			if( Collections.disjoint(allowedTypes, detectedTypes) )
			{
				file.setProblemKey(KEY_ERROR_NOTALLOWEDPACKAGE);
			}
		}
		else if( fileSettings.isPackagesOnly() && detectedTypes.isEmpty() )
		{
			file.setProblemKey(KEY_ERROR_NOTPACKAGE);
		}
	}

	private boolean isCorrectMimetype(UploadedFile file)
	{
		for( String mimeType : fileSettings.getMimeTypes() )
		{
			if( mimeType.equals(mimeService.getMimeTypeForFilename(file.getIntendedFilepath())) )
			{
				return true;
			}

		}
		return false;
	}

	private boolean isZipFile(UploadedFile file)
	{
		return dialogState.getRepository().isArchive(file.getIntendedFilepath());
	}

	public static boolean isWebPage(String filename)
	{
		String lower = filename.toLowerCase();
		return lower.endsWith(".html") || lower.endsWith(".htm");
	}

	private List<String> getAllowedTypes()
	{
		List<String> allowed = Lists.newArrayList();
		if( fileSettings.isPackagesOnly() )
		{
			if( fileSettings.isQtiPackagesOnly() )
			{
				allowed.add("QTITEST");
			}
			if( fileSettings.isScormPackagesOnly() )
			{
				allowed.add("SCORM");
			}
		}
		return allowed;
	}

	@Override
	public boolean isHiddenFromSummary(IAttachment attachment)
	{
		boolean restricted = super.isHiddenFromSummary(attachment);
		if( attachment instanceof ZipAttachment )
		{
			return !((ZipAttachment) attachment).isAttachZip() || restricted;
		}
		return restricted;
	}

	public List<String> getPackageTypes(SectionInfo info, UploadedFile file)
	{
		List<String> couldBePackage = file.getPackageTypes();
		if( couldBePackage == null )
		{
			couldBePackage = dialogState.getRepository().determinePackageTypes(info, file.getFilepath());
			file.setPackageTypes(couldBePackage);
		}
		return couldBePackage;
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(LABEL_NAME, LABEL_DESCRIPTION);
	}

	@EventHandlerMethod
	public void startSelection(SectionInfo info)
	{
		getModel(info).setSelecting(myContentService.isMyContentContributionAllowed());
	}

	/**
	 * Scrapbook selections ONLY
	 *
	 * @param info
	 * @param selections
	 */
	@EventHandlerMethod
	public void selectionsMade(SectionInfo info, List<SelectedResourceDetails> selectedResources)
	{
		getModel(info).setSelecting(false);
		final LERepository repo = dialogState.getRepository();
		UploadState uploadState = getUploadState(info);
		for( SelectedResourceDetails resource : selectedResources )
		{
			// copy the actual files into the uploads folder and add an
			// UploadedFile
			final ItemId itemId = new ItemId(resource.getUuid(), resource.getVersion());
			final Item item = itemService.get(itemId);
			final List<FileAttachment> attachments = new UnmodifiableAttachments(item).getList(AttachmentType.FILE);

			// There should only ever be one
			for( FileAttachment attachment : attachments )
			{
				final String uuid = UUID.randomUUID().toString();
				final String filename = uniqueName(info, PathUtils.getFilenameFromFilepath(attachment.getFilename()));

				// Yes, you *could* spawn a new thread and let it return
				// immediately to the uploads page and have funky progress just
				// like a regular upload.
				// But do *you* really want to put it the days of pissing around
				// to get this working?
				// I thought not. Maybe a 10.0 feature
				try( InputStream in = fileSystemService.read(new ItemFile(itemId), attachment.getFilename()) )
				{
					final UploadedFile upload = new UploadedFile(uuid);
					upload.setFilepath(UPLOADS_FOLDER + '/' + filename);
					upload.setDescription(CurrentLocale.get(item.getName()));
					upload.setIntendedFilepath(filename);
					uploadState.addUpload(upload);
					writeStreamToDisk(repo, upload, in);
					validateUpload(info, upload);
					upload.setFinished(true);
				}
				catch( StreamKilledException k )
				{
					LOGGER.debug("User cancelled upload");
				}
				catch( Exception e )
				{
					SectionUtils.throwRuntime(e);
				}
			}
		}
	}

	/**
	 * @param upload Must have filepath already set
	 * @param stream
	 * @throws Exception
	 */
	protected void writeStreamToDisk(LERepository repo, UploadedFile upload, InputStream stream) throws IOException
	{
		String actualPath = UPLOADS_FOLDER + '/' + upload.getIntendedFilepath();
		upload.setFilepath(actualPath);
		final FileInfo fileInfo = repo.uploadStream(actualPath, stream, true);
		// make sure filepath is the same as the one actually written to
		// disk
		if( Check.isEmpty(upload.getDescription()) )
		{
			upload.setDescription(upload.getIntendedFilepath());
		}
		upload.setSize(fileInfo.getLength());
		upload.setMd5(fileInfo.getMd5CheckSum());
		upload.setMimeType(mimeService.getMimeTypeForFilename(upload.getIntendedFilepath()));
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		// FIXME: extension point, this is a dog's breakfast
		return ((attachment instanceof FileAttachment && !(attachment instanceof HtmlAttachment))
			|| attachment instanceof ImsAttachment
			|| attachment instanceof ZipAttachment
			|| (attachment instanceof CustomAttachment && ((CustomAttachment) attachment).getType().equalsIgnoreCase(
				"scorm"))
			|| (attachment instanceof CustomAttachment && ((CustomAttachment) attachment).getType().equalsIgnoreCase(
				"qtitest")) || (attachment instanceof CustomAttachment && ((CustomAttachment) attachment).getType()
			.equalsIgnoreCase("mets")));
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new FileUploadModel();
	}

	/**
	 * This will return null in the cases that there are no options available
	 * (or the page type doesn't support any options anyway)
	 *
	 * @param page
	 * @return
	 */
	@Nullable
	private TypeOptions getTypeOptions(UploadedFile file)
	{
		if( file.getResolvedType() == null )
		{
			switch( file.getPotentialType() )
			{
				case FILE_TYPE_ZIP:
					return zipOptions;
				case FILE_TYPE_PACKAGE:
					return packageOptions;
				default:
					return null;
			}
		}
		if( file.getPotentialType().equals(FILE_TYPE_PACKAGE) && file.getResolvedSubType() == null )
		{
			return packageOptions;
		}
		return null;
	}

	// FIXME: use extension point
	/**
	 * This will NEVER return null.
	 *
	 * @param page
	 * @return
	 */
	private TypeDetails getTypeDetails(String type)
	{
		switch( type )
		{
			case FILE_TYPE_ZIP:
				return zipDetails;
			case FILE_TYPE_PACKAGE:
				// case SCORM:
				// case QTI:
				return packageDetails;
			case FILE_TYPE_FILE:
				return fileDetails;
			default:
				throw new Error("Unresolved file type");
		}
	}

	public FileUpload getFileUpload()
	{
		return fileUpload;
	}

	public FileDrop getFileDrop()
	{
		return fileDrop;
	}

	public Link getFilesFromScrapbookLink()
	{
		return filesFromScrapbookLink;
	}

	public JSCallAndReference getResultsCallback()
	{
		return resultsCallback;
	}

	private static class UploadState implements Serializable
	{
		private final Map<String, UploadedFile> uploadMap = Collections
			.synchronizedMap(new HashMap<String, UploadedFile>());
		private final List<UploadedFile> orderedFiles = Lists.newArrayList();
		private final Set<String> erroredFiles = Sets.newHashSet();
		private Label errorLabel = null;
		private String errorUuid = null;

		@Nullable
		public synchronized UploadedFile getUploadForUuid(String file)
		{
			return uploadMap.get(file);
		}

		public synchronized boolean isAvailable(String uuid)
		{
			return uploadMap.containsKey(uuid) || erroredFiles.contains(uuid);
		}

		public synchronized void removeUpload(String uuid)
		{
			UploadedFile uploadedFile = uploadMap.remove(uuid);
			if( uploadedFile != null )
			{
				orderedFiles.remove(uploadedFile);
				if( uploadedFile.isErrored() )
				{
					erroredFiles.add(uuid);
				}
			}
		}

		public synchronized void addUpload(UploadedFile uploadedFile)
		{
			orderedFiles.add(uploadedFile);
			uploadMap.put(uploadedFile.getUuid(), uploadedFile);
		}

		public synchronized List<UploadedFile> getOrderedFiles()
		{
			return Lists.newArrayList(orderedFiles);
		}

		public synchronized void processErrors()
		{
			List<String> erroredUuids = new ArrayList<>();
			UploadedFile firstError = null;
			for (UploadedFile file : uploadMap.values())
			{
				if (file.isErrored())
				{
					if (firstError == null)
					{
						firstError = file;
					}
					erroredUuids.add(file.getUuid());
				}
			}
			for (String uuid : erroredUuids)
			{
				removeUpload(uuid);
			}
			if (firstError != null)
			{
				errorLabel = new KeyLabel(firstError.getProblemKey(), new TextLabel(firstError.getFilename()));
				errorUuid = firstError.getUuid();
			}
		}

		public Label getErrorLabel()
		{
			return errorLabel;
		}

		public synchronized void clearErrorUnless(String uploadId)
		{
			if (!uploadId.equals(errorUuid))
			{
				errorUuid = null;
				errorLabel = null;
			}
		}

		public synchronized UploadedFile initialiseUpload(String uploadId, String filename)
		{
			UploadedFile uploadedFile = uploadMap.get(uploadId);
			if (uploadedFile == null)
			{
				uploadedFile = new UploadedFile(uploadId);
				uploadedFile.setIntendedFilepath(filename);
				addUpload(uploadedFile);
			}
			return uploadedFile;
		}
	}

	@NonNullByDefault(false)
	public static class FileUploadModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		@Bookmarked(name = "s")
		private boolean selecting;
		@Bookmarked(name = "tr")
		private boolean typeResolved;

		private String selectionUrl;
		private List<UploadDisplay> uploads;
		private UploadedFile fileInfo;
		private boolean canScrapbook;
		private Label warningLabel;
		private Label problemLabel;
		private ViewableResource detailsViewableResource;

		public boolean isSelecting()
		{
			return selecting;
		}

		public void setSelecting(boolean selecting)
		{
			this.selecting = selecting;
		}

		public String getSelectionUrl()
		{
			return selectionUrl;
		}

		public void setSelectionUrl(String selectionUrl)
		{
			this.selectionUrl = selectionUrl;
		}

		public List<UploadDisplay> getUploads()
		{
			return uploads;
		}

		public void setUploads(List<UploadDisplay> uploads)
		{
			this.uploads = uploads;
		}

		public UploadedFile getFileInfo()
		{
			return fileInfo;
		}

		public void setFileInfo(UploadedFile fileInfo)
		{
			this.fileInfo = fileInfo;
		}

		public Label getProblemLabel()
		{
			return problemLabel;
		}

		public void setProblemLabel(Label problemLabel)
		{
			this.problemLabel = problemLabel;
		}

		public boolean isCanScrapbook()
		{
			return canScrapbook;
		}

		public void setCanScrapbook(boolean canScrapbook)
		{
			this.canScrapbook = canScrapbook;
		}

		public Label getWarningLabel()
		{
			return warningLabel;
		}

		public void setWarningLabel(Label warningLabel)
		{
			this.warningLabel = warningLabel;
		}

		public ViewableResource getDetailsViewableResource()
		{
			return detailsViewableResource;
		}

		public void setDetailsViewableResource(ViewableResource detailsViewableResource)
		{
			this.detailsViewableResource = detailsViewableResource;
		}

		public boolean isTypeResolved()
		{
			return typeResolved;
		}

		public void setTypeResolved(boolean typeResolved)
		{
			this.typeResolved = typeResolved;
		}

	}

	public static class UploadDisplay
	{
		private String progressUrl;
		private DivRenderer progressDiv;
		private String filename;
		private UnselectLinkRenderer remove;
		private Label problemLabel;

		public String getProgressUrl()
		{
			return progressUrl;
		}

		public void setProgressUrl(String progressUrl)
		{
			this.progressUrl = progressUrl;
		}

		public DivRenderer getProgressDiv()
		{
			return progressDiv;
		}

		public void setProgressDiv(DivRenderer div)
		{
			this.progressDiv = div;
		}

		public String getFilename()
		{
			return filename;
		}

		public void setFilename(String filename)
		{
			this.filename = filename;
		}

		public UnselectLinkRenderer getRemove()
		{
			return remove;
		}

		public void setRemove(UnselectLinkRenderer remove)
		{
			this.remove = remove;
		}

		public Label getProblemLabel()
		{
			return problemLabel;
		}

		public void setProblemLabel(Label problemLabel)
		{
			this.problemLabel = problemLabel;
		}
	}

	public ViewableResource getDetailsViewableResource(SectionInfo info)
	{
		FileUploadModel model = getModel(info);
		ViewableResource viewableResource = model.getDetailsViewableResource();
		if( viewableResource == null )
		{
			viewableResource = attachmentResourceService.getViewableResource(info, dialogState.getRepository()
				.getViewableItem(), getDetailsUpload(info).getAttachment());
			model.setDetailsViewableResource(viewableResource);
		}
		return viewableResource;
	}

	public StagingFile getStagingFile()
	{
		return new StagingFile(dialogState.getRepository().getStagingid());
	}

	public static String getUploadFilepath(String filename)
	{
		return UPLOADS_FOLDER + '/' + filename;
	}

	public Table getDetailTable()
	{
		return detailTable;
	}

	public ViewersListModel createViewerModel()
	{
		return new ViewersListModel();
	}

	public class ViewersListModel extends DynamicHtmlListModel<NameValue>
	{
		@Override
		protected Iterable<NameValue> populateModel(SectionInfo info)
		{
			return viewItemService.getEnabledViewers(info, getDetailsViewableResource(info));
		}

		@Override
		protected Option<NameValue> getTopOption()
		{
			return new KeyOption<NameValue>(KEY_DEFAULT, "", null);
		}
	}

	public FileUploadSettings getFileSettings()
	{
		return fileSettings;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return getDetailsUpload(info).getMimeType();
	}
}