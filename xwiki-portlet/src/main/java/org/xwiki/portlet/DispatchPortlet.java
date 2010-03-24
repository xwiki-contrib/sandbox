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

/**
 * Dispatches portlet requests coming from a JSR286 compatible portal to the URL provided in the
 * {@link #PARAMETER_DISPATCH_URL} request parameter. The dispatch target must be on the same context path as the
 * dispatch portlet.
 * 
 * @version $Id$
 */
public class DispatchPortlet extends GenericPortlet
{
    /**
     * The name of the portlet request parameter holding the URL to dispatch the request to.
     */
    public static final String PARAMETER_DISPATCH_URL = "org.xwiki.portlet.parameter.dispatchURL";

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
     * The name of the preference that can be used to force the dispatch target to use the writer instead of the output
     * stream to generate the response. This is useful because some portlet containers (e.g. GateIn) don't support too
     * well the output stream when the content type is text/html.
     */
    public static final String PREFERENCE_FORCE_WRITER = "forceWriter";

    /**
     * Attribute used to pass the request type information to the dispatch servlet filter.
     */
    public static final String ATTRIBUTE_REQUEST_TYPE = "org.xwiki.portlet.attribute.requestType";

    /**
     * The request attribute used to pass the {@link DispatchURLFactory} to the dispatch target.
     */
    public static final String ATTRIBUTE_DISPATCH_URL_FACTORY = "org.xwiki.portlet.attribute.dispatchURLFactory";

    /**
     * The map of response data, stored on the session.
     */
    public static final String ATTRIBUTE_RESPONSE_DATA_MAP = "org.xwiki.portlet.attribute.responseDataMap";

    /**
     * The data of a dispatched response.
     */
    public static final String ATTRIBUTE_RESPONSE_DATA = "org.xwiki.portlet.attribute.responseData";

    /**
     * @see #PREFERENCE_FORCE_WRITER
     */
    public static final String ATTRIBUTE_FORCE_WRITER = "org.xwiki.portlet.attribute.forceWriter";

    /**
     * The object used get the portlet request type associated with a servlet URL.
     */
    private URLRequestTypeMapper urlRequestTypeMapper;

    /**
     * {@inheritDoc}
     * 
     * @see GenericPortlet#init()
     */
    @Override
    public void init() throws PortletException
    {
        urlRequestTypeMapper = new URLRequestTypeMapper();
    }

    /**
     * {@inheritDoc}
     * 
     * @see GenericPortlet#processAction(ActionRequest, ActionResponse)
     */
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
        getPortletContext().getRequestDispatcher(getDispatchURL(request)).forward(request, response);
        ResponseData responseData = (ResponseData) request.getAttribute(ATTRIBUTE_RESPONSE_DATA);
        if (responseData.getRedirect() != null) {
            response.setRenderParameter(PARAMETER_DISPATCH_URL, responseData.getRedirect());
        } else {
            String responseKey = storeResponseData(request.getPortletSession(), responseData);
            response.setRenderParameter(PARAMETER_DISPATCHED_RESPONSE_KEY, responseKey);
        }
    }

    /**
     * Processes the edit action.
     * 
     * @param request the action request
     * @param response the action response
     */
    protected void processEdit(ActionRequest request, ActionResponse response)
    {
        // Do nothing for now.
    }

    /**
     * {@inheritDoc}
     * 
     * @see GenericPortlet#doView(RenderRequest, RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        boolean forceWriter = Boolean.valueOf(request.getPreferences().getValue(PREFERENCE_FORCE_WRITER, null));
        DispatchURLFactory dispatchURLFactory = new DispatchURLFactory(response, urlRequestTypeMapper);
        ResponseData responseData = getResponseData(request);
        if (responseData != null) {
            URLRewriter rewriter = new URLRewriter(dispatchURLFactory, request.getContextPath());
            renderResponseData(responseData, response, rewriter, forceWriter);
        } else {
            request.setAttribute(ATTRIBUTE_FORCE_WRITER, forceWriter);
            request.setAttribute(ATTRIBUTE_REQUEST_TYPE, RequestType.RENDER);
            request.setAttribute(ATTRIBUTE_DISPATCH_URL_FACTORY, dispatchURLFactory);
            getPortletContext().getRequestDispatcher(getDispatchURL(request)).forward(request, response);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see GenericPortlet#doEdit(RenderRequest, RenderResponse)
     */
    @Override
    protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        // Do nothing for now.
        super.doEdit(request, response);
    }

    /**
     * {@inheritDoc}
     * 
     * @see GenericPortlet#serveResource(ResourceRequest, ResourceResponse)
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
    {
        request.setAttribute(ATTRIBUTE_REQUEST_TYPE, RequestType.RESOURCE);
        request.setAttribute(ATTRIBUTE_DISPATCH_URL_FACTORY, new DispatchURLFactory(response, urlRequestTypeMapper));
        getPortletContext().getRequestDispatcher(getDispatchURL(request)).forward(request, response);
    }

    /**
     * @param request a portlet request
     * @return the URL to dispatch the request to, if defined on the given portlet request, {@code null} otherwise
     */
    private String getDispatchURL(PortletRequest request)
    {
        PortletPreferences preferences = request.getPreferences();
        String dispatchURL = request.getParameter(PARAMETER_DISPATCH_URL);
        if (dispatchURL == null) {
            dispatchURL = preferences.getValue(PREFERENCE_DEFAULT_DISPATCH_URL, null);
        }
        return dispatchURL;
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
     * @param rewriter the object used to rewrite servlet URLs into portlet URLs
     * @param forceWriter {@code true} to force the use of the writer, {@code false} to use whatever the response data
     *            prefers
     * @throws IOException if the rendering fails
     */
    private void renderResponseData(ResponseData responseData, RenderResponse response, URLRewriter rewriter,
        boolean forceWriter) throws IOException
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
        // Set response body.
        if (!forceWriter && responseData.isByteStream()) {
            rewriter.rewrite(responseData.getInputStream(), response.getPortletOutputStream());
        } else {
            rewriter.rewrite(responseData.getReader(), response.getWriter());
        }
        response.flushBuffer();
    }
}
