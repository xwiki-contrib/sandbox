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
package org.xwiki.portlet.url;

import javax.portlet.BaseURL;

import org.xwiki.portlet.model.RequestType;

/**
 * Rewrites relative servlet URLs into portlet URLs.
 * 
 * @version $Id$
 */
public class DefaultURLRewriter implements URLRewriter
{
    /**
     * The object used to create portlet URLs.
     */
    private final DispatchURLFactory dispatchURLFactory;

    /**
     * The servlet context path. Relative URLs prefixed with this string are transformed into portlet URLs.
     */
    private final String contextPath;

    /**
     * Creates a new URL rewriter that transforms relative servlet URLs into portlet URLs.
     * 
     * @param dispatchURLFactory the object used to create portlet URLs
     * @param contextPath the servlet context path, used to determine relative servlet URLs
     */
    public DefaultURLRewriter(DispatchURLFactory dispatchURLFactory, String contextPath)
    {
        this.dispatchURLFactory = dispatchURLFactory;
        this.contextPath = contextPath;
    }

    /**
     * {@inheritDoc}
     * 
     * @see URLRewriter#rewrite(String, Object...)
     */
    public String rewrite(String servletURL, Object... parameters)
    {
        BaseURL portletURL = null;
        RequestType requestType = parameters.length > 0 ? (RequestType) parameters[0] : null;
        if (servletURL.length() == 0 && requestType != null) {
            portletURL = dispatchURLFactory.createURL(requestType);
        } else if (servletURL.startsWith(contextPath)) {
            if (requestType != null) {
                portletURL = dispatchURLFactory.createURL(servletURL.substring(contextPath.length()), requestType);
            } else {
                portletURL = dispatchURLFactory.createURL(servletURL.substring(contextPath.length()));
            }
        }
        return portletURL != null ? portletURL.toString() : servletURL;
    }
}
