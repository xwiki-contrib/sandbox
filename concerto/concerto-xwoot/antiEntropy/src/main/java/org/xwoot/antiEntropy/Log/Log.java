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
package org.xwoot.antiEntropy.Log;

import java.io.File;
import java.io.Serializable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.xwoot.xwootUtil.FileUtil;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * This class embeds a {@link Hashtable} used to store messages having ids as keys. The embedded hashtable is serialized
 * in a given file.
 * <p>
 * Log is used by {@link org.xwoot.antiEntropy.AntiEntropy AntiEntropy} to store generated and received messages.
 * 
 * @version $Id$
 * @see org.xwoot.antiEntropy.AntiEntropy
 */
public class Log implements Serializable
{
    /** The name of the file where the hashtable will be serialized. */
    public static final String LOG_FILE_NAME = "log";

    /** Unique ID used in the serialization process. */
    private static final long serialVersionUID = -3836149914436685731L;

    /** The serialized hashtable storing key-value having the id as key and the message as value. */
    private Map<Object, Object> log;

    /** The path on drive where to serialize the log. */
    private String logFilePath;

    /** The directory where to store the anti-entropy log. */
    private String logDir;

    /**
     * Creates a new Log object.
     * 
     * @param logFilePath the file path used to serialize the log. If it does not exist, it will be created.
     * @throws LogException if the specified path is not a writable directory.
     */
    public Log(String logFilePath) throws LogException
    {
        try {
            FileUtil.checkDirectoryPath(logFilePath);
        } catch (Exception e) {
            throw new LogException("Problems with the specified log file path: ", e);
        }

        this.logDir = logFilePath;
        this.logFilePath = logDir + File.separator + LOG_FILE_NAME;
    }

    /** @return the directory where to store the anti-entropy log. */
    public String getWorkingDirectory()
    {
        return this.logDir;
    }

    /** Deletes the log file from the working directory. */
    public void clearWorkingDir()
    {
        File file = new File(this.logFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * @param id the key associate with the wanted log entry.
     * @return the wanted log entry or null if key's not present in log.
     * @throws LogException if deserialization problems occur.
     */
    public Object getMessage(Object id) throws LogException
    {
        this.loadLog();

        return this.log.get(id);
    }

    /**
     * @param id key for new log entry.
     * @param message the value of the new log entry.
     * @throws LogException if log serialization/deserialization problems occur.
     */
    public synchronized void addMessage(Object id, Object message) throws LogException
    {
        this.loadLog();
        this.log.put(id, message);
        this.storeLog();
    }

    /**
     * Removes all entries in the log.
     * 
     * @throws LogException if serialization problems occur.
     */
    public void clearLog() throws LogException
    {
        this.log = new Hashtable<Object, Object>();
        this.storeLog();
    }

    /**
     * @param messageId the searched message's id
     * @return true if the message exists in the log, false otherwise.
     * @throws LogException if deserialization problems occur.
     */
    public boolean existInLog(Object messageId) throws LogException
    {
        this.loadLog();

        return this.log.containsKey(messageId);
    }

    /**
     * @return a Map of all the entries in the log.
     * @throws LogException if deserialization problems occur.
     */
    public Map<Object, Object> getAllEntries() throws LogException
    {
        this.loadLog();

        return this.log;
    }

    /**
     * @return a table with all message ids.
     * @throws LogException if deserialization problems occur.
     */
    public Object[] getMessageIds() throws LogException
    {
        this.loadLog();

        return this.log.keySet().toArray();
    }

    /**
     * Computes the diff between the log's keys and a given table of keys.
     * 
     * @param site2ids an array of message IDs.
     * @return an array of all the message IDs in the local log, excluding the ones in the given array.
     * @throws LogException if deserialization problems occur.
     */
    public Object[] getDiffKey(Object[] site2ids) throws LogException
    {
        this.loadLog();

        return this.diffANotInB(this.getMessageIds(), site2ids);
    }

    /**
     * @param a the first array
     * @param b the second array
     * @return the elements from the first array that are not contained in the second. A null array is considered an
     *         empty array.
     */
    public Object[] diffANotInB(Object[] a, Object[] b)
    {
        if (a == null || a.length == 0) {
            return new Object[0];
        }

        Set<Object> diff = new HashSet<Object>();
        Collections.addAll(diff, a);

        if (b != null) {
            for (Object o : b) {
                diff.remove(o);
            }
        }

        return diff.toArray();
    }

    /**
     * Method for persistent storage. Store the log on file.
     * 
     * @throws LogException if problems occur while accessing or writing the log to file.
     */
    private void storeLog() throws LogException
    {
        try {
            PersistencyUtil.saveObjectToFile(this.log, this.logFilePath);
        } catch (Exception e) {
            throw new LogException("Problems while storing the Log: ", e);
        }
    }

    /**
     * Method for persistent storage. Loads the log from file.
     * 
     * @throws LogException if problems occur while accessing or reading the log from file.
     */
    @SuppressWarnings("unchecked")
    private void loadLog() throws LogException
    {
        File logFile = new File(this.logFilePath);

        if (!logFile.exists()) {
            this.log = new Hashtable<Object, Object>();
            this.storeLog();
            return;
        }

        try {
            this.log = (Map<Object, Object>) PersistencyUtil.loadObjectFromFile(this.logFilePath);
        } catch (Exception e) {
            throw new LogException("Problems loading the Log: ", e);
        }

    }

    /**
     * @return the number of entries in log
     * @throws LogException if deserialization problems occur.
     */
    public int logSize() throws LogException
    {
        this.loadLog();

        return this.log.size();
    }

}
