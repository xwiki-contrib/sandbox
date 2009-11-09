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
package org.xwiki.opensocial.social.model;

import org.apache.shindig.common.JsonSerializer;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;

import junit.framework.TestCase;

public class PersonXWTest extends TestCase
{
    /**
     * @throws Exception
     */
    public void testJsonSerializerAppendPojo() throws Exception
    {
        Person person = new PersonXW();
        person.setId("XWiki.Alice");
        Name aname = new NameXW();
        aname.setGivenName("Alice");
        person.setName(aname);
        person.setAboutMe("This is me!");
        String expected = "{\"id\":\"XWiki.Alice\",\"aboutMe\":\"This is me!\",\"name\":{\"givenName\":\"Alice\"}}";

        StringBuffer actual = new StringBuffer();
        JsonSerializer.appendPojo(actual, person);

        assertEquals(expected, actual.toString());
    }
}
