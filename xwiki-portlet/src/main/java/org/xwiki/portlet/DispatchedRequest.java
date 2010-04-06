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
package org.xwiki.portlet;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wraps a servlet request object dispatched from a portlet.
 * 
 * @version $Id$
 */
public class DispatchedRequest extends HttpServletRequestWrapper
{
    /**
     * The request attribute holding the map of initial get parameters. These are the parameters from the query string
     * of the URL used to make the request to the portal. The portal hides these parameters but we need to access them
     * because some query string parameters might have been added through JavaScript. Adding query string parameters
     * through JavaScript is not recommended when working with portlet URLs but since we can't fully control the
     * JavaScript code of the servlet application where the request is dispatched to, we have to expose these initial
     * get parameters.
     */
    public static final String ATTRIBUTE_INITIAL_GET_PARAMETERS = "org.xwiki.portlet.attribute.initialGetParameters";

    /**
     * The character encoding set through {@link #setCharacterEncoding(String)}.
     */
    private String settedCharacterEncoding;

    /**
     * The stack that holds the information about all the dispatches that affected this request.
     */
    private Stack<RequestDispatchInfo> dispatchStack = new Stack<RequestDispatchInfo>();

    /**
     * Wraps the given request that has been dispatched from a portlet.
     * 
     * @param request the request to be wrapped
     */
    public DispatchedRequest(HttpServletRequest request)
    {
        super(request);
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

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getRequestURI()
     */
    @Override
    public String getRequestURI()
    {
        if (dispatchStack.isEmpty()) {
            return super.getRequestURI();
        } else {
            return dispatchStack.peek().getRequestURI();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getServletPath()
     */
    @Override
    public String getServletPath()
    {
        if (dispatchStack.isEmpty()) {
            return super.getServletPath();
        } else {
            return dispatchStack.peek().getServletPath();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getPathInfo()
     */
    @Override
    public String getPathInfo()
    {
        if (dispatchStack.isEmpty()) {
            return super.getPathInfo();
        } else {
            return dispatchStack.peek().getPathInfo();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getQueryString()
     */
    @Override
    public String getQueryString()
    {
        if (dispatchStack.isEmpty()) {
            return super.getQueryString();
        } else {
            return dispatchStack.peek().getQueryString();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getParameter(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getParameter(String name)
    {
        String value = super.getParameter(name);
        if (value == null && getAttribute(ATTRIBUTE_INITIAL_GET_PARAMETERS) != null) {
            Map<String, List<String>> initialGetParameters =
                (Map<String, List<String>>) getAttribute(ATTRIBUTE_INITIAL_GET_PARAMETERS);
            if (initialGetParameters.containsKey(name)) {
                value = initialGetParameters.get(name).get(0);
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getParameterMap()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String[]> getParameterMap()
    {
        Map<String, List<String>> initialGetParameters =
            (Map<String, List<String>>) getAttribute(ATTRIBUTE_INITIAL_GET_PARAMETERS);
        if (initialGetParameters != null) {
            Map<String, String[]> map = new HashMap<String, String[]>((Map<String, String[]>) super.getParameterMap());
            for (Map.Entry<String, List<String>> entry : initialGetParameters.entrySet()) {
                if (!map.containsKey(entry.getKey())) {
                    map.put(entry.getKey(), entry.getValue().toArray(new String[] {}));
                }
            }
            return Collections.unmodifiableMap(map);
        } else {
            return super.getParameterMap();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getParameterNames()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<String> getParameterNames()
    {
        Map<String, List<String>> initialGetParameters =
            (Map<String, List<String>>) getAttribute(ATTRIBUTE_INITIAL_GET_PARAMETERS);
        if (initialGetParameters != null) {
            Set<String> allNames = new HashSet<String>();
            Enumeration<String> names = (Enumeration<String>) super.getParameterNames();
            while (names.hasMoreElements()) {
                allNames.add(names.nextElement());
            }
            allNames.addAll(initialGetParameters.keySet());
            return Collections.enumeration(allNames);
        } else {
            return super.getParameterNames();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletRequestWrapper#getParameterValues(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = super.getParameterValues(name);
        if (values == null && getAttribute(ATTRIBUTE_INITIAL_GET_PARAMETERS) != null) {
            Map<String, List<String>> initialGetParameters =
                (Map<String, List<String>>) getAttribute(ATTRIBUTE_INITIAL_GET_PARAMETERS);
            if (initialGetParameters.containsKey(name)) {
                values = initialGetParameters.get(name).toArray(new String[] {});
            }
        }
        return values;
    }

    /**
     * Push the given dispatch info on the top of the dispatch stack and update the request path info accordingly.
     * 
     * @param dispatch the dispatch to put on the top of the dispatch stack
     */
    void pushDispatch(Dispatch dispatch)
    {
        // TODO: Handle includes and forwards differently as per servlet specification.
        dispatchStack.push(parseDispatchPath(dispatch.getPath()));
    }

    /**
     * Remove the top of the dispatch stack and restore the request path info as it was before the latest dispatch.
     */
    void popDispatch()
    {
        dispatchStack.pop();
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
}
