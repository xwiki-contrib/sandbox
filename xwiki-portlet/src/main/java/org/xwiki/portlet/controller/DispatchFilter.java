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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.portlet.PortletRequest;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.portlet.DispatchPortlet;
import org.xwiki.portlet.model.ResponseData;
import org.xwiki.portlet.view.StreamFilterManager;

/**
 * Wraps servlet requests that are dispatched from a portlet to overcome some of the limitations enforced by the JSR286
 * Portlet Specification.
 * 
 * @version $Id$
 */
public class DispatchFilter implements Filter
{
    /**
     * The logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchFilter.class);

    /**
     * The name of the request attribute that specifies if this filter has already been applied to the current request.
     * This flag can be used to prevent processing the same request multiple times. The value of this request attribute
     * is a {@link Boolean} value.
     */
    private static final String ATTRIBUTE_APPLIED = DispatchFilter.class.getName() + ".applied";

    /**
     * The configuration object.
     */
    private FilterConfig config;

    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException
    {
        if (request instanceof HttpServletRequest
            && Boolean.TRUE.equals(request.getAttribute(DispatchPortlet.ATTRIBUTE_DISPATCHED))) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Mark the request to know that the filter has been applied.
            boolean applied = Boolean.TRUE.equals(request.getAttribute(ATTRIBUTE_APPLIED));
            request.setAttribute(ATTRIBUTE_APPLIED, Boolean.TRUE);

            boolean done = true;
            PortletRequest portletRequest = (PortletRequest) request.getAttribute("javax.portlet.request");
            String phase = (String) portletRequest.getAttribute(PortletRequest.LIFECYCLE_PHASE);
            if (PortletRequest.RESOURCE_PHASE.equals(phase)) {
                doResource(httpRequest, httpResponse, chain);
            } else if (!applied && PortletRequest.RENDER_PHASE.equals(phase)) {
                doRender(httpRequest, httpResponse, chain);
            } else if (!applied && PortletRequest.ACTION_PHASE.equals(phase)) {
                // During the action phase the response redirects are simulated with a request dispatcher but the
                // dispatches are done one after another from the portlet so this filter is not called recursively.
                // As a consequence we don't handle nested calls during the action phase.
                doAction(httpRequest, httpResponse, chain);
            } else {
                done = false;
            }

            // Remove the marker to allow consecutive calls to this filter, as long as they are not nested.
            request.removeAttribute(ATTRIBUTE_APPLIED);
            if (done) {
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Filters an action request.
     * 
     * @param request the request object
     * @param response the response object
     * @param chain the filter chain
     * @throws IOException if writing the response fails
     * @throws ServletException if processing the request fails
     */
    private void doAction(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        // NOTE: Exposing the initial query string parameters on both action and render portlet requests can have
        // unexpected side effects if the action and render portlet requests share the same client request. For the
        // moment we expose the parameters only on action request.
        HttpServletRequest requestWrapper = wrapRequest(request, true);
        DispatchedActionResponse responseWrapper = new DispatchedActionResponse(response);
        chain.doFilter(requestWrapper, responseWrapper);
        ResponseData responseData = responseWrapper.getResponseData();
        // Transform the redirect URL to be able to use it with a request dispatcher.
        // We pass the request wrapper because we need the request URL for the transformation.
        responseData.sendRedirect(getDispatchURL(responseData.getRedirect(), requestWrapper));
        request.setAttribute(DispatchPortlet.ATTRIBUTE_RESPONSE_DATA, responseData);
    }

    /**
     * Filters a render request.
     * 
     * @param request the request object
     * @param response the response object
     * @param chain the filter chain
     * @throws ServletException if processing the request fails
     * @throws IOException if writing the response fails
     */
    private void doRender(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        // The content type is not always set for static resources. The dispatched response needs to know the content
        // type before its writer or output stream are requested in order to determine if the output should be written
        // directly to the original response or stored for processing.
        String mimeType = config.getServletContext().getMimeType(request.getRequestURI());
        if (mimeType != null && response.getContentType() == null) {
            response.setContentType(mimeType);
        }

        HttpServletRequest requestWrapper = wrapRequest(request, false);
        StreamFilterManager streamFilterManager = new StreamFilterManager(requestWrapper);
        DispatchedMimeResponse responseWrapper =
            new DispatchedMimeResponse(response, streamFilterManager.getKnownMimeTypes());
        chain.doFilter(requestWrapper, responseWrapper);

        if (responseWrapper.getRedirect() != null) {
            String dispatchURL = getDispatchURL(responseWrapper.getRedirect(), requestWrapper);
            request.setAttribute(DispatchPortlet.ATTRIBUTE_REDIRECT_URL, dispatchURL);
        } else if (responseWrapper.isOutputIntercepted()) {
            // Invalidate the current content length because the content is going to be transformed.
            response.setContentLength(-1);
            LOGGER.debug("Filtering " + request.getRequestURI());
            streamFilterManager.filter(responseWrapper.getMediaType(), responseWrapper.getReader(),
                response.getWriter());
        }
    }

    /**
     * Filters a resource request.
     * 
     * @param request the request object
     * @param response the response object
     * @param chain the filter chain
     * @throws ServletException if processing the request fails
     * @throws IOException if writing the response fails
     */
    private void doResource(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        doRender(request, response, chain);

        String dispatchURL = (String) request.getAttribute(DispatchPortlet.ATTRIBUTE_REDIRECT_URL);
        if (dispatchURL != null) {
            // Simulate the (forbidden) redirect with a forward.
            request.getRequestDispatcher(dispatchURL).forward(wrapRequest(request, false), response);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        this.config = config;
    }

    /**
     * Creates an URL that can be used to dispatch the request.
     * 
     * @param url the source URL from where the dispatch URL is extracted/computed
     * @param request the request object used to extract/compute the dispatch URL
     * @return an URL that can be used to create a request dispatcher
     */
    private String getDispatchURL(String url, HttpServletRequest request)
    {
        if (url == null) {
            return null;
        }
        // Remove the fragment identifier from the URL if present.
        String dispatchURL = StringUtils.substringBefore(url, "#");
        if (dispatchURL.length() > 0 && dispatchURL.charAt(0) == '/') {
            // URL relative to the servlet container root.
            if (dispatchURL.startsWith(request.getContextPath())) {
                // We can dispatch only if the URL has the same context path as the current request.
                dispatchURL = dispatchURL.substring(request.getContextPath().length());
            }
        } else {
            if (dispatchURL.indexOf("://") > 0) {
                // Absolute URL.
                try {
                    URL actualURL = new URL(dispatchURL);
                    URL expectedURL = new URL(request.getRequestURL().toString());
                    if (sameServer(expectedURL, actualURL)
                        && actualURL.getPath().startsWith(request.getContextPath())) {
                        dispatchURL = actualURL.getPath().substring(request.getContextPath().length());
                    }
                } catch (MalformedURLException e) {
                    // We shouldn't get here.
                }
            } else {
                // URL relative to the current request URI.
                dispatchURL = request.getRequestURI().substring(request.getContextPath().length()) + '/' + url;
            }
        }
        return dispatchURL;
    }

    /**
     * @param expectedURL the expected URL
     * @param actualURL the actual URL
     * @return {@code true} if the given URLs point to the same web server, {@code false} otherwise
     */
    private boolean sameServer(URL expectedURL, URL actualURL)
    {
        return expectedURL.getProtocol().equals(actualURL.getProtocol())
            && expectedURL.getAuthority().equals(actualURL.getAuthority());
    }

    /**
     * Wraps the given request, optionally exposing the initial query string parameters.
     * 
     * @param request the request to be wrapped
     * @param exposeInitialQueryStringParameters {@code true} to expose the initial query string parameters,
     *            {@code false} otherwise
     * @return the request wrapper
     * @throws ServletException if wrapping the given request fails
     */
    private HttpServletRequest wrapRequest(HttpServletRequest request, boolean exposeInitialQueryStringParameters)
        throws ServletException
    {
        String redirectURL = (String) request.getAttribute(DispatchPortlet.ATTRIBUTE_REDIRECT_URL);
        if (redirectURL != null) {
            request.removeAttribute(DispatchPortlet.ATTRIBUTE_REDIRECT_URL);
            return new DispatchedRequest(request, redirectURL);
        } else {
            // Check if the request is not already wrapped.
            ServletRequest wrappedRequest = request;
            while (wrappedRequest instanceof HttpServletRequestWrapper) {
                if (wrappedRequest instanceof DispatchedRequest) {
                    return request;
                }
                wrappedRequest = ((HttpServletRequestWrapper) wrappedRequest).getRequest();
            }
            return new DispatchedRequest(request, exposeInitialQueryStringParameters);
        }
    }
}
