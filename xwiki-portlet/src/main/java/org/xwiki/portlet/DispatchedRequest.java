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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wraps a servlet request object dispatched from a portlet.
 * 
 * @version $Id$
 */
public class DispatchedRequest extends HttpServletRequestWrapper
{
    /**
     * The character encoding set through {@link #setCharacterEncoding(String)}.
     */
    private String settedCharacterEncoding;

    /**
     * Wraps the given request that has been dispatched from a portlet.
     * 
     * @param request the request to be wrapped
     */
    public DispatchedRequest(HttpServletRequest request)
    {
        super(request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * We override the default implementation to overcome the fact that calling {@code getRequestURL()} on a servlet
     * request dispatched from a portlet returns {@code null}. See chapter
     * "PLT.19.3.4 Request and Response Objects for Included Servlets/JSPs from within the Render Method" in JSR286
     * Portlet Specification.
     * 
     * @see HttpServletRequestWrapper#getRequestURL()
     * @see org.apache.catalina.connector.Request#getRequestURL()
     */
    public StringBuffer getRequestURL()
    {
        StringBuffer url = super.getRequestURL();
        if (url != null) {
            return url;
        }

        url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            // Work around for a java.net.URL bug.
            port = 80;
        }

        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());

        return url;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation returns {@code null} if the request was dispatched from a portlet's render method. We
     * override this method to return either the value set through {@link #setCharacterEncoding(String)} or the value
     * passed from a portlet's action processing method.
     * 
     * @see HttpServletRequestWrapper#getCharacterEncoding()
     * @see HttpServletRequestWrapper#setCharacterEncoding(String)
     */
    @Override
    public String getCharacterEncoding()
    {
        String characterEncoding = super.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = settedCharacterEncoding;
        }
        return characterEncoding;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation does nothing if the request was dispatched from a portlet's render method. We override
     * this method to store the given character encoding. This way the behavior is consistent with what happens when the
     * request is not dispatched from a portlet.
     * 
     * @see HttpServletRequestWrapper#setCharacterEncoding(String)
     */
    @Override
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException
    {
        super.setCharacterEncoding(characterEncoding);
        this.settedCharacterEncoding = characterEncoding;
    }
}
