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

import static org.junit.Assert.*;

import org.junit.Test;
import org.xwoot.xwootApp.core.tre.PageFieldValue;

public class MDValueTest
{

    @Test
    public void testEqualsAndHashCodeObject()
    {

        // initial field
        PageFieldValue v = new PageFieldValue("Alien");
        // field with no value
        PageFieldValue v0 = new PageFieldValue(null);
        // v1.value!=v.value
        PageFieldValue v1 = new PageFieldValue("Predator");
        // v2==v
        PageFieldValue v2 = new PageFieldValue("Alien");
        // v3==v
        PageFieldValue v3 = new PageFieldValue("Alien");
        // v4==v
        PageFieldValue v4 = v;

        // reflexive : x==x
        assertTrue(v.equals(v));

        // null : value x!=null
        assertFalse(v.equals(null));
        // deep null : v0.notnull!=v.null
        assertFalse(v0.equals(v));
        // Class : cat!=dog
        assertFalse(v.equals("yopla"));
        // normal :v.value!=v1.value
        assertFalse(v.equals(v1));
        // symmetric
        assertTrue(v.equals(v2));
        assertTrue(v2.equals(v));
        // transitive
        assertTrue(v.equals(v3));
        assertTrue(v2.equals(v3));
        // consistent
        assertTrue(v4.equals(v3));

        // hashCode
        assertEquals(v.hashCode(), v.hashCode());
        assertEquals(v.hashCode(), v2.hashCode());
        assertEquals(v.hashCode(), v3.hashCode());
        assertEquals(v4.hashCode(), v3.hashCode());
        assertEquals(v2.hashCode(), v3.hashCode());

    }

    @Test
    public void testGet()
    {
        PageFieldValue v = new PageFieldValue("Alien");

        assertEquals("Alien", v.get());
    }

    @Test
    public void testToString()
    {
        PageFieldValue v = new PageFieldValue("Predator");

        assertEquals("Predator", v.toString());
    }

}
