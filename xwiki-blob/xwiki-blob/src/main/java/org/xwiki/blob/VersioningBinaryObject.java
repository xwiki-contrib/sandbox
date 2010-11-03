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
 * A BinaryObject which on save produces new storage rather than overwriting the existing storage, also
 * provides a method for accessing the last version.
 *
 * @version $Id$
 * @since 2.6M1
 */
@ComponentRole
public interface VersioningBinaryObject extends BinaryObject
{
    /**
     * Save the current content into the persistent storage location.
     * If this BinaryObject has been saved before, this function will save in a new location and 
     * If this BinaryObject is still being written to, this call will block until it is finished.
     *
     * @return UUID the ID which can be used to load the same BinaryObject content later.
     * @throws IOException if the BinaryObject is unable to read from the persistent storage.
     */
    UUID save() throws IOException;
}
