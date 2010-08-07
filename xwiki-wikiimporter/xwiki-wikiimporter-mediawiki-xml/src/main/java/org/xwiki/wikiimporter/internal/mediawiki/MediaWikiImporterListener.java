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
package org.xwiki.wikiimporter.internal.mediawiki;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.QuotationBlock;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.listener.Link;
import org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge;
import org.xwiki.wikiimporter.internal.importer.WikiImporterLogger;
import org.xwiki.wikiimporter.internal.mediawiki.wiki.MediaWikiAttachment;
import org.xwiki.wikiimporter.internal.mediawiki.wiki.MediaWikiPage;
import org.xwiki.wikiimporter.internal.mediawiki.wiki.MediaWikiPageRevision;
import org.xwiki.wikiimporter.listener.AbstractWikiImporterListenerXDOM;

/**
 * Contains callback events called when a document to be imported has been parsed by MediWiki XML Parser
 * 
 * @version $Id$
 */
public class MediaWikiImporterListener extends AbstractWikiImporterListenerXDOM
{
    private MediaWikiPage currentPage;

    private MediaWikiPageRevision currentPageRevision;

    private MediaWikiImportParameters importParams;

    private List<String> attachments = new ArrayList<String>();

    private int macroErrors;

    private WikiImporterLogger logger;

    private WikiImporterDocumentBridge docBridge;

    public MediaWikiImporterListener(ComponentManager componentManager, MediaWikiImportParameters params)
        throws ComponentLookupException
    {
        this.logger = componentManager.lookup(WikiImporterLogger.class);
        this.docBridge = componentManager.lookup(WikiImporterDocumentBridge.class);
        this.importParams = params;
    }

    private void newXDOMGeneratorListener()
    {
        setWrappedListener(new XDOMGeneratorListener()
        {
            public Stack<Block> getStack()
            {
                try {
                    Field field = getClass().getDeclaredField("stack");
                    field.setAccessible(true);

                    return (Stack<Block>) field.get(this);
                } catch (Exception e) {

                }

                return null;
            }
        });
    }

    private XDOMGeneratorListener getXDOMGeneratorListener()
    {
        return (XDOMGeneratorListener) getWrappedListener();
    }

    private Stack<Block> getStack()
    {
        try {
            Field field = XDOMGeneratorListener.class.getDeclaredField("stack");
            field.setAccessible(true);

            return (Stack<Block>) field.get(getXDOMGeneratorListener());
        } catch (Exception e) {

        }

        return null;
    }

    private List<Block> generateListFromStack()
    {
        try {
            Method method = XDOMGeneratorListener.class.getDeclaredMethod("generateListFromStack");
            method.setAccessible(true);

            return (List<Block>) method.invoke(getXDOMGeneratorListener());
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginAttachment(java.lang.String)
     */
    public void beginAttachment(String attachmentName)
    {
        this.currentPage.addAttachment(new MediaWikiAttachment(this.importParams.getAttachmentSrcPath(),
            attachmentName, this.importParams.getAttachmentExcludeDirs(), this.logger));
        endAttachment();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginObject(java.lang.String)
     */
    public void beginObject(String objectType)
    {
        // TODO
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginWikiPage()
     */
    public void beginWikiPage()
    {
        // Begin Page Flags.
        this.currentPage = new MediaWikiPage(this.importParams.getDefaultSpace());
        this.currentPageRevision = new MediaWikiPageRevision();
        this.currentPage.addRevision(this.currentPageRevision);
        this.logger.nextPage();
        newXDOMGeneratorListener();
        this.attachments.clear();
        this.macroErrors = 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginWikiPageRevision()
     */
    public void beginWikiPageRevision()
    {
        if (this.currentPageRevision == null) {
            this.currentPageRevision = new MediaWikiPageRevision(this.currentPage.getLastRevision());
            this.currentPage.addRevision(this.currentPageRevision);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endAttachment()
     */
    public void endAttachment()
    {
        // TODO
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endObject(java.lang.String)
     */
    public void endObject(String objectType)
    {
        // TODO
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endWikiPage()
     */
    public void endWikiPage()
    {
        // Logging - set original page title for reference.
        this.logger.getPageLog().setLog(this.currentPage.getLastRevision().getTitle());
        if (this.macroErrors > 0) {
            this.logger.warn("Total Macro Errors reported on this page :" + macroErrors, true);
        }

        // attachments
        if (this.currentPage.getAttachments().size() > 0) {
            this.logger.info("Total Attachments encountered :" + attachments.size(), true);
        }

        try {
            // Save the Wiki Page.
            this.docBridge.addWikiPage(this.currentPage, this.importParams);
        } catch (Exception e) {
            this.logger.error("Failed to create the page: " + e.getMessage(), true);
            // Do nothing.
        }

        this.currentPage = null;
        this.currentPageRevision = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endWikiPageRevision()
     */
    public void endWikiPageRevision()
    {
        this.currentPageRevision.setContent(getXDOMGeneratorListener().getXDOM());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#onAttachmentRevision(java.lang.String,
     *      org.xml.sax.InputSource)
     */
    public void onAttachmentRevision(String attachmentName, InputSource input)
    {
        // TODO
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#onProperty(java.lang.String, java.lang.String)
     */
    public void onProperty(String property, String value)
    {
        if (property.equals(MediaWikiConstants.PAGE_TITLE_TAG)) {
            this.currentPageRevision.setTitle(value);
        } else if (property.equals(MediaWikiConstants.AUTHOR_TAG)) {
            this.currentPageRevision.setAuthor(value);
        } else if (property.equals(MediaWikiConstants.COMMENT_TAG)) {
            this.currentPageRevision.setComment(value);
        } else if (property.equals(MediaWikiConstants.VERSION_TAG)) {
            this.currentPageRevision.setVersion(value);
        } else if (property.equals(MediaWikiConstants.IS_MINOR_TAG)) {
            this.currentPageRevision.setMinorEdit(Boolean.valueOf(value));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.WrappingListener#endQuotation(java.util.Map)
     */
    // TODO: this should be fixed in the MediaWiki parser itself
    public void endQuotation(Map<String, String> parameters)
    {
        QuotationBlock quotationBlock = new QuotationBlock(generateListFromStack(), parameters);
        if (!getStack().isEmpty() && getStack().peek() instanceof QuotationBlock) {
            QuotationBlock lastBlock = (QuotationBlock) getStack().peek();
            lastBlock.addChildren(quotationBlock.getChildren());
        } else {
            getStack().push(quotationBlock);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.WrappingListener#beginLink(org.xwiki.rendering.listener.Link, boolean,
     *      java.util.Map)
     */
    public void beginLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        String linkReference = link.getReference();

        // Convert Categories to Tags.
        if (linkReference.startsWith("Category")) {
            this.currentPageRevision.addTag(linkReference.split(":")[1]);
            return;
        }

        // Convert from MediaWiki link to XWiki link
        link = convertLink(link);

        super.beginLink(link, isFreeStandingURI, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.WrappingListener#endLink(org.xwiki.rendering.listener.Link, boolean,
     *      java.util.Map)
     */
    public void endLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        String linkReference = link.getReference();

        // Convert Categories to Tags.
        if (linkReference.startsWith("Category")) {
            return;
        }

        // Convert from MediaWiki link to XWiki link
        link = convertLink(link);

        super.endLink(link, isFreeStandingURI, parameters);
    }

    /**
     * Convert from MediaWiki reference to XWiki reference.
     */
    private Link convertLink(Link mediaWikiLink)
    {
        // If link reference is a external url
        if (-1 != mediaWikiLink.getReference().indexOf("://")) {
            return mediaWikiLink;
        }

        // if link reference is an email
        if (mediaWikiLink.getReference().startsWith("mailto:")) {
            int spaceOccurence = mediaWikiLink.getReference().indexOf(' ');
            if (-1 != spaceOccurence) {

            }

            return mediaWikiLink;
        }

        Link xwikiLink = new Link();

        xwikiLink.setAnchor(mediaWikiLink.getAnchor());
        xwikiLink.setInterWikiAlias(mediaWikiLink.getInterWikiAlias());
        xwikiLink.setQueryString(mediaWikiLink.getQueryString());
        xwikiLink.setReference(mediaWikiLink.getReference());
        xwikiLink.setType(mediaWikiLink.getType());

        // Handle Colon (:) - Links like [[Space:Page]]
        if (xwikiLink.getReference().contains(":") && !xwikiLink.getReference().endsWith(":")) {
            String[] parts = xwikiLink.getReference().split(":");
            String nameSpace = parts[0];
            if (StringUtils.isNotEmpty(this.importParams.getTargetSpace())) {
                nameSpace = this.importParams.getTargetSpace();
            }
            String resourceName = parts[1];

            if (isImage(nameSpace, resourceName)) {
                xwikiLink.setReference("image:" + resourceName);
                beginAttachment(resourceName);

            } else if (nameSpace.equalsIgnoreCase("media") || nameSpace.equalsIgnoreCase("file")) {
                xwikiLink.setReference("attach:" + resourceName);
                beginAttachment(resourceName);

            } else if (-1 != resourceName.indexOf('/')) {
                xwikiLink.setReference(nameSpace + "."
                    + MediaWikiConstants.convertPageName(resourceName.substring(resourceName.lastIndexOf('/') + 1)));
            } else {
                xwikiLink.setReference(nameSpace + "." + MediaWikiConstants.convertPageName(resourceName));
            }
        } else if (StringUtils.isNotEmpty(xwikiLink.getReference())) {
            // If linkreference is not referred to a space, set the default space as Main.
            xwikiLink.setReference(getDefaultSpace() + "."
                + MediaWikiConstants.convertPageName(xwikiLink.getReference()));
        }

        // Fix Category Link [[:Category:Help|HELP]]
        if (xwikiLink.getReference().startsWith(":Category:") || xwikiLink.getReference().startsWith(":category:")) {
            String categoryReference = xwikiLink.getReference().substring(":Category:".length()).trim();
            if (!"".equals(categoryReference)) {
                xwikiLink.setReference("Main.Tags");
                xwikiLink.setQueryString("do=viewTag&tag=" + categoryReference);
            } else {

            }
        }

        // Handle hierarchy ('/')
        if (-1 != xwikiLink.getReference().indexOf('/')) {
            xwikiLink.setReference(getDefaultSpace()
                + "."
                + MediaWikiConstants.convertPageName(xwikiLink.getReference().substring(
                    xwikiLink.getReference().lastIndexOf('/') + 1)));
        }

        return xwikiLink;
    }

    /**
     * Check if the file is a image.
     * 
     * @param nameSpace Namespace of the file.Images usually have File or Image and namespace.
     * @param fileName name of the file with extension
     * @return
     */
    private boolean isImage(String nameSpace, String fileName)
    {
        int dotIndex = fileName.indexOf('.');
        String[] fileExtensions = {"png", "gif", "jpg", "jpeg", "svg", "tiff", "tif"};
        if ((nameSpace.equalsIgnoreCase("image") || nameSpace.equalsIgnoreCase("file")) && -1 != dotIndex) {
            String fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
            return Arrays.asList(fileExtensions).contains(fileExtension);
        }

        return false;
    }

    /**
     * @return the default space.
     */
    private String getDefaultSpace()
    {
        if (StringUtils.isNotBlank(this.importParams.getTargetSpace())) {
            return this.importParams.getTargetSpace();
        } else if (StringUtils.isNotBlank(this.importParams.getDefaultSpace())) {
            return this.importParams.getDefaultSpace();
        }

        return "Main";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onMacro(java.lang.String, java.util.Map, java.lang.String, boolean)
     */
    public void onMacro(String id, Map<String, String> macroParameters, String content, boolean isInline)
    {
        if (id.equals("toc") || id.equals("forcetoc")) {
            macroParameters =
                macroParameters != null ? new HashMap<String, String>(macroParameters) : new HashMap<String, String>();
            macroParameters.put("numbered", "true");
        } else {
            id = "warning";
            this.macroErrors++;
        }

        super.onMacro(id, macroParameters, content, isInline);
    }
}
