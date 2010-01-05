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
package org.xwiki.wikiimporter.descriptor;

import org.xwiki.properties.PropertyDescriptor;

/**
 * The default implementation of {@link WikiImporterParameterDescriptor}
 * 
 * @version $Id$
 */
public class DefaultWikiImporterParameterDescriptor implements WikiImporterParameterDescriptor
{

    /**
     * The description of the parameter.
     */
    private PropertyDescriptor propertyDescriptor;

    /**
     * @param propertyDescriptor
     */
    public DefaultWikiImporterParameterDescriptor(PropertyDescriptor propertyDescriptor)
    {
        this.propertyDescriptor = propertyDescriptor;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterParameterDescriptor#getDefaultValue()
     */
    public Object getDefaultValue()
    {
        return propertyDescriptor.getDefaultValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterParameterDescriptor#getDescription()
     */
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return propertyDescriptor.getDescription();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterParameterDescriptor#getId()
     */
    public String getId()
    {
        // TODO Auto-generated method stub
        return propertyDescriptor.getId();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterParameterDescriptor#getName()
     */
    public String getName()
    {
        // TODO Auto-generated method stub
        return propertyDescriptor.getName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterParameterDescriptor#getType()
     */
    public Class< ? > getType()
    {
        // TODO Auto-generated method stub
        return propertyDescriptor.getPropertyClass();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterParameterDescriptor#isMandatory()
     */
    public boolean isMandatory()
    {
        // TODO Auto-generated method stub
        return propertyDescriptor.isMandatory();
    }

}
