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
package org.xwoot.clockEngine;

import java.io.File;
import java.io.Serializable;

import org.xwoot.xwootUtil.FileUtil;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Provides the clock service. It is used to associate a private clock to an object. The clock is stored in a file.
 * 
 * @version $Id$
 */
public class Clock implements Serializable
{
    /** The name of the file where to store the clock. */
    public static final String CLOCK_FILE_NAME = "clock";

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -9038141018304727598L;

    /** The file path to store the clock. */
    private String clockFilePath;

    /** The value of the clock to increment. */
    private int clock;

    /**
     * Creates a new persistent clock stored in a file in the provided directory.
     * 
     * @param directoryPath where to store the clock.
     * @throws ClockException if path access problems occur.
     */
    public Clock(String directoryPath) throws ClockException
    {
        try {
            FileUtil.checkDirectoryPath(directoryPath);
        } catch (Exception e) {
            throw new ClockException("Problems while creating a Clock instance: ", e);
        }

        this.clockFilePath = directoryPath + File.separator + CLOCK_FILE_NAME;

        this.reset();
    }

    /**
     * Users <b>must</b> load clock before using this function.
     * 
     * @return the current clock value.
     * @throws ClockException if problems occur.
     */
    public int getValue() throws ClockException
    {
        return this.clock;
    }

    /**
     * @param value the clock's new value.
     * @throws ClockException if problems occur.
     */
    public void setValue(int value) throws ClockException
    {
        this.clock = value;
    }

    /**
     * Increments the clock's value.
     * 
     * @param units the number of units by which to increment the clock's value.
     * @return the clock's value right before ticking.
     * @throws ClockException if problems occur.
     */
    public int tick(int units) throws ClockException
    {
        int oldValue = this.getValue();

        this.setValue(oldValue + units);

        return oldValue;
    }

    /**
     * Increments the clock's value by exactly 1 unit.
     * <p>
     * Equivalent to tick(1).
     * 
     * @return the clock's value right before ticking.
     * @throws ClockException if problems occur.
     * @see #tick(int)
     */
    public int tick() throws ClockException
    {
        return this.tick(1);
    }

    /**
     * Resets the clock's value to 0.
     * 
     * @throws ClockException if problems occur.
     */
    public void reset() throws ClockException
    {
        this.clock = 0;
    }

    /**
     * Removes the clock file from the system.
     */
    public void clearWorkingDir()
    {
        File file = new File(this.clockFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Serializes the clock to file.
     * 
     * @throws ClockException if file access problems occur.
     */
    public synchronized void store() throws ClockException
    {
        try {
            PersistencyUtil.saveObjectToFile(Integer.valueOf(this.clock), this.clockFilePath);
        } catch (Exception e) {
            throw new ClockException("Problems while storing the Clock: ", e);
        }
    }

    /**
     * Deserializes the clock from the file it is stored in.
     * 
     * @return the current clock value
     * @throws ClockException if file access problems occur.
     */
    public synchronized Clock load() throws ClockException
    {
        File clockFile = new File(this.clockFilePath);

        if (!clockFile.exists()) {
            this.reset();
            this.store();
        } else {

            try {
                this.clock = ((Integer) PersistencyUtil.loadObjectFromFile(this.clockFilePath)).intValue();
            } catch (Exception e) {
                throw new ClockException("Problems loading the Clock: ", e);
            }
        }
        return this;
    }

}
