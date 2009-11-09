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
package org.xwoot.xwootApp.test;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.contentprovider.XWootId;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.core.LastPatchAndXWikiXWootId;

/**
 * Test the LastModifiedPageNameList class.
 * 
 * @version $Id:$
 */
public class LastModifiedContentIdMapTest extends AbstractXWootTest
{
    private String pn1 = "toto";

    private String pn2="titi";
    //    
    // private String pn3="tata";

    /** content id for test. */
    private String cid1 = "page.mainPageContent";

    /** content id for test. */
    private String cid2 = "comment1.mainContent";

    /** content id for test. */
    private String cid3 = "tag.mainContent";

    /**
     * Test the add and remove methods on the last modified contentid list in {@link ContentManager}.
     * 
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    @Test
    public void pageListTest() throws XWootException
    {
        // page toto have been modified to version => 1.0
        LastPatchAndXWikiXWootId lmcim = new LastPatchAndXWikiXWootId(WORKINGDIR);
        lmcim.removeAllPatchId();
        XWootId id1 = new XWootId(this.pn1, 10, 1, 0);
        XWootId id2 = new XWootId(this.pn2, 10, 1, 0);
        lmcim.add2PatchIdMap(id1, this.cid1);
        lmcim.add2PatchIdMap(id1, this.cid2);
        lmcim.add2PatchIdMap(id1, this.cid3);
        lmcim.add2PatchIdMap(id2, this.cid3);
        lmcim.add2PatchIdMap(id2, this.cid3);
        lmcim.add2PatchIdMap(id2, this.cid1);
        
        Map<XWootId, Set<String>> currentMap = lmcim.getCurrentPatchIdMap();

        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertTrue(currentMap.containsKey(id2));
        System.out.println(currentMap);
        Assert.assertEquals(currentMap.size(), 2);
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertEquals(currentMap.get(id2).size(), 2);
        Assert.assertTrue(currentMap.get(id1).contains(this.cid1));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid2));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid3));
        lmcim.removePatchId(id1,this.cid1);
        lmcim.removePatchId(id1,this.cid2);
        lmcim.removePatchId(id1,this.cid3);
        lmcim.removePatchId(id1,this.cid3);
        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertTrue(currentMap.containsKey(id2));
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertEquals(currentMap.get(id2).size(), 2);
        currentMap = lmcim.getCurrentPatchIdMap();
        Assert.assertFalse(currentMap.containsKey(id1));
        Assert.assertTrue(currentMap.containsKey(id2));
        Assert.assertEquals(currentMap.get(id2).size(), 2);
        Assert.assertEquals(currentMap.size(), 1);
    }

    /**
     * Test concurrency add and remove methods on the last modified contentid list in {@link ContentManager}.
     * 
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    @Test
    public void concurrencyPageListTest() throws XWootException
    {
        // page toto have been modified to version => 1.0
        LastPatchAndXWikiXWootId lmcim = new LastPatchAndXWikiXWootId(WORKINGDIR);
        lmcim.removeAllPatchId();
        XWootId id1 = new XWootId(this.pn1, 10, 1, 0);
        lmcim.add2PatchIdMap(id1, this.cid1);
        lmcim.add2PatchIdMap(id1, this.cid2);
        lmcim.add2PatchIdMap(id1, this.cid3);

        Map<XWootId, Set<String>> currentMap = lmcim.getCurrentPatchIdMap();

        XWootId id2 = new XWootId(this.pn1, 11, 1, 1);

        lmcim.add2PatchIdMap(id2, this.cid1);
        lmcim.add2PatchIdMap(id2, this.cid2);
        lmcim.add2PatchIdMap(id2, this.cid3);

        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.size(), 1);
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertTrue(currentMap.get(id1).contains(this.cid1));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid2));
        Assert.assertTrue(currentMap.get(id1).contains(this.cid3));
        Assert.assertFalse(currentMap.containsKey(id2));
        lmcim.removePatchId(id1,this.cid1);
        lmcim.removePatchId(id1,this.cid2);
        lmcim.removePatchId(id1,this.cid3);
        Assert.assertTrue(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.get(id1).size(), 3);
        Assert.assertFalse(currentMap.containsKey(id2));
        currentMap = lmcim.getCurrentPatchIdMap();
        Assert.assertFalse(currentMap.containsKey(id1));
        Assert.assertEquals(currentMap.size(), 1);
        Assert.assertTrue(currentMap.containsKey(id2));
        Assert.assertEquals(currentMap.get(id2).size(), 3);
        Assert.assertTrue(currentMap.get(id2).contains(this.cid1));
        Assert.assertTrue(currentMap.get(id2).contains(this.cid2));
        Assert.assertTrue(currentMap.get(id2).contains(this.cid3));
    }

}
