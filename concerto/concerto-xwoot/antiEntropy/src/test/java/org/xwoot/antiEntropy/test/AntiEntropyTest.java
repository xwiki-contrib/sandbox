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
package org.xwoot.antiEntropy.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.antiEntropy.AntiEntropyException;

/**
 * Test unit for the AntiEntropy class.
 * 
 * @version $Id$Revision$
 */
public class AntiEntropyTest
{
    /** Test working directory. */
    private static final String WORKING_DIR = "/tmp/xwootTests/antiEntropy";

    /** Test working path inside the working directory. */
    private String workingPath1;

    /** Test working path inside the working directory. */
    private String workingPath2;

    /** Test AntiEntropy object. */
    private AntiEntropy ae1;

    /** Test AntiEntropy object. */
    private AntiEntropy ae2;

    /** Initialize working directory if it does not exist. */
    @BeforeClass
    public static void initFile()
    {
        if (!new File(WORKING_DIR).exists()) {
            new File(WORKING_DIR).mkdirs();
        }
    }

    /**
     * Init working paths and test AntiEntropy objects.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void setUp() throws Exception
    {
        this.workingPath1 = WORKING_DIR + File.separator + "site1";

        if (!new File(this.workingPath1).exists()) {
            new File(this.workingPath1).mkdirs();
        }

        this.workingPath2 = WORKING_DIR + File.separator + "site2";

        if (!new File(this.workingPath2).exists()) {
            new File(this.workingPath2).mkdirs();
        }

        this.ae1 = new AntiEntropy(this.workingPath1);
        this.ae2 = new AntiEntropy(this.workingPath2);

    }

    /**
     * Clear the Logs.
     * 
     * @throws Exception if problems occur.
     */
    @After
    public void tearDown() throws Exception
    {
        this.ae1.getLog().clearLog();
        this.ae2.getLog().clearLog();
    }

    /**
     * Test if working dir exists.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testInit() throws Exception
    {
        assertTrue(new File(WORKING_DIR).exists());
    }

    /**
     * Test the antiEntropy answer mechanism and see if the results are the expected ones.
     * 
     * @throws AntiEntropyException if problems occur.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAntiEntropy() throws AntiEntropyException
    {
        assertEquals(this.ae1.getLog().logSize(), 0);
        assertEquals(this.ae2.getLog().logSize(), 0);

        String message1 = "toto";
        String message2 = "titi";
        String message3 = "tata";

        String messageId1 = "1";
        String messageId2 = "2";
        String messageId3 = "3";

        // receive
        this.ae1.logMessage(messageId1, message1);
        this.ae1.logMessage(messageId2, message2);
        this.ae1.logMessage(messageId3, message3);
        assertEquals(this.ae1.getLog().logSize(), 3);

        // anti entropy
        Object[] site2ids = this.ae2.getMessageIdsForAskAntiEntropy();
        Collection diff = this.ae1.answerAntiEntropy(site2ids);
        assertEquals(diff.size(), 3);

        assertTrue(diff.contains(message1) && diff.contains(message2) && diff.contains(message3));

        this.ae2.logMessage(messageId1, message1);
        site2ids = this.ae2.getMessageIdsForAskAntiEntropy();
        diff = this.ae1.answerAntiEntropy(site2ids);
        assertEquals(diff.size(), 2);

        assertTrue(diff.contains(message2) && diff.contains(message3));

        this.ae2.logMessage(messageId2, message2);
        site2ids = this.ae2.getMessageIdsForAskAntiEntropy();
        diff = this.ae1.answerAntiEntropy(site2ids);
        assertTrue(diff.size() == 1);

        assertTrue(diff.contains(message3));

        this.ae2.logMessage(messageId3, message3);
        site2ids = this.ae2.getMessageIdsForAskAntiEntropy();
        diff = this.ae1.answerAntiEntropy(site2ids);
        assertTrue((diff.size() == 0) && (this.ae2.getLog().logSize() == 3));
    }
}
