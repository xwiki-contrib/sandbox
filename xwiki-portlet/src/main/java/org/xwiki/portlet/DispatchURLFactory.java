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
     * Creates a new URL factory based on the given response.
     * 
     * @param response the object used to create the portlet URLs
     * @param urlRequestTypeMapper the object used to get the portlet request type associated with a given servlet URL
     */
    public DispatchURLFactory(MimeResponse response, URLRequestTypeMapper urlRequestTypeMapper)
    {
        this.response = response;
        this.urlRequestTypeMapper = urlRequestTypeMapper;
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
                url = null;
                break;
        }
        url.setParameter(DispatchPortlet.PARAMETER_DISPATCH_URL, dispatchURL);
        // TODO: The following line is just for demo. Don't forget to remove it.
        url.setParameter("xpage", "plain");
        return url;
    }
}