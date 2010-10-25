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

package org.xwiki.blob.internal;

import java.io.OutputStream;
import java.io.IOException;

/**
 * An OutputStream which does something when it's closed such as releasing a lock.
 *
 * @version $Id$
 * @since 2.6M1
 */
public class RunOnCloseOutputStream extends OutputStream
{
    /** The stream which this stream wraps. */
    private final OutputStream stream;

    /** The runnable which will be run when this stream is closed. */
    private final Runnable runnable;

    /**
     * The Constructor.
     *
     * @param toWrap the OutputStream which this stream should wrap.
     * @param toRunOnClose the Runnable which should be run when this stream is closed.
     */
    public RunOnCloseOutputStream(OutputStream toWrap, Runnable toRunOnClose)
    {
        this.stream = toWrap;
        this.runnable = toRunOnClose;
    }

    /**
     * {@inheritDoc}
     *
     * Runs the given Runnable after the underlying stream is closed.
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException
    {
        this.stream.close();
        this.runnable.run();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException
    {
        this.stream.flush();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(final byte[] byteArray) throws IOException
    {
        this.stream.write(byteArray);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(final byte[] byteArray, final int offset, final int length) throws IOException
    {
        this.stream.write(byteArray, offset, length);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#write(int)
     */
    public void write(final int singleByte) throws IOException
    {
        this.stream.write(singleByte);
    }
}
