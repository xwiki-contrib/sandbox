/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.lpbcast.neighbors;

import java.io.File;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.xwoot.xwootUtil.FileUtil;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Abstract implementation for the Neighbors interface. The neighbors are serialized and deserialized when needed.
 * <p>
 * Users are left to implement the {@link #notifyNeighbor(Object, Object)} and {@link #notifyNeighbors(Object)} methods.
 * 
 * @version $Id$
 */
public abstract class AbstractNeighbors implements Neighbors
{
    /** The name of the file storing the known neighbors. */
    public static final String NEIGHBORS_FILE_NAME = "neighbors";

    /** All the known neighbors. */
    private Set<Object> neighbors;

    /** The file location where to save the known neighbors. */
    private String neighborsFilePath;

    /** The maximum number of neighbors to remember. */
    private int maxNumber;

    /** The siteId of the wootEngine managing the neighbors. */
    private Integer siteId;

    /**
     * Creates a new Neighbors object.
     * 
     * @param workingDirectoryPath the location where to save the file containing the serialized known neighbors.
     * @param maxNumber the maximum number of neighbors to remember.
     * @param siteId the siteId of the woot node this object is assigned to.
     * @throws NeighborsException if the provided workingDirectory is not usable.
     * @see FileUtil#checkDirectoryPath(String)
     */
    public AbstractNeighbors(String workingDirectoryPath, int maxNumber, Integer siteId) throws NeighborsException
    {
        this.siteId = siteId;

        try {
            FileUtil.checkDirectoryPath(workingDirectoryPath);
        } catch (Exception e) {
            throw new NeighborsException(this.siteId + " - Problems initializing Neighbors\n", e);
        }

        this.neighborsFilePath = workingDirectoryPath + File.separator + NEIGHBORS_FILE_NAME;
        this.maxNumber = maxNumber;

        this.neighbors = new HashSet<Object>();
        // this.loadNeighbors();
    }

    /**
     * Deletes the file where the stored neighbors are kept.
     */
    public void clearWorkingDir()
    {
        File f = new File(this.neighborsFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * @param neighbor the new neighbor to add.
     * @return true if the neighbor was successfully added or false if it already existed or the provided value was
     *         null.
     * @throws NeighborsException if problems occur loading/unloading the neighbors or while removing a random neighbor.
     */
    public boolean addNeighbor(Object neighbor) throws NeighborsException
    {
        boolean result = false;

        if (neighbor == null) {
            return false;
        }

        this.loadNeighbors();

        if (!this.neighbors.contains(neighbor)) {
            if (this.neighbors.size() == this.maxNumber) {
                this.removeNeighborRandomly();
            } else {
                result = true;
            }

            this.neighbors.add(neighbor);
        } else {
            return false;
        }

        this.storeNeighbors();

        return result;
    }

    /**
     * @param neighbor the neighbor to remove.
     * @throws NeighborsException if problems loading or storing the neighbors occur.
     */
    public void removeNeighbor(Object neighbor) throws NeighborsException
    {
        this.loadNeighbors();
        this.neighbors.remove(neighbor);
        this.storeNeighbors();
    }

    /**
     * Randomly remove a neighbor.
     * 
     * @throws NeighborsException if problems occur while loading/storing the neighbors.
     * @see #getNeighborRandomly()
     */
    private void removeNeighborRandomly() throws NeighborsException
    {
        Object neighbor = getNeighborRandomly();
        this.neighbors.remove(neighbor);
        this.storeNeighbors();
    }

    /**
     * Removes all neighbors.
     * 
     * @throws NeighborsException if problems occur while storing.
     */
    public void clearNeighbors() throws NeighborsException
    {
        this.neighbors = new HashSet<Object>();
        this.storeNeighbors();
    }

    /**
     * @return a random neighbor from the known neighbors.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    public Object getNeighborRandomly() throws NeighborsException
    {
        this.loadNeighbors();

        if (this.neighbors.size() == 0) {
            return null;
        }

        int randomIndex = new Random().nextInt(this.neighbors.size());

        return this.neighbors.toArray()[randomIndex];
    }

    /**
     * @return true if there are any known neighbors, false otherwise.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    public boolean isConnected() throws NeighborsException
    {
        this.loadNeighbors();

        return this.neighbors.size() > 0;
    }

    /**
     * Serializes the known neighbors to file.
     * 
     * @throws NeighborsException if problems occur.
     * @see FileUtil#saveCollectionToFile(Collection, String)
     */
    private void storeNeighbors() throws NeighborsException
    {
        try {
            PersistencyUtil.saveCollectionToFile(this.neighbors, this.neighborsFilePath);
        } catch (Exception e) {
            throw new NeighborsException(this.siteId + " - Problems while storing the neighbors.\n", e);
        }
    }

    /**
     * Load the neighbors from file.
     * 
     * @throws NeighborsException if problems occur.
     * @see FileUtil#loadObjectFromFile(String)
     */
    @SuppressWarnings("unchecked")
    private void loadNeighbors() throws NeighborsException
    {
        Object fallBack = new HashSet<Object>();
        try {
            this.neighbors = (HashSet<Object>) PersistencyUtil.loadObjectFromFile(this.neighborsFilePath, fallBack);
        } catch (Exception e) {
            throw new NeighborsException(this.siteId + " - problems loading neighbors.\n", e);
        }
    }

    /**
     * @return a collection of known neighbors.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    @SuppressWarnings("unchecked")
    public Collection getNeighborsList() throws NeighborsException
    {
        this.loadNeighbors();

        return (Collection) ((HashSet) this.neighbors).clone();
    }

    /**
     * @return the number of known neighbors.
     * @throws NeighborsException if problems loading the neighbors occur.
     */
    public int getNeighborsListSize() throws NeighborsException
    {
        this.loadNeighbors();

        return this.neighbors.size();
    }

    /**
     * @return the siteId of the wootEngine managing the neighbors.
     */
    public Integer getSiteId()
    {
        return this.siteId;
    }

    /**
     * @param neighbor the neighbor to notify.
     * @param message the message with which to notify the neighbor.
     */
    public abstract void notifyNeighbor(Object neighbor, Object message);

    /**
     * @param message the message with which to notify all the known neighbors.
     */
    public abstract void notifyNeighbors(Object message);
}
