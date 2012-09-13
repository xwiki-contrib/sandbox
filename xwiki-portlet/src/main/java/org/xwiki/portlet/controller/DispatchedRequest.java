/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.portlet.controller;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.ArrayUtils;
import org.xwiki.portlet.util.QueryStringParser;

/**
 * Wraps a servlet request object dispatched from a portlet.
 * 
 * @version $Id$
 */
public class DispatchedRequest extends HttpServletRequestWrapper
{
    /**
     * The character encoding set through {@link #setCharacterEncoding(String)}.
     */
    private String settedCharacterEncoding;

    /**
     * The stack that holds the information about all the dispatches that affected this request.
     */
    private final Stack<RequestDispatchInfo> dispatchStack = new Stack<RequestDispatchInfo>();

    /**
     * The stack of dispatch parameters. These parameters are extracted from the dispatch URL query string. If the
     * request was redirected then only these parameters are exposed.
     */
    private final Stack<Map<String, String[]>> dispatchParametersStack = new Stack<Map<String, String[]>>();

    /**
     * Flag indicating if the request was redirected, i.e. if the HTTP POST parameters are available or not.
     */
    private final boolean redirected;

    /**
     * The object used to parse the query string of dispatch URLs.
     */
    private final QueryStringParser queryStringParser = new QueryStringParser();

    /**
     * Wraps the given request that has been dispatched from a portlet, exposing the query string parameters of the
     * initial request.
     * 
     * @param request the request to be wrapped
     * @param exposeInitialQueryStringParameters {@code true} to expose the initial query string parameters,
     *            {@code false} otherwise
     * @throws ServletException if wrapping the given request fails
     */
    public DispatchedRequest(HttpServletRequest request, boolean exposeInitialQueryStringParameters)
        throws ServletException
    {
        super(request);

        redirected = false;

        Map<String, List<String>> queryStringParameters = new HashMap<String, List<String>>();
        if (exposeInitialQueryStringParameters) {
            queryStringParameters.putAll(parseInitialQueryString());
        }
        // When forwarding a request the query string parameters specified on the path used to create the request
        // dispatcher have to be aggregated with the parameters of the forwarded request (PLT.19.1.1). Unfortunately not
        // all portlet containers do this (e.g. WebSphere Portal 6.1.5.0).
        queryStringParameters.putAll(parseQueryString());
        if (!queryStringParameters.isEmpty()) {
            dispatchParametersStack.push(listToArray(queryStringParameters));
        }
    }

    /**
     * Wraps the given request and behaves as if it was redirected to the specified URL.
     * 
     * @param request the request to be wrapped
     * @param redirectURL the redirect URL
     * @throws ServletException if wrapping the given request fails
     */
    public DispatchedRequest(HttpServletRequest request, String redirectURL) throws ServletException
    {
        super(request);

        redirected = true;
        pushDispatch(new Dispatch(DispatchType.FORWARD, redirectURL));
    }

    /**
     * {@inheritDoc}
     * <p>
     * We override the default implementation to overcome the fact that calling {@code getRequestURL()} on a servlet
     * request dispatched from a portlet returns {@code null}. See chapter
     * "PLT.19.3.4 Request and Response Objects for Included Servlets/JSPs from within the Render Method" in JSR286
     * Portlet Specification.
     * 
     * @see HttpServletRequestWrapper#getRequestURL()
     * @see org.apache.catalina.connector.Request#getRequestURL()
     */
    public StringBuffer getRequestURL()
    {
        StringBuffer url = super.getRequestURL();
        if (url != null) {
            return url;
        }

        url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            // Work around for a java.net.URL bug.
            port = 80;
        }

        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());

        return url;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation returns {@code null} if the request was dispatched from a portlet's render method. We
     * override this method to return either the value set through {@link #setCharacterEncoding(String)} or the value
     * passed from a portlet's action processing method.
     * 
     * @see HttpServletRequestWrapper#getCharacterEncoding()
     * @see HttpServletRequestWrapper#setCharacterEncoding(String)
     */
    @Override
    public String getCharacterEncoding()
    {
        String characterEncoding = super.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = settedCharacterEncoding;
        }
        return characterEncoding;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation does nothing if the request was dispatched from a portlet's render method. We override
     * this method to store the given character encoding. This way the behavior is consistent with what happens when the
     * request is not dispatched from a portlet.
     * 
     * @see HttpServletRequestWrapper#setCharacterEncoding(String)
     */
    @Override
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException
    {
        super.setCharacterEncoding(characterEncoding);
        this.settedCharacterEncoding = characterEncoding;
    }

    /**
     * {@inheritDoc}
     * <p>
     * We wrap the request dispatcher to be notified whenever this request is forwarded or included. We have to do this
     * in order to update the path info.
     * 
     * @see HttpServletRequestWrapper#getRequestDispatcher(String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        RequestDispatcher dispatcher = super.getRequestDispatcher(path);
        if (dispatcher != null) {
            return new NotifyingRequestDispatcher(dispatcher, this, path);
        } else {
            return null;
        }
    }

    @Override
    public String getRequestURI()
    {
        if (dispatchStack.isEmpty()) {
            return super.getRequestURI();
        } else {
            return dispatchStack.peek().getRequestURI();
        }
    }

    @Override
    public String getServletPath()
    {
        if (dispatchStack.isEmpty()) {
            return super.getServletPath();
        } else {
            return dispatchStack.peek().getServletPath();
        }
    }

    @Override
    public String getPathInfo()
    {
        if (dispatchStack.isEmpty()) {
            return super.getPathInfo();
        } else {
            return dispatchStack.peek().getPathInfo();
        }
    }

    @Override
    public String getQueryString()
    {
        if (dispatchStack.isEmpty()) {
            return super.getQueryString();
        } else {
            return dispatchStack.peek().getQueryString();
        }
    }

    @Override
    public String getParameter(String name)
    {
        String value = redirected ? null : super.getParameter(name);
        if (value == null) {
            String[] values = dispatchParametersStack.isEmpty() ? null : dispatchParametersStack.peek().get(name);
            return values != null ? values[0] : null;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String[]> getParameterMap()
    {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        if (!redirected) {
            parameterMap.putAll(super.getParameterMap());
        }
        if (!dispatchParametersStack.isEmpty()) {
            for (String parameter : dispatchParametersStack.peek().keySet()) {
                parameterMap.put(parameter, getParameterValues(parameter));
            }
        }
        return Collections.unmodifiableMap(parameterMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<String> getParameterNames()
    {
        Set<String> parameterNames = new HashSet<String>();
        if (!redirected) {
            parameterNames.addAll(super.getParameterMap().keySet());
        }
        if (!dispatchParametersStack.isEmpty()) {
            parameterNames.addAll(dispatchParametersStack.peek().keySet());
        }
        return Collections.enumeration(parameterNames);
    }

    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = redirected ? null : super.getParameterValues(name);
        String[] dispatchValues = dispatchParametersStack.isEmpty() ? null : dispatchParametersStack.peek().get(name);
        return (String[]) ArrayUtils.addAll(values, dispatchValues);
    }

    /**
     * Push the given dispatch info on the top of the dispatch stack and update the request path info accordingly.
     * 
     * @param dispatch the dispatch to put on the top of the dispatch stack
     * @throws ServletException if pushing the given dispatch fails
     */
    void pushDispatch(Dispatch dispatch) throws ServletException
    {
        dispatchStack.push(parseDispatchPath(dispatch.getPath()));
        try {
            dispatchParametersStack
                .push(listToArray(queryStringParser.parse(getQueryString(), getCharacterEncoding())));
        } catch (UnsupportedEncodingException e) {
            // Roll back.
            dispatchStack.pop();
            throw new ServletException("Failed to decode the query string parameters of the dispatch.", e);
        }
    }

    /**
     * Remove the top of the dispatch stack and restore the request path info as it was before the latest dispatch.
     */
    void popDispatch()
    {
        dispatchStack.pop();
        dispatchParametersStack.pop();
    }

    /**
     * Extracts the request dispatch info from the given path.
     * 
     * @param path the path where the request has been dispatched
     * @return the request info changed as a result of the dispatch
     */
    private RequestDispatchInfo parseDispatchPath(String path)
    {
        RequestDispatchInfo info = new RequestDispatchInfo();

        // TODO: Find a reliable way to determine the servlet path. The current code works only if the request is
        // dispatched to the same servlet (w/o a different path info and query string).
        String currentServletPath = getServletPath();
        info.setServletPath(path.startsWith(currentServletPath) ? currentServletPath : "");

        int queryStringStart = path.indexOf('?');
        if (queryStringStart < 0) {
            info.setPathInfo(path.substring(info.getServletPath().length()));
            info.setQueryString("");
        } else {
            info.setPathInfo(path.substring(info.getServletPath().length(), queryStringStart));
            info.setQueryString(path.substring(queryStringStart + 1));
        }

        info.setRequestURI(getContextPath() + info.getServletPath() + info.getPathInfo());

        // Follow servlet specification: return null if the URL doesn't have any extra path info.
        if (info.getPathInfo().length() == 0) {
            info.setPathInfo(null);
        }

        return info;
    }

    /**
     * Utility method for converting a map of lists into a map of arrays.
     * 
     * @param mapOfList the map of lists to be converted
     * @return the map of arrays
     */
    private Map<String, String[]> listToArray(Map<String, List<String>> mapOfList)
    {
        Map<String, String[]> mapOfArray = new HashMap<String, String[]>();
        for (Map.Entry<String, List<String>> entry : mapOfList.entrySet()) {
            mapOfArray.put(entry.getKey(), entry.getValue().toArray(new String[] {}));
        }
        return mapOfArray;
    }

    /**
     * Parses the query string of the initial request that reached the portal. The request received by the dispatch
     * filter wraps the initial request and hides the initial query string which usually contains only portal related
     * information. In some cases the initial query string contains parameters set through JavaScript. The reason for
     * parsing the initial query string is to expose those parameters set from JavaScript.
     * 
     * @return the map of initial query string parameters
     * @throws ServletException if decoding the parameters fails
     */
    private Map<String, List<String>> parseInitialQueryString() throws ServletException
    {
        HttpServletRequest initialRequest = (HttpServletRequest) getRequest();
        while (initialRequest instanceof HttpServletRequestWrapper) {
            initialRequest = (HttpServletRequest) ((HttpServletRequestWrapper) initialRequest).getRequest();
        }
        if (initialRequest.getQueryString() != null) {
            try {
                return queryStringParser.parse(initialRequest.getQueryString(), initialRequest.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                throw new ServletException("Failed to decode the initial query string parameters.", e);
            }
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Parses the query string parameters from {@link #getQueryString()}.
     * 
     * @return the map of query string parameters
     * @throws ServletException if decoding the query string parameters fails
     */
    private Map<String, List<String>> parseQueryString() throws ServletException
    {
        String queryString = getQueryString();
        if (queryString == null) {
            return Collections.emptyMap();
        } else {
            try {
                return queryStringParser.parse(queryString, getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                throw new ServletException("Failed to decode the query string parameters.", e);
            }
        }
    }
}
