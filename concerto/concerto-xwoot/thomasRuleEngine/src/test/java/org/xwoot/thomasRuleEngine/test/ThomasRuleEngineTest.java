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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.mock.MockIdentifier;
import org.xwoot.thomasRuleEngine.mock.MockValue;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpDel;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpNew;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpSet;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;

import junit.framework.Assert;

/**
 * Tests for the ThomasRuleEngine class.
 * 
 * @version $Id$
 */
public class ThomasRuleEngineTest
{
    /** Test working dir path for saving entries. */
    private static final String WORKING_DIR_PATH = FileUtil.getTestsWorkingDirectoryPathForModule("thomasRuleEngine");

    /** Test treId. */
    private String treId1 = String.valueOf(1);

    /** Test treId. */
    private String treId2 = String.valueOf(2);

    /** Test instance. */
    private ThomasRuleEngine tre1;

    /** Test instance. */
    private ThomasRuleEngine tre2;

    /** Test ID. */
    private Identifier id1;

    /** Test ID. */
    private Identifier id2;

    /** Test ID. */
    private Identifier id3;

    /** Test value. */
    private Value val1;

    /** Test value. */
    private Value val2;

    /** Test value. */
    private Value val3;

    /**
     * Check/Create working directory.
     * 
     * @throws Exception if the directory is not usable.
     */
    @BeforeClass
    public static void initFile() throws Exception
    {
        FileUtil.deleteDirectory(WORKING_DIR_PATH);
        FileUtil.checkDirectoryPath(WORKING_DIR_PATH);
    }
    
    /**
     * Clean the working directory.
     */
    @AfterClass
    public static void cleanFile()
    {
        FileUtil.deleteDirectory(WORKING_DIR_PATH);
    }

    /**
     * Tests if the working dir was initialized.
     * 
     * @throws Exception if the working dir is null.
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertTrue(new File(WORKING_DIR_PATH).exists());
    }

    /**
     * Initialize test objects.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void initTest() throws Exception
    {
        this.tre1 = new ThomasRuleEngine(this.treId1, new File(WORKING_DIR_PATH, this.treId1).toString());
        this.tre2 = new ThomasRuleEngine(this.treId2, new File(WORKING_DIR_PATH, this.treId2).toString());

        this.tre1.clearWorkingDir();
        this.tre2.clearWorkingDir();

        String pageName = "page1";

        this.id1 = new MockIdentifier(pageName, "id1");
        this.id2 = new MockIdentifier(pageName, "id2");
        this.id3 = new MockIdentifier(pageName, "id3");

        this.val1 = new MockValue("val1");
        this.val2 = new MockValue("val2");
        this.val3 = new MockValue("val3");
    }

    /**
     * Tests the basic behavior of applyOp.
     * <p>
     * Result: as described in {@link ThomasRuleEngine#applyOp(ThomasRuleOp)}.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     */
    @Test
    public void testApplyOpBasic() throws ThomasRuleEngineException
    {
        // ////////////////////
        // (!existInBase,NewOp) => Creation
        // ////////////////////
        ThomasRuleOp op0new = this.tre1.getOp(this.id1, this.val1);

        this.tre1.applyOp(op0new);
        this.tre2.applyOp(op0new);

        Assert.assertEquals(this.val1, this.tre1.getValue(this.id1));
        Assert.assertEquals(this.val1, this.tre2.getValue(this.id1));

        // ////////////////////
        // (!existInBase,SetOp) => Creation + Set value
        // ////////////////////
        ThomasRuleOp op0set = this.tre1.getOp(this.id2, this.val1);

        this.tre1.applyOp(op0set);
        this.tre2.applyOp(op0set);

        Assert.assertEquals(this.val1, this.tre1.getValue(this.id2));
        Assert.assertEquals(this.val1, this.tre2.getValue(this.id2));

        // ////////////////////
        // (!existInBase,DelOp) => Nothing
        // ////////////////////
        ThomasRuleOp op0del = this.tre1.getOp(this.id3, null);
        Assert.assertNull(op0del);

        // /////////////////////////
        // (existInBase,BaseTc>opTc) => Nothing (local value is last writer)
        // /////////////////////////
        // new id3 with val=val1 on tre1 and tre2
        ThomasRuleOp op1greater = this.tre1.getOp(this.id3, this.val1);
        this.tre1.applyOp(op1greater);
        this.tre2.applyOp(op1greater);
        Assert.assertEquals(this.val1, this.tre1.getValue(this.id3));
        Assert.assertEquals(this.val1, this.tre2.getValue(this.id3));

        // del id3 on tre1
        ThomasRuleOp op1greaterBis = this.tre1.getOp(this.id3, null);
        this.tre1.applyOp(op1greaterBis);
        Assert.assertNull(this.tre1.getValue(this.id3));
    }

    /**
     * Tests the behavior of applyOp based on consulting the timestamps.
     * <p>
     * Result: No operation older than the last modification done on an entry is performed, as described in
     * {@link ThomasRuleEngine#applyOp(ThomasRuleOp)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testApplyOpBasedOnTimestamps() throws Exception
    {
        // /////////////////////////
        // Don't apply an operation that is older than the last operation applied on the entry.
        // /////////////////////////

        // set id3 to val2 on tre2
        ThomasRuleOp op1greaterQ = this.tre2.getOp(this.id3, this.val2);
        this.tre2.applyOp(op1greaterQ);
        Thread.sleep(10);

        // create id3 with val3 on tre1
        ThomasRuleOp op1greaterTer = this.tre1.getOp(this.id3, this.val3);

        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1greaterTer.getTimestampIdCreation().compareTo(op1greaterQ.getTimestampIdCreation()) == 1);
        this.tre1.applyOp(op1greaterTer);
        Assert.assertEquals(this.val3, this.tre1.getValue(this.id3));

        // verify
        Assert.assertEquals(this.val2, this.tre2.getValue(this.id3));
        this.tre1.applyOp(op1greaterQ);
        Assert.assertEquals(this.val3, this.tre1.getValue(this.id3));

        // ///////////////////////////////
        // (existInBase,BaseTc<opTc,NewOp) => overwrite local value
        // ///////////////////////////////
        Assert.assertTrue((op1greaterTer instanceof ThomasRuleOpNew));

        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1greaterTer.getTimestampIdCreation().compareTo(op1greaterQ.getTimestampIdCreation()) == 1);

        this.tre2.applyOp(op1greaterTer);
        Assert.assertEquals(this.val3, this.tre2.getValue(this.id3));
    }

    /**
     * Tests the behavior of applyOp.
     * <p>
     * Result: as described in {@link ThomasRuleEngine#applyOp(ThomasRuleOp)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testApplyOpBasedOnTimestamps2() throws Exception
    {

        // create a New operation.
        ThomasRuleOp op1greaterTer = this.tre1.getOp(this.id3, this.val3);
        Thread.sleep(10);

        // ///////////////////////////////
        // (existInBase,BaseTc<opTc,SetOp) => overwrite local value
        // ///////////////////////////////
        this.tre1.applyOp(this.tre1.getOp(this.id3, null));
        this.tre1.applyOp(this.tre1.getOp(this.id3, this.val1));

        ThomasRuleOp op1lowerSet = this.tre1.getOp(this.id3, this.val2);
        Assert.assertTrue((op1lowerSet instanceof ThomasRuleOpSet));
        this.tre1.applyOp(op1lowerSet);

        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1lowerSet.getTimestampIdCreation().compareTo(op1greaterTer.getTimestampIdCreation()) == 1);

        this.tre2.applyOp(op1lowerSet);
        Assert.assertEquals(this.val2, this.tre2.getValue(this.id3));

        // ///////////////////////////////
        // (existInBase,BaseTc<opTc,DelOp) => overwrite local value
        // ///////////////////////////////
        this.tre1.applyOp(this.tre1.getOp(this.id3, null));
        this.tre1.applyOp(this.tre1.getOp(this.id3, this.val3));

        ThomasRuleOp op1lowerDel = this.tre1.getOp(this.id3, null);
        Assert.assertTrue((op1lowerDel instanceof ThomasRuleOpDel));

        this.tre1.applyOp(op1lowerDel);
        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1lowerDel.getTimestampIdCreation().compareTo(op1lowerSet.getTimestampIdCreation()) == 1);

        this.tre2.applyOp(op1lowerDel);
        Assert.assertNull(this.tre2.getValue(this.id3));

        // ///////////////////////////////////////
        // (existInBase,BaseTc==opTc,BaseTm>=opTm) => Nothing (local value is
        // last writer)
        // ///////////////////////////////////////
        this.tre1.applyOp(this.tre1.getOp(this.id1, null));
        this.tre1.applyOp(this.tre1.getOp(this.id1, this.val1));

        ThomasRuleOp op1TcEqualTmG = this.tre1.getOp(this.id1, this.val2);
        this.tre1.applyOp(op1TcEqualTmG);

        ThomasRuleOp op1TcEqualTmGBis = this.tre1.getOp(this.id1, this.val3);
        this.tre1.applyOp(op1TcEqualTmGBis);
        Assert.assertTrue(op1TcEqualTmG.getTimestampIdCreation().compareTo(
            op1TcEqualTmGBis.getTimestampIdCreation()) == 0);
        Assert
            .assertEquals(-1, op1TcEqualTmG.getTimestampModif().compareTo(op1TcEqualTmGBis.getTimestampModif()));

        this.tre1.applyOp(op1TcEqualTmG);

        Assert.assertEquals(this.val3, this.tre1.getValue(this.id1));
    }

    /**
     * Tests the behavior of getOp.
     * <p>
     * Result: As described in {@link ThomasRuleEngine#getOp(Identifier, Value)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testGetOp() throws Exception
    {
        // get op new
        // ////////////////////////
        // (!existInBase,givenVal) => normal id creation
        // ////////////////////////
        ThomasRuleOp op01 = this.tre1.getOp(this.id1, this.val1);
        Assert.assertTrue((op01 instanceof ThomasRuleOpNew));

        // /////////////////////////
        // (!existInBase,!givenVal) => del on unknown value == return null
        // /////////////////////////
        ThomasRuleOp op00 = this.tre1.getOp(this.id2, null);
        Assert.assertNull(op00);

        // apply op new
        this.tre1.applyOp(op01);
        Assert.assertEquals(this.val1, this.tre1.getValue(this.id1));

        // get op set
        // ///////////////////////////////////////
        // (existInBase,!deletedInBase,givenVal) => normal set value
        // ///////////////////////////////////////
        // NORMAL SET
        ThomasRuleOp op101 = this.tre1.getOp(this.id1, this.val2);
        Assert.assertTrue((op101 instanceof ThomasRuleOpSet));

        // ///////////////////////////////////////
        // (existInBase,!deletedInBase,!givenVal) => normal del value
        // ///////////////////////////////////////
        // NORMAL DEL
        ThomasRuleOp op100 = this.tre1.getOp(this.id1, null);
        Assert.assertTrue((op100 instanceof ThomasRuleOpDel));

        // apply op set
        this.tre1.applyOp(op101);
        Assert.assertEquals(this.val2, this.tre1.getValue(this.id1));

        // apply op del
        Assert.assertNotNull(this.tre1.applyOp(op100));
        Assert.assertEquals(null, this.tre1.getValue(this.id1));

        // get op with new val on a deleted entry
        // /////////////////////////////////////
        // (existInBase,deletedInBase,givenVal) => normal re-creation (old
        // deleted value is re-created)
        // /////////////////////////////////////
        // NORMAL RE-NEW
        ThomasRuleOp op111 = this.tre1.getOp(this.id1, this.val3);
        Assert.assertTrue((op111 instanceof ThomasRuleOpNew));
        Assert.assertTrue(op111.getTimestampIdCreation().compareTo(op01.getTimestampIdCreation()) > 0);

        // get op with no val on a deleted entry
        // ///////////////////////////////////////
        // (existInBase,deletedInBase,!givenVal) => "normal" re-deletion (the
        // modif timestamp is update ...)
        // ///////////////////////////////////////
        // NORMAL RE-DEL
        ThomasRuleOp op110 = this.tre1.getOp(this.id1, null);
        Assert.assertTrue((op110 instanceof ThomasRuleOpDel));
        Assert.assertTrue(op110.getTimestampModif().compareTo(op111.getTimestampModif()) >= 0);

        // tre1 must not apply the new op (same timestamp of creation and
        // timestamp modif of op new is older than the latest modif)
        this.tre1.applyOp(op01);
        Assert.assertNull(this.tre1.getValue(this.id1));

        // tre1 must not apply the new op (same timestamp of creation and
        // timestamp modif of op set is older than the latest modif)
        this.tre1.applyOp(op101);
        Assert.assertNull(this.tre1.getValue(this.id1));
    }

    /**
     * Generate timestamps on 2 different TREs.
     * <p>
     * Result: the latest timestamp will always be greater than any other previously generated timestamp, no matter on
     * what TRE it was generated.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public synchronized void testGetTimestamp() throws Exception
    {
        Timestamp t1 = this.tre1.getTimestamp();
        Timestamp t2 = this.tre2.getTimestamp();

        this.wait(10);

        Timestamp t3 = this.tre1.getTimestamp();

        Assert.assertEquals(1, t3.compareTo(t1));
        Assert.assertEquals(1, t2.compareTo(t1));
        Assert.assertEquals(0, t2.compareTo(t2));
        Assert.assertEquals(-1, t1.compareTo(t3));
    }
}
