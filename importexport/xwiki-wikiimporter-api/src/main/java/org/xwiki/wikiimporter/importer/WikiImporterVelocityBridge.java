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

import org.xwiki.component.logging.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.wikiimporter.type.WikiImporterType;
import org.xwiki.wikiimporter.type.WikiImporterTypeFactory;

/**
 * A bridge between Velocity and Wiki Importer.
 * 
 * @version $Id$
 */
public class WikiImporterVelocityBridge
{
    private WikiImporter wikiImporter;

    /**
     * Wiki Importer factory can be used to query supported wikis and export types.
     */
    private WikiImporterTypeFactory wikiImporterTypeFactory;

    /**
     * XWiki component manager.
     */
    private ComponentManager componentManager;

    private Logger logger;

    public WikiImporterVelocityBridge(ComponentManager componentManager, Logger logger) throws WikiImporterException
    {
        try {
            this.logger = logger;
            this.componentManager = componentManager;
            this.wikiImporterTypeFactory = componentManager.lookup(WikiImporterTypeFactory.class);
        } catch (Exception e) {
            throw new WikiImporterException("Error while initializing wiki importer velocity bridge.", e);
        }
    }

    /**
     * @return {@link WikiImporterTypeFactory}
     */
    public WikiImporterTypeFactory getWikiImporterTypeFactory()
    {
        return wikiImporterTypeFactory;
    }

    /**
     * Returns the Wiki Importer based on WikiImporter type id (eg: mediawiki/xml, confluence/xml .. )
     * 
     * @param wikiImporterType WikiImporter type id as String
     * @return WikiImporter object
     * @throws WikiImporterException if WikiImporter of given type is not supported or found.
     */
    public WikiImporter getWikiImporter(String wikiImporterType) throws WikiImporterException
    {

        try {
            WikiImporterType type = wikiImporterTypeFactory.createTypeFromIdString(wikiImporterType);
            wikiImporter = componentManager.lookup(WikiImporter.class, type.toIdString());
        } catch (Exception e) {
            logger.error("Unable to create wiki importer, Unsupported wiki importer type");
            throw new WikiImporterException("Unable to create wiki importer, Unsupported wiki importer type", e);
        }
        return wikiImporter;
    }

}
