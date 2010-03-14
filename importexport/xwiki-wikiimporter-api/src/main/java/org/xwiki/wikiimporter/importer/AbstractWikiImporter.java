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
package org.xwiki.wikiimporter.importer;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.properties.BeanManager;
import org.xwiki.wikiimporter.descriptor.DefaultWikiImporterDescriptor;
import org.xwiki.wikiimporter.descriptor.WikiImporterDescriptor;

/**
 * Abstract Implementation of WikiImporter. Various export types should extend this class.
 * 
 * @version $Id$
 */
public abstract class AbstractWikiImporter extends AbstractLogEnabled implements WikiImporter, Initializable
{

    /**
     * The {@link BeanManager} component.
     */
    @Requirement
    protected BeanManager beanManager;

    /**
     * The human-readable wiki importer type (eg "MediaWiki XML" or "Wordpress XMLRPC").
     */
    private String name;

    /**
     * WikiImporter description used to generate the WikiImporter descriptor.
     */
    private String description;

    /**
     * The descriptor of the WikiImporter
     */
    private WikiImporterDescriptor descriptor;

    /**
     * Parameter bean class used to generate the WikiImporter descriptor.
     */
    private Class< ? > parametersBeanClass;

    /**
     * @param name The human-readable wiki importer name
     * @param description WikiImporter description
     * @param parameterBeanClass Parameter bean class used to generate the WikiImporter descriptor
     */

    public AbstractWikiImporter(String name, String description, Class< ? > parameterBeanClass)
    {
        this.name = name;
        this.description = description;
        this.parametersBeanClass = parameterBeanClass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.importer.WikiImporter#getDescriptor()
     */
    public WikiImporterDescriptor getDescriptor()
    {
        return this.descriptor;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        // Initialise WikiImporter Descriptor.
        DefaultWikiImporterDescriptor descriptor =
            new DefaultWikiImporterDescriptor(name, description, beanManager.getBeanDescriptor(parametersBeanClass));

        setDescriptor(descriptor);
    }

    /**
     * @param descriptor the descriptor to set
     */
    public void setDescriptor(WikiImporterDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

}
