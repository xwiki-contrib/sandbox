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

import org.junit.Test;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;

import junit.framework.Assert;

/**
 * Tests the WootRow class, mainly the compareTo behavior.
 * 
 * @version $Id$
 */
public class RowTest extends AbstractWootEngineTest
{
    /**
     * Tests if rows are compared correctly by comparing the default first row with the default last row.
     * <p>
     * Result: the default first row is always "smaller" than the last one.
     */
    @Test
    public void testCompareFirstRowWithLastRow()
    {
        Assert.assertTrue(WootRow.FIRST_WOOT_ROW.compareTo(WootRow.LAST_WOOT_ROW) < 0);
    }

    /**
     * Two rows having the same siteId and content but being created one after another (different clock values).
     * <p>
     * Result: The latest row (having a greater clock value) is the "biggest".
     */
    @Test
    public void testCompareByLocalClock()
    {
        WootRow r1 = new WootRow(new WootId(this.site0.getWootEngineId(), 0), this.line1);
        WootRow r2 = new WootRow(new WootId(this.site0.getWootEngineId(), 1), this.line1);

        Assert.assertTrue(r1.compareTo(r2) < 0);
    }

    /**
     * Two rows having the same clock value and content but coming from two different engines.
     * <p>
     * Result: The row having the greater siteId is the "biggest".
     */
    @Test
    public void testCompareBySiteId()
    {
        WootRow r1 = new WootRow(new WootId(this.site0.getWootEngineId(), 0), this.line1);
        WootRow r2 = new WootRow(new WootId(this.site1.getWootEngineId(), 0), this.line1);

        Assert.assertTrue(r1.compareTo(r2) < 0);
    }
}
