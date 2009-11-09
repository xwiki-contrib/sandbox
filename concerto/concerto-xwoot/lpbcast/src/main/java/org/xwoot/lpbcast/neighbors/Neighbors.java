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

package org.xwoot.lpbcast.neighbors;

import java.util.Collection;

/**
 * Handles the neighbors in the P2P Network.
 * 
 * @version $Id$
 */
public interface Neighbors
{    
    /**
     * Deletes the file where the stored neighbors are kept.
     */
    void clearWorkingDir();

    /**
     * @param neighbor the new neighbor to add.
     * @return true if the neighbor was successfully added or false if it already existed or the provided value was
     *         null.
     * @throws NeighborsException if problems occur loading/unloading the neighbors or while removing a random neighbor.
     */
    boolean addNeighbor(Object neighbor) throws NeighborsException;

    /**
     * @param neighbor the neighbor to remove.
     * @throws NeighborsException if problems loading or storing the neighbors occur.
     */
    void removeNeighbor(Object neighbor) throws NeighborsException;

    /**
     * Removes all neighbors.
     * 
     * @throws NeighborsException if problems occur while storing.
     */
    void clearNeighbors() throws NeighborsException;

    /**
     * @return a random neighbor from the known neighbors.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    Object getNeighborRandomly() throws NeighborsException;

    /**
     * @return true if there are any known neighbors, false otherwise.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    boolean isConnected() throws NeighborsException;

    /**
     * @return a collection of known neighbors.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    @SuppressWarnings("unchecked")
    Collection getNeighborsList() throws NeighborsException;

    /**
     * @return the number of known neighbors.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    int getNeighborsListSize() throws NeighborsException;

    /**
     * @return the siteId of the wootEngine managing the neighbors.
     */
    Integer getSiteId();

    /**
     * @param neighbor the neighbor to notify.
     * @param message the message with which to notify the neighbor.
     */
    void notifyNeighbor(Object neighbor, Object message);

    /**
     * @param message the message with which to notify all the known neighbors.
     */
    void notifyNeighbors(Object message);
}
