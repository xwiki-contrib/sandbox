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
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.PageFieldValue;

public class FieldsTREIdentifierTest
{

    @Test
    public void testFieldsTREIdentifier()
    {
        XWootObjectIdentifier f = new XWootObjectIdentifier("test.0.content");
        assertEquals("test.0.content", f.getId());
        f.setId("test.2.content");
        assertEquals("test.2.content", f.getId());
        f.setId("test.2.Comment");
        assertEquals("test.2.Comment", f.getId());
    }

    @Test
    public void testToString()
    {
        XWootObjectIdentifier f0 = new XWootObjectIdentifier("test.0.content");
        assertEquals("test.0.content", f0.toString());
    }

    @Test
    public void testEqualsAndHashCodeObject()
    {
        XWootObjectIdentifier f0 = new XWootObjectIdentifier("test.0.content");
        XWootObjectIdentifier f1 = new XWootObjectIdentifier("test.1.content");
        XWootObjectIdentifier f2 = new XWootObjectIdentifier("test.1.content");
        XWootObjectIdentifier f3 = new XWootObjectIdentifier("test.1.content");
        XWootObjectIdentifier f4 = f3;

        PageFieldValue v = new PageFieldValue("tagada");

        // reflexive : x==x
        assertTrue(f1.equals(f1));

        // null : value x!=null
        assertFalse(f1.equals(null));

        // Class : cat!=dog
        assertFalse(f1.equals(v));

        // normal :v.value!=v1.value
        assertFalse(f1.equals(f0));
        // symmetric
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));
        // transitive
        assertTrue(f1.equals(f3));
        assertTrue(f2.equals(f3));
        // consistent
        assertTrue(f4.equals(f3));

        // hashCode
        assertEquals(f1.hashCode(), f1.hashCode());
        assertEquals(f1.hashCode(), f2.hashCode());
        assertEquals(f1.hashCode(), f3.hashCode());
        assertEquals(f4.hashCode(), f3.hashCode());
        assertEquals(f2.hashCode(), f3.hashCode());

    }

}
