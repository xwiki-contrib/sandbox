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
 * BinaryObjectProvider allows you to get as many {@link BinaryObject}s as you want based on whatever
 * {@link StorageItem} implementations you choose.
 *
 * @version $Id$
 * @since 2.6M1
 */
@ComponentRole
public interface BinaryObjectProvider
{
    /**
     * @return a BinaryObject based on the default {@link StorageItem}.
     */
    BinaryObject get();

    /**
     * Get a {@link BinaryObject} based on a custom underlying {@link StorageItem}.
     *
     * @param storageItemRoleHint the string which identifies your custom implementation of {@link StorageItem},
     *                            see http://code.xwiki.org/xwiki/bin/view/Modules/ComponentModule for more
     *                            information about role hints.
     * @return a {@link BinaryObject} which wraps an instance of the specified {@link StorageItem}.
     */
    BinaryObject get(final String storageItemRoleHint);
}
