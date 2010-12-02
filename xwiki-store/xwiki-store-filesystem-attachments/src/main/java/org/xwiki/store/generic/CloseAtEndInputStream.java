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
 *
 */

package org.xwiki.store.generic.internal;

import java.io.InputStream;
import java.io.IOException;

/**
 * A wrapper around an InputStream which will close it when it reaches the end of the stream.
 *
 * @version $Id$
 * @since TODO
 */
public class CloseAtEndInputStream extends InputStream
{
    /** The stream which this stream wraps. */
    private InputStream stream;

    /**
     * The Constructor.
     *
     * @param toWrap the InputStream which this stream should wrap.
     */
    public CloseAtEndInputStream(final InputStream toWrap)
    {
        this.stream = toWrap;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException
    {
        if (this.stream != null) {
            this.stream.close();
            this.stream = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException
    {
        return this.stream != null ? handleReadOut(this.stream.available()) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException
    {
        return this.stream != null ? handleReadOut(this.stream.read()) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read(byte[])
     */
    public int read(final byte[] buffer) throws IOException
    {
        return this.stream != null ? handleReadOut(this.stream.read(buffer)) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(final byte[] buffer, final int offset, final int length) throws IOException
    {
        return this.stream != null ? handleReadOut(this.stream.read(buffer, offset, length)) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#skip(long)
     */
    public long skip(final long bytesToSkip) throws IOException
    {
        return this.stream != null ? this.stream.skip(bytesToSkip) : 0L;
    }

    /**
     * Close and drop the stream if the read request returns -1.
     *
     * @param readOutput the response from the read request.
     * @return readOutput for ease of chaining.
     * @throws IOException if the wrapped stream fails to close.
     */
    private int handleReadOut(final int readOutput) throws IOException
    {
        if (readOutput == -1) {
            this.stream.close();
            this.stream = null;
        }
        return readOutput;
    }
}
