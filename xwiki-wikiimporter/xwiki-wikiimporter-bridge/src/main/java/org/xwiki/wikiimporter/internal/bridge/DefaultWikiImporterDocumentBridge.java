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
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge;
import org.xwiki.wikiimporter.importer.WikiImportParameters;
import org.xwiki.wikiimporter.importer.WikiImporterException;
import org.xwiki.wikiimporter.internal.importer.WikiImporterLogger;
import org.xwiki.wikiimporter.wiki.Attachment;
import org.xwiki.wikiimporter.wiki.WikiPage;
import org.xwiki.wikiimporter.wiki.WikiPageRevision;

/**
 * Default Implementation for WikiImporterDocumentBridge.
 * 
 * @version $Id$
 */
@Component
public class DefaultWikiImporterDocumentBridge extends AbstractLogEnabled implements WikiImporterDocumentBridge
{
    @Requirement
    private DocumentAccessBridge docAccessBridge;

    @Requirement
    private WikiImporterLogger logger;

    @Requirement("xwiki/2.0")
    private BlockRenderer renderer;

    @Requirement
    private EntityReferenceSerializer<String> serializer;

    @Requirement
    private DocumentReferenceResolver<String> resolver;

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
     * @see org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge#addWikiPage(org.xwiki.wikiimporter.wiki.WikiPage,
     *      WikiImportParameters)
     */
    public void addWikiPage(WikiPage page, WikiImportParameters parameters) throws WikiImporterException
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

            this.logger.error("Page Skipped - " + error, true);

            return;
        }

        String pageWiki = page.getWiki();
        if (StringUtils.isNotEmpty(parameters.getTargetWiki())) {
            pageWiki = parameters.getTargetWiki();
        } else if (StringUtils.isEmpty(pageWiki)) {
            pageWiki = this.docAccessBridge.getCurrentWiki();
        }

        String pageSpace = page.getSpace();
        if (StringUtils.isNotEmpty(parameters.getTargetSpace())) {
            pageSpace = parameters.getTargetSpace();
        } else if (StringUtils.isEmpty(pageSpace)) {
            pageSpace = "Main";
        }

        // Set Document Properties
        DocumentReference documentReference = new DocumentReference(pageWiki, pageSpace, page.getName());
        String stringReference = this.serializer.serialize(documentReference);

        try {
            // Attachments
            for (Attachment attachment : page.getAttachments()) {
                AttachmentReference attachmentRef =
                    new AttachmentReference(attachment.getFileName(), documentReference);
                this.docAccessBridge.setAttachmentContent(attachmentRef, attachment.getContent());
            }

            if (parameters.getPreserveHistory()) {
                // For each wiki page revision render xdom and set page content.
                for (WikiPageRevision revision : page.getRevisions()) {
                    addRevision(documentReference, revision);
                }
            } else {
                addRevision(documentReference, page.getLastRevision());
            }
        } catch (Exception e) {
            getLogger().error("Error while creating the sucessfully parsed page.", e);
            throw new WikiImporterException("Error while creating the sucessfully parsed page.", e);
        }

        // On successful page creation
        this.logger.info(
            "Page Created ->  <a href=\"" + this.docAccessBridge.getDocumentURL(documentReference, "view", "", "")
                + "\">" + stringReference + "</a>", true);
    }

    private void addRevision(DocumentReference documentReference, WikiPageRevision revision) throws Exception
    {
        String stringReference = this.serializer.serialize(documentReference);

        // Document Content
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.renderer.render(revision.getContent(), printer);
        this.docAccessBridge.setDocumentContent(documentReference, printer.toString(), revision.getComment(),
            revision.isMinorEdit());

        // Document Title
        if (revision.getTitle() != null) {
            this.docAccessBridge.setDocumentTitle(documentReference, revision.getTitle());
        }

        // Document Parent
        if (revision.getParent() != null) {
            DocumentReference parentReference = this.resolver.resolve(revision.getParent(), documentReference);
            this.docAccessBridge.setDocumentParentReference(documentReference, parentReference);
        }

        // Document Tags
        if (revision.getTags() != null && !revision.getTags().isEmpty()) {
            StringBuilder tagString = new StringBuilder();
            for (String tag : revision.getTags()) {
                tagString.append(tag + "|");
            }
            this.docAccessBridge.setProperty(stringReference, "XWiki.TagClass", "tags", tagString.toString());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge#saveLog(org.xwiki.model.reference.DocumentReference,
     *      java.lang.String)
     */
    public void log(String log) throws WikiImporterException
    {
        try {
            DocumentReference logDocReference = new DocumentReference("xwiki", "WikiImporter", "WikiImporterLog");
            this.docAccessBridge.setDocumentContent(logDocReference, log, "Log Page Created", true);
        } catch (Exception e) {
            throw new WikiImporterException("Error while creating the wiki importer log page", e);
        }
    }
}
