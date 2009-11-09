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
 *
 */
package org.xwiki.opensocial.social.spi;

import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.xwiki.opensocial.social.model.NameXW;
import org.xwiki.opensocial.social.model.PersonXW;
import org.xwiki.opensocial.social.model.PersonXWComparator;

import junit.framework.TestCase;

public class PersonXWComparatorTest extends TestCase
{
    /**
     * @throws Exception
     */
    public void testCompareTo() throws Exception
    {
        Person alice = new PersonXW();
        alice.setId("XWiki.Alice");
        Name aname = new NameXW();
        aname.setGivenName("Alice");
        alice.setName(aname);
        alice.setAboutMe("xyz");

        Person bob = new PersonXW();
        bob.setId("XWiki.Bob");
        Name bname = new NameXW();
        bname.setGivenName("Bob");
        bob.setName(bname);
        bob.setAboutMe("abc");

        PersonXWComparator pcmpAsc = new PersonXWComparator(Person.Field.ABOUT_ME, SortOrder.ascending);
        assertTrue(pcmpAsc.compare(alice, bob) > 0);
        assertTrue(pcmpAsc.compare(bob, alice) < 0);
        assertTrue(pcmpAsc.compare(alice, alice) == 0);

        PersonXWComparator pcmpDesc = new PersonXWComparator(Person.Field.ABOUT_ME, SortOrder.descending);
        assertTrue(pcmpDesc.compare(alice, bob) < 0);
        assertTrue(pcmpDesc.compare(bob, alice) > 0);
        assertTrue(pcmpDesc.compare(alice, alice) == 0);

        PersonXWComparator pcmpNameAsc = new PersonXWComparator(Person.Field.NAME, SortOrder.ascending);
        assertTrue(pcmpNameAsc.compare(alice, bob) < 0);
        assertTrue(pcmpNameAsc.compare(bob, alice) > 0);
        assertTrue(pcmpNameAsc.compare(alice, alice) == 0);

        PersonXWComparator pcmpNameDesc = new PersonXWComparator(Person.Field.NAME, SortOrder.descending);
        assertTrue(pcmpNameDesc.compare(alice, bob) > 0);
        assertTrue(pcmpNameDesc.compare(bob, alice) < 0);
        assertTrue(pcmpNameDesc.compare(alice, alice) == 0);
    }
}
