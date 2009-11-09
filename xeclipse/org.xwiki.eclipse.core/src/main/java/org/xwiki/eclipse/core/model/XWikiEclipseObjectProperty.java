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
package org.xwiki.eclipse.core.model;

import org.eclipse.core.runtime.Assert;

/**
 * This class is an utility class used for encapsulating and accessing the information concerning a property in a more
 * usable way (in this way properties can be compactly passed around using a single reference)
 */
public class XWikiEclipseObjectProperty extends ModelObject
{
    private XWikiEclipseObject object;

    private String propertyName;

    /**
     * Constructor.
     * 
     * @param object The XWiki object containing the actual property.
     * @param propertyName The name of the property.
     */
    public XWikiEclipseObjectProperty(XWikiEclipseObject object, String propertyName)
    {
        super(object.getDataManager());

        Assert.isNotNull(object);
        this.object = object;

        Assert.isNotNull(propertyName);
        this.propertyName = propertyName;
    }

    /**
     * @return The XWiki object the property belongs to.
     */
    public XWikiEclipseObject getObject()
    {
        return object;
    }

    public String getName()
    {
        return propertyName;
    }

    public String getPrettyName()
    {
        if (getAttribute("prettyName") != null) {
            return getAttribute("prettyName");
        }

        return getName();
    }

    /**
     * @return The value associated to the property.
     */
    public Object getValue()
    {
        return object.getData().getProperty(propertyName);
    }

    /**
     * @param value The value to be associated to the property.
     */
    public void setValue(Object value)
    {
        object.getData().setProperty(propertyName, value);
    }

    /**
     * This method allows the user to access to the attributes defined for the property. For example "unmodifiable",
     * "prettyName", etc. Attributes are defined in the XWiki class of the object the property belongs to.
     * 
     * @param attributeName
     * @return The value for the attribute.
     */
    public String getAttribute(String attributeName)
    {
        Object attribute = object.getXWikiClass().getPropertyAttribute(propertyName, attributeName);

        if (attribute != null) {
            return attribute.toString();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXWikiEclipseId()
    {
        return String
            .format(
                "xwikieclipse://%s/%s/object/%s/%d/%s", getDataManager().getName(), object.getData().getPageId(), object.getData().getClass(), object.getData().getId(), propertyName); //$NON-NLS-1$        
    }
}
