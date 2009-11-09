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

package org.xwiki.annotation.alterer;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xwiki.annotation.ContentAlterer;
import org.xwiki.annotation.internal.content.AlteredContent;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Testing HTML Content alterer.
 * 
 * @version $Id$
 */
public class HTMLContentAltererTest extends AbstractComponentTestCase
{
    /**
     * Content alterer tested by this suite.
     */
    private ContentAlterer contentAlterer;

    /**
     * Content used for tests in this suite.
     */
    private String testContent = "<a>lucien</a>dfdsf<b>sd<c>qsd</c>sq</b>";

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception
    {
        contentAlterer = getComponentManager().lookup(ContentAlterer.class, "HTML");

        super.setUp();
    }

    /**
     * Tests that the input is being removed all tags, spaces, etc.
     */
    @Test
    public void testContent()
    {
        String output = "luciendfdsfsdqsdsq";
        AlteredContent ac = contentAlterer.alter(testContent);
        assertEquals(ac.getContent(), output);
    }

    /**
     * Tests that the offsets are correctly mapped for the altered HTML input.
     */
    @Test
    public void testOffset()
    {
        /*
         * 012345678901234567890123456789012345678 <a>lucien</a>dfdsf<b>sd<c>qsd</c>sq</b> lucien dfdsf sd qsd sq 012345
         * 67890 12 345 67
         */
        Map<Integer, Integer> initialToAltered = new HashMap<Integer, Integer>();
        Map<Integer, Integer> alteredToInitial = new HashMap<Integer, Integer>();
        /*
         * 0 => 0; 1 => 0; 2 => 0
         */
        initialToAltered.put(0, 0);
        initialToAltered.put(1, 0);
        initialToAltered.put(2, 0);

        /*
         * 3 <=> 0; 4 <=> 1; 5 <=> 2; 6 <=> 3; 7 <=> 4; 8 <=> 5
         */
        initialToAltered.put(3, 0);
        alteredToInitial.put(0, 3);
        initialToAltered.put(4, 1);
        alteredToInitial.put(1, 4);
        initialToAltered.put(5, 2);
        alteredToInitial.put(2, 5);
        initialToAltered.put(6, 3);
        alteredToInitial.put(3, 6);
        initialToAltered.put(7, 4);
        alteredToInitial.put(4, 7);
        initialToAltered.put(8, 5);
        alteredToInitial.put(5, 8);

        /*
         * 9 => 6; 10 => 6; 11 => 6; 12 => 6
         */
        initialToAltered.put(9, 6);
        initialToAltered.put(10, 6);
        initialToAltered.put(11, 6);
        initialToAltered.put(12, 6);

        /*
         * 13 <=> 6; 14 <=> 7; 15 <=> 8; 16 <=> 9; 17 <=> 10
         */
        initialToAltered.put(13, 6);
        alteredToInitial.put(6, 13);
        initialToAltered.put(14, 7);
        alteredToInitial.put(7, 14);
        initialToAltered.put(15, 8);
        alteredToInitial.put(8, 15);
        initialToAltered.put(16, 9);
        alteredToInitial.put(9, 16);
        initialToAltered.put(17, 10);
        alteredToInitial.put(10, 17);

        /*
         * 18 => 11; 19 => 11; 20 => 11
         */
        initialToAltered.put(18, 11);
        initialToAltered.put(19, 11);
        initialToAltered.put(20, 11);

        /*
         * 21 <=> 11; 22 <=> 12
         */
        initialToAltered.put(21, 11);
        alteredToInitial.put(11, 21);
        initialToAltered.put(22, 12);
        alteredToInitial.put(12, 22);

        /*
         * 23 => 13; 24 => 13; 25 => 13
         */
        initialToAltered.put(23, 13);
        initialToAltered.put(24, 13);
        initialToAltered.put(25, 13);

        /*
         * 26 <=> 13; 27 <=> 14; 28 <=> 15
         */
        initialToAltered.put(26, 13);
        alteredToInitial.put(13, 26);
        initialToAltered.put(27, 14);
        alteredToInitial.put(14, 27);
        initialToAltered.put(28, 15);
        alteredToInitial.put(15, 28);

        /*
         * 29 => 16; 30 => 16; 31 => 16; 32 => 16
         */
        initialToAltered.put(29, 16);
        initialToAltered.put(30, 16);
        initialToAltered.put(31, 16);
        initialToAltered.put(32, 16);

        /*
         * 33 <=> 16; 34 <=> 17
         */
        initialToAltered.put(33, 16);
        alteredToInitial.put(16, 33);
        initialToAltered.put(34, 17);
        alteredToInitial.put(17, 34);

        /*
         * 35 => 17; 36 => 17; 37 => 17; 38 => 17
         */
        initialToAltered.put(35, 17);
        initialToAltered.put(36, 17);
        initialToAltered.put(37, 17);
        initialToAltered.put(38, 17);

        AlteredContent ac = contentAlterer.alter(testContent);
        for (int a = 0; a < testContent.length(); ++a) {
            assertEquals(initialToAltered.get(a), Integer.valueOf(ac.getAlteredOffset(a)));
        }
        for (int a = 0; a < ac.getContent().length(); ++a) {
            assertEquals(alteredToInitial.get(a), Integer.valueOf(ac.getInitialOffset(a)));
        }
    }
}
