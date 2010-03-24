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
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wraps a servlet response object dispatched from a portlet's process action method.
 * 
 * @version $Id$
 */
public class DispatchedActionResponse extends HttpServletResponseWrapper
{
    /**
     * The response data.
     */
    private final ResponseData responseData = new ResponseData();

    /**
     * Wraps the given response object that has been dispatched from a portlet's process action method.
     * 
     * @param response the response object to be wrapped
     */
    public DispatchedActionResponse(HttpServletResponse response)
    {
        super(response);

        responseData.setCharacterEncoding(response.getCharacterEncoding());
        responseData.setContentType(response.getContentType());
        responseData.setLocale(response.getLocale());
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#addCookie(Cookie)
     */
    @Override
    public void addCookie(Cookie cookie)
    {
        responseData.addCookie(cookie);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#addHeader(String, String)
     */
    @Override
    public void addHeader(String name, String value)
    {
        responseData.addHeader(name, value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#containsHeader(String)
     */
    @Override
    public boolean containsHeader(String name)
    {
        return responseData.containsHeader(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding()
    {
        return responseData.getCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getContentType()
     */
    @Override
    public String getContentType()
    {
        return responseData.getContentType();
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getLocale()
     */
    @Override
    public Locale getLocale()
    {
        return responseData.getLocale();
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return responseData.getOutputStream();
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException
    {
        return responseData.getWriter();
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#setCharacterEncoding(String)
     */
    @Override
    public void setCharacterEncoding(String charset)
    {
        responseData.setCharacterEncoding(charset);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#setContentLength(int)
     */
    @Override
    public void setContentLength(int len)
    {
        responseData.setContentLength(len);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#setContentType(String)
     */
    @Override
    public void setContentType(String type)
    {
        responseData.setContentType(type);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#setHeader(String, String)
     */
    @Override
    public void setHeader(String name, String value)
    {
        responseData.setHeader(name, value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#setLocale(Locale)
     */
    @Override
    public void setLocale(Locale loc)
    {
        responseData.setLocale(loc);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#sendError(int, String)
     */
    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        responseData.sendError(sc, msg);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#sendRedirect(String)
     */
    @Override
    public void sendRedirect(String location) throws IOException
    {
        responseData.sendRedirect(location);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#setStatus(int, String)
     */
    @Override
    public void setStatus(int sc, String sm)
    {
        responseData.setStatus(sc, sm);
    }

    /**
     * @return the response data
     */
    public ResponseData getResponseData()
    {
        return responseData;
    }
}
