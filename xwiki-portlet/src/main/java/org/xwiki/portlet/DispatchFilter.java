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
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Wraps servlet requests that are dispatched from a portlet to overcome some of the limitations enforced by the JSR286
 * Portlet Specification.
 * 
 * @version $Id$
 */
public class DispatchFilter implements Filter
{
    /**
     * The name of the request attribute that specifies if this filter has already been applied to the current request.
     * This flag is required to prevent prevent processing the same request multiple times. The value of this request
     * attribute is a string. The associated boolean value is determined using {@link Boolean#valueOf(String)}.
     */
    private static final String ATTRIBUTE_APPLIED = DispatchFilter.class.getName() + ".applied";

    /**
     * {@inheritDoc}
     * 
     * @see Filter#destroy()
     */
    public void destroy()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException
    {
        if (request instanceof HttpServletRequest
            && !Boolean.valueOf((String) request.getAttribute(ATTRIBUTE_APPLIED))) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            RequestType requestType = (RequestType) request.getAttribute(DispatchPortlet.ATTRIBUTE_REQUEST_TYPE);
            if (requestType != null) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                request.setAttribute(ATTRIBUTE_APPLIED, "true");
                switch (requestType) {
                    case ACTION:
                        doAction(httpRequest, httpResponse, chain);
                        return;
                    case RENDER:
                        doRender(httpRequest, httpResponse, chain);
                        return;
                    case RESOURCE:
                        doResource(httpRequest, httpResponse, chain);
                        return;
                    default:
                        break;
                }
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
        DispatchedRequest requestWrapper = new DispatchedRequest(request);
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
        DispatchedMimeResponse responseWrapper = new DispatchedMimeResponse(response);
        chain.doFilter(new DispatchedRequest(request), responseWrapper);

        if (responseWrapper.isHTML()) {
            URLRewriter rewriter =
                new URLRewriter((DispatchURLFactory) request
                    .getAttribute(DispatchPortlet.ATTRIBUTE_DISPATCH_URL_FACTORY), request.getContextPath());
            rewriter.rewrite(responseWrapper.getReader(), response.getWriter());
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
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
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
        String dispatchURL = url;
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
}
