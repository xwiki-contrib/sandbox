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
package org.xwoot.thomasRuleEngine.mock;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xwoot.thomasRuleEngine.core.Value;

/**
 * Mockup for the Value interface.
 * 
 * @version $Id$
 */
public class MockValue implements Value
{
    /** SerialVesrionUID for serialized object. */
    private static final long serialVersionUID = 5957593391711730737L;

    /**
     * @see #get()
     */
    private String value;

    /**
     * Creates a new MockValue object.
     * 
     * @param value the value.
     */
    public MockValue(String value)
    {
        this.value = value;
    }

    /** {@inheritDoc} */
    public Serializable get()
    {
        return this.value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param value DOCUMENT ME!
     */
    public void set(String value)
    {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "MockValue:" + this.value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Value)) {
            return false;
        }

        final MockValue other = (MockValue) obj;

        return new EqualsBuilder().append(this.value, other.value).isEquals();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.value).hashCode();
    }
}
