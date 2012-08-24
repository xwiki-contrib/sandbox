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
package org.xwiki.portlet.view;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.url.DefaultURLRewriter;
import org.xwiki.portlet.url.DispatchURLFactory;
import org.xwiki.portlet.url.URLRequestTypeMapper;
import org.xwiki.portlet.url.URLRewriter;

/**
 * Applies the right stream filter based on the stream content type.
 * 
 * @version $Id$
 */
public class StreamFilterManager
{
    /**
     * The logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamFilterManager.class);

    /**
     * The object used get the portlet request type associated with a servlet URL.
     */
    private static final URLRequestTypeMapper URL_REQUEST_TYPE_MAPPER = new URLRequestTypeMapper();

    /**
     * The mapping between mime types and stream filters.
     */
    private final Map<String, StreamFilter> streamFilters = new HashMap<String, StreamFilter>();

    /**
     * Creates a new {@link StreamFilter} manager for the given servlet request.
     * 
     * @param request the servlet request whose response will be filtered
     */
    public StreamFilterManager(HttpServletRequest request)
    {
        this((PortletRequest) request.getAttribute("javax.portlet.request"), (MimeResponse) request
            .getAttribute("javax.portlet.response"), getDispatchURL(request));
    }

    /**
     * Creates a new {@link StreamFilter} manager for the given portlet request and response.
     * 
     * @param request the portlet request
     * @param response the portlet response
     * @param dispatchURL the URL where the request will be dispatched
     */
    public StreamFilterManager(PortletRequest request, MimeResponse response, String dispatchURL)
    {
        this(new DispatchURLFactory(response, URL_REQUEST_TYPE_MAPPER, dispatchURL), request.getContextPath(), response
            .getNamespace(), PortletRequest.RESOURCE_PHASE.equals(request.getAttribute(PortletRequest.LIFECYCLE_PHASE))
            ? RequestType.RESOURCE : RequestType.RENDER);
    }

    /**
     * Creates a new {@link StreamFilter} manager that uses the given portlet URL factory and the given portlet
     * name-space.
     * 
     * @param dispatchURLFactory the object used to create portlet URLs
     * @param contextPath the servlet context path, used to determine relative servlet URLs
     * @param portletNamespace the string used to name-space HTML element identifiers in the context of the portal page
     * @param requestType the type of the current request
     */
    public StreamFilterManager(DispatchURLFactory dispatchURLFactory, String contextPath, String portletNamespace,
        RequestType requestType)
    {
        URLRewriter urlRewriter = new DefaultURLRewriter(dispatchURLFactory, contextPath);
        streamFilters.put("text/html", new HTMLStreamFilter(urlRewriter, portletNamespace,
            requestType == RequestType.RENDER));
        streamFilters.put("text/css", new CSSStreamFilter(portletNamespace, urlRewriter));
        streamFilters.put("text/javascript", new JavaScriptStreamFilter(portletNamespace));
    }

    /**
     * @param request a HTTP servlet request
     * @return the corresponding dispatch URL
     */
    private static String getDispatchURL(HttpServletRequest request)
    {
        StringBuilder dispatchURL = new StringBuilder(request.getServletPath());
        dispatchURL.append(StringUtils.defaultString(request.getPathInfo()));
        if (request.getQueryString() != null) {
            dispatchURL.append('?').append(request.getQueryString());
        }
        return dispatchURL.toString();
    }

    /**
     * Applies the right stream filter based on the given content type.
     * 
     * @param contentType the stream content type
     * @param reader the character stream to be filtered
     * @param writer the resulting character stream
     */
    public void filter(String contentType, Reader reader, Writer writer)
    {
        StreamFilter streamFilter = streamFilters.get(contentType);
        if (streamFilter != null) {
            streamFilter.filter(reader, writer);
        } else {
            try {
                IOUtils.copy(reader, writer);
            } catch (IOException e) {
                LOGGER.error("Failed to copy stream.", e);
            }
        }
    }

    /**
     * @return the set of mime types known by this filter manager, i.e. the set of mime types for which there is a
     *         registered filter
     */
    public Set<String> getKnownMimeTypes()
    {
        return Collections.unmodifiableSet(streamFilters.keySet());
    }
}
