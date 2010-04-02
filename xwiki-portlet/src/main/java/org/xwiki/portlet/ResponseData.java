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
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

/**
 * Stores the response data.
 * 
 * @version $Id$
 */
public class ResponseData
{
    /**
     * The list of cookies.
     */
    private List<Cookie> cookies = new ArrayList<Cookie>();

    /**
     * The list of HTTP headers.
     */
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();

    /**
     * The character encoding.
     */
    private String characterEncoding;

    /**
     * The content type.
     */
    private String contentType;

    /**
     * The locale.
     */
    private Locale locale;

    /**
     * The content length.
     */
    private int contentLength;

    /**
     * The writer.
     */
    private StringServletPrintWriter writer;

    /**
     * The output stream.
     */
    private ByteArrayServletOutputStream outputStream;

    /**
     * The redirect location.
     */
    private String redirect;

    /**
     * The error code.
     */
    private int errorCode;

    /**
     * The error message.
     */
    private String errorMessage;

    /**
     * The status code.
     */
    private int statusCode;

    /**
     * The status message.
     */
    private String statusMessage;

    /**
     * Adds a cookie.
     * 
     * @param cookie the cookie to be added
     */
    public void addCookie(Cookie cookie)
    {
        cookies.add(cookie);
    }

    /**
     * @return the list of cookies
     */
    public List<Cookie> getCookies()
    {
        return Collections.unmodifiableList(cookies);
    }

    /**
     * Adds a new value to an HTTP response header.
     * 
     * @param name the header name
     * @param value the value to add
     */
    public void addHeader(String name, String value)
    {
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        values.add(value);
    }

    /**
     * @return the map of HTTP response headers
     */
    public Map<String, List<String>> getHeaders()
    {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * @param name the header name
     * @return {@code true} if the specified header was set, {@code false} otherwise
     */
    public boolean containsHeader(String name)
    {
        return headers.containsKey(name);
    }

    /**
     * @return the character encoding
     */
    public String getCharacterEncoding()
    {
        return characterEncoding;
    }

    /**
     * @return the content type
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * @return the content length
     */
    public int getContentLength()
    {
        return contentLength;
    }

    /**
     * @return the locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * @return the output stream
     * @throws IOException if the output stream is not accessible
     */
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (writer == null) {
            if (outputStream == null) {
                outputStream = new ByteArrayServletOutputStream();
            }
            return outputStream;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * @return the writer
     * @throws IOException if the writer is not accessible
     */
    public PrintWriter getWriter() throws IOException
    {
        if (outputStream == null) {
            if (writer == null) {
                writer = new StringServletPrintWriter();
            }
            return writer;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Sets the character encoding.
     * 
     * @param charset the new character encoding
     */
    public void setCharacterEncoding(String charset)
    {
        characterEncoding = charset;
    }

    /**
     * Sets the content length.
     * 
     * @param len the content length
     */
    public void setContentLength(int len)
    {
        contentLength = len;
    }

    /**
     * Sets the content type.
     * 
     * @param type the new content type
     */
    public void setContentType(String type)
    {
        contentType = type;
    }

    /**
     * Overwrite all the values associated with an HTTP response header.
     * 
     * @param name the header name
     * @param value the new value
     */
    public void setHeader(String name, String value)
    {
        headers.put(name, Arrays.asList(new String[] {value}));
    }

    /**
     * Sets the locale.
     * 
     * @param loc the new locale
     */
    public void setLocale(Locale loc)
    {
        locale = loc;
    }

    /**
     * Specifies that the request must be redirected.
     * 
     * @param location the location where to redirect the request
     */
    public void sendRedirect(String location)
    {
        redirect = location;
    }

    /**
     * @return the redirect location
     */
    public String getRedirect()
    {
        return redirect;
    }

    /**
     * Specifies that the request could not be fulfilled.
     * 
     * @param code the error code
     * @param message the error message
     */
    public void sendError(int code, String message)
    {
        errorCode = code;
        errorMessage = message;
    }

    /**
     * @return the error code
     */
    public int getErrorCode()
    {
        return errorCode;
    }

    /**
     * @return the error message;
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * Sets the response status.
     * 
     * @param code the status code
     * @param message the status message
     */
    public void setStatus(int code, String message)
    {
        statusCode = code;
        statusMessage = message;
    }

    /**
     * @return the status code
     */
    public int getStatusCode()
    {
        return statusCode;
    }

    /**
     * @return the status message
     */
    public String getStatusMessage()
    {
        return statusMessage;
    }

    /**
     * @return a reader that can be used to access the content written using either the writer returned by
     *         {@link #getWriter()} or the output stream returned by {@link #getOutputStream()}
     * @throws UnsupportedEncodingException if the encoding returned by {@link #getCharacterEncoding()} is unsupported
     */
    public Reader getReader() throws UnsupportedEncodingException
    {
        if (writer != null) {
            return writer.toReader();
        } else if (outputStream != null) {
            return outputStream.toReader(getCharacterEncoding());
        } else {
            return new StringReader("");
        }
    }
}
