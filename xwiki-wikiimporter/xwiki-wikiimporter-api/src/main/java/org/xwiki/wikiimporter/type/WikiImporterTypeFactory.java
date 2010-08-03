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
package org.xwiki.wikiimporter.type;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.wikiimporter.importer.WikiImporterException;

/**
 * WikiImporter factory component exposes the information about supported wikis and dataformats
 * 
 * @version $Id$
 */
@ComponentRole
public interface WikiImporterTypeFactory
{

    /**
     * Create a specific WikiImporter type with given Id as String. (eg : "mediawiki/xml", "wordpress/xmlrpc" .. )
     * 
     * @param wikiImporterType Type id as String.
     * @return WikiImporterType
     */
    WikiImporterType createTypeFromIdString(String wikiImporterType) throws WikiImporterException;

    /**
     * @return list of available/supported wikis and dataformats
     */
    List<WikiImporterType> getAvailableTypes() throws WikiImporterException;

}
