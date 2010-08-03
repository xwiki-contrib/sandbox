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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.DefinitionDescriptionBlock;
import org.xwiki.rendering.block.DefinitionListBlock;
import org.xwiki.rendering.block.DefinitionTermBlock;
import org.xwiki.rendering.block.EmptyLinesBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.HorizontalLineBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.NewLineBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.QuotationBlock;
import org.xwiki.rendering.block.QuotationLineBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.syntax.Syntax;
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
    private MediaWikiPage page;

    private MediaWikiPageRevision pageRevision;

    private boolean onPageFlag;

    private Map<String, String> pageProps = new HashMap<String, String>();

    private Map<String, String> pageRevProps = new HashMap<String, String>();

    private boolean onPageRevisionFlag;

    private String fileExtensions;

    private String wiki;

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

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginAttachment(java.lang.String)
     */
    public void beginAttachment(String attachmentName)
    {
        if (onPageFlag) {
            page.addAttachment(new MediaWikiAttachment(this.importParams.getAttachmentSrcPath(), attachmentName,
                this.importParams.getAttachmentExcludeDirs(), logger));
            endAttachment();
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginObject(java.lang.String)
     */
    public void beginObject(String objectType)
    {

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginWikiPage()
     */
    public void beginWikiPage()
    {
        // Begin Page Flags.
        this.onPageFlag = true;
        this.pageProps.clear();
        this.page = new MediaWikiPage(this.importParams.getDefaultSpace(), this.wiki);
        this.logger.nextPage();
        this.stack.clear();
        this.attachments.clear();
        this.macroErrors = 0;

        this.logger.info("Begin Page", true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginWikiPage(java.lang.String)
     */
    public void beginWikiPage(String pageName)
    {

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginWikiPageRevision()
     */
    public void beginWikiPageRevision()
    {
        if (this.onPageFlag) {
            this.onPageRevisionFlag = true;
            this.pageRevProps.clear();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#beginWikiPageRevision(java.lang.String, int)
     */
    public void beginWikiPageRevision(String pageName, int revision)
    {

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endAttachment()
     */
    public void endAttachment()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endObject(java.lang.String)
     */
    public void endObject(String objectType)
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endWikiPage()
     */
    public void endWikiPage()
    {
        // Populate the Page properties.
        this.page.populateProps(this.pageProps);
        this.onPageFlag = false;

        // Logging - set original page title for reference.
        this.logger.getPageLog().setLog(this.page.getPageTitleForReference());
        if (macroErrors > 0) {
            this.logger.warn("Total Macro Errors reported on this page :" + macroErrors, true);
        }

        // attachments
        if (this.page.getAttachments().size() > 0) {
            this.logger.info("Total Attachments encountered :" + attachments.size(), true);
        }

        try {
            // Save the Wiki Page.
            this.docBridge.addWikiPage(this.page);
        } catch (Exception e) {
            // Do nothing.
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#endWikiPageRevision()
     */
    public void endWikiPageRevision()
    {
        if (this.onPageFlag) {
            this.pageRevision = new MediaWikiPageRevision(this.pageRevProps);
            this.pageRevision.setTextContent(getXDOM());
            this.page.addRevision(pageRevision);
            this.onPageRevisionFlag = false;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#onAttachmentRevision(java.lang.String,
     *      org.xml.sax.InputSource)
     */
    public void onAttachmentRevision(String attachmentName, InputSource input)
    {

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.listener.WikiImporterListener#onProperty(java.lang.String, java.lang.String)
     */
    public void onProperty(String property, String value)
    {
        if (this.onPageFlag) {
            if (this.onPageRevisionFlag) {
                this.pageRevProps.put(property, value);
            } else {
                this.pageProps.put(property, value);
            }
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDefinitionList(java.util.Map)
     */
    public void beginDefinitionList(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDocument(java.util.Map)
     */
    public void beginDocument(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginFormat(org.xwiki.rendering.listener.Format, java.util.Map)
     */
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginGroup(java.util.Map)
     */
    public void beginGroup(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginHeader(org.xwiki.rendering.listener.HeaderLevel,
     *      java.lang.String, java.util.Map)
     */
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginList(org.xwiki.rendering.listener.ListType, java.util.Map)
     */
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginListItem()
     */
    public void beginListItem()
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginMacroMarker(java.lang.String, java.util.Map, java.lang.String,
     *      boolean)
     */
    public void beginMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginParagraph(java.util.Map)
     */
    public void beginParagraph(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginQuotation(java.util.Map)
     */
    public void beginQuotation(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginSection(java.util.Map)
     */
    public void beginSection(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTable(java.util.Map)
     */
    public void beginTable(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTableCell(java.util.Map)
     */
    public void beginTableCell(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTableHeadCell(java.util.Map)
     */
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTableRow(java.util.Map)
     */
    public void beginTableRow(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.LinkListener#beginLink(org.xwiki.rendering.listener.Link, boolean,
     *      java.util.Map)
     */
    public void beginLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        this.stack.push(new DefinitionDescriptionBlock(generateListFromStack()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDefinitionList(java.util.Map)
     */
    public void endDefinitionList(Map<String, String> parameters)
    {
        this.stack.push(new DefinitionListBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        this.stack.push(new DefinitionTermBlock(generateListFromStack()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDocument(java.util.Map)
     */
    public void endDocument(Map<String, String> parameters)
    {
        // Do nothing. This is supposed to append only once for the hole document
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endFormat(org.xwiki.rendering.listener.Format, java.util.Map)
     */
    public void endFormat(Format format, Map<String, String> parameters)
    {
        this.stack.push(new FormatBlock(generateListFromStack(), format, parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endGroup(java.util.Map)
     */
    public void endGroup(Map<String, String> parameters)
    {
        this.stack.push(new GroupBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endHeader(org.xwiki.rendering.listener.HeaderLevel, java.lang.String,
     *      java.util.Map)
     */
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.stack.push(new HeaderBlock(generateListFromStack(), level, parameters, id));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endList(org.xwiki.rendering.listener.ListType, java.util.Map)
     */
    public void endList(ListType listType, Map<String, String> parameters)
    {
        if (listType == ListType.BULLETED) {
            this.stack.push(new BulletedListBlock(generateListFromStack(), parameters));
        } else {
            this.stack.push(new NumberedListBlock(generateListFromStack(), parameters));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endListItem()
     */
    public void endListItem()
    {
        this.stack.push(new ListItemBlock(generateListFromStack()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endMacroMarker(java.lang.String, java.util.Map, java.lang.String,
     *      boolean)
     */
    public void endMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.stack.push(new MacroMarkerBlock(name, macroParameters, content, generateListFromStack(), isInline));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endParagraph(java.util.Map)
     */
    public void endParagraph(Map<String, String> parameters)
    {
        this.stack.push(new ParagraphBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endQuotation(java.util.Map)
     */
    public void endQuotation(Map<String, String> parameters)
    {
        QuotationBlock quotationBlock = new QuotationBlock(generateListFromStack(), parameters);
        if (!this.stack.empty() && this.stack.peek() instanceof QuotationBlock) {
            QuotationBlock lastBlock = (QuotationBlock) this.stack.pop();
            List<Block> blockList = new ArrayList<Block>();
            blockList.addAll(lastBlock.getChildren());
            blockList.addAll(quotationBlock.getChildren());
            quotationBlock = new QuotationBlock(blockList, parameters);
        }
        this.stack.push(quotationBlock);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endQuotationLine()
     */
    public void endQuotationLine()
    {
        this.stack.push(new QuotationLineBlock(generateListFromStack()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endSection(java.util.Map)
     */
    public void endSection(Map<String, String> parameters)
    {
        this.stack.push(new SectionBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTable(java.util.Map)
     */
    public void endTable(Map<String, String> parameters)
    {
        this.stack.push(new TableBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTableCell(java.util.Map)
     */
    public void endTableCell(Map<String, String> parameters)
    {
        this.stack.push(new TableCellBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTableHeadCell(java.util.Map)
     */
    public void endTableHeadCell(Map<String, String> parameters)
    {
        this.stack.push(new TableHeadCellBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTableRow(java.util.Map)
     */
    public void endTableRow(Map<String, String> parameters)
    {
        this.stack.push(new TableRowBlock(generateListFromStack(), parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.LinkListener#endLink(org.xwiki.rendering.listener.Link, boolean, java.util.Map)
     */
    public void endLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        LinkBlock linkBlock = new LinkBlock(generateListFromStack(), link, isFreeStandingURI, parameters);
        String linkReference = linkBlock.getLink().getReference();

        // Check if link reference is empty/blank
        if (linkReference == null || "".equals(linkReference.trim())) {

        }

        // Convert Categories to Tags.
        if (linkReference.startsWith("Category")) {
            this.page.addTag(linkReference.split(":")[1]);
            return;
        }

        // Convert from MediaWiki link to XWiki link
        convertLink(linkBlock.getLink());

        this.stack.push(linkBlock);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onEmptyLines(int)
     */
    public void onEmptyLines(int count)
    {
        this.stack.push(new EmptyLinesBlock(count));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onHorizontalLine(java.util.Map)
     */
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.stack.push(new HorizontalLineBlock(parameters));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onId(java.lang.String)
     */
    public void onId(String name)
    {
        this.stack.push(new IdBlock(name));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onMacro(java.lang.String, java.util.Map, java.lang.String, boolean)
     */
    public void onMacro(String id, Map<String, String> macroParameters, String content, boolean isInline)
    {
        if (id.equals("toc") || id.equals("forcetoc")) {
            if (macroParameters == null) {
                macroParameters = new HashMap<String, String>();
            }
            macroParameters.put("numbered", "true");
        } else {
            id = "warning";
            this.macroErrors++;
        }
        MacroBlock macroBlock = new MacroBlock(id, macroParameters, content, isInline);
        this.stack.push(macroBlock);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onNewLine()
     */
    public void onNewLine()
    {
        this.stack.push(NewLineBlock.NEW_LINE_BLOCK);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onRawText(java.lang.String, org.xwiki.rendering.syntax.Syntax)
     */
    public void onRawText(String rawContent, Syntax syntax)
    {
        this.stack.push(new RawBlock(rawContent, syntax));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onSpace()
     */
    public void onSpace()
    {
        this.stack.push(SpaceBlock.SPACE_BLOCK);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onSpecialSymbol(char)
     */
    public void onSpecialSymbol(char symbol)
    {
        this.stack.push(new SpecialSymbolBlock(symbol));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onVerbatim(java.lang.String, boolean, java.util.Map)
     */
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        this.stack.push(new VerbatimBlock(protectedString, parameters, isInline));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onWord(java.lang.String)
     */
    public void onWord(String word)
    {
        this.stack.push(new WordBlock(word));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.ImageListener#onImage(org.xwiki.rendering.listener.Image, boolean,
     *      java.util.Map)
     */
    public void onImage(Image image, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        this.stack.push(new ImageBlock(image, isFreeStandingURI, parameters));
    }

    private void convertLink(Link mediaWikiLink)
    {
        // If link reference is a external url
        if (-1 != mediaWikiLink.getReference().indexOf("://")) {
            return;
        }

        // if link reference is an email
        if (mediaWikiLink.getReference().startsWith("mailto:")) {
            int spaceOccurence = mediaWikiLink.getReference().indexOf(' ');
            if (-1 != spaceOccurence) {

            }
            return;
        }

        // Handle Colon (:) - Links like [[Space:Page]]
        if (mediaWikiLink.getReference().contains(":") && !mediaWikiLink.getReference().endsWith(":")) {
            String[] parts = mediaWikiLink.getReference().split(":");
            String nameSpace = parts[0];
            String resourceName = parts[1];

            if (isImage(nameSpace, resourceName)) {
                mediaWikiLink.setReference("image:" + resourceName);
                beginAttachment(resourceName);

            } else if (nameSpace.equalsIgnoreCase("media") || nameSpace.equalsIgnoreCase("file")) {
                mediaWikiLink.setReference("attach:" + resourceName);
                beginAttachment(resourceName);

            } else if (-1 != resourceName.indexOf('/')) {
                mediaWikiLink.setReference(nameSpace + "." + resourceName.substring(resourceName.lastIndexOf('/') + 1));
            } else {
                mediaWikiLink.setReference(nameSpace + "." + resourceName);
            }
        } else {
            // If linkreference is not referred to a space, set the default space as Main.
            mediaWikiLink.setReference(getDefaultSpace() + "." + mediaWikiLink.getReference());
        }

        // Anchored Links.
        if (null != mediaWikiLink.getAnchor()) {
            mediaWikiLink.setReference(mediaWikiLink.getReference() + "#" + mediaWikiLink.getAnchor());
            mediaWikiLink.setAnchor(null);
        }

        // Fix Category Link [[:Category:Help|HELP]]
        if (mediaWikiLink.getReference().startsWith(":Category:")
            || mediaWikiLink.getReference().startsWith(":category:")) {
            String categoryReference = mediaWikiLink.getReference().substring(":Category:".length()).trim();
            if (!"".equals(categoryReference)) {
                mediaWikiLink.setReference("Main.Tags");
                mediaWikiLink.setQueryString("do=viewTag&tag=" + categoryReference);

            } else {

            }
        }

        // Handle hierarchy ('/')
        if (-1 != mediaWikiLink.getReference().indexOf('/')) {
            mediaWikiLink.setReference(getDefaultSpace() + "."
                + mediaWikiLink.getReference().substring(mediaWikiLink.getReference().lastIndexOf('/') + 1));
        }

        // Fix local anchored link like [[#TOP|TOP OF PAGE]]
        if (mediaWikiLink.getReference().startsWith("#")) {
            mediaWikiLink.setAnchor("H" + mediaWikiLink.getReference().substring(1).replaceAll("[^a-zA-Z0-9]", ""));
            mediaWikiLink.setReference(null);
        }

        // Remove Space if any.
        mediaWikiLink.setReference(mediaWikiLink.getReference().replaceAll("[ ]", ""));

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
        if (StringUtils.isNotBlank(this.importParams.getDefaultSpace())) {
            return this.importParams.getDefaultSpace();
        }

        return "Main";
    }
}
