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
package org.xwoot.xwootApp.core.tre;

import java.io.Serializable;

import org.xwoot.thomasRuleEngine.core.Value;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PageFieldValue implements Value
{
    /**  */
    private static final long serialVersionUID = 3460912988689095045L;

    String value;

    /**
     * Creates a new MDValue object.
     * 
     * @param value DOCUMENT ME!
     */
    public PageFieldValue(String value)
    {
        this.value = value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param obj DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final PageFieldValue other = (PageFieldValue) obj;

        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else if ((other.value != null) && !(this.value.compareTo(other.value) == 0)) {
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Serializable get()
    {
        return this.value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.value == null) ? 0 : this.value.hashCode());

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return this.value;
    }
}
