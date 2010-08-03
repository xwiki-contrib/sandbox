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
package org.xwiki.wikiimporter.bridge;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.wikiimporter.importer.WikiImporterException;
import org.xwiki.wikiimporter.wiki.WikiPage;

/**
 * WikiImporterDocumentBridge acts as a bridge between WikiImporter and XWiki. Mainly used to create parsed wiki pages
 * from other wiki's export dump.
 * 
 * @version $Id$
 */
@ComponentRole
public interface WikiImporterDocumentBridge
{
    /**
     * @return instance of DocumentAcessBridge.
     */
    DocumentAccessBridge getDocAccessBridge();

    /**
     * Creates a Wiki Page inside xwiki, includes page content, parent, attachments and tags creation.
     * 
     * @param page Wiki Page object to be created inside xwiki.
     * @throws WikiImporterException if DocumentAcessBridge throws any error while creating the WikiPage.
     */
    void createWikiPage(WikiPage page) throws WikiImporterException;

    /**
     * Create the log page if not existing and set the log content of the import process.
     * 
     * @param logPageName
     * @param log Log content as String.
     * @throws WikiImporterException if DocumentAcessBridge throws any error while creating the given log page.
     */
    void createLogPage(String logPageName, String log) throws WikiImporterException;
}
