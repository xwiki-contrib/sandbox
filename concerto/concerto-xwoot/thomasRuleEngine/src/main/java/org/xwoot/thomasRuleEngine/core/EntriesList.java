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
package org.xwoot.thomasRuleEngine.core;

import java.io.File;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.xwootUtil.FileUtil;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * A persistent collection (database) of entries that will be synchronized.
 * <p>
 * Each entry is stored in a HashMap&lt;Identifier, Entry&gt;.
 * 
 * @version $Id$
 */
public class EntriesList
{
    /** The list of entries. */
    private Map<org.xwoot.thomasRuleEngine.core.Identifier, org.xwoot.thomasRuleEngine.core.Entry> entriesList;

    /** The location on drive where to store the database. */
    private String filePath;

    /** The name of the file where the database is saved. */
    private String filename;

    /** The location of the directory where the database is saved. */
    private String workingDir;

    /**
     * Creates a new EntriesList object.
     * 
     * @param workingDir DOCUMENT ME!
     * @param filename DOCUMENT ME!
     * @throws ThomasRuleEngineException if the workingDir is not usable or the fileName is null.
     */
    public EntriesList(String workingDir, String filename) throws ThomasRuleEngineException
    {
        try {
            FileUtil.checkDirectoryPath(workingDir);
            if (filename == null) {
                throw new NullPointerException("filename must not be null.");
            }
        } catch (Exception e) {
            throw new ThomasRuleEngineException("Problems initializing EntriesList.\n", e);
        }

        this.workingDir = workingDir;
        this.filename = filename;
        this.filePath = this.workingDir + File.separator + this.filename;
    }

    /** @return the location of the directory where the database is saved. */
    public String getWorkingDir()
    {
        return this.workingDir;
    }

    /** Removes the file where the entries list is saved. */
    public void clearWorkingDir()
    {
        File entriesListFile = new File(this.filePath);
        if (entriesListFile.exists()) {
            entriesListFile.delete();
        }
    }

    /**
     * @param entry the entry to add.
     * @throws ThomasRuleEngineException if loading/storing problems occur.
     * @throws NullPointerException if either the entry or the entry's ID is null.
     * @see #load()
     * @see #store()
     */
    public void addEntry(org.xwoot.thomasRuleEngine.core.Entry entry) throws ThomasRuleEngineException
    {
        if ((entry == null) || (entry.getId() == null)) {
            throw new NullPointerException("The entry must not be null.");
        }

        this.load();
        this.entriesList.put(entry.getId(), entry);
        this.store();
    }

    /**
     * @param id the id of the entry to get.
     * @return the entry having the specified id.
     * @throws ThomasRuleEngineException if loading problems occur.
     * @throws NullPointerException if the id is null.
     * @see #load()
     */
    public Entry getEntry(Identifier id)
        throws ThomasRuleEngineException
    {
        if (id == null) {
            throw new NullPointerException("The ID must not be null.");
        }

        this.load();

        return this.entriesList.get(id);
    }

    /**
     * @param internalId the internal ID of an Identifier stored in the database.
     * @return a list of entries that refer to the supplied id.
     * @throws ThomasRuleEngineException if problems occur while loading.
     * @throws NullPointerException if the pageId is null.
     */
    public List<Entry> getEntries(String internalId) throws ThomasRuleEngineException
    {
        if (internalId == null || internalId.length() == 0) {
            throw new NullPointerException("Parameters must not be null");
        }

        this.load();

        List<Entry> result = new ArrayList<Entry>();

        for (Identifier id : this.getAllIds()) {
            if (id.getId().equals(internalId)) {
                result.add(this.entriesList.get(id));
            }
        }

        return result;
    }
    
    /**
     * @return the set of Identifiers stored in the entries list. 
     * @throws ThomasRuleEngineException if problems loading the database occur.
     */
    public Set<Identifier> getAllIds() throws ThomasRuleEngineException
    {
        this.load();
        return this.entriesList.keySet();
    }
    
    /**
     * @return the list of entries.
     * @throws ThomasRuleEngineException if problems loading the database occur.
     */
    public List<Entry> getAllEntries() throws ThomasRuleEngineException
    {
        this.load();
        
        List<Entry> result = new ArrayList<Entry>();
        
        for (Map.Entry<Identifier, Entry> mapEntry : this.entriesList.entrySet()) {
            result.add(mapEntry.getValue());
        }
        
        return result;
    }

    /**
     * @return the number of entries currently in the list.
     * @throws ThomasRuleEngineException if problems occur while loading.
     */
    public int size() throws ThomasRuleEngineException
    {
        this.load();

        return this.entriesList.size();
    }

    /**
     * @param id the id of the entry to remove.
     * @throws ThomasRuleEngineException if problems loading/storing occur.
     * @throws NullPointerException if the id is null.
     */
    public void removeEntry(org.xwoot.thomasRuleEngine.core.Identifier id) throws ThomasRuleEngineException
    {
        if (id == null) {
            throw new NullPointerException("Parameters must not be null.");
        }

        this.load();
        this.entriesList.remove(id);
        this.store();
    }

    /**
     * Store the entries to file.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     * @see {@link FileUtil#saveMapToFile(Map, String)}
     */
    private void store() throws ThomasRuleEngineException
    {
        try {
            PersistencyUtil.saveMapToFile(this.entriesList, this.filePath);
        } catch (Exception e) {
            throw new ThomasRuleEngineException("Problems storing the entries list.\n", e);
        }
    }

    /**
     * Loads the entries from file. If the file does not exist, a new and empty collection will be loaded.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     * @see FileUtil#loadObjectFromFile(String)
     */
    @SuppressWarnings("unchecked")
    private void load() throws ThomasRuleEngineException
    {
        Object fallback =
            new Hashtable<org.xwoot.thomasRuleEngine.core.Identifier, org.xwoot.thomasRuleEngine.core.Entry>();
        try {
            this.entriesList =
                (Hashtable<org.xwoot.thomasRuleEngine.core.Identifier, 
                    org.xwoot.thomasRuleEngine.core.Entry>) PersistencyUtil
                    .loadObjectFromFile(this.filePath, fallback);
        } catch (Exception e) {
            throw new ThomasRuleEngineException("Problems loading the entris list.\n", e);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public String toString()
    {
        try {
            this.load();
        } catch (ThomasRuleEngineException e1) {
            e1.printStackTrace();
        }

        Iterator<Map.Entry<org.xwoot.thomasRuleEngine.core.Identifier, org.xwoot.thomasRuleEngine.core.Entry>> i =
            this.entriesList.entrySet().iterator();

        String result = "List : ";
        while (i.hasNext()) {
            Map.Entry temp = i.next();
            result = result + "<" + temp.toString() + ">";
        }

        return result;
    }

}
