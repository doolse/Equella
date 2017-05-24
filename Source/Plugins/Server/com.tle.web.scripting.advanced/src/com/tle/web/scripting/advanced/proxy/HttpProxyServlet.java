package com.tle.web.scripting.advanced.proxy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.user.CurrentUser;

/**
 * <p>
 * Found at p/geturl
 * <p>
 * URL parameter is "url"
 * <p>
 * E.g. http://inst/p/geturl?url=http%3A%2F%2Fwww.theveronicas.com
 * 
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class HttpProxyServlet extends HttpServlet
{
	// private static final Logger LOGGER =
	// Logger.getLogger(HttpProxyServlet.class);
	private static final long serialVersionUID = 1L;

	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;

	@Override
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
	{
		if( CurrentUser.isGuest() )
		{
			resp.sendError(HttpServletResponse.SC_FORBIDDEN,
				CurrentLocale.get("com.tle.web.scripting.advanced.redirectionservlet.error.notloggedin"));
			return;
		}

		final String url = req.getParameter("url");
		if( Check.isEmpty(url) )
		{
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
				CurrentLocale.get("com.tle.web.scripting.advanced.redirectionservlet.error.parammissing", "url"));
			return;
		}

		Map<String, String[]> forwardedParams = Maps.newHashMap(req.getParameterMap());
		forwardedParams.remove("url");

		final Request request = new Request(url);
		request.setMethod(Method.fromString(req.getMethod()));
		final Enumeration<String> parameterNames = req.getParameterNames();
		while( parameterNames.hasMoreElements() )
		{
			final String pname = parameterNames.nextElement();
			final String[] vals = req.getParameterValues(pname);
			if( vals != null )
			{
				for( String val : vals )
				{
					request.addParameter(pname, val);
				}
			}
		}
		if( request.getMethod() == Method.POST )
		{
			final StringWriter sw = new StringWriter();
			final ServletInputStream inputStream = req.getInputStream();
			if( inputStream != null )
			{
				try( InputStreamReader from = new InputStreamReader(inputStream) )
				{
					CharStreams.copy(from, sw);
				}
			}
			request.setBody(sw.toString());
			request.setMimeType(req.getContentType());
			request.setCharset(req.getCharacterEncoding());
		}

		final Response response = httpService.getWebContent(request, configService.getProxyDetails());
		// Copy over all the headers, except for Set-Cookie, as we
		// don't want to
		// overwrite our EQUELLA session.
		for( NameValue header : response.getHeaders() )
		{
			final String name = header.getName();
			if( !"Set-Cookie".equals(name) && !"Transfer-Encoding".equals(name) )
			{
				resp.addHeader(name, header.getValue());
			}
		}
		if( !response.isOk() )
		{
			resp.sendError(response.getCode(), response.getMessage());
		}
		else
		{
			response.copy(resp.getOutputStream());
		}
		response.close();
	}
}