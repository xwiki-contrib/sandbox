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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * Class used to write the servlet response in a string.
 * 
 * @version $Id$
 */
public class StringServletPrintWriter extends PrintWriter
{
    /**
     * Creates a new print writer that writes the servlet response content in a string.
     */
    public StringServletPrintWriter()
    {
        super(new StringWriter());
    }

    /**
     * @param encoding the encoding used to convert characters to bytes
     * @return an input stream that can be used to access the content written so far
     * @throws UnsupportedEncodingException if the specified encoding is not supported
     */
    public InputStream toInputStream(String encoding) throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream(((StringWriter) out).toString().getBytes(encoding));
    }

    /**
     * @return a reader that can be used to access the content written so far
     */
    public Reader toReader()
    {
        return new StringReader(((StringWriter) out).toString());
    }
}
