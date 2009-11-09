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
package org.xwoot.thomasRuleEngine.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.thomasRuleEngine.core.EntriesList;
import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.mock.MockIdentifier;
import org.xwoot.thomasRuleEngine.mock.MockValue;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;

import junit.framework.Assert;

/**
 * Tests for the EntriesList.
 * 
 * @version $Id$
 */
public class EntriesListTest
{
    /** Test file name where to store the entries. */
    private static final String FILENAME = "TREFile";

    /** Test working dir where to store the entries. */
    private static final String WORKING_DIR_PATH = FileUtil.getTestsWorkingDirectoryPathForModule("thomasRuleEngine");

    /** EntriesList instace to test. */
    private EntriesList entriesList;

    /** Test ID. */
    private Identifier id1;

    /** Test ID. */
    private Identifier id2;

    /** Test ID. */
    private Identifier id3;

    /** Test Value. */
    private Value value1;

    /** Test Value. */
    private Value value2;

    /** Test Value. */
    private Value value3;

    /**
     * Init the workingDirectory.
     * 
     * @throws Exception if the working dir is not usable.
     */
    @BeforeClass
    public static void initFile() throws Exception
    {
        FileUtil.deleteDirectory(WORKING_DIR_PATH);
        FileUtil.checkDirectoryPath(WORKING_DIR_PATH);
    }
    
    /**
     * Clean the workingDirectory.
     */
    @AfterClass
    public static void cleanFile()
    {
        FileUtil.deleteDirectory(WORKING_DIR_PATH);
    }

    /**
     * Init the test object and clear its working directory.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void initTest() throws Exception
    {
        this.entriesList = new EntriesList(WORKING_DIR_PATH, FILENAME);
        this.entriesList.clearWorkingDir();

        String pageName1 = "page1";
        String pageName2 = "page2";

        this.id1 = new MockIdentifier(pageName1, "Id1");
        this.id2 = new MockIdentifier(pageName1, "Id2");
        this.id3 = new MockIdentifier(pageName2, "Id3");
        this.value1 = new MockValue("value1");
        this.value2 = new MockValue("value2");
        this.value3 = new MockValue("value3");
    }

    /**
     * Clean the generated files.
     */
    @After
    public void cleanTest() 
    {
        this.entriesList.clearWorkingDir();
    }
    
    /**
     * Check if the working directory is properly created.
     * 
     * @throws Exception if working dir path is null.
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertTrue(new File(WORKING_DIR_PATH).exists());
    }

    /**
     * Tests the behavior of the EntriesList constructor.
     * <p>
     * Result: If a non existing directory is passed, an exception will be caught and the object will be null. If an
     * existing directory is passed, the object is properly initialized.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testConstructor() throws Exception
    {
        File fakeDirectory = new File(EntriesListTest.WORKING_DIR_PATH, "fakeDir");
        if (fakeDirectory.exists()) {
            if (fakeDirectory.isFile()) {
                fakeDirectory.delete();
            } else {
                FileUtil.deleteDirectory(fakeDirectory);
            }
        }
        
        fakeDirectory.mkdir();
        fakeDirectory.setReadOnly();
        
        // If Root/Super user is running the tests. Skip this part because access restrictions do not apply.
        if (!fakeDirectory.canWrite()) {
            
            // Constructor must send an exception if the given working dir is unusable.
            this.entriesList = null;
    
            try {
                this.entriesList = new EntriesList(fakeDirectory.toString(), FILENAME);
            } catch (ThomasRuleEngineException e) {
                // nothing.
            }
            Assert.assertNull(this.entriesList);
        }

        // Constructor must make the wanted object
        this.entriesList = new EntriesList(WORKING_DIR_PATH, FILENAME);
        Assert.assertNotNull(this.entriesList);

        /*
         * // Constructor must delete the file corresponding to the given if it // exist in file system File f = new
         * File(WORKINGDIR + File.separator + FILENAME); Assert.assertEquals(true, f.createNewFile());
         * Assert.assertEquals(true, f.exists()); el = new EntriesList(WORKINGDIR, FILENAME); Assert.assertEquals(false,
         * f.exists());
         */
    }

    /**
     * Tests the functionality and unicity of the addEntry method. Also tests that the entry is stored. The size,
     * getEntry and getEntries methods are also tested.
     * <p>
     * Result: After adding an entry twice, it is stored in a file, and only one entry will be kept.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     */
    @Test
    public void testAddEntry() throws ThomasRuleEngineException
    {
        Entry entry1 = new Entry(this.id1, this.value1, false, null, null);

        // add one entry
        this.entriesList.addEntry(entry1);

        File f = new File(WORKING_DIR_PATH + File.separator + FILENAME);
        // verify the creation of the file resulting of the list serialisation
        Assert.assertEquals(true, f.exists());

        // test the getEntry function
        Assert.assertEquals(entry1, this.entriesList.getEntry(this.id1));

        // test unicity of the add
        this.entriesList.addEntry(entry1);
        Assert.assertEquals(entry1, this.entriesList.getEntry(this.id1));
        Assert.assertEquals(this.entriesList.size(), 1);

        // test a second entry
        Entry entry2 = new Entry(this.id2, this.value2, false, null, null);
        this.entriesList.addEntry(entry2);
        Assert.assertEquals(2, this.entriesList.size());
        Assert.assertEquals(1, this.entriesList.getEntries(this.id1.getId()).size());
        Assert.assertEquals(entry2, this.entriesList.getEntry(this.id2));

        // test a third in a different page entry
        Entry entry3 = new Entry(this.id3, this.value3, false, null, null);
        this.entriesList.addEntry(entry3);
        Assert.assertEquals(3, this.entriesList.size());
        Assert.assertEquals(1, this.entriesList.getEntries(this.id1.getId()).size());
        Assert.assertEquals(1, this.entriesList.getEntries(this.id3.getId()).size());
        Assert.assertEquals(entry3, this.entriesList.getEntry(this.id3));
    }

    /**
     * Tests the functionality and unicity of the removeEntry method and also the serialization of an empty entriesList.
     * <p>
     * Result: After adding deleting the only entry, even twice, no more entries must exist in the entriesList. Also,
     * the serialized file will be deleted when no entries are managed.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     */
    @Test
    public void testRemoveEntry() throws ThomasRuleEngineException
    {
        // add one entry
        this.entriesList.addEntry(new Entry(this.id1, this.value1, false, null, null));

        // test the removeEntry function
        this.entriesList.removeEntry(this.id1);
        Assert.assertEquals(this.entriesList.size(), 0);

        // test unicity of the remove
        this.entriesList.removeEntry(this.id1);
        Assert.assertEquals(this.entriesList.size(), 0);

        // no file when list size is 0
        File f = new File(WORKING_DIR_PATH + File.separator + FILENAME);
        Assert.assertEquals(false, f.exists());
    }

    /**
     * Tests the assignment property of the entriesList.
     * <p>
     * Result: If 2 entries with the same ID are added, the result will contain only the last entry.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     */
    @Test
    public void testSetEntry() throws ThomasRuleEngineException
    {
        // add one entry
        this.entriesList.addEntry(new Entry(this.id1, this.value1, false, null, null));
        Assert.assertEquals(this.entriesList.size(), 1);

        // add another entry with the same id but different value.
        Entry entry2 = new Entry(this.id1, this.value2, false, null, null);
        this.entriesList.addEntry(entry2);
        Assert.assertEquals(this.entriesList.size(), 1);
        Assert.assertEquals(entry2, this.entriesList.getEntry(this.id1));
    }
}
