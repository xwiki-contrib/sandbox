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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wraps a servlet response object dispatched from a portlet's render or serve resource method.
 * 
 * @version $Id$
 */
public class DispatchedMimeResponse extends HttpServletResponseWrapper
{
    /**
     * The response output stream wrapper.
     */
    private ByteArrayServletOutputStream outputStreamWrapper;

    /**
     * The response writer wrapper.
     */
    private StringServletPrintWriter writerWrapper;

    /**
     * Wraps the given servlet response that has been dispatched from a portlet's render or serve resource method.
     * 
     * @param response the response object to be wrapped
     */
    public DispatchedMimeResponse(HttpServletResponse response)
    {
        super(response);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (isHTML()) {
            if (writerWrapper == null) {
                if (outputStreamWrapper == null) {
                    outputStreamWrapper = new ByteArrayServletOutputStream();
                }
                return outputStreamWrapper;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return super.getOutputStream();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HttpServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (isHTML()) {
            if (outputStreamWrapper == null) {
                if (writerWrapper == null) {
                    writerWrapper = new StringServletPrintWriter();
                }
                return writerWrapper;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return super.getWriter();
        }
    }

    /**
     * @return {@code true} if the content type of the response body is HTML, {@code false} otherwise
     */
    public boolean isHTML()
    {
        return getContentType() == null || getContentType().startsWith("text/html");
    }

    /**
     * @return {@code true} if the response was generated using the output stream wrapper, {@code false} otherwise
     */
    public boolean isByteStream()
    {
        return outputStreamWrapper != null;
    }

    /**
     * @return an input stream that can be used to access the content written using the output stream returned by
     *         {@link #getOutputStream()}
     * @throws UnsupportedEncodingException if the encoding returned by {@link #getCharacterEncoding()} is not supported
     */
    public InputStream getInputStream() throws UnsupportedEncodingException
    {
        if (outputStreamWrapper != null) {
            return outputStreamWrapper.toInputStream();
        } else if (writerWrapper != null) {
            return writerWrapper.toInputStream(getCharacterEncoding());
        } else {
            return new ByteArrayInputStream(new byte[] {});
        }
    }

    /**
     * @return a reader that can be used to access the content written using the writer returned by {@link #getWriter()}
     * @throws UnsupportedEncodingException if the encoding returned by {@link #getCharacterEncoding()} is unsupported
     */
    public Reader getReader() throws UnsupportedEncodingException
    {
        if (writerWrapper != null) {
            return writerWrapper.toReader();
        } else if (outputStreamWrapper != null) {
            return outputStreamWrapper.toReader(getCharacterEncoding());
        } else {
            return new StringReader("");
        }
    }
}
