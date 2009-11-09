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

import org.xwoot.contentprovider.XWootObject;
import org.xwoot.contentprovider.XWootObjectField;
import org.xwoot.thomasRuleEngine.core.Value;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWootObjectValue implements Value
{
    /**  */
    private static final long serialVersionUID = 3460912988689095045L;

    private Serializable value;

    /**
     * Creates a new TagValue object.
     * 
     * @param value DOCUMENT ME!
     */
    public XWootObjectValue()
    {
        // void
    }

    public void setObject(Serializable value)
    {
        this.value = value;
    }

    public boolean setObjectField(XWootObjectField value)
    {
        if (((XWootObject) this.value).getFieldValue(value.getName()) == null) {
            return false;
        }
        ((XWootObject) this.value).setFieldValue(value.getName(), value.getValue());
        return true;
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

        final XWootObjectValue other = (XWootObjectValue) obj;

        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!(this.value.equals(other.value))) {
            return false;
        }

        return true;
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
        return this.value.toString();
    }

    public Serializable get()
    {
        return this.value;
    }
}
