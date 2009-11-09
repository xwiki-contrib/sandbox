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

package org.xwoot.lpbcast.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.neighbors.httpservletneighbors.HttpServletNeighbors;
import org.xwoot.xwootUtil.FileUtil;

import java.util.Random;

import junit.framework.Assert;

/**
 * Tests for the neighbors manager.
 * 
 * @version $Id$
 */
public class NeighborsTest
{
    /** Working directory for testing this module. */
    protected static final String WORKINGDIR = FileUtil.getTestsWorkingDirectoryPathForModule("lpbcast");

    /** The maximum number of neighbors used for the test object. */
    protected static final int MAXIMUM_NUMBER_OF_NEIGHBORS = 10;

    /** The neighbors object to test. */
    private Neighbors neighbors;

    /** A test neighbor. */
    private String testNeighbor = "testNeighbor";

    /**
     * Initializes the working directory.
     * 
     * @throws Exception if the directory is not usable.
     * @see FileUtil#checkDirectoryPath(String)
     */
    @BeforeClass
    public static void initFile() throws Exception
    {
        FileUtil.checkDirectoryPath(WORKINGDIR);
    }

    /**
     * Creates a new test object.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void setUp() throws Exception
    {
        this.neighbors = new HttpServletNeighbors(WORKINGDIR, MAXIMUM_NUMBER_OF_NEIGHBORS, new Integer(0));
    }

    /**
     * Clears the test object's contents.
     * 
     * @throws Exception if problems occur.
     */
    @After
    public void tearDown() throws Exception
    {
        this.neighbors.clearNeighbors();
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 0);
    }

    /**
     * Test if the working
     * 
     * @throws Exception DOCUMENT ME!
     */
    /*
     * @Test public void testInitFile() throws Exception { Assert.assertTrue(new File(WORKINGDIR).exists()); }
     */

    /**
     * Check the status of the neighbors manager right after initialization.
     * <p>
     * Result: the list of neighbors should be empty.
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 0);
    }

    /**
     * Test adding a neighbor.
     * <p>
     * Result: The list of neighbors should contain only the added neighbor.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testAdd() throws Exception
    {
        this.neighbors.addNeighbor(this.testNeighbor);

        Assert.assertTrue(this.neighbors.getNeighborsList().contains(this.testNeighbor));
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 1);
    }

    /**
     * When no neighbors are known, then the neighbor manages is not connected.
     * <p>
     * After adding at least one neighbor, it becomes connected.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testIsConnected() throws Exception
    {
        Assert.assertFalse(this.neighbors.isConnected());

        this.neighbors.addNeighbor(this.testNeighbor);
        Assert.assertTrue(this.neighbors.isConnected());
    }

    /**
     * Add 10 neighbors, clear the list then add other 5 neighbors.
     * <p>
     * Result: the list of neighbors will contain only the 5 neighbors added at the end.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testList() throws Exception
    {
        for (int i = 0; i < 10; i++) {
            this.neighbors.addNeighbor(this.testNeighbor + i);
        }

        Assert.assertEquals(this.neighbors.getNeighborsList().size(), 10);

        String randomTestNeighbor = this.testNeighbor + new Random().nextInt(10);

        Assert.assertTrue(this.neighbors.getNeighborsList().contains(randomTestNeighbor));

        this.neighbors.clearNeighbors();

        for (int i = 10; i < 15; i++) {
            this.neighbors.addNeighbor(this.testNeighbor + i);
        }

        Assert.assertEquals(this.neighbors.getNeighborsList().size(), 5);

        randomTestNeighbor = this.testNeighbor + (10 + new Random().nextInt(5));

        Assert.assertTrue(this.neighbors.getNeighborsList().contains(randomTestNeighbor));
    }

    /**
     * Add more neighbors than the test object is set to allow.
     * <p>
     * Result: Each time a neighbor add request is received and the list of neighbors is full, a random neighbor from
     * the list will be removed so that the new neighbor can be added. In the end, the list will contain exactly or less
     * than {@link #MAXIMUM_NUMBER_OF_NEIGHBORS}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testMax() throws Exception
    {
        for (int i = 0; i < (MAXIMUM_NUMBER_OF_NEIGHBORS * 2); i++) {
            this.neighbors.addNeighbor(this.testNeighbor + i);
        }

        Assert.assertEquals(this.neighbors.getNeighborsListSize(), MAXIMUM_NUMBER_OF_NEIGHBORS);
    }

    /**
     * Add a neighbor and then remove him.
     * <p>
     * Result: The list of neighbors will be empty.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testRemove() throws Exception
    {
        this.neighbors.addNeighbor(this.testNeighbor);
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 1);

        this.neighbors.removeNeighbor(this.testNeighbor);
        Assert.assertEquals(0, this.neighbors.getNeighborsListSize());
    }

    /**
     * Add a neighbor twice.
     * <p>
     * Result: Only one instance of that neighbor will be in the list.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testUnicity() throws Exception
    {
        this.neighbors.addNeighbor(this.testNeighbor);
        this.neighbors.addNeighbor(this.testNeighbor);

        Assert.assertEquals(1, this.neighbors.getNeighborsListSize());
    }
}
