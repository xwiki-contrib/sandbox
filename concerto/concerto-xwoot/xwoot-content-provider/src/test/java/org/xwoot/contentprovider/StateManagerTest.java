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
package org.xwoot.contentprovider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.xwoot.contentprovider.XWootContentProviderStateManager;
import org.xwoot.contentprovider.XWootId;
import org.xwoot.contentprovider.XWootIdComparatorAscending;

public class StateManagerTest extends TestCase
{
    private String[] PAGE_IDS = {"P1", "P2"};

    private int NUMBER_OF_UPDATES_PER_PAGE = 10;

    private XWootContentProviderStateManager stateManager;

    @Override
    protected void setUp() throws Exception
    {
        System.out.format("****************\n");
        stateManager = new XWootContentProviderStateManager("DB", true);
        for (int i = 1; i <= NUMBER_OF_UPDATES_PER_PAGE; i++) {
            for (int j = 0; j < PAGE_IDS.length; j++) {
                stateManager.addModification(new XWootId(PAGE_IDS[j], i * PAGE_IDS.length + j, i, 1));
            }

        }
    }

    @Override
    protected void tearDown()
    {
        stateManager.dispose();
        System.out.format("****************\n\n");
    }

    public void testInit() throws Exception
    {
        System.out.format("***** testInit()\n");
        for (String pageId : PAGE_IDS) {
            List<XWootId> xwootIds = stateManager.getModificationsFor(pageId, false);
            assertEquals(NUMBER_OF_UPDATES_PER_PAGE, xwootIds.size());
        }
    }

    public void testAddDuplicateModification() throws Exception
    {
        System.out.format("***** testAddDuplicateModification()\n");
        XWootId xwootId = new XWootId("A", 1, 1, 1);
        stateManager.addModification(xwootId);
        assertEquals(false, stateManager.addModification(xwootId));
    }

    public void testGetHighestModificationTimestamp() throws Exception
    {
        System.out.format("***** testGetHighestModificationTimestamp()\n");
        long highestModificationTimestamp = stateManager.getHighestModificationTimestamp();
        assertEquals(NUMBER_OF_UPDATES_PER_PAGE * PAGE_IDS.length + 1, highestModificationTimestamp);
    }

    public void testClearModification() throws Exception
    {
        System.out.format("***** testClearModification()\n");
        for (String pageId : PAGE_IDS) {
            List<XWootId> xwootIds = stateManager.getModificationsFor(pageId, false);

            for (XWootId xwootId : xwootIds) {
                stateManager.clearModification(xwootId);
                XWootId lastCleared = stateManager.getLastCleared(xwootId.getPageId());

                assertEquals(xwootId, lastCleared);
            }

            xwootIds = stateManager.getModificationsFor(pageId, false);
            assertEquals(0, xwootIds.size());
        }
    }

    public void testClearAllModificationsExcept() throws Exception
    {
        System.out.format("***** testClearAllModificationsExcept()\n");
        for (String pageId : PAGE_IDS) {
            List<XWootId> xwootIds = stateManager.getModificationsFor(pageId, false);
            XWootId xwootId = xwootIds.get(0);
            stateManager.clearAllModificationExcept(xwootId);
            xwootIds = stateManager.getModificationsFor(pageId, false);
            assertEquals(1, xwootIds.size());
            assertEquals(xwootId, xwootIds.get(0));
        }
    }

    public void testGetNonClearedModificationsWithLowestTimestamp() throws Exception
    {
        System.out.format("***** testGetNonClearedModifiationsWithLowestTimestamp()\n");

        Map<String, Long> pageIdToLastTimestamp = new HashMap<String, Long>();

        Set<XWootId> xwootIds = stateManager.getNonClearedModificationsWithLowestTimestamp();
        while (xwootIds.size() != 0) {
            for (XWootId xwootId : xwootIds) {
                Long lastTimestamp = pageIdToLastTimestamp.get(xwootId.getPageId());
                if (lastTimestamp != null) {
                    assertTrue(xwootId.getTimestamp() > lastTimestamp);
                }

                pageIdToLastTimestamp.put(xwootId.getPageId(), xwootId.getTimestamp());

                stateManager.clearModification(xwootId);
            }

            xwootIds = stateManager.getNonClearedModificationsWithLowestTimestamp();
        }
    }

    public void testGetModificationsInRage() throws Exception
    {
        System.out.format("***** testGetModificationsInRage()\n");

        for (String pageId : PAGE_IDS) {
            List<XWootId> xwootIds = stateManager.getModificationsFor(pageId, false);

            Collections.sort(xwootIds, new XWootIdComparatorAscending());

            for (int i = 0; i < xwootIds.size(); i++) {
                long lowest = xwootIds.get(i).getTimestamp();
                long highest = xwootIds.get(xwootIds.size() - 1).getTimestamp();

                List<XWootId> range =
                    stateManager.getModificationsInRange(pageId, xwootIds.get(i).getTimestamp(), xwootIds.get(
                        xwootIds.size() - 1).getTimestamp());

                System.out.format("Range for %d - %d: %s\n", lowest, highest, range);

                assertEquals(xwootIds.size() - i, range.size());
            }
        }
    }

    public void testGetPreviousModification() throws Exception
    {
        System.out.format("***** testGetPreviousModification()\n");

        for (String pageId : PAGE_IDS) {
            List<XWootId> xwootIds = stateManager.getModificationsFor(pageId, false);

            Collections.sort(xwootIds, new XWootIdComparatorAscending());

            for (int i = 0; i < xwootIds.size(); i++) {
                XWootId xwootId = stateManager.getPreviousModification(xwootIds.get(i));
                if (i == 0) {
                    assertNull(xwootId);
                } else {
                    assertEquals(xwootIds.get(i - 1), xwootId);
                }
            }
        }
    }
}
