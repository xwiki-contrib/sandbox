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

import java.io.Serializable;

/**
 * This class represents an field of an XWoot object.
 * 
 * @version $Id$
 */
public class XWootObjectField implements Serializable
{
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 3230723710897699107L;

    /**
     * True is the field should be handled by XWoot.
     */
    private boolean wootable;

    /**
     * Field name.
     */
    private String name;

    /**
     * Field value. If wootable is true then this field has to be a String.
     */
    private Serializable value;

    /**
     * This is the field's original type. This is used to keep track of the real type that must be reflected when
     * converting back the XWoot object this field belongs to, to an XWikiObject. In particular this is used to
     * understand when a List is converted to a String because it is declared wootable (e.g., tags).
     */
    private Class originalType;

    public XWootObjectField(String name, Serializable value, boolean wootable)
    {
        if (wootable) {
            if (!value.getClass().equals(String.class)) {
                throw new IllegalArgumentException("Wootable fields must have String values");
            }
        }

        this.name = name;
        this.value = value;
        if (value != null) {
            this.originalType = value.getClass();
        } else {
            this.originalType = null;
        }
        this.wootable = wootable;
    }

    /**
     * Constructor
     * 
     * @param name The field's name.
     * @param value The field's value.
     * @param originalType Original field's type. This is useful for keeping track of "converted" fields, such as
     *            wootable lists that are converted to String.
     * @param wootable Wootable flag.
     */
    public XWootObjectField(String name, Serializable value, Class originalType, boolean wootable)
    {
        if (wootable) {
            if (!value.getClass().equals(String.class)) {
                throw new IllegalArgumentException("Wootable fields must have String values");
            }
        }

        this.name = name;
        this.value = value;
        this.originalType = originalType;
        this.wootable = wootable;
    }

    public Serializable getValue()
    {
        return value;
    }

    public void setValue(Serializable value)
    {
        if (!value.getClass().equals(this.value.getClass())) {
            throw new IllegalArgumentException(String.format("Invalid type for %s. Expected %s, got %s", name,
                this.value.getClass(), value.getClass()));
        }
        this.value = value;
    }

    public boolean isWootable()
    {
        return wootable;
    }

    public String getName()
    {
        return name;
    }

    public Class getOriginalType()
    {
        return originalType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XWootObjectField))
            return false;
        XWootObjectField other = (XWootObjectField) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
