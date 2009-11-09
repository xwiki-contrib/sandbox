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
package org.xwoot.thomasRuleEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.thomasRuleEngine.core.EntriesList;
import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpDel;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpNew;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpSet;
import org.xwoot.xwootUtil.FileUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Implements the maintenance of duplicate databases on a "last writer wins" basis as described in RFC677.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc677">RFC677 - The Maintenance of Duplicate Databases</a>
 * @version $Id$
 */
public class ThomasRuleEngine
{
    /** FileName prefix to be used when saving the entriesList. */
    public static final String TRE_FILE_NAME = "EntriesListFile";

    /** Used when computing the state of the TRE. */
    public static final String TRE_STATE_FILE_NAME = "treState.zip";
    
    /** The name prefix of the output file containing the zipped state. */
    public static final String STATE_FILE_NAME_PREFIX = "treState";
    
    /** The file extension of the output file containing the zipped state. */
    public static final String STATE_FILE_EXTENSION = ".zip";
    
    /** Method name to get pageID from an XWootObject. */
    public static final String GET_PAGE_ID_METHOD_NAME = "getPageId";

    /** @see #getThomasRuleEngineId() */
    private String thomasRuleEngineId;

    /** The associated EntriesList for managing entries. */
    private EntriesList entriesList;

    /** Used for logging. */
    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * Creates a new ThomasRuleEngine object.
     * 
     * @param thomasRuleEngineId the siteId of the owning node.
     * @param workingDir working directory where to store the entries.
     * @throws ThomasRuleEngineException if the workingDir is not usable.
     */
    public ThomasRuleEngine(String thomasRuleEngineId, String workingDir) throws ThomasRuleEngineException
    {
        this.thomasRuleEngineId = thomasRuleEngineId;

        try {
            FileUtil.checkDirectoryPath(workingDir);
        } catch (Exception e) {
            throw new ThomasRuleEngineException("Problems initializing TRE.");
        }

        this.entriesList = new EntriesList(workingDir, TRE_FILE_NAME);

        this.logger.info(this.thomasRuleEngineId + " - Thomas rule engine created !");
    }

    /**
     * Creates a new ThomasRuleOp, corresponding to the provided values.
     * 
     * @param id the id of the entry.
     * @param value the value of the entry.
     * @return A ThomasRuleOpNew having the provided id and value if the value is not null and the entry does not
     *         already exist or is marked as deleted.
     *         <p>
     *         A ThomasRuleOpSet having the provided id and value if the entry already exists, the value is not null and
     *         is different from the existing entry's value and the entry is not marked as deleted.
     *         <p>
     *         A ThomasRuleOpDel having the provided id, the deletion status set to true and the value of the existing
     *         entry and the value is null or the entry is marked as deleted.
     *         <p>
     *         null otherwise.
     *         <p>
     *         Note: All operations have the modifiedTimestamp set to the time when getOp is called and, in the case of
     *         a New operation, the creationTimestamp set tot the same value.
     * @see <a href="http://tools.ietf.org/html/rfc677">RFC677 - The Maintenance of Duplicate Databases</a>
     * @throws ThomasRuleEngineException if problems occur while getting the entry with the specified id.
     * @throws NullPointerException if the id is null.
     */
    public synchronized ThomasRuleOp getOp(Identifier id, Value value) throws ThomasRuleEngineException
    {
        Entry entry = this.entriesList.getEntry(id);

        // new
        if ((value != null) && ((entry == null) || entry.isDeleted())) {
            return new ThomasRuleOpNew(id, value, false, this.getTimestamp(), this.getTimestamp());
        } else if (entry != null) {
            // set
            if ((value != null) && !entry.isDeleted()) {

                return new ThomasRuleOpSet(id, value, false, entry.getTimestampIdCreation(), this.getTimestamp());

            }

            // del
            return new ThomasRuleOpDel(id, entry.getValue(), true, entry.getTimestampIdCreation(), this.getTimestamp());
        }

        // nothing
        return null;
    }

    /**
     * Applies an operation and adds the resulting entry in the entriesList.
     * 
     * @param operation the operation to apply.
     * @return the resulting entry or null if the operation is null.
     * @throws ThomasRuleEngineException if loading/storing entry problems occur.
     */
    public synchronized Entry applyOp(ThomasRuleOp operation) throws ThomasRuleEngineException
    {
        if (operation == null) {
            return null;
        }

        // execute op
        Entry result = operation.execute(this.entriesList.getEntry(operation.getId()));

        // add the result of op execution
        if (result != null) {
            this.entriesList.addEntry(result);
        }

        return result;
    }

    /**
     * @param id the id of the entry.
     * @return the value of the entry at the specified id if that entry is not marked as deleted; null otherwise or if
     *         the entry does not exist.
     * @throws ThomasRuleEngineException if problems getting the entry occur.
     * @throws NullPointerException if the id is null.
     */
    public synchronized Value getValue(Identifier id) throws ThomasRuleEngineException
    {
        Entry entry = this.entriesList.getEntry(id);

        if (entry != null) {
            if (!entry.isDeleted()) {
                return entry.getValue();
            }
        }

        return null;
    }
    
    /**
     * @param pageId the page id.
     * @return a list of all the Identifiers of the objects stored for a given page.
     * @throws ThomasRuleEngineException if problems occur while loading the database.
     */
    public List<Identifier> getIds(String pageId) throws ThomasRuleEngineException
    {
        if (pageId == null || pageId.length() == 0) {
            throw new NullPointerException("Parameters must not be null");
        }

        List<Identifier> result = new ArrayList<Identifier>();

        for (Identifier id : this.entriesList.getAllIds()) {
            String objectPageId = null;
            try {
                Object xwootObject = this.entriesList.getEntry(id).getValue().get();
                Method getPageIdMethod = xwootObject.getClass().getMethod(GET_PAGE_ID_METHOD_NAME, new Class[0]);
                objectPageId = (String) getPageIdMethod.invoke(xwootObject, new Object[0]);
            } catch (Exception e) {
                // bad content.
            }
            
            if (id.getId().equals("page:" + pageId) || pageId.equals(objectPageId)) {
                result.add(id);
            }
        }

        return result;
    }
    
    /**
     * @param pageId the id of the page.
     * @return a list of entries that correspond to the a page. 
     * @throws ThomasRuleEngineException if problems occur while loading the database.
     */
    public List<Value> getValues(String pageId) throws ThomasRuleEngineException
    {
        if (pageId == null || pageId.length() == 0) {
            throw new NullPointerException("Page ID must not be null");
        }

        List<Value> result = new ArrayList<Value>();

        for (Identifier id : this.entriesList.getAllIds()) {
            String objectPageId = null;
            Entry entry = null;
            try {
                entry = this.entriesList.getEntry(id);
                Object xwootObject = entry.getValue().get();
                Method getPageIdMethod = xwootObject.getClass().getMethod(GET_PAGE_ID_METHOD_NAME, new Class[0]);
                objectPageId = (String) getPageIdMethod.invoke(xwootObject, new Object[0]);
            } catch (Exception e) {
                // bad content.
            }
            
            if (pageId.equals(objectPageId)) {
                result.add(entry.getValue());
            }
        }

        return result;
    }
    
    /**
     * @return IDs of all objects in the model.
     * @throws ThomasRuleEngineException if problems occur while loading the database.
     */
    public Set<Identifier> getAllIds() throws ThomasRuleEngineException
    {
        return this.entriesList.getAllIds();
    }
    
    /**
     * @return the list of all entries stored in the database.
     * @throws ThomasRuleEngineException if problems loading the database occur.
     */
    public List<Entry> getAllEntries() throws ThomasRuleEngineException 
    {
        return this.entriesList.getAllEntries();
    }

    /**
     * @return the working directory where to store the entries.
     */
    public String getWorkingDir()
    {
        return this.entriesList.getWorkingDir();
    }

    /** Clear the working directory. */
    public void clearWorkingDir()
    {
        this.entriesList.clearWorkingDir();
    }

    /**
     * @return the current time.
     */
    public Timestamp getTimestamp()
    {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();

        return new Timestamp(time, this.getThomasRuleEngineId());
    }

    /**
     * @return the number of entries in the entriesList.
     * @throws ThomasRuleEngineException if problems occur while loading the list.
     */
    public int size() throws ThomasRuleEngineException
    {
        return this.entriesList.size();
    }

    /**
     * @return the siteId of the owning node.
     */
    public String getThomasRuleEngineId()
    {
        return this.thomasRuleEngineId;
    }

    /**
     * @param thomasRuleEngineId the thomasRuleEngineId to set.
     * @see #getThomasRuleEngineId()
     */
    public void setThomasRuleEngineId(String thomasRuleEngineId)
    {
        this.thomasRuleEngineId = thomasRuleEngineId;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String toString()
    {
        return "Id - " + this.thomasRuleEngineId + " : " + this.entriesList.toString();
    }

}
