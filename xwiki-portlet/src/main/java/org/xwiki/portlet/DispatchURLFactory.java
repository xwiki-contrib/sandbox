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

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Creates portlet URLs that tell to their target where to dispatch the associated request.
 * 
 * @version $Id$
 */
public class DispatchURLFactory
{
    /**
     * The object used to create the portlet URLs.
     */
    private final MimeResponse response;

    /**
     * The object used to get the portlet request type associated with a given servlet URL.
     */
    private final URLRequestTypeMapper urlRequestTypeMapper;

    /**
     * The URL to use when the dispatch URL is not specified or is {@code null}.
     */
    private final String defaultDispatchURL;

    /**
     * Creates a new URL factory based on the given response.
     * 
     * @param response the object used to create the portlet URLs
     * @param urlRequestTypeMapper the object used to get the portlet request type associated with a given servlet URL
     * @param defaultDispatchURL the URL to use when the dispatch URL is not specified or is {@code null}
     */
    public DispatchURLFactory(MimeResponse response, URLRequestTypeMapper urlRequestTypeMapper,
        String defaultDispatchURL)
    {
        this.response = response;
        this.urlRequestTypeMapper = urlRequestTypeMapper;
        this.defaultDispatchURL = defaultDispatchURL;
    }

    /**
     * @return a dispatch URL pointing to the current page
     */
    public BaseURL createURL()
    {
        return createURL(defaultDispatchURL);
    }

    /**
     * Creates a dispatch URL with the type determined from configuration.
     * 
     * @param dispatchURL where to dispatch the request to
     * @return a portlet URL that tells to its target to dispatch the request to the given dispatch URL
     */
    public BaseURL createURL(String dispatchURL)
    {
        return createURL(dispatchURL, urlRequestTypeMapper.getType(dispatchURL));
    }

    /**
     * @param requestType the type of URL to create
     * @return a dispatch URL with the specified type, pointing to the current page
     */
    public BaseURL createURL(RequestType requestType)
    {
        return createURL(defaultDispatchURL, requestType);
    }

    /**
     * Creates a dispatch URL with the specified type.
     * 
     * @param dispatchURL where to dispatch the request to
     * @param requestType the type of URL to create
     * @return a portlet URL that tells to its target to dispatch the request to the given dispatch URL
     */
    public BaseURL createURL(String dispatchURL, RequestType requestType)
    {
        BaseURL url;
        switch (requestType) {
            case ACTION:
                url = response.createActionURL();
                break;
            case RENDER:
                url = response.createRenderURL();
                break;
            case RESOURCE:
                url = response.createResourceURL();
                break;
            default:
                url = response.createRenderURL();
                break;
        }
        // We remove the fragment identifier from the dispatch URL. It would have been nice if we could add the fragment
        // identifier to the created portlet URL but the portlet specification doesn't support it so we just ignore the
        // portlet identifier.
        url.setParameter(DispatchPortlet.PARAMETER_DISPATCH_URL, StringUtils.substringBefore(dispatchURL, "#"));
        return url;
    }
}
