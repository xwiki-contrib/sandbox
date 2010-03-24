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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;

/**
 * Class used to write the servlet response in a byte array.
 * 
 * @version $Id$
 */
public class ByteArrayServletOutputStream extends ServletOutputStream
{
    /**
     * The underlying byte array output stream used.
     */
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    /**
     * {@inheritDoc}
     * 
     * @see ServletOutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException
    {
        outputStream.write(b);
    }

    /**
     * @return an input stream that can be used to access the content written so far in this output stream
     */
    public InputStream toInputStream()
    {
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * @param encoding the encoding to be used to read the byte stream
     * @return a reader that can be used to access the content written so far in this output stream
     * @throws UnsupportedEncodingException if the specified encoding is not supported
     */
    public Reader toReader(String encoding) throws UnsupportedEncodingException
    {
        return new StringReader(outputStream.toString(encoding));
    }
}
