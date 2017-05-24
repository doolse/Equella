package com.tle.integration.blackboard;

import javax.servlet.http.HttpServletRequest;

import com.tle.common.NameValue;
import com.tle.web.integration.IntegrationSessionData;

public class BlackBoardSessionData implements IntegrationSessionData
{
	private static final long serialVersionUID = 1L;

	private final String blackBoardSession;
	private final String bbUrl;
	private final String courseId;
	private final String contentId;
	private final NameValue location;

	private String courseInfoCode;
	private String entryUrl;
	// TODO: used?
	private String username;

	public BlackBoardSessionData()
	{
		blackBoardSession = null;
		bbUrl = null;
		courseId = null;
		contentId = null;
		entryUrl = null;
		location = null;
	}

	public BlackBoardSessionData(HttpServletRequest request)
	{
		blackBoardSession = request.getParameter("bbsession");
		bbUrl = request.getParameter("bburl");
		courseId = request.getParameter("course_id");
		contentId = request.getParameter("content_id");
		entryUrl = request.getParameter("action");

		location = new NameValue(request.getParameter("contentName"), contentId);
	}

	@Override
	public String getIntegrationType()
	{
		return "bb";
	}

	public void setEntryUrl(String entryUrl)
	{
		this.entryUrl = entryUrl;
	}

	public String getBbUrl()
	{
		return bbUrl;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public String getContentId()
	{
		return contentId;
	}

	public String getEntryUrl()
	{
		return entryUrl;
	}

	public String getBlackBoardSession()
	{
		return blackBoardSession;
	}

	public String getCourseInfoCode()
	{
		return courseInfoCode;
	}

	public void setCourseInfoCode(String courseInfoCode)
	{
		this.courseInfoCode = courseInfoCode;
	}

	public NameValue getLocation()
	{
		return location;
	}

	public boolean canSelect()
	{
		return contentId != null;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
}