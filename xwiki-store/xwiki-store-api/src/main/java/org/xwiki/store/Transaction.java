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
 * A Transaction represents an atomic unit of work on the storage engine.
 * Implementations may use whatever type of storage they like as long as it supports
 * beginning a transaction, and committing or rolling back that transaction.
 *
 * @version $Id$
 * @since TODO
 */
public interface Transaction
{
    /**
     * Start the transaction.
     * Prepare the storage engine to handle a transaction.
     *
     * @throws Exception if something goes wrong with the storage engine.
     */
    void begin() throws Exception;

    /**
     * Commit the transaction.
     * Save the work which was done during this transaction.
     *
     * @throws Exception if something goes wrong with the storage.
     */
    void commit() throws Exception;

    /**
     * Rollback the transaction.
     * Return the storage engine to the state it was before the transaction began.
     *
     * @throws Exception if something goes wrong with the storage.
     */
    void rollback() throws Exception;
}
