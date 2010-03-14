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

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.wikiimporter.descriptor.WikiImporterDescriptor;
import org.xwiki.wikiimporter.listener.WikiImporterListener;
import org.xwiki.wikiimporter.type.WikiImporterType;

/**
 * WikiImporter imports wiki pages from other wikis into XWiki.
 * 
 * @version $Id$
 */
@ComponentRole
public interface WikiImporter
{

    /**
     * @return WikiImporter descriptor
     */
    WikiImporterDescriptor getDescriptor();

    /**
     * This uses default WikiImporterListener implemented for the given WikiImporter type.
     * 
     * @param paramMap the wiki-importer parameters as a map.
     * @throws WikiImporterException
     */

    void importWiki(Map<String, ? > paramMap) throws WikiImporterException;

    /**
     * Parses the document with given import parameters and fires events on custom WikiImporterListener.
     * 
     * @param object the wiki-importer parameters class object.
     * @param listener listens to the events generated during parsing/import process.
     * @throws WikiImporterException if a unexpected error happens during the import process.
     */
    void importWiki(Object object, WikiImporterListener listener) throws WikiImporterException;

    /**
     * @return the export type format of WikiImporter (eg: MediaWiki XML, Confluence XML, Wordpress XMLRPC... )
     */
    WikiImporterType getType();

}
