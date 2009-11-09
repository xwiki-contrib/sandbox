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
package org.xwoot.wootEngine;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootUtil.FileUtil;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Implements a pooling mechanism or a waiting queue for {@link WootOp} elements. The WootOp elements are serialized to
 * file.
 * 
 * @version $Id$
 */
public class Pool implements Serializable
{
    /** The name of the file where to serialize the pool's data. */
    public static final String POOL_FILE_NAME = "pool";

    /** Unique ID for the serialization process. */
    private static final long serialVersionUID = -7473219928332142278L;

    /** The file where to store the Pool's contents. */
    private File poolFile;

    /** List of {@link WootOp} elements currently in the Pool. */
    private List<WootOp> content;

    /**
     * Creates a new instance of Pool serializing it's contents in the specified directory.
     * <p>
     * If the pool file and/or directory does not exist, it will be created.
     * 
     * @param directoryPath The location where to store the Pool file.
     * @throws WootEngineException if the directory path is not usable.
     * @see FileUtil#checkDirectoryPath(String)
     */
    public Pool(String directoryPath) throws WootEngineException
    {
        try {
            FileUtil.checkDirectoryPath(directoryPath);
        } catch (Exception e) {
            throw new WootEngineException("Problems while creating the Pool: ", e);
        }

        this.poolFile = new File(directoryPath + File.separator + POOL_FILE_NAME);
        this.initializePool(false);
    }

    /**
     * This method clears the contents and suggests the start of the garbage collection system.
     * <p>
     * It is used when unloading the pool to free up the resources as soon as possible.
     * 
     * @see System#runFinalization()
     * @see System#gc()
     */
    public void free()
    {
        this.setContent(new ArrayList<WootOp>());
        System.runFinalization();
        System.gc();
    }

    /**
     * @param position The position in the Pool to get.
     * @return the WootOp at the specified position in the Pool or null if it is not found.
     */
    public WootOp get(int position)
    {
        if (this.content != null) {
            return this.content.get(position);
        }

        return null;
    }

    /**
     * @param position The position from which to remove a {@link WootOp} element from the pool.
     * @return The removed WootOp element.
     */
    public WootOp remove(int position)
    {
        if (this.content != null) {
            return this.content.remove(position);
        }

        return null;
    }

    /**
     * @return A List of {@link WootOp} instances representing the content of the Pool.
     */
    public List<WootOp> getContent()
    {
        return this.content;
    }

    /**
     * @param content A List of {@link WootOp} instances representing the Pool's content.
     */
    public void setContent(List<WootOp> content)
    {
        this.content = content;
    }

    /**
     * @return The File object referring the location on drive where the Pool's data is being serialized.
     */
    public File getPoolFile()
    {
        return this.poolFile;
    }

    /**
     * @param poolFile The File object referring the location on drive where the Pool's data will be serialized.
     */
    public void setPoolFile(File poolFile)
    {
        this.poolFile = poolFile;
    }

    /**
     * Initialize the Pool by creating the file that will hold the serialization of the content.
     * 
     * @param override Indicate if the method must override any existing log.
     * @throws WootEngineException if IO problems occur.
     * @see #getContent()
     */
    public void initializePool(boolean override) throws WootEngineException
    {
        if (this.getPoolFile().exists() && override) {
            this.getPoolFile().delete();
        }

        if (!this.getPoolFile().exists()) {
            this.content = new ArrayList<WootOp>();
            this.storePool();
        }
    }

    /**
     * Serialize the pool's content to file.
     * 
     * @throws WootEngineException if problems occur.
     * @see FileUtil#saveObjectToFile(Object, String)
     */
    public final synchronized void storePool() throws WootEngineException
    {
        try {
            PersistencyUtil.saveCollectionToFile(this.getContent(), this.getPoolFile().toString());
        } catch (Exception e) {
            throw new WootEngineException("Problems while storing the pool: ", e);
        }
    }

    /**
     * Deserializes the pool's content from file.
     * 
     * @throws WootEngineException if problems occur.
     * @see FileUtil#loadObjectFromFile(String)
     */
    @SuppressWarnings("unchecked")
    public synchronized void loadPool() throws WootEngineException
    {
        Object fallback = new ArrayList<WootOp>();
        try {
            this.content =
                (ArrayList<WootOp>) PersistencyUtil.loadObjectFromFile(this.getPoolFile().toString(), fallback);
        } catch (Exception e) {
            throw new WootEngineException("Problems loading the pool: ", e);
        }
    }

    /**
     * Serializes the pool's contents to file and unloads it from memory.
     * 
     * @throws WootEngineException if problems occur.
     * @see #storePool()
     * @see #free()
     */
    public final synchronized void unLoadPool() throws WootEngineException
    {
        this.storePool();
        this.free();
    }
}
