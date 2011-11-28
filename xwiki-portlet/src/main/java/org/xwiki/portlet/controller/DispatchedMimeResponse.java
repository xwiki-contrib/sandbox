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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.portlet.util.ByteArrayServletOutputStream;
import org.xwiki.portlet.util.StringServletPrintWriter;

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
     * Flag indicating if the response output is preserved or not. Once this flag is set to {@code true} it remains so
     * and the response body is written to the output stream or writer of the original response object.
     */
    private boolean outputPreserved;

    /**
     * The set of content types that must suffer transformations and thus can't be written directly to the original
     * response object.
     */
    private final Set<String> knownMimeTypes;

    /**
     * The media type of this response.
     * <p>
     * NOTE: We store the content type because some portlet containers don't allow the content type to be set when the
     * request has been forwarded (PLT.19.4 from JSR286).
     * 
     * @see http://www-01.ibm.com/support/docview.wss?uid=swg1PM22501
     */
    private String contentType;

    /**
     * Wraps the given servlet response that has been dispatched from a portlet's render or serve resource method.
     * 
     * @param response the response object to be wrapped
     * @param knownMimeTypes the set of known mime types, i.e. the content types for which the output is not preserved
     */
    public DispatchedMimeResponse(HttpServletResponse response, Set<String> knownMimeTypes)
    {
        super(response);

        this.knownMimeTypes = knownMimeTypes;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (isOutputIntercepted()) {
            if (writerWrapper == null) {
                if (outputStreamWrapper == null) {
                    outputStreamWrapper = new ByteArrayServletOutputStream();
                }
                return outputStreamWrapper;
            } else {
                throw new IllegalStateException();
            }
        } else {
            outputPreserved = true;
            return super.getOutputStream();
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (isOutputIntercepted()) {
            if (outputStreamWrapper == null) {
                if (writerWrapper == null) {
                    writerWrapper = new StringServletPrintWriter();
                }
                return writerWrapper;
            } else {
                throw new IllegalStateException();
            }
        } else {
            outputPreserved = true;
            return super.getWriter();
        }
    }

    /**
     * @return {@code true} if the response output was intercepted and not committed to the original response object,
     *         {@code false} otherwise
     */
    public boolean isOutputIntercepted()
    {
        return !outputPreserved
            && (outputStreamWrapper != null || writerWrapper != null || knownMimeTypes.contains(getMediaType()));
    }

    /**
     * @return a reader that can be used to access the content written using either the writer returned by
     *         {@link #getWriter()} or the output stream returned by {@link #getOutputStream()}
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

    /**
     * @return the media type of the resource that is being served
     */
    public String getMediaType()
    {
        return StringUtils.substringBefore(getContentType(), ";");
    }

    @Override
    public void setContentType(String contentType)
    {
        super.setContentType(contentType);
        this.contentType = contentType;
    }

    @Override
    public String getContentType()
    {
        String type = super.getContentType();
        return type != null ? type : this.contentType;
    }
}
