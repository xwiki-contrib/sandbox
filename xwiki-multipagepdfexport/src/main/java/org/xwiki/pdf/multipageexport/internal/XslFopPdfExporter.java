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
package org.xwiki.pdf.multipageexport.internal;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.display.internal.DocumentDisplayer;
import org.xwiki.display.internal.DocumentDisplayerParameters;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.pdf.multipageexport.MultipagePdfExporter;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.velocity.VelocityManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.pdf.api.PdfExport;
import com.xpn.xwiki.pdf.impl.PdfExportImpl;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiURLFactory;

/**
 * XSL-FO/FOP based implementation of the multipage pdfexporter. <br />
 * 
 * @version $Id$
 */
@Component("xslfop")
@Singleton
public class XslFopPdfExporter implements MultipagePdfExporter
{
    @Inject
    protected Logger logger;

    /**
     * The execution, to get the context from it.
     */
    @Inject
    protected Execution execution;

    @Inject
    protected ExecutionContextManager ecManager;

    @Inject
    protected DocumentAccessBridge documentAccessBridge;

    @Inject
    protected VelocityManager vManager;

    /**
     * Attachment resolver to resolve references to attachments in document content to absolutize.
     */
    @Inject
    @Named("current")
    private AttachmentReferenceResolver<String> currentARResolver;

    /**
     * Default string Reference serializer.
     */
    @Inject
    private EntityReferenceSerializer<String> defaultERSerializer;

    /**
     * Reference resolver for string representations of references.
     */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> currentDRResolver;

    /**
     * Document displayer, to handle the display of the documents.
     */
    @Inject
    @Named("configured")
    private DocumentDisplayer documentDisplayer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.pdf.multipageexport.MultipagePdfExporter#export(java.lang.String, java.util.List)
     */
    public void export(String name, List<String> docs) throws Exception
    {
        export(name, docs, false, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.pdf.multipageexport.MultipagePdfExporter#export(java.lang.String, java.util.List, boolean)
     */
    public void export(String name, List<String> docs, boolean multiPageSequence) throws Exception
    {
        export(name, docs, multiPageSequence, multiPageSequence);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.pdf.multipageexport.MultipagePdfExporter#export(java.lang.String, java.util.List, boolean, boolean)
     */
    public void export(String name, List<String> docs, boolean multiPageSequence, boolean alwaysStartOnRecto)
        throws Exception
    {
        XWikiContext context = getXWikiContext();

        // if list is empty, throw an exception and stop everything
        if (docs.size() == 0) {
            XWikiException exc = new XWikiException();
            exc.setMessage("No documents to export");
            throw exc;
        }

        // Preparing the PDF http headers to have the browser recognize the file
        // as PDF
        context.getResponse().setContentType("application/pdf");
        context.getResponse()
            .addHeader("Content-disposition", "inline; filename=" + name.replaceAll(" ", "_") + ".pdf");

        // Preparing the PDF Exporter and PDF URL Factory (this last one is
        // necessary for images includes)
        PdfExportImpl pdfexport;
        if (multiPageSequence) {
            pdfexport = new PDFExportImplMultipage();
        } else {
            pdfexport = new PdfExportImpl();
        }
        XWikiURLFactory urlf =
            context.getWiki().getURLFactoryService().createURLFactory(XWikiContext.MODE_PDF, context);
        context.setURLFactory(urlf);
        // Preparing temporary directories for the PDF URL Factory
        File dir = context.getWiki().getTempDirectory(context);
        File tempdir = new File(dir, RandomStringUtils.randomAlphanumeric(8));
        // we need to prepare the pdf export directory before running the transclusion
        tempdir.mkdirs();
        context.put("pdfexportdir", tempdir);
        context.put("pdfexport-file-mapping", new HashMap<String, File>());

        try {
            XWikiDocument fakeDoc = new XWikiDocument(this.currentDRResolver.resolve(""));
            fakeDoc.setTitle(name);
            fakeDoc.setDate(new Date());
            fakeDoc.setContent("");
            // get the combined xdom of all documents
            XDOM xDom = fakeDoc.getXDOM();
            // absolutize the list first. We need this pass before the actual export since the list will be used as a
            // context to relativize document links to places inside the pdf export, if links are towards pages which
            // are exported as well
            List<String> pagesToExport = new ArrayList<String>(docs.size());
            for (String docName : docs) {
                pagesToExport.add(this.defaultERSerializer.serialize(this.currentDRResolver.resolve(docName)));
            }
            // and export each document
            for (String docName : pagesToExport) {
                DocumentReference currentDocReference = this.currentDRResolver.resolve(docName);
                XWikiDocument currentDoc = context.getWiki().getDocument(currentDocReference, context);
                XDOM childXDom =
                    getXDOMForDoc(currentDoc, multiPageSequence, alwaysStartOnRecto,
                        this.getPDFTemplateDocument(context), pagesToExport);
                xDom.addChildren(childXDom.getChildren());
            }

            WikiPrinter printer = new DefaultWikiPrinter();
            // Here I'm using the XHTML renderer, other renderers can be used simply
            // by changing the syntax argument.
            BlockRenderer renderer =
                (BlockRenderer) Utils.getComponent(BlockRenderer.class, Syntax.XHTML_1_0.toIdString());
            renderer.render(xDom, printer);
            String renderedContent = printer.toString();

            VelocityContext vcontext = (VelocityContext) context.get("vcontext");
            // prepare the content of the fake doc, with a html macro and all this rendered stuff in it
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("wiki", Boolean.FALSE.toString());
            parameters.put("clean", Boolean.FALSE.toString());
            xDom = new XDOM(Arrays.<Block> asList(new MacroBlock("html", parameters, renderedContent, false)));
            // put a fake document on the context, we don't really care that much. Actually we don't even need something
            // as complicated as pdfmulti, we put some title and stuff to allow a nice cover page to be generated
            fakeDoc.setContent(xDom);
            Document vdoc = new Document(fakeDoc, context);
            vcontext.put("doc", vdoc);
            vcontext.put("cdoc", vdoc);
            vcontext.put("tdoc", vdoc);

            String tcontent = null;
            if (tcontent == null) {
                // we should remove this deprecation here but I am leaving it since PdfExportImpl is using it as well
                tcontent = context.getWiki().parseTemplate("pdf.vm", context).trim();
            }
            // launching the export
            pdfexport.exportHtml(tcontent, context.getResponse().getOutputStream(), PdfExport.ExportType.PDF, context);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // cleaning temporary directories
            File[] filelist = tempdir.listFiles();
            if (filelist != null) {
                for (int i = 0; i < filelist.length; i++) {
                    filelist[i].delete();
                }
                tempdir.delete();
            }
        }
    }

    /**
     * Prepares and gets the XDOM for a single document.
     * 
     * @param doc the document to get the xdom for
     * @param multiPageSequence whether the pdf changes header on every new document or presents them all as a single
     *            document
     * @param startOnRecto whether this document rendering should always start on recto
     * @param pdfTemplateDoc the template document used for this export
     * @param pagesList the total list of pages to be exported, needed so that the xdom preparation can be done in
     *            context
     * @return the prepared xdom for the passed document
     * @throws Exception in case there are errors parsing this document in XDOM or preparing it
     */
    private XDOM getXDOMForDoc(XWikiDocument doc, boolean multiPageSequence, boolean startOnRecto,
        XWikiDocument pdfTemplateDoc, List<String> pagesList) throws Exception
    {
        XDOM childXdom = (doc == null) ? null : doc.getXDOM();
        childXdom = (childXdom == null) ? null : childXdom.clone();

        // execute the transformations to have the fully transformed xdom here

        // prepare a bit the context
        // 1. put the document on the execution context
        Map<String, Object> backupObjects = new HashMap<String, Object>();
        documentAccessBridge.pushDocumentInContext(backupObjects, doc.getDocumentReference());
        // 2. backup the xwiki context variables since we're gonna put some stuff in there as well
        XWikiContext currentEcXContext = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        String originalDatabase = currentEcXContext.getDatabase();
        String originalAction = currentEcXContext.getAction();
        // we need to change this to mimic properly the export action and allow proper interpretation of the fields in
        // pdf templates. isInRendering engine needs to be true when document content is rendered and false for the rest
        // since otherwise will add some {{html}} macros around the rendered fields which are not in the export action
        // this is true here because we call this service from a page content which is being rendered on view
        Boolean originalIsInRenderingEngine =
            BooleanUtils.toBoolean((Boolean) currentEcXContext.get("isInRenderingEngine"));
        // sdoc is used to determine the syntax of the rendered fields, back it up and set it to the current document
        Object originalSDoc = currentEcXContext.get("sdoc");
        Object originalVContext = currentEcXContext.get("vcontext");
        try {
            // 3. setup xwiki context
            currentEcXContext.setDatabase(doc.getDocumentReference().getWikiReference().getName());
            currentEcXContext.setAction("export");
            // is in rendering engine must be true for document content, false for all the other vms evaluated
            // (header / footer)
            currentEcXContext.put("isInRenderingEngine", true);
            // at this point, the velocity context of the xwiki context and the ones of the execution context are
            // different, because the current document was pushed in the context. So we need to reput it back here,
            // however we back it up because xcontext is never cloned when the context is backed up, so we need to put
            // back the proper one when we're done
            currentEcXContext.put("vcontext", vManager.getVelocityContext());
            // set current document as the sdoc, since the sdoc is used to get syntax for rendered fields and
            // programming rights check
            currentEcXContext.put("sdoc", doc);
            // 4. display xdom of the document (apply sheet -- if any, perform transformations)
            DocumentDisplayerParameters parameters = new DocumentDisplayerParameters();
            parameters.setTransformationContextIsolated(true);
            parameters.setContentTranslated(true);
            childXdom = documentDisplayer.display(doc, parameters);
            // is in rendering engine must be true for document content, false for all the other vms evaluated
            // (header / footer)
            currentEcXContext.put("isInRenderingEngine", false);
            // 5. process references in the XDOM for export, since they need to be either relative to the pdf (if links
            // to documents in the same pdf export), or absolute (if images or external documents)
            updateXDOMReferences(doc, childXdom, pagesList);
            // 6. decorate the xdom with stuff needed for the export: if multipagesequences, add header and footer,
            // otherwise title and id
            String renderedTitle = doc.getRenderedTitle(Syntax.XHTML_1_0, currentEcXContext);
            if (multiPageSequence) {
                // render document header and footer in this nice context as well, if they are needed
                String renderedHeader = getRenderedField(pdfTemplateDoc, "header", currentEcXContext);
                String renderedFooter = getRenderedField(pdfTemplateDoc, "footer", currentEcXContext);
                childXdom =
                    wrapupXDOMWithHeaders(doc, childXdom, renderedTitle, renderedHeader, renderedFooter, startOnRecto);
            } else {
                // add some title and id in front of the rendered document
                XDOM decoratedXDom = new XDOM(Collections.<Block> emptyList());
                IdBlock idBlock =
                    new IdBlock("child_" + this.defaultERSerializer.serialize(doc.getDocumentReference()).hashCode());
                // add a title and the content
                String rawTitle = doc.getTitle();
                // we only insert a title if specified or not specified and not extracted up from content (compatibility
                // mode)
                // TODO: fix this condition, it doesn't actually mean that the title was not extracted from document,
                // as the title extracted from document can be the same as the document name, but there is no usable
                // function to find out what is the extracted title
                if (!StringUtils.isEmpty(rawTitle)
                    || (StringUtils.isEmpty(rawTitle) && renderedTitle.equals(doc.getDocumentReference().getName()))) {
                    Parser parser = Utils.getComponent(Parser.class, Syntax.PLAIN_1_0.toIdString());
                    List<Block> childlist =
                        parser.parse(new StringReader(renderedTitle)).getChildren().get(0).getChildren();
                    int level = 1;
                    HeaderLevel hLevel = HeaderLevel.parseInt(level);
                    HeaderBlock hBlock =
                        new HeaderBlock(childlist, hLevel, new HashMap<String, String>(), idBlock.getName());

                    decoratedXDom.addChild(hBlock);
                } else {
                    // Append the id macro -> to be able to link here with the links from documents exported in the same
                    // pdf
                    decoratedXDom.addChild(idBlock);
                }
                decoratedXDom.addChildren(childXdom.getChildren());
                childXdom = decoratedXDom;
            }
        } finally {
            // restore execution context
            documentAccessBridge.popDocumentFromContext(backupObjects);
            // restore xwiki context
            currentEcXContext.setDatabase(originalDatabase);
            currentEcXContext.setAction(originalAction);
            // make sure to check all these for not-null since context doesn't get null values, and just in case one of
            // them is null, we're dead
            if (originalIsInRenderingEngine != null) {
                currentEcXContext.put("isInRenderingEngine", originalIsInRenderingEngine);
            }
            if (originalVContext != null) {
                currentEcXContext.put("vcontext", originalVContext);
            }
            if (originalSDoc != null) {
                currentEcXContext.put("sdoc", originalSDoc);
            }
        }

        return childXdom;
    }

    protected XDOM wrapupXDOMWithHeaders(XWikiDocument doc, XDOM xdom, String renderedTitle, String renderedHeader,
        String renderedFooter, boolean startOnRecto)
    {
        // if multiple page sequence is required, add pagesequence and header and footer generation
        // header and footer generation will be later, ftm just put the pagesequence elt + content
        Map<String, String> pageSequenceParams = new HashMap<String, String>();
        pageSequenceParams.put("class",
            "pdfpagesequence " + this.defaultERSerializer.serialize(doc.getDocumentReference()));
        if (startOnRecto) {
            pageSequenceParams.put("initial-page-number", "auto-odd");
        }
        GroupBlock pageSequenceBlock = new GroupBlock(pageSequenceParams);
        // put the title of the document here, to be picked up by pdfsequence title and by toc generator
        Map<String, String> pdfTitleParams = new HashMap<String, String>();
        pdfTitleParams.put("class", "pdfpagesequencetitle");
        GroupBlock pdfTitleBlock = new GroupBlock(pdfTitleParams);
        RawBlock rawTitleBlock = new RawBlock(renderedTitle, Syntax.XHTML_1_0);
        pdfTitleBlock.addChild(rawTitleBlock);
        // pdf page sequence header block
        Map<String, String> pdfHeaderParams = new HashMap<String, String>();
        pdfHeaderParams.put("class", "pdfpagesequenceheader");
        GroupBlock pdfHeaderBlock = new GroupBlock(pdfHeaderParams);
        RawBlock rawHeaderBlock = new RawBlock(renderedHeader, Syntax.XHTML_1_0);
        pdfHeaderBlock.addChild(rawHeaderBlock);
        // pdf page sequence footer block
        Map<String, String> pdfFooterParams = new HashMap<String, String>();
        pdfFooterParams.put("class", "pdfpagesequencefooter");
        GroupBlock pdfFooterBlock = new GroupBlock(pdfFooterParams);
        RawBlock rawFooterBlock = new RawBlock(renderedFooter, Syntax.XHTML_1_0);
        pdfFooterBlock.addChild(rawFooterBlock);
        // pdf page sequence content
        Map<String, String> pdfContentParams = new HashMap<String, String>();
        pdfContentParams.put("class", "pdfpagesequencecontent");
        GroupBlock pdfContentBlock = new GroupBlock(pdfContentParams);
        pdfContentBlock.addChildren(xdom.getChildren());
        // get all blocks in
        pageSequenceBlock.addChild(pdfTitleBlock);
        pageSequenceBlock.addChild(pdfHeaderBlock);
        pageSequenceBlock.addChild(pdfFooterBlock);
        pageSequenceBlock.addChild(pdfContentBlock);

        return new XDOM(Arrays.<Block> asList(pageSequenceBlock));
    }

    protected String getRenderedField(XWikiDocument pdfdoc, String fieldName, XWikiContext context)
    {
        BaseObject bobj = null;
        String displayed = "";
        if (pdfdoc != null) {
            bobj = pdfdoc.getXObject(new DocumentReference(context.getDatabase(), "XWiki", "PDFClass"));
        }

        // If such an object exists, get the displayed value of the field
        if (bobj != null) {
            displayed = bobj.displayView(fieldName, context);
        }
        // if the displayed value is still empty, use the one in the templates. This allows to fallback on the templates
        // if the behaviour is not overwritten in the used pdf template. This behaviour is consistent with the one
        // implemented in pdfhtmlheader
        if (StringUtils.isEmpty(displayed)) {
            // no pdfdoc document or no object of type pdf class in the pdftemplate, use the templates
            String templateName = "pdf" + fieldName + ".vm";
            try {
                displayed = context.getWiki().evaluateTemplate(templateName, context);
            } catch (IOException e) {
                logger.warn(
                    "There was an error evaluating template " + templateName + " when exporting document to pdf "
                        + this.defaultERSerializer.serialize(pdfdoc.getDocumentReference()), e);
            }
        }

        return displayed;
    }

    /**
     * Copy the get pdf template function of the old pdf exporter since the new one doesn't expose this function
     * anymore.
     * 
     * @param context the xwiki context
     * @return the XWikiDocument corresponding to the passed pdf template
     * @throws XWikiException in case anything goes wrong
     */
    private XWikiDocument getPDFTemplateDocument(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc = null;
        XWikiRequest request = context.getRequest();
        if (request != null) {
            String pdftemplate = request.get("pdftemplate");
            if (pdftemplate != null) {
                doc = context.getWiki().getDocument(this.currentDRResolver.resolve(pdftemplate), context);
            }
        }
        if (doc == null) {
            doc = context.getDoc();
        }
        return doc;
    }

    /**
     * Update the references for the pdf export:
     * <ul>
     * <li>image references and attachment references are all absolutized since otherwise relative images will be badly
     * resolved on html rendering since the final rendering will happen in a different document context</li>
     * <li>document references are first checked if they point to documents exported in the same pdf, in which case they
     * are relativized to the pdf, then if it's not the case, they are as well absolutized</li>
     * </ul>
     * 
     * @param doc Document that we are handling the content of
     * @param xdom XDOM to update the links and images
     * @param selectlist List of documents that are included in the multi page export
     * @return list of documents that have been found as links in case selectlist is null
     */
    private List<String> updateXDOMReferences(XWikiDocument doc, XDOM xdom, List<String> selectlist)
    {
        List<String> list = new ArrayList<String>();

        // Step 1: Find all the image blocks inside this XDOM to make sure we have absolute image references.
        // This is necessary as the XDOM will be included in a different document
        for (ImageBlock imageBlock : xdom.<ImageBlock> getBlocks(new ClassBlockMatcher(ImageBlock.class),
            Axes.DESCENDANT)) {
            ResourceReference reference = imageBlock.getReference();
            if (reference.getType().equals(ResourceType.ATTACHMENT)) {
                // It's an image coming from an attachment
                AttachmentReference resolvedReference =
                    this.currentARResolver.resolve(reference.getReference(), doc.getDocumentReference());
                String serializedReference = this.defaultERSerializer.serialize(resolvedReference);
                reference.setReference(serializedReference);
            }
        }

        // Step 2: Find all the link blocks inside this XDOM and either absolutize them, or, if they refer a page which
        // is exported in the same pdf, make them link to the page anchor.
        // FIXME: relative links in the pdf will fail, however, for the moment in multipage mode, since there are no
        // generated anchors for a document in multipage mode.
        for (LinkBlock linkBlock : xdom.<LinkBlock> getBlocks(new ClassBlockMatcher(LinkBlock.class), Axes.DESCENDANT)) {
            boolean relativized = false;
            ResourceType linkType = linkBlock.getReference().getType();
            // We are only interested in links to other pages.
            if (linkType.equals(ResourceType.DOCUMENT)) {
                String childDocumentName = linkBlock.getReference().getReference();
                if (childDocumentName != null) {
                    // Absolutize this reference to the child, since we only work with absolute refs
                    DocumentReference childDocRef =
                        this.currentDRResolver.resolve(childDocumentName, doc.getDocumentReference());
                    childDocumentName = this.defaultERSerializer.serialize(childDocRef);

                    if (selectlist == null) {
                        list.add(childDocumentName);
                    }

                    if ((selectlist == null) || selectlist.contains(childDocumentName)) {
                        // Now we need to recreate the link (which was pointing to child document) into this anchor
                        // (id macro). We create a new link and replace the original one.
                        DocumentResourceReference newLinkBlockReference = new DocumentResourceReference("");
                        newLinkBlockReference.setAnchor("child_" + childDocumentName.hashCode());
                        LinkBlock newLinkBlock = new LinkBlock(linkBlock.getChildren(), newLinkBlockReference, false);

                        // we need this variable here since we're not sure to be able to re-create the link properly, so
                        // in case this does not work, we won't replace the link
                        boolean replaceLink = true;
                        // if there was no children we need to create a Label
                        if (linkBlock.getChildren().isEmpty()) {
                            Parser parser = Utils.getComponent(Parser.class, Syntax.PLAIN_1_0.toIdString());
                            try {
                                newLinkBlock.addChildren(parser.parse(
                                    new StringReader(linkBlock.getReference().getReference())).getChildren());
                            } catch (ParseException e) {
                                // don't replace
                                replaceLink = false;
                            }
                        }

                        // Replace the original link
                        if (replaceLink) {
                            linkBlock.getParent().insertChildBefore(newLinkBlock, linkBlock);
                            linkBlock.getParent().getChildren().remove(linkBlock);
                            relativized = true;
                        }
                    } // if in selectlist or selectlist is null
                } // if childDocumentName
            } // if LinkType.Document

            // If we don't relativize the link to the pdf it will remain as it is, so we need to absolutize it if it's
            // document or attachment, since it's gonna be in a different context when the full final xdom will be
            // assembled
            // FIXME: however, this still fails to create proper links in the final pdf for attachments, since the pdf
            // url factory copies the attachments to a temp folder and points all urls to it, in order for the images to
            // be properly handled. It does not make a difference whether the url is needed for image or for url.
            // TODO: to make it work, we need to change the link completely to a full external URL for attachments, so
            // that we bypass the pdf url factory. However, xwiki.getExternalURL() won't work since it uses
            // pdfurlfactory as well.
            if (!relativized && (linkType.equals(ResourceType.DOCUMENT) || linkType.equals(ResourceType.ATTACHMENT))) {
                String linkTarget = linkBlock.getReference().getReference();
                // parse it depending on its type
                EntityReference absoluteLinkTarget =
                    linkType.equals(ResourceType.ATTACHMENT) ? this.currentARResolver.resolve(linkTarget,
                        doc.getDocumentReference()) : this.currentDRResolver.resolve(linkTarget,
                        doc.getDocumentReference());
                // serialize it
                String newLinkTarget = this.defaultERSerializer.serialize(absoluteLinkTarget);
                // if we have created a new link target, we need to replace it in the xdom
                if (!newLinkTarget.equals(linkTarget)) {
                    LinkBlock newLinkBlock =
                        new LinkBlock(linkBlock.getChildren(), new ResourceReference(newLinkTarget, linkType), false);
                    // Replace the original link
                    linkBlock.getParent().insertChildBefore(newLinkBlock, linkBlock);
                    linkBlock.getParent().getChildren().remove(linkBlock);
                }
            }
        } // for linkBlocks
        return list;
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
