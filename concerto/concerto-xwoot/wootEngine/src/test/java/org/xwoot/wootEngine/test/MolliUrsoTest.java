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
package org.xwoot.wootEngine.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.op.WootOp;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Tests for the WootEngine class.
 * 
 * @version $Id$
 */
public class MolliUrsoTest extends AbstractWootEngineTest
{
    /**
     * Test inserting three lines in a page.
     * 
     * @throws Exception if loading/unloading problems occur.
     */
    @Test
    public void testBasic() throws Exception
    {
        WootContent wootContent =
            this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);

        this.site0.insert(wootContent, this.line1, 0);
        this.site0.insert(wootContent, this.line2, 1);
        this.site0.insert(wootContent, this.line3, 2);

        this.site0.getContentManager().unloadWootContent(wootContent);

        assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site0.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * If 2 WootEngines modify the same line in the same page.
     * <p>
     * After applying the patches with insert operations, on each engine the page contains both the lines, in the same
     * order.
     * 
     * @throws Exception if problems loading/unloading the pages occur.
     */
    @Test
    public void testCross() throws Exception
    {
        WootContent wc1 = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootContent wc2 = this.site1.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);

        WootOp op1 = this.site0.insert(wc1, this.line1, 0);
        WootOp op2 = this.site1.insert(wc2, this.line2, 0);
        this.site0.getContentManager().unloadWootContent(wc1);
        this.site1.getContentManager().unloadWootContent(wc2);

        assertEquals(addEndLine(this.line1), this.site0.getContentManager().getContent(this.pageName, this.objectId,
            this.fieldId));
        assertEquals(addEndLine(this.line2), this.site1.getContentManager().getContent(this.pageName, this.objectId,
            this.fieldId));

        // operation 2
        List<WootOp> data0 = new Vector<WootOp>();
        data0.add(op2);
        Patch patch0 = new Patch(data0, null, this.pageName, this.objectId, 0, 0, 0);

        // operation 1
        List<WootOp> data1 = new Vector<WootOp>();
        data1.add(op1);
        Patch patch1 = new Patch(data1, null, this.pageName, this.objectId, 0, 0, 0);

        this.site0.deliverPatch(patch0);
        this.site1.deliverPatch(patch1);

        assertEquals(wrapStartEndMarkers(this.line1 + this.line2), this.site0.getContentManager().getContentInternal(
            this.pageName, this.objectId, this.fieldId));
        assertEquals(this.site0.getContentManager().getContentInternal(this.pageName, this.objectId, this.fieldId),
            this.site1.getContentManager().getContentInternal(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * A page containing 3 lines gets an insert operation at position 0.
     * <p>
     * The result will have on position 0 the newly inserted line and all the other lines will be shifted below by one
     * position.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testInsertBeginning() throws Exception
    {
        WootContent wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        this.site0.insert(wc, this.line1, 0);
        this.site0.insert(wc, this.line2, 1);
        this.site0.insert(wc, this.line3, 2);
        this.site0.insert(wc, this.line4, 0);
        this.site0.getContentManager().unloadWootContent(wc);

        assertEquals(wrapStartEndMarkers(this.line4 + this.line1 + this.line2 + this.line3), this.site0
            .getContentManager().getContentInternal(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * Delete the only line in a page.
     * <p>
     * As a result, the page should contain only the start and end row.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testSimpleDel() throws Exception
    {
        WootContent wc1 = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        this.site0.insert(wc1, this.line1, 0);
        this.site0.getContentManager().unloadWootContent(wc1);
        assertEquals(wrapStartEndMarkers(this.line1), this.site0.getContentManager().getContentInternal(this.pageName,
            this.objectId, this.fieldId));

        wc1 = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        this.site0.delete(wc1, 0);
        this.site0.getContentManager().unloadWootContent(wc1);
        assertEquals(this.emptyPageContent, this.site0.getContentManager().getContentInternalVisible(this.pageName,
            this.objectId, this.fieldId));
    }

    /**
     * Make concurrent changes on 3 engines.
     * <p>
     * In the end, all 3 engines have the same content.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testTP2() throws Exception
    {
        WootContent wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op0 = this.site0.insert(wc, this.line1, 0);
        WootOp op1 = this.site0.insert(wc, this.line2, 1);
        WootOp op2 = this.site0.insert(wc, this.line3, 2);
        this.site0.getContentManager().unloadWootContent(wc);

        /* site0: line1 line2 line3 */

        // List<WootOp> data = Arrays.asList(new WootOp[] {op0, op1, op2});
        Patch patch =
            new Patch(Arrays.asList(new WootOp[] {op0, op1, op2}), null, this.pageName, this.objectId, 0, 0, 0);

        this.site1.deliverPatch(patch);
        this.site2.deliverPatch(patch);

        /*
         * site0: line1 line2 line3 site1: line1 line2 line3 site2: line1 line2 line3
         */
        assertEquals(this.site0.getContentManager().getContent(this.pageName, this.objectId, this.fieldId), this.site1
            .getContentManager().getContent(this.pageName, this.objectId, this.fieldId));
        assertEquals(this.site1.getContentManager().getContent(this.pageName, this.objectId, this.fieldId), this.site2
            .getContentManager().getContent(this.pageName, this.objectId, this.fieldId));

        wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootContent wc2 = this.site1.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootContent wc3 = this.site2.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op3 = this.site0.insert(wc, this.line4, 2);
        WootOp op4 = this.site1.delete(wc2, 2);
        WootOp op5 = this.site2.insert(wc3, this.line5, 3);
        this.site0.getContentManager().unloadWootContent(wc);
        this.site1.getContentManager().unloadWootContent(wc2);
        this.site2.getContentManager().unloadWootContent(wc3);

        // crux
        // data = Arrays.asList(new WootOp[] {op5});
        this.site1.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op5}), null, this.pageName, this.objectId, 0, 0,
            0));

        // data = Arrays.asList(new WootOp[] {op4});
        this.site2.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op4}), null, this.pageName, this.objectId, 0, 0,
            0));

        /*
         * site1: line1 line2 {deleted line3} line5 site2: line1 line2 {deleted line3} line5
         */
        assertEquals(this.site1.getContentManager().getContent(this.pageName, this.objectId, this.fieldId), this.site2
            .getContentManager().getContent(this.pageName, this.objectId, this.fieldId));

        // op3 descent
        // data = Arrays.asList(new WootOp[] {op3});
        patch.setData(Arrays.asList(new WootOp[] {op3}));

        this.site1.deliverPatch(patch);
        this.site2.deliverPatch(patch);

        /*
         * site1: line1 line2 {deleted line3} line4 line5 site2: line1 line2 {deleted line3} line4 line5
         */
        assertEquals(this.site1.getContentManager().getContent(this.pageName, this.objectId, this.fieldId), this.site2
            .getContentManager().getContent(this.pageName, this.objectId, this.fieldId));

        // data = Arrays.asList(new WootOp[] {op4});
        this.site0.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op4}), null, this.pageName, this.objectId, 0, 0,
            0));

        // data = Arrays.asList(new WootOp[] {op5});
        this.site0.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op5}), null, this.pageName, this.objectId, 0, 0,
            0));

        /*
         * site0: line1 line2 {deleted line3} line4 line5 site1: line1 line2 {deleted line3} line4 line5
         */
        assertEquals(this.site0.getContentManager().getContent(this.pageName, this.objectId, this.fieldId), this.site1
            .getContentManager().getContent(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * Test inserting more times on the same position.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testTPUrso() throws Exception
    {
        WootContent wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op0 = this.site0.insert(wc, this.line1, 0);
        WootOp op1 = this.site0.insert(wc, this.line2, 1);
        this.site0.getContentManager().unloadWootContent(wc);

        List<WootOp> data = Arrays.asList(new WootOp[] {op0, op1});
        Patch patch = new Patch(data, null, this.pageName, this.objectId, 0, 0, 0);

        this.site1.deliverPatch(patch);
        this.site2.deliverPatch(patch);
        /*
         * SITE0: [line1line2] SITE1: [line1line2] SITE2: [line1line2]
         */

        wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootContent wc2 = this.site1.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op2 = this.site0.insert(wc, this.line3, 1);
        WootOp op3 = this.site1.insert(wc2, this.line4, 1);
        this.site0.getContentManager().unloadWootContent(wc);
        this.site1.getContentManager().unloadWootContent(wc2);

        data = Arrays.asList(new WootOp[] {op2});
        patch.setData(data);

        this.site2.deliverPatch(patch);
        /*
         * SITE0: [line1line3line2] SITE1: [line1line4line2] SITE2: [line1line3line2]
         */

        wc2 = this.site2.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op4 = this.site2.insert(wc2, this.line5, 1);
        this.site2.getContentManager().unloadWootContent(wc2);

        data = Arrays.asList(new WootOp[] {op3});
        patch.setData(data);

        this.site2.deliverPatch(patch);
        this.site0.deliverPatch(patch);
        /*
         * SITE0: [line1line3line4line2] SITE1: [line1line4line2] SITE2: [line1line5line3line4line2]
         */

        data = Arrays.asList(new WootOp[] {op4});
        patch.setData(data);

        this.site0.deliverPatch(patch);
        /*
         * SITE0: [line1line5line3line4line2] SITE1: [line1line4line2] SITE2: [line1line5line3line4line2]
         */

        assertEquals(this.site0.getContentManager().getContent(this.pageName, this.objectId, this.fieldId), this.site2
            .getContentManager().getContent(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * Replace the same line in each engine with different content.
     * <p>
     * As a result, the in both engines will have both new lines, one after another.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testUpdateSameLine() throws Exception
    {
        WootContent wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op0 = this.site0.insert(wc, this.line1, 0);
        this.site0.getContentManager().unloadWootContent(wc);

        assertEquals(addEndLine(this.line1), this.site0.getContentManager().getContent(this.pageName, this.objectId,
            this.fieldId));

        // send op00
        List<WootOp> data = new Vector<WootOp>();
        data.add(op0);
        Patch patch = new Patch(data, null, this.pageName, this.objectId, 0, 0, 0);
        this.site1.deliverPatch(patch);
        assertEquals(addEndLine(this.line1), this.site1.getContentManager().getContent(this.pageName, this.objectId,
            this.fieldId));

        // update on site0
        wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op00 = this.site0.delete(wc, 0);
        WootOp op01 = this.site0.insert(wc, this.line2, 0);
        this.site0.getContentManager().unloadWootContent(wc);

        assertEquals(addEndLine(this.line2), this.site0.getContentManager().getContent(this.pageName, this.objectId,
            this.fieldId));

        // update on site1
        WootContent wc2 = this.site1.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op10 = this.site1.delete(wc2, 0);
        WootOp op11 = this.site1.insert(wc2, this.line3, 0);
        this.site1.getContentManager().unloadWootContent(wc2);

        assertEquals(addEndLine(this.line3), this.site1.getContentManager().getContent(this.pageName, this.objectId,
            this.fieldId));

        // send op00 + op01
        data.clear();
        data.add(op00);
        data.add(op01);
        patch.setData(data);
        this.site1.deliverPatch(patch);
        /*
         * Site0: [line2] Site1: [line2line3]
         */

        // send op10 + op11
        data.clear();
        data.add(op10);
        data.add(op11);
        patch.setData(data);
        this.site0.deliverPatch(patch);
        /*
         * Site0: [line2line3] Site1: [line2line3]
         */

        assertEquals(this.site0.getContentManager().getContentInternal(this.pageName, this.objectId, this.fieldId),
            this.site1.getContentManager().getContentInternal(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * 3 insert operations are sent/received in the wrong order. First op 3 and op 2, then the last one.
     * <p>
     * The result must contain all 3 of them in the right order.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testWaitingQueue() throws Exception
    {
        WootContent wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op1 = this.site0.insert(wc, this.line1, 0);
        WootOp op2 = this.site0.insert(wc, this.line2, 1);
        WootOp op3 = this.site0.insert(wc, this.line3, 2);
        this.site0.getContentManager().unloadWootContent(wc);

        assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site0.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));

        // real test : send 2 last op without the first
        // => wootEngine must put this two last op in wating queue
        // after send the first op => woot engine can integrate
        // the 3 op in the good order

        List<WootOp> data = new Vector<WootOp>();
        data.add(op3);
        data.add(op2);
        Patch patch = new Patch(data, null, this.pageName, this.objectId, 0, 0, 0);

        this.site1.deliverPatch(patch);
        assertEquals(this.emptyPageContent, this.site1.getContentManager().getContentInternal(this.pageName,
            this.objectId, this.fieldId));

        data.clear();
        data.add(op1);
        patch.setData(data);

        this.site1.deliverPatch(patch);
        assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site1.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));
    }

    /**
     * Same as {@link #testWaitingQueue()} but this time operation 2 is repeated in the first patch and after all the
     * operations are applied it is sent one more time.
     * <p>
     * The result must be the same and the duplicated operation must be applied only once.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testWaitingQueue2() throws Exception
    {
        WootContent wc = this.site0.getContentManager().loadWootContent(this.pageName, this.objectId, this.fieldId);
        WootOp op1 = this.site0.insert(wc, this.line1, 0);
        WootOp op2 = this.site0.insert(wc, this.line2, 1);
        WootOp op3 = this.site0.insert(wc, this.line3, 2);
        this.site0.getContentManager().unloadWootContent(wc);

        assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site0.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));

        // real test : send 2 last op without the first
        // => wootEngine must put this two last op in wating queue
        // after send the first op => woot engine can integrate
        // the 3 op in the good order
        List<WootOp> data = new Vector<WootOp>();
        data.add(op3);
        data.add(op2);
        data.add(op2);
        Patch patch = new Patch(data, null, this.pageName, this.objectId, 0, 0, 0);
        this.site1.deliverPatch(patch);

        assertEquals(this.emptyPageContent, this.site1.getContentManager().getContentInternal(this.pageName,
            this.objectId, this.fieldId));

        data.clear();
        data.add(op1);
        patch.setData(data);

        this.site1.deliverPatch(patch);
        assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site1.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));

        data.add(op2);
        patch.setData(data);
        this.site1.deliverPatch(patch);

        assertEquals(wrapStartEndMarkers(this.line1 + this.line2 + this.line3), this.site1.getContentManager()
            .getContentInternal(this.pageName, this.objectId, this.fieldId));
    }
}
