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
        DispatchedActionResponse responseWrapper = new DispatchedActionResponse(response);
        chain.doFilter(new DispatchedRequest(request), responseWrapper);
        request.setAttribute(DispatchPortlet.ATTRIBUTE_RESPONSE_DATA, responseWrapper.getResponseData());
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
}
