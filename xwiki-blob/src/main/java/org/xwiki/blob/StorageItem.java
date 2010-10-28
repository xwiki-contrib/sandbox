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


/**
 * Representation of a single file or memory location.
 * Read and write methods always act on the same data and no effort is made to enforce thread safety.
 * This class should not be used outside of xwiki-blob but for each implementation of it, an implementation
 * of {@link BinaryObject} will be registered with the same role hint allowing for plug-in backends to
 * {@link BinaryObject}. To write an implementation, use the @{@link org.xwiki.component.annotation.Component}
 * annotation and specify the role for this StorageItemProvider as something unique such as "myStore".
 * Then to get BinaryObjects based on your storage implementation, use:
 * <code>
 * @Requirement("myStore")
 * private BinaryObject binaryObjectUsingMyStore;
 * </code>
 *
 * @version $Id$
 * @since 2.6M1
 * @see org.xwiki.blob.internal.FilesystemStorageItem for a simple implementation.
 */
@ComponentRole
public interface StorageItem
{
    /**
     * Initialize and the underlying resource.
     *
     * @param key the unique ID for finding the same data again.
     * @throws IOException if initialization fails.
     */
    void init(final UUID key) throws IOException;

    /**
     * @return a stream containing the content of the item.
     * @throws IOException if read operation fails.
     */
    InputStream read() throws IOException;

    /**
     * Write content into this item.
     * Each write will append content to the last, to avoid appending, use clear first.
     *
     * @return a stream to which content can be written into the item.
     * @throws IOException if write operation fails.
     */
    OutputStream write() throws IOException;

    /**
     * Get rid of all the content in the item.
     * @throws IOException if clear operation fails.
     */
    void clear() throws IOException;

    /** @return the key for getting this item again later. */
    UUID getKey();
}
