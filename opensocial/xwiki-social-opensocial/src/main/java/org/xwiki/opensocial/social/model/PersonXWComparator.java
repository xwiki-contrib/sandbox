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

import java.util.Comparator;

import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Person.Field;

public class PersonXWComparator implements Comparator<Person>
{
    private Person.Field byField;

    private SortOrder sortOrder;

    public PersonXWComparator(Field byField, SortOrder sortOrder)
    {
        this.byField = byField;
        this.sortOrder = sortOrder;
    }

    @SuppressWarnings("unchecked")
    public int compare(Person o1, Person o2)
    {
        Comparable value1 = ((PersonXW) o1).fetchPropertyByField(byField);
        Comparable value2 = ((PersonXW) o2).fetchPropertyByField(byField);
        int result = (value1 == null) ? ((value2 == null) ? 0 : 1) : (value2 == null) ? -1 : value1.compareTo(value2);
        return SortOrder.descending.equals(sortOrder) ? -result : result;
    }
}
