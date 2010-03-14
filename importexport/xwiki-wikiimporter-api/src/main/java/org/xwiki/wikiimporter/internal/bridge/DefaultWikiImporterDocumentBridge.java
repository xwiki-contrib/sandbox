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
package org.xwiki.wikiimporter.internal.bridge;

import org.apache.commons.lang.StringUtils;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge;
import org.xwiki.wikiimporter.importer.WikiImporterException;
import org.xwiki.wikiimporter.internal.importer.WikiImporterLogger;
import org.xwiki.wikiimporter.wiki.WikiPage;
import org.xwiki.wikiimporter.wiki.WikiPageRevision;
import org.xwiki.wikiimporter.wiki.Attachment;

/**
 * Default Implementation for WikiImporterDocumentBridge.
 * 
 * @version $Id$
 */
@Component
public class DefaultWikiImporterDocumentBridge implements WikiImporterDocumentBridge
{
    @Requirement
    private DocumentAccessBridge docAccessBridge;

    private WikiPrinter printer;

    @Requirement
    private ComponentManager componentManager;

    private WikiImporterLogger logger = WikiImporterLogger.getLogger();

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge#getDocAccessBridge()
     */
    public DocumentAccessBridge getDocAccessBridge()
    {
        return docAccessBridge;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge#createWikiPage(org.xwiki.wikiimporter.wiki.WikiPage)
     */
    public void createWikiPage(WikiPage page) throws WikiImporterException
    {

        // Skip creating pages if page name or space is null.
        // TODO Skip pages . CSS Category JSX and stuff.
        if (StringUtils.isBlank(page.getName()) || StringUtils.isBlank(page.getSpace())
            || "Category".equalsIgnoreCase(page.getSpace())) {

            String error = "";

            if (StringUtils.isBlank(page.getName())) {
                error = "Page cant be created with the given name ";
            } else if (StringUtils.isBlank(page.getSpace())) {
                error = "Page cant be created with the given space ";
            } else if ("Category".equalsIgnoreCase(page.getSpace())) {
                error = " Given page is a Category page.";
            }

            logger.info("Page Skipped - " + error, true, WikiImporterLogger.ERROR);
            return;
        }

        printer = new DefaultWikiPrinter();

        // Set Document Properties
        DocumentReference reference = new DocumentReference(page.getWiki(), page.getSpace(), page.getName());

        try {
            docAccessBridge.setProperty(page.getSpace() + "." + page.getName(), "XWiki.TagClass", "tags", page
                .getTagsAsString());

            // Document Parent.
            String parentPageName = page.getParent() != null ? page.getParent() : "WebHome";

            DocumentReference parentReference = new DocumentReference(page.getWiki(), page.getSpace(), parentPageName);
            if (!docAccessBridge.exists(parentReference)) {
                docAccessBridge.setDocumentContent(parentReference, "Add Content to the page",
                    "Created for the first time.", false);
            }
            docAccessBridge.setDocumentParentReference(reference, parentReference);

            // Document Title.
            docAccessBridge.setDocumentTitle(reference, page.getTitle());

            // Attachments
            for (Attachment attachment : page.getAttachments()) {
                AttachmentReference attachmentRef = new AttachmentReference(attachment.getFileName(), reference);
                docAccessBridge.setAttachmentContent(attachmentRef, attachment.getContent());
            }

            // For each wiki page revision render xdom and set page content.
            for (WikiPageRevision revision : page.getRevisions()) {
                // Render the XDOM to XWiki 2.0 Syntax.
                BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, Syntax.XWIKI_2_0.toIdString());
                renderer.render(revision.getContent(), printer);

                docAccessBridge.setDocumentContent(reference, printer.toString(), revision.getComment(), revision
                    .isMinorEdit());
            }

        } catch (Exception e) {
            throw new WikiImporterException("Error while creating the sucessfully parsed page.", e);
        }

        // On successful page creation
        String pageLink = page.getSpace() + "." + page.getName();
        logger.info("Page Created ->  <a href=\"$xwiki.getDocument('" + pageLink + "').getExternalURL()>" + pageLink
            + "</a>", true, WikiImporterLogger.INFO);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge#createLogPage(java.lang.String, java.lang.String)
     */
    public void createLogPage(String logPageName, String log) throws WikiImporterException
    {
        try {
            DocumentReference logDocReference = new DocumentReference("xwiki", "WikiImporter", "WikiImporterLog");
            docAccessBridge.setDocumentContent(logDocReference, log, "Log Page Created", true);
        } catch (Exception e) {
            throw new WikiImporterException("Error while creating the wiki importer log page", e);
        }
    }

}
