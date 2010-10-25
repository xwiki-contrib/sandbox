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

package org.xwiki.blob;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

/**
 * BinaryObject represents a potentially large amount of binary content.
 * Implementations must be thread safe and should make an effort to prevent blocking where possible.
 *
 * @version $Id$
 * @since 2.6M1
 */
@ComponentRole()
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public interface BinaryObject extends Cloneable
{
    /**
     * Put content into this BinaryObject.
     * If the object already has content then more is appended to what already exists.
     * This change is not effective until {@link #save()} has been called.
     *
     * @param content a stream containing the content to place in this BinaryObject, the adding of content will end
     *        when the end of the stream is reached.
     * @throws IOException if the BinaryObject is unable to write to the storage location.
     */
    void addContent(final InputStream content) throws IOException;

    /**
     * Put content into this BinaryObject.
     * If the object already has content then more is appended to what already exists.
     * This change is not effective until {@link #save()} has been called.
     *
     * @return an OutputStream to which the content can be written to. Additional requests to write may block until
     *         the stream is closed.
     * @throws IOException if the BinaryObject is unable to write to the storage location.
     */
    OutputStream addContent() throws IOException;

    /**
     * Remove all content from this BinaryObject.
     * This change is not effective until {@link #save()} has been called.
     *
     * @throws IOException if the BinaryObject is unable to write the storage location.
     */
    void clear() throws IOException;

    /**
     * Save the current content into the persistent storage location.
     * If this BinaryObject has been saved before, this function will overwrite that content.
     * If this BinaryObject is still being written to, this call will block until it is finished.
     *
     * @return UUID the ID which can be used to load the same BinaryObject content later.
     * @throws IOException if the BinaryObject is unable to read from the persistent storage.
     */
    UUID save() throws IOException;

    /**
     * Load content from the persistent storage location.
     * If it is being written to, any further writing to the same stream will have no effect.
     * If key is null then this BinaryObject will be returned to the state of the last save,
     * similar to a database rollback.
     *
     * @param key the UUID of the BinaryObject data to load.
     * @throws IOException if the BinaryObject is unable to read from the persistent storage.
     */
    void load(final UUID key) throws IOException;

    /**
     * Read the content from this BinaryObject.
     * Unless the entire BinaryObject is read, this stream should be closed after reading is finished.
     * The content in this stream will reflect the BinaryObject at the time when the method was called.
     *
     * @return an InputStream containing the content of this object.
     * @throws IOException if the BinaryObject is unable to read from the storage location.
     */
    InputStream getContent() throws IOException;

    /**
     * Read the content from this BinaryObject.
     * Unless the entire BinaryObject is read, this stream should be closed after reading is finished.
     * The content written to this stream will reflect the BinaryObject at the time when the method was called.
     *
     * @param writeTo an OutputStream to which will have the content written to it.
     * @throws IOException if the BinaryObject is unable to read from the storage location.
     */
    void getContent(final OutputStream writeTo) throws IOException;
}
