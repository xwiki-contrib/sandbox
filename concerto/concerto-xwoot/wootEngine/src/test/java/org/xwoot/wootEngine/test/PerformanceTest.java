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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;

import java.util.List;
import java.util.Vector;

/**
 * Stress and performance tests.
 * 
 * @version $Id$
 */
public class PerformanceTest extends AbstractWootEngineTest
{
    /**
     * Do 1000 insert operations between the default first row and the previous row inserted by the previous operation.
     * Basically, you will always be inserting on the first position.
     * <p>
     * Normally this takes about 2.4-2.5 seconds but 4 seconds should be enough on other machines too. 1.4 seconds :
     * Processor 2x Intel(R) Core(TM)2 CPU 6600 @ 2.40GHz Memory 3369MB Operating System Ubuntu 8.10 Linux
     * 2.6.27-10-generic (i686)
     * 
     * @throws Exception if problems loading/unloading pages occur.
     */
    @Test(timeout = 4000)
    public void testFlood() throws Exception
    {
        String line =
            "---------------FLOOD---------------|" + "---------------FLOOD---------------|---------------"
                + "FLOOD---------------|---------------FLOOD---------------";

        // Add a first line between the default first and last woot row.
        WootId firstRowId = new WootId(this.site0.getWootEngineId(), 0);
        WootIns op0 = new WootIns(new WootRow(firstRowId, line), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(this.pageName, this.objectId, this.fieldId, false));
        op0.setOpId(firstRowId);

        List<WootOp> data = new Vector<WootOp>();
        data.add(op0);

        // do 1000 insert operations on the first position, relative to the previous inserted row.
        for (int i = 0; i < 1000; i++) {
            WootId previouslyAddedRowId = new WootId(this.site0.getWootEngineId(), i);
            WootId newRowId = new WootId(this.site0.getWootEngineId(), i + 1);
            WootIns op = new WootIns(new WootRow(newRowId, line), WootId.FIRST_WOOT_ID, previouslyAddedRowId);
            op.setContentId(new ContentId(this.pageName, this.objectId, this.fieldId, false));
            op.setOpId(newRowId);
            // woot.ins("index", line, 0).toString();
            data.add(op);
        }

        Patch patch = new Patch(data, null, this.pageName, this.objectId, 0, 0, 0);

        Log log = LogFactory.getLog(this.getClass());
        log.debug("Started time-consuming operation...");

        long start = System.currentTimeMillis();
        this.site0.deliverPatch(patch);

        // Get elapsed time in milliseconds
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;

        log.debug("Finished in: " + elapsedTimeSec + " seconds.");
    }
}
