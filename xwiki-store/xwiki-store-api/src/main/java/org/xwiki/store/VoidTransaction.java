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
package org.xwiki.store;

/**
 * A VoidTransaction is a Transaction which does nothing.
 * To be used in cases when there is no need to open or close transactions with any database.
 *
 * @version $Id$
 * @since TODO
 */
public class VoidTransaction implements Transaction
{
    /** A single instance since VoidTransaction has no state. */
    public static final VoidTransaction INSTANCE = new VoidTransaction();

    /**
     * {@inheritDoc}
     *
     * @see Transaction#begin()
     */
    public void begin() throws Exception
    {
        // No op, which is why it's void.
    }

    /**
     * {@inheritDoc}
     *
     * @see Transaction#commit()
     */
    public void commit() throws Exception
    {
        // No op, which is why it's void.
    }

    /**
     * {@inheritDoc}
     *
     * @see Transaction#rollback()
     */
    public void rollback() throws Exception
    {
        // No op, which is why it's void.
    }
}
