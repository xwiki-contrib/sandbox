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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.properties.BeanDescriptor;
import org.xwiki.properties.PropertyDescriptor;

/**
 * Describe a WikiImporter with no parameters.
 * 
 * @version $Id$
 */
public class DefaultWikiImporterDescriptor implements WikiImporterDescriptor
{
    /**
     * The description of WikiImporter
     */
    private String description;

    /**
     * The name of the WikiImporter
     */
    private String name;

    /**
     * The description of the parameters bean.
     */
    private BeanDescriptor parametersBeanDescriptor;

    /**
     * A map containing the {@link WikiImporterParameterDescriptor} for each parameters supported for this WikiImporter.
     */
    private Map<String, WikiImporterParameterDescriptor> parameterDescriptorMap =
        new LinkedHashMap<String, WikiImporterParameterDescriptor>();

    /**
     * @param name Name of the wiki importer (eg: MediaWiki XML, Confluence XML..)
     * @param description the description of the wiki importer.
     * @param parameterBeanClass the description of the parameters bean or null if there are no parameters
     */
    public DefaultWikiImporterDescriptor(String name, String description, BeanDescriptor beanDescriptor)
    {
        this.description = description;
        this.name = name;
        this.parametersBeanDescriptor = beanDescriptor;
        extractParameterDescriptorMap();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterDescriptor#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterDescriptor#getName()
     */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterDescriptor#getParametersBeanClass()
     */
    public Class< ? > getParametersBeanClass()
    {
        return (null != this.parametersBeanDescriptor) ? this.parametersBeanDescriptor.getBeanClass() : Object.class;
    }

    /**
     * Extract parameters informations from {@link #parametersBeanDescriptor} and insert it in
     * {@link #parameterDescriptorMap}.
     */
    protected void extractParameterDescriptorMap()
    {
        for (PropertyDescriptor propertyDescriptor : parametersBeanDescriptor.getProperties()) {
            DefaultWikiImporterParameterDescriptor desc =
                new DefaultWikiImporterParameterDescriptor(propertyDescriptor);
            this.parameterDescriptorMap.put(desc.getId().toLowerCase(), desc);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.descriptor.WikiImporterDescriptor#getParameterDescriptorMap()
     */
    public Map<String, WikiImporterParameterDescriptor> getParameterDescriptorMap()
    {
        return (null != parametersBeanDescriptor) ? Collections.unmodifiableMap(this.parameterDescriptorMap)
            : Collections.<String, WikiImporterParameterDescriptor> emptyMap();
    }
}
