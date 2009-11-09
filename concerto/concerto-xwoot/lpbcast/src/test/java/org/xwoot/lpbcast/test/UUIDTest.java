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

package org.xwoot.lpbcast.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * Test the Java UUID class to be used for message IDs.
 * <p>
 * Although 2<sup>128</sup> is a more than enough domain of values, these tests are provided to prove the usability of
 * this class.
 * 
 * @version $Id$
 * @see UUID
 */
public class UUIDTest
{
    /** Number of runs to test unicity. */
    private static final int UNICITY_NUMBER_OF_RUNS = 10000;

    /** Timestamp for performance test. */
    private long startTimestamp;

    /** Timestamp for performance test. */
    private long endTimestamp;

    /** Used to log the performance results. */
    private Log log = LogFactory.getLog(this.getClass());

    /**
     * Test the time needed to create a random UUID, 3 times in a row.
     */
    @Test
    public void testPerformanceJavaUuidRandom()
    {
        this.startTimestamp = System.currentTimeMillis();
        UUID.randomUUID();
        this.endTimestamp = System.currentTimeMillis();
        this.log.info("First run: " + (this.endTimestamp - this.startTimestamp) + "ms passed. ");

        this.startTimestamp = System.currentTimeMillis();
        UUID.randomUUID();
        this.endTimestamp = System.currentTimeMillis();
        this.log.info("Second run: " + (this.endTimestamp - this.startTimestamp) + "ms passed.  ");

        this.startTimestamp = System.currentTimeMillis();
        UUID.randomUUID();
        this.endTimestamp = System.currentTimeMillis();
        this.log.info("Third run: " + (this.endTimestamp - this.startTimestamp) + "ms passed.");
    }

    /**
     * Create a list of {@link #UNICITY_NUMBER_OF_RUNS} random IDs. Generate another {@link #UNICITY_NUMBER_OF_RUNS}
     * random IDs and check for each of them if they are found in the firs list.
     * <p>
     * Result: None of the tested IDs will be found in the list.
     */
    @Test
    public void testUnicity()
    {
        List<String> list = new ArrayList<String>();
        this.startTimestamp = System.currentTimeMillis();
        for (int i = 0; i < UNICITY_NUMBER_OF_RUNS; i++) {
            list.add(UUID.randomUUID().toString());
        }
        this.endTimestamp = System.currentTimeMillis();
        this.log.info("Generated and added in: " + (this.endTimestamp - this.startTimestamp) + "ms. ");

        this.startTimestamp = System.currentTimeMillis();
        for (int i = 0; i < UNICITY_NUMBER_OF_RUNS; i++) {
            String random = UUID.randomUUID().toString();

            Assert.assertFalse(list.contains(random));
        }
        this.endTimestamp = System.currentTimeMillis();
        this.log.info("Tested in: " + (this.endTimestamp - this.startTimestamp) + "ms.");
    }
}
