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

/**
 * The request information that can change when a request is dispatched.
 * 
 * @version $Id$
 */
public class RequestDispatchInfo
{
    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    private String requestURI;

    /**
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    private String servletPath;

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    private String pathInfo;

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    private String queryString;

    /**
     * @return {@link #requestURI}
     */
    public String getRequestURI()
    {
        return requestURI;
    }

    /**
     * Sets the request URI.
     * 
     * @param requestURI the new request URI
     */
    public void setRequestURI(String requestURI)
    {
        this.requestURI = requestURI;
    }

    /**
     * @return {@link #setServletPath(String)}
     */
    public String getServletPath()
    {
        return servletPath;
    }

    /**
     * Sets the servlet path.
     * 
     * @param servletPath the new servlet path
     */
    public void setServletPath(String servletPath)
    {
        this.servletPath = servletPath;
    }

    /**
     * @return {@link #pathInfo}
     */
    public String getPathInfo()
    {
        return pathInfo;
    }

    /**
     * Sets the path info.
     * 
     * @param pathInfo the new path info
     */
    public void setPathInfo(String pathInfo)
    {
        this.pathInfo = pathInfo;
    }

    /**
     * @return {@link #queryString}
     */
    public String getQueryString()
    {
        return queryString;
    }

    /**
     * Sets the query string.
     * 
     * @param queryString the new query string
     */
    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }
}
