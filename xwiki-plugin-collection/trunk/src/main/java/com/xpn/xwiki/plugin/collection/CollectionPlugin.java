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
package com.xpn.xwiki.plugin.collection;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.notify.DocChangeRule;
import com.xpn.xwiki.objects.classes.ListItem;
import com.xpn.xwiki.pdf.impl.PdfExportImpl;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.plugin.packaging.Package;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class CollectionPlugin extends XWikiDefaultPlugin implements XWikiPluginInterface
{
    protected CollectionActivityStream collectionActivityStream;

    public CollectionPlugin(String name, String className, XWikiContext context)
    {
        super(name, className, context);
        collectionActivityStream = new CollectionActivityStream();
    }

    public String getName()
    {
        return "collection";
    }

    public void init(XWikiContext context)
    {
        try {
            // send notifications to the collection activity stream
            context.getWiki().getNotificationManager().addGeneralRule(new DocChangeRule(collectionActivityStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDebug(String string)
    {
        // TODO Auto-generated method stub
    }

    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new CollectionPluginApi((CollectionPlugin) plugin, context);
    }

    /**
     * Exports a set of pages as a JAR
     *
     * @param packageName Name of the package to use for the export file name
     * @param selectList List of page names to export
     * @param withVersions Should versions be included in the export
     * @return false if failure, true if success. The upload will takeover the connection in case of success
     */
    public boolean exportToXAR(String packageName, List selectList, boolean withVersions, XWikiContext context)
        throws XWikiException, IOException
    {
        Package packager = new Package();
        if (withVersions) {
            packager.setWithVersions(true);
        } else {
            packager.setWithVersions(false);
        }
        packager.setAuthorName("");
        packager.setLicence("");
        packager.setVersion("");
        packager.setName(packageName);
        boolean complete = true;

        for (Iterator iterator = selectList.iterator(); iterator.hasNext();) {
            String page = (String) iterator.next();
            if (context.getWiki().getRightService().hasAccessLevel("view", context.getUser(), page, context)) {
                packager.add(page, 0, context);
            } else {
                complete = false;
            }
        }
        packager.export(context.getResponse().getOutputStream(), context);
        return complete;
    }

    /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public void exportWithLinks(String documentName, String type, XWikiContext context) throws Exception
    {
        exportWithLinks(documentName, documentName, null, type, context);
    }

    /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public void exportWithLinks(String documentName,
        List<String> selectlist, String type, XWikiContext context) throws Exception
    {
        exportWithLinks(documentName, documentName, selectlist, type, context);
    }

    /**
     * Returns a pdf or rtf of a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public void exportWithLinks(String packageName, String documentName,
        List<String> selectlist, String type, XWikiContext context) throws Exception
    {
        XWikiDocument doc = (documentName == null) ? null
            : context.getWiki().getDocument(documentName, context);
        exportWithLinks(packageName, (XWikiDocument) doc, selectlist, type, context);
    }

    /**
     * Returns a pdf of the transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public void exportWithLinks(String packageName, XWikiDocument doc,
        List<String> selectlist, String type, XWikiContext context) throws Exception
    {
        exportWithLinks(packageName, doc, selectlist, type, null, context);
    }

    /**
     * Returns a pdf of the transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public void exportWithLinks(String packageName, XWikiDocument doc,
        List<String> selectlist, String type, String pdftemplatepage, XWikiContext context) throws Exception
    {
        // XWikiContext context2 = context.getContext();
        // Preparing the PDF Exporter and PDF URL Factory (this last one is
        // necessary for images includes)
        PdfExportImpl pdfexport = new PdfExportImpl();
        XWikiURLFactory urlf = context.getWiki().getURLFactoryService()
            .createURLFactory(XWikiContext.MODE_PDF, context);
        context.setURLFactory(urlf);
        // Preparing the PDF http headers to have the browser recognize the file
        // as PDF
        context.getResponse().setContentType("application/" + type);
        if (doc == null) {
            context.getResponse().addHeader("Content-disposition",
                "inline; filename=" + packageName + "." + type);
        } else {
            context.getResponse().addHeader(
                "Content-disposition",
                "inline; filename="
                    + Utils.encode(doc.getSpace(), context) + "_"
                    + Utils.encode(doc.getName(), context) + "."
                    + type);
        }
        // Preparing temporary directories for the PDF URL Factory
        File dir = context.getWiki().getTempDirectory(context);
        File tempdir = new File(dir, RandomStringUtils.randomAlphanumeric(8));
        // We should call this but we cannot do it. It might not be a problem
        // but if we have an encoding issue we should look into it
        // this.tidy.setOutputEncoding(context2.getWiki().getEncoding());
        // this.tidy.setInputEncoding(context2.getWiki().getEncoding());
        try {
            // we need to prepare the pdf export directory before running the
            // transclusion
            tempdir.mkdirs();
            context.put("pdfexportdir", tempdir);
            // running the transclusion and the final rendering to HTML
            String content = getRenderedContentWithLinks(doc, selectlist, context);
            // preparing velocity context for the adding of the headers and
            // footers
            VelocityContext vcontext = (VelocityContext) context.get("vcontext");
            vcontext.put("content", content);
            Document vdoc = new Document(doc, context);
            vcontext.put("doc", vdoc);
            vcontext.put("cdoc", vdoc);
            vcontext.put("tdoc", vdoc);

            String tcontent = null;
            // pdfmulti.vm should be declared  in the skin
            if (pdftemplatepage != null) {
            }
            if (tcontent == null) {
                tcontent = context.getWiki().parseTemplate("pdfmulti.vm", context);
            }
            // launching the export
            pdfexport.exportHtml(tcontent, context.getResponse()
                .getOutputStream(), (type.equals("rtf")) ? PdfExportImpl.RTF
                : PdfExportImpl.PDF, context);
        } finally {
            // cleaning temporary directories
            File[] filelist = tempdir.listFiles();
            for (int i = 0; i < filelist.length; i++) {
                filelist[i].delete();
            }
            tempdir.delete();
        }
    }

    /**
     * Returns a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public String getRenderedContentWithLinks(String documentName, XWikiContext context) throws Exception
    {
        return getRenderedContentWithLinks(documentName, null, context);
    }

    /**
     * Returns a transcluded view of the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public String getRenderedContentWithLinks(String documentName, List<String> selectlist, XWikiContext context)
        throws Exception
    {
        XWikiDocument doc = (documentName == null || documentName.equals("")) ? null
            : context.getWiki().getDocument(documentName, context);
        return getRenderedContentWithLinks((XWikiDocument) doc, selectlist, context);
    }

    /**
     * Returns a transcluded view of the given xwiki 2.0 document.
     *
     * @param doc document, should be a xwiki 2.0 document.
     * @return the transcluded result in xhtml syntax.
     */
    public String getRenderedContentWithLinks(XWikiDocument doc, List<String> selectlist, XWikiContext context)
        throws Exception
    {
        List<String> includedList = new ArrayList<String>();
        List<String> headerIds = new ArrayList<String>();
        addDebug("Start transclude with: " + doc);
        // if we don't have a main document
        // then we use the first document in the select list as the main one
        if (doc == null) {
            addDebug("No main document passed");
            if ((selectlist == null) || (selectlist.size() < 1)) {
                return "";
            }
            String childDocName = selectlist.get(0);
            if (childDocName.equals("")) {
                if (selectlist.size() < 2) {
                    return "";
                } else {
                    childDocName = selectlist.get(1);
                }
            }
            addDebug("New main document is: " + childDocName);
            doc = context.getWiki().getDocument(childDocName, context);
        }
        XDOM rootXdom = (doc == null) ? null : doc.getXDOM();
        rootXdom = (rootXdom == null) ? null : rootXdom.clone();
        if (rootXdom == null) {
            return "could not read the main document";
        }
        // add main document to included list
        includedList.add(doc.getFullName());
        // Recursively transclude the root xdom
        getRenderedContentWithLinks(doc, rootXdom, selectlist, includedList, headerIds, context);
        // Render the result.
        // we are given a select list so we should append them in order
        // transclude has already modified the link to make them point to
        // anchors inside the document
        if (selectlist != null) {
            for (String childDocumentName : selectlist) {
                appendChild(childDocumentName.toString(), rootXdom, selectlist,
                    includedList, headerIds, context);
            }
        }
        WikiPrinter printer = new DefaultWikiPrinter();
        // Here I'm using the XHTML renderer, other renderers can be used simply
        // by changing the syntax argument.
        BlockRenderer renderer = (BlockRenderer) Utils.getComponent(BlockRenderer.class, Syntax.XHTML_1_0.toIdString());
        renderer.render(rootXdom, printer);
        return printer.toString();
    }

    /**
     * Recursively transcludes the given xdom.
     */
    public void getRenderedContentWithLinks(XWikiDocument doc, XDOM xdom, List<String> selectlist,
        List<String> includedList, List<String> headerIds, XWikiContext context) throws Exception
    {
        List<String> list = updateXDOM(doc, xdom, selectlist, headerIds);
        for (String childDocumentName : list) {
            appendChild(childDocumentName.toString(), xdom, selectlist,
                includedList, headerIds, context);
        }
    }

    /**
     * Add a child document to the given XDOM
     *
     * @param childDocumentName document to append to the xdom
     * @param xdom XDOM to append to
     * @param selectlist list of documents that are included in the multi page export
     * @param includedList list of documents already appended
     */
    protected void appendChild(String childDocumentName, XDOM xdom,
        List<String> selectlist, List<String> includedList,
        List<String> headerIds, XWikiContext context) throws Exception
    {
        // make sure we don't include twice
        if (childDocumentName == "" || includedList.contains(childDocumentName)) {
            return;
        }
        includedList.add(childDocumentName);
        XWikiDocument childDoc = context.getWiki().getDocument(childDocumentName, context);
        XDOM childXdom = (childDoc == null) ? null : childDoc.getXDOM();
        childXdom = (childXdom == null) ? null : childXdom.clone();
        // Transclude (recursive call) the child xdom.
        if (childXdom != null) {
            getRenderedContentWithLinks(childDoc, childXdom, selectlist, includedList, headerIds, context);
        }
        // Now, before we insert the childXdom into current (parent) xdom, we
        // must place an anchor (id macro)
        // so that we can link to the child's content later.
        Map<String, String> idMacroParams = new HashMap<String, String>();
        IdBlock idBlock = new IdBlock("child_" + childDocumentName.hashCode());
        // Append the id macro
        xdom.addChild(idBlock);
        // Now append the childXdom
        if (childXdom != null) {
            xdom.addChildren(childXdom.getChildren());
        }
    }

    /**
     * Update the links in the given xdom.
     *
     * @param doc Document that we are handling the content of
     * @param xdom XDOM to update the links and images
     * @param selectlist List of documents that are included in the multi page export. If null then we will include all
     * childrens
     * @return list of documents that have been found as links in case selectlist is null
     */
    protected List<String> updateXDOM(XWikiDocument doc, XDOM xdom,
        List<String> selectlist, List<String> headerIds) throws Exception
    {
        List<String> list = new ArrayList<String>();
        // Find all the image blocks inside this XDOM to make sure we have
        // absolute image links
        // This is necessary as the XDOM will be included in a different
        // document
        List<ImageBlock> imageBlocks = xdom.getChildrenByType(ImageBlock.class,
            true);
        // Process each image block
        for (ImageBlock imageBlock : imageBlocks) {
            // We are only interested in images that are attachments to the
            // current document
            if ((imageBlock.getImage().getType() == ImageType.DOCUMENT)
                && (null != doc.getAttachment(imageBlock.getImage()
                .getName())))
            {
                // new image block with an absolute link
                Image newImageBlockImage = new DocumentImage(
                    new DefaultAttachement(doc.getFullName(), imageBlock
                        .getImage().getName()));
                ImageBlock newImageBlock = new ImageBlock(newImageBlockImage,
                    imageBlock.isFreeStandingURI(), imageBlock
                    .getParameters());
                // Replace the original link
                imageBlock.getParent().insertChildBefore(newImageBlock,
                    imageBlock);
                imageBlock.getParent().getChildren().remove(imageBlock);
                addDebug("Image in doc: " + imageBlock.getImage());
            }
        }
        // Resolve duplicate anchors
        List<HeaderBlock> headerBlocks = xdom.getChildrenByType(
            HeaderBlock.class, true);
        for (HeaderBlock hBlock : headerBlocks) {
            // Check if this header id has occured before.
            String oldId = hBlock.getId();
            if (headerIds.contains(oldId)) {
                // Now we need to replace this header block with a new one (with
                // a new id).
                String newId = oldId;
                int localIndex = 0;
                while (headerIds.contains(newId)) {
                    newId = oldId + (++localIndex);
                }
                // Create a new HeaderBlock
                HeaderBlock newhBlock = new HeaderBlock(hBlock.getChildren(),
                    hBlock.getLevel(), hBlock.getParameters(), newId);
                // Replace the old one.
                List<Block> replacement = new ArrayList<Block>();
                replacement.add(newhBlock);
                hBlock.getParent().replaceChild(replacement, hBlock);
                // Finally, add the newId into the headerIds list.
                headerIds.add(newId);
            } else {
                headerIds.add(oldId);
            }
        }
        // Find all the link blocks inside this XDOM
        List<LinkBlock> linkBlocks = xdom.getChildrenByType(LinkBlock.class,
            true);
        // Process each link block
        for (LinkBlock linkBlock : linkBlocks) {
            // We are only interested in links to other pages.
            if (linkBlock.getLink().getType() == LinkType.DOCUMENT) {
                String childDocumentName = linkBlock.getLink().getReference();
                if (childDocumentName != null) {
                    // Create the child xdom
                    if (childDocumentName.indexOf(".") == -1) {
                        childDocumentName = doc.getSpace() + "."
                            + childDocumentName;
                    }
                    addDebug("Found one link to: " + childDocumentName);

                    if (selectlist == null) {
                        list.add(childDocumentName);
                    }

                    if ((selectlist == null)
                        || selectlist.contains(childDocumentName))
                    {
                        // Now we need to recreate the link (which was pointing
                        // to child document) into this anchor (id macro)
                        // We create a new link and replace the original one.
                        Link newLinkBlockLink = new Link();
                        newLinkBlockLink.setType(LinkType.DOCUMENT);
                        newLinkBlockLink.setAnchor("child_"
                            + childDocumentName.hashCode());
                        LinkBlock newLinkBlock = new LinkBlock(linkBlock
                            .getChildren(), newLinkBlockLink, false);

                        // if there was no children we need to create a Label
                        if (linkBlock.getChildren().isEmpty()) {
                            Parser parser = Utils.getComponent(Parser.class, Syntax.PLAIN_1_0.toIdString());
                            newLinkBlock.addChildren(
                                parser.parse(new StringReader(linkBlock.getLink().getReference())).getChildren());
                        }

                        // Replace the original link
                        linkBlock.getParent().insertChildBefore(newLinkBlock,
                            linkBlock);
                        linkBlock.getParent().getChildren().remove(linkBlock);
                    } // if in selectlist or selectlist is null
                } // if childDocumentName
            } // if LinkType.Document
        } // for linkBlocks
        return list;
    }

    /**
     * Returns the list of linked docs in the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return list of linked docs
     */
    public List<String> getLinks(String documentName, XWikiContext context) throws Exception
    {
        XWikiDocument doc = context.getWiki().getDocument(documentName, context);
        XDOM rootXdom = (doc == null) ? null : doc.getXDOM();
        rootXdom = (rootXdom == null) ? null : rootXdom.clone();
        if (rootXdom == null) {
            return null;// "could not read the main document";
        }
        // get the links
        return getLinks(doc, rootXdom);
    }

    /**
     * Get links for the given xdom
     *
     * @param doc parent doc
     * @param xdom xdom to find links in
     */
    public List<String> getLinks(XWikiDocument doc, XDOM xdom) throws Exception
    {
        List<String> linkList = new ArrayList<String>();
        // Find all the link blocks inside this XDOM
        List<LinkBlock> linkBlocks = xdom.getChildrenByType(LinkBlock.class,
            true);
        // Process each link block
        for (LinkBlock linkBlock : linkBlocks) {
            // We are only interested in links to other pages.
            if (linkBlock.getLink().getType() == LinkType.DOCUMENT) {
                String childDocumentName = linkBlock.getLink().getReference();
                if (childDocumentName != null) {
                    // Create the child xdom
                    if (childDocumentName.indexOf(".") == -1) {
                        childDocumentName = doc.getSpace() + "."
                            + childDocumentName;
                    }
                    addDebug("Found one link to: " + childDocumentName);
                    linkList.add(childDocumentName);
                }
            }
        }
        return linkList;
    }

    /**
     * Returns the recursive list of linked docs in the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return th list of linked docs
     */
    public List<ListItem> getLinksTreeList(String documentName, XWikiContext context)
        throws Exception
    {
        List<ListItem> treeList = new ArrayList<ListItem>();
        List<String> safeList = new ArrayList<String>();
        safeList.add(documentName);
        treeList.add(new ListItem(documentName, context.getWiki().getDocument(documentName, context)
            .getDisplayTitle(context), ""));
        getLinksTreeList(documentName, treeList, safeList, context);
        return treeList;
    }

    /**
     * Returns the recursive list of linked docs in the given xwiki 2.0 document.
     *
     * @param documentName name of the input document, should be a xwiki 2.0 document.
     * @return list of linked docs
     */
    public void getLinksTreeList(String documentName, List<ListItem> treeList,
        List<String> safeList, XWikiContext context) throws Exception
    {
        for (String link : getLinks(documentName, context)) {
            if (!safeList.contains(link)) {
                safeList.add(link);
                treeList.add(new ListItem(link.toString(), context.getWiki().getDocument(
                    link.toString(), context).getDisplayTitle(context), documentName));
                getLinksTreeList(link.toString(), treeList, safeList, context);
            }
        }
    }

    /**
     * Fix to the linked pages function in XWiki document which allows to call it from a different context than the
     * current document.
     *
     * @return list of linked pages
     */
    public List<String> getLinkedPages(XWikiDocument document, XWikiContext context)
    {
        XWikiDocument olddocument = context.getDoc();
        try {
            if (document == null) {
                return new ArrayList<String>();
            } else {
                context.setDoc(document);
                return document.getLinkedPages(context);
            }
        } finally {
            if (olddocument != null) {
                context.setDoc(olddocument);
            }
        }
    }

    /**
     * Retrieves the class names that represent collections from the preferences
     *
     * @param context XWiki Context
     * @return list of collections comma separated
     */
    public String getCollectionsClassName(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.collections.classnames", "");
    }

    /**
     * Detects if a document represents a collection
     *
     * @param docName document to check
     * @param context XWiki Context
     * @return true if it represents a collection
     */
    public boolean isCollection(String docName, XWikiContext context)
    {
        try {
            return isCollection(context.getWiki().getDocument(docName, context), context);
        } catch (XWikiException e) {
            return false;
        }
    }

    /**
     * Detects if a document represents a collection
     *
     * @param doc XWiki Document to check
     * @param context XWiki Context
     * @return true if it represents a collection
     */
    public boolean isCollection(XWikiDocument doc, XWikiContext context)
    {
        String classNames = getCollectionsClassName(context);
        if (classNames != null) {
            String[] classNameList = StringUtils.split(classNames, " ,");
            for (int i = 0; i < classNameList.length; i++) {
                String className = classNameList[i];
                if (doc.getObject(className) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Detects if a document represents a collection of class className
     *
     * @param docName document to check
     * @param className class to use to detect if it is a collection
     * @param context XWiki Context
     * @return true if it represents a collection
     */
    public boolean isCollection(String docName, String className, XWikiContext context)
    {
        try {
            return isCollection(context.getWiki().getDocument(docName, context), className, context);
        } catch (XWikiException e) {
            return false;
        }
    }

    /**
     * Detects if a document represents a collection of class className
     *
     * @param doc XWiki Document to check
     * @param className class to use to detect if it is a collection. If null use the default list of collections.
     * @param context XWiki Context
     * @return true if it represents a collection
     */
    public boolean isCollection(XWikiDocument doc, String className, XWikiContext context)
    {
        if (className == null) {
            return isCollection(doc, context);
        }

        if (doc.getObject(className) != null) {
            return true;
        }

        return false;
    }

    /**
     * Retrieves the collections in which the document docName is present
     *
     * @param docName document to search collections for
     * @return list of pages representing collections
     */
    public List<String> getCollections(String docName, XWikiContext context)
    {
        return getCollections(docName, null, new ArrayList<String>(), context);
    }

    /**
     * Retrieves the collections in which the document docName is present
     *
     * @param docName document to search collections for
     * @param pageList list of pages already traversed to avoid infinite loops
     * @param context XWiki Context
     * @return list of pages representing collections
     */
    public List<String> getCollections(String docName, ArrayList<String> pageList, XWikiContext context)
    {
        return getCollections(docName, null, pageList, context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document.
     *
     * @param docName document to search collections for
     * @param context XWiki Context
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, XWikiContext context)
    {
        return getCollectionsWithPath(docName, null, "", new ArrayList<String>(), context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document.
     *
     * @param docName document to search collections for
     * @param pageList list of pages already traversed to avoid infinite loops
     * @param context XWiki Context
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, String path, ArrayList<String> pageList,
        XWikiContext context)
    {
        return getCollectionsWithPath(docName, null, "", pageList, context);
    }

    /**
     * Retrieves the collections in which the document docName is present and limit only to collection using class
     * className
     *
     * @param docName document to search collections for
     * @param className class to use to detect if it is a collection
     * @return list of pages representing collections
     */
    public List<String> getCollections(String docName, String className, XWikiContext context)
    {
        return getCollections(docName, className, new ArrayList<String>(), context);
    }

    /**
     * Retrieves the collections in which the document docName is present and limit only to collection using class
     * className
     *
     * @param docName document to search collections for
     * @param className class to use to detect if it is a collection
     * @param pageList list of pages already traversed to avoid infinite loops
     * @param context XWiki Context
     * @return list of pages representing collections
     */
    public List<String> getCollections(String docName, String className, ArrayList<String> pageList,
        XWikiContext context)
    {
        List<String> collectionList = new ArrayList<String>();
        XWikiDocument doc;
        // add to the pageList to avoid infinite loops
        pageList.add(docName);
        try {
            doc = context.getWiki().getDocument(docName, context);
        } catch (XWikiException e1) {
            // could not read document ignore it
            return collectionList;
        }
        if (isCollection(doc, className, context)) {
            collectionList.add(docName);
        } else {
            try {
                List<String> backLinks = context.getWiki().getDocument(docName, context).getBackLinkedPages(context);
                for (Iterator<String> iterator = backLinks.iterator(); iterator.hasNext();) {
                    String backLink = (String) iterator.next();
                    if (!pageList.contains(backLink)) {
                        collectionList.addAll(getCollections(backLink, className, pageList, context));
                    }
                }
            } catch (XWikiException e) {
                // could not read back links
            }
        }
        return collectionList;
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document and limit only to collection using class className.
     *
     * @param docName document to search collections for
     * @param className class to use to detect if it is a collection
     * @param context XWiki Context
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, String className, XWikiContext context)
    {
        return getCollectionsWithPath(docName, className, "", new ArrayList<String>(), context);
    }

    /**
     * Retrieves the collections in which the document docName is present and include the path to this collection with
     * the document and limit only to collection using class className.
     *
     * @param docName document to search collections for
     * @param className class to use to detect if it is a collection
     * @param pageList list of pages already traversed to avoid infinite loops
     * @param context XWiki Context
     * @return map of pages representing collections and the path as the map values
     */
    public Map<String, String> getCollectionsWithPath(String docName, String className, String path,
        ArrayList<String> pageList, XWikiContext context)
    {
        Map<String, String> collectionMap = new HashMap<String, String>();
        // add to the pageList to avoid infinite loops
        pageList.add(docName);
        XWikiDocument doc;
        try {
            doc = context.getWiki().getDocument(docName, context);
        } catch (XWikiException e1) {
            // could not read document ignore it
            return collectionMap;
        }
        if (isCollection(doc, className, context)) {
            collectionMap.put(docName, path);
        } else {
            try {
                List<String> backLinks = context.getWiki().getDocument(docName, context).getBackLinkedPages(context);
                for (Iterator<String> iterator = backLinks.iterator(); iterator.hasNext();) {
                    String backLink = (String) iterator.next();
                    // check if pages already handle to avoid infinite loop
                    if (!pageList.contains(backLink)) {
                        collectionMap.putAll(
                            getCollectionsWithPath(backLink, className, docName + ";" + path, pageList, context));
                    }
                }
            } catch (XWikiException e) {
                // could not read back links
            }
        }
        return collectionMap;
    }

    /**
     * Retrieve the breadcrumb path by looking up parents
     *
     * @param docName page from which to start the breadcrumb
     * @param pageList list of pages already traversed to avoid infinite loops
     * @param context XWiki Context
     */
    public List<String> getBreadcrumbFromParents(String docName, ArrayList<String> pageList, XWikiContext context)
    {
        try {
            XWikiDocument doc = context.getWiki().getDocument(docName, context);
            String page = doc.getParent();
            if ((page != null) & !page.equals("") && !pageList.contains(page) && !page.equals("XWiki.XWikiGuest") &&
                !page.equals("XWiki.XWikiUsers"))
            {
                pageList.add(page);
                return getBreadcrumbFromParents(page, pageList, context);
            } else {
                return pageList;
            }
        } catch (XWikiException e1) {
            // could not read document ignore it
            return pageList;
        }
    }

    /**
     * Retrieves the breadcrumb path for a document first by looking at the request.bc param then by looking up parents
     *
     * @param docName page from which to start the breadcrumb
     * @param context XWiki Context
     */
    public List<String> getBreadcrumb(String docName, XWikiContext context)
    {
        String bcParam = context.getRequest().get("bc");
        ArrayList<String> pageList = new ArrayList<String>();
        String lastPage = docName;
        if (bcParam != null) {
            // adding
            String[] pages = StringUtils.split(bcParam, ";");
            for (int i = pages.length - 1; i >= 0; i--) {
                String page = pages[i];
                if ((i != pages.length - 1) || !docName.equals(page)) {
                    if (!page.equals("") && !pageList.contains(page) && !page.equals("XWiki.XWikiGuest") &&
                        !page.equals("XWiki.XWikiUsers"))
                    {
                        pageList.add(page);
                        lastPage = page;
                    }
                }
            }
        }
        // now continue the BC from the last page using the parent information
        return getBreadcrumbFromParents(lastPage, pageList, context);
    }
}
