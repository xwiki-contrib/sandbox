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

import org.xwiki.component.annotation.ComponentRole;


/**
 * Implement this class if your StorageItem can be read and written fast enough to be used as a cache. A file on
 * the filesystem is an example of a good FastStorageItem while a compressed archive should be a plain StorageItem.
 *
 * @version $Id$
 * @since 2.6M1
 */
@ComponentRole
public interface FastStorageItem extends StorageItem
{
    // Nothing here, the interface is the same as StorageItem.
}
