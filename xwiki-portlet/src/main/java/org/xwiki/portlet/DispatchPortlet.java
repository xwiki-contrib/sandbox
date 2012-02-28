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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.model.ResponseData;
import org.xwiki.portlet.url.DispatchURLFactory;
import org.xwiki.portlet.url.URLRequestTypeMapper;
import org.xwiki.portlet.view.StreamFilterManager;

/**
 * Dispatches portlet requests coming from a JSR286 compatible portal to the URL provided in the
 * {@link DispatchURLFactory#PARAMETER_DISPATCH_URL} request parameter. The dispatch target must be on the same context
 * path as the dispatch portlet.
 * 
 * @version $Id$
 */
public class DispatchPortlet extends GenericPortlet
{
    /**
     * The key to access the data of a dispatched response from the session.
     */
    public static final String PARAMETER_DISPATCHED_RESPONSE_KEY = "org.xwiki.portlet.parameter.dispatchedResponseKey";

    /**
     * The name of the preference holding the default dispatch URL, i.e. the URL used to dispatch the request when
     * there's no dispatch URL parameter specified on the request.
     */
    public static final String PREFERENCE_DEFAULT_DISPATCH_URL = "defaultDispatchURL";

    /**
     * The name of the preference holding the edit URL, i.e. the URL where the edit request is dispatched to. This URL
     * must be relative to the context path where the portlet is running.
     */
    public static final String PREFERENCE_EDIT_URL = "editURL";

    /**
     * The name of the preference that controls the title of the portlet.
     */
    public static final String PREFERENCE_TITLE = "title";

    /**
     * Attribute used to pass the request type information to the dispatch servlet filter.
     */
    public static final String ATTRIBUTE_REQUEST_TYPE = "org.xwiki.portlet.attribute.requestType";

    /**
     * The request attribute used to pass the {@link StreamFilterManager} to the dispatch target.
     */
    public static final String ATTRIBUTE_STREAM_FILTER_MANAGER = "org.xwiki.portlet.attribute.streamFilterManager";

    /**
     * The map of response data, stored on the session.
     */
    public static final String ATTRIBUTE_RESPONSE_DATA_MAP = "org.xwiki.portlet.attribute.responseDataMap";

    /**
     * The data of a dispatched response.
     */
    public static final String ATTRIBUTE_RESPONSE_DATA = "org.xwiki.portlet.attribute.responseData";

    /**
     * The attribute used to pass the URL to the home (landing) page to the dispatch target. The dispatch target can use
     * this URL to create "Back to home" links.
     */
    public static final String ATTRIBUTE_HOME_URL = "org.xwiki.portlet.attribute.homeURL";

    /**
     * The request attribute used to pass the redirect URL to the dispatch filter in order to adjust the dispatched
     * request accordingly. We transform redirects into dispatches because redirects are not allowed during render
     * request and we can't create action URLs during action request.
     * <p>
     * Forwarding the request multiple times is not enough because some portlet containers send all the requests to the
     * same dispatch target, the target of the first dispatch.
     * 
     * @see DispatchURLFactory#PARAMETER_DISPATCH_URL
     */
    public static final String ATTRIBUTE_REDIRECT_URL = "org.xwiki.portlet.attribute.redirectURL";

    /**
     * The maximum number of redirects allowed. This number is used to prevent endless redirect loops.
     */
    private static final int MAX_REDIRECT_COUNT = 5;

    /**
     * The object used get the portlet request type associated with a servlet URL.
     */
    private URLRequestTypeMapper urlRequestTypeMapper;

    @Override
    public void init() throws PortletException
    {
        urlRequestTypeMapper = new URLRequestTypeMapper();
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            processView(request, response);
        } else if (PortletMode.EDIT.equals(request.getPortletMode())) {
            processEdit(request, response);
        }
    }

    /**
     * Processed the view action.
     * 
     * @param request the action request
     * @param response the action response
     * @throws IOException if writing the response fails
     * @throws PortletException if processing the action fails
     */
    protected void processView(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        request.setAttribute(ATTRIBUTE_REQUEST_TYPE, RequestType.ACTION);
        request.setAttribute(ATTRIBUTE_HOME_URL,
            request.getContextPath() + getDefaultDispatchURL(request.getPreferences()));
        String dispatchURL = getDispatchURL(request);
        String redirectURL = null;
        int i = 0;
        do {
            // Dispatch the request until there are no redirects.
            request.setAttribute(ATTRIBUTE_REDIRECT_URL, redirectURL);
            getPortletContext().getRequestDispatcher(dispatchURL).forward(request, response);
            ResponseData responseData = (ResponseData) request.getAttribute(ATTRIBUTE_RESPONSE_DATA);
            // Make sure the response data is fresh after the next dispatch.
            request.removeAttribute(ATTRIBUTE_RESPONSE_DATA);
            if (responseData.getRedirect() != null) {
                // Transform the redirect into a dispatch because redirects are not allowed during render request and we
                // can't create action URLs during action request.
                dispatchURL = responseData.getRedirect();
                redirectURL = dispatchURL;
            } else {
                // Put the response data on the session and pass the key as a render parameter.
                String responseKey = storeResponseData(request.getPortletSession(), responseData);
                response.setRenderParameter(PARAMETER_DISPATCHED_RESPONSE_KEY, responseKey);
                // Pass the last dispatch URL as a render parameter because the response data is removed from the
                // session when it is first accessed and the portal can trigger a render requests at any time.
                response.setRenderParameter(DispatchURLFactory.PARAMETER_DISPATCH_URL, dispatchURL);
                break;
            }
        } while (++i < MAX_REDIRECT_COUNT);
    }

    /**
     * Processes the edit action.
     * 
     * @param request the action request
     * @param response the action response
     * @throws PortletException if processing the edit form fails
     * @throws IOException if saving the portlet preferences fails
     */
    protected void processEdit(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        savePortletPreferences(request);
        response.setPortletMode(PortletMode.VIEW);
    }

    @Override
    protected String getTitle(RenderRequest request)
    {
        return request.getPreferences().getValue(PREFERENCE_TITLE, super.getTitle(request));
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        String dispatchURL = getDispatchURL(request);
        DispatchURLFactory dispatchURLFactory = new DispatchURLFactory(response, urlRequestTypeMapper, dispatchURL);
        StreamFilterManager streamFilterManager =
            new StreamFilterManager(dispatchURLFactory, request.getContextPath(), response.getNamespace(),
                RequestType.RENDER);
        ResponseData responseData = getResponseData(request);
        if (responseData != null) {
            renderResponseData(responseData, response, streamFilterManager);
        } else {
            request.setAttribute(ATTRIBUTE_REQUEST_TYPE, RequestType.RENDER);
            request.setAttribute(ATTRIBUTE_HOME_URL,
                request.getContextPath() + getDefaultDispatchURL(request.getPreferences()));
            request.setAttribute(ATTRIBUTE_STREAM_FILTER_MANAGER, streamFilterManager);
            getPortletContext().getRequestDispatcher(dispatchURL).forward(request, response);
        }
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        String editURL = request.getPreferences().getValue(PREFERENCE_EDIT_URL, null);
        DispatchURLFactory dispatchURLFactory = new DispatchURLFactory(response, urlRequestTypeMapper, editURL);
        StreamFilterManager streamFilterManager =
            new StreamFilterManager(dispatchURLFactory, request.getContextPath(), response.getNamespace(),
                RequestType.RENDER);
        request.setAttribute(ATTRIBUTE_REQUEST_TYPE, RequestType.RENDER);
        request.setAttribute(ATTRIBUTE_STREAM_FILTER_MANAGER, streamFilterManager);
        exposePortletPreferences(request);
        getPortletContext().getRequestDispatcher(editURL).forward(request, response);
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
    {
        request.setAttribute(ATTRIBUTE_REQUEST_TYPE, RequestType.RESOURCE);
        request.setAttribute(ATTRIBUTE_HOME_URL,
            request.getContextPath() + getDefaultDispatchURL(request.getPreferences()));
        String dispatchURL = getDispatchURL(request);
        DispatchURLFactory dispatchURLFactory = new DispatchURLFactory(response, urlRequestTypeMapper, dispatchURL);
        StreamFilterManager streamFilterManager =
            new StreamFilterManager(dispatchURLFactory, request.getContextPath(), response.getNamespace(),
                RequestType.RESOURCE);
        request.setAttribute(ATTRIBUTE_STREAM_FILTER_MANAGER, streamFilterManager);
        getPortletContext().getRequestDispatcher(dispatchURL).forward(request, response);
    }

    /**
     * @param request a portlet request
     * @return the URL to dispatch the request to, if defined on the given portlet request, {@code null} otherwise
     */
    private String getDispatchURL(PortletRequest request)
    {
        String dispatchURL = request.getParameter(DispatchURLFactory.PARAMETER_DISPATCH_URL);
        if (StringUtils.isEmpty(dispatchURL)) {
            dispatchURL = getDefaultDispatchURL(request.getPreferences());
        }
        // Remove the context path. This is required for dispatch URLs received from the client side.
        dispatchURL = StringUtils.removeStart(dispatchURL, request.getContextPath());
        // Remove the fragment identifier if present.
        return StringUtils.substringBefore(dispatchURL, "#");
    }

    /**
     * @param preferences the portlet preferences to take the default dispatch URL from
     * @return the URL used to dispatch the request when there's no dispatch URL parameter specified on the request
     */
    protected String getDefaultDispatchURL(PortletPreferences preferences)
    {
        return preferences.getValue(PREFERENCE_DEFAULT_DISPATCH_URL, null);
    }

    /**
     * Stores the response data on the session.
     * 
     * @param session the session
     * @param responseData the response data
     * @return the key that can be used to retrieve the response data from the session
     */
    @SuppressWarnings("unchecked")
    private String storeResponseData(PortletSession session, ResponseData responseData)
    {
        String responseKey = Long.toHexString(Double.doubleToLongBits(Math.random()));
        Map<String, ResponseData> responseDataMap =
            (Map<String, ResponseData>) session.getAttribute(ATTRIBUTE_RESPONSE_DATA_MAP);
        if (responseDataMap == null) {
            responseDataMap = new HashMap<String, ResponseData>();
            session.setAttribute(ATTRIBUTE_RESPONSE_DATA_MAP, responseDataMap);
        }
        responseDataMap.put(responseKey, responseData);
        return responseKey;
    }

    /**
     * @param request a request object
     * @return the response data that was stored on the session using the key from the request
     */
    @SuppressWarnings("unchecked")
    private ResponseData getResponseData(PortletRequest request)
    {
        String responseKey = request.getParameter(PARAMETER_DISPATCHED_RESPONSE_KEY);
        if (responseKey == null) {
            return null;
        }
        Map<String, ResponseData> responseDataMap =
            (Map<String, ResponseData>) request.getPortletSession().getAttribute(ATTRIBUTE_RESPONSE_DATA_MAP);
        if (responseDataMap == null) {
            return null;
        }
        return responseDataMap.remove(responseKey);
    }

    /**
     * Renders the data of a dispatched response.
     * 
     * @param responseData the data of a dispatched response
     * @param response the portlet response object used to render the data
     * @param streamFilterManager the object used to filter the output returned by the dispatch target
     * @throws IOException if the rendering fails
     */
    private void renderResponseData(ResponseData responseData, RenderResponse response,
        StreamFilterManager streamFilterManager) throws IOException
    {
        // Set cookies.
        for (Cookie cookie : responseData.getCookies()) {
            response.addProperty(cookie);
        }
        // Set HTTP headers.
        for (Map.Entry<String, List<String>> entry : responseData.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                response.addProperty(entry.getKey(), value);
            }
        }
        // Set response meta data.
        response.setContentType(responseData.getContentType());
        // We can set the character encoding of the portlet response only through its content type. Currently we don't
        // extract the character encoding from the content type in ResponseData#setContentType(String) so it can remain
        // unset. The character encoding is needed though to read the response data, in case it was written using the
        // output stream. We let the portlet response extract the character encoding from the content type and set the
        // result on the response data.
        if (responseData.getCharacterEncoding() == null) {
            responseData.setCharacterEncoding(response.getCharacterEncoding());
        }
        // Set response body.
        // Follow portlet recommendations to use the writer for text-based markup (PLT.12.5.2).
        streamFilterManager.filter(responseData.getMimeType(), responseData.getReader(), response.getWriter());
        response.flushBuffer();
    }

    /**
     * Exposes all changeable portlet preferences as request attributes.
     * 
     * @param request the request object used to access and expose the preferences
     */
    private void exposePortletPreferences(RenderRequest request)
    {
        PortletPreferences preferences = request.getPreferences();
        Enumeration<String> names = preferences.getNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!preferences.isReadOnly(name)) {
                request.setAttribute(name, preferences.getValues(name, null));
            }
        }
    }

    /**
     * Updates the portlet preferences with the values submitted with the given request.
     * 
     * @param request the request to take the new preference values from
     * @throws PortletException if setting the new preference values fails
     * @throws IOException if saving the preferences fails
     */
    private void savePortletPreferences(ActionRequest request) throws PortletException, IOException
    {
        PortletPreferences preferences = request.getPreferences();
        Enumeration<String> names = preferences.getNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!preferences.isReadOnly(name) && request.getParameter(name) != null) {
                preferences.setValues(name, request.getParameterValues(name));
            }
        }
        preferences.store();
    }
}
