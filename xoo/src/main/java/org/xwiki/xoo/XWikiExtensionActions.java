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

package org.xwiki.xoo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.PageSummary;
import org.codehaus.swizzle.confluence.Space;
import org.codehaus.swizzle.confluence.SpaceSummary;
import org.codehaus.swizzle.confluence.Attachment;
import org.jdom.IllegalDataException;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xoo.extensionexceptions.AttachmentNotSelectedException;
import org.xwiki.xoo.extensionexceptions.NotLoggedInException;
import org.xwiki.xoo.extensionexceptions.PageInUseException;
import org.xwiki.xoo.extensionexceptions.PageNullException;
import org.xwiki.xoo.extensionexceptions.PageNotSelectedException;
import org.xwiki.xoo.extensionexceptions.PagePublishException;
import org.xwiki.xoo.extensionexceptions.SpaceNotSelectedException;
import org.xwiki.xoo.openoffice.OpenFileException;
import org.xwiki.xoo.openoffice.OpenOfficeActions;
import org.xwiki.xoo.xwiki.PageNode;
import org.xwiki.xoo.xwiki.SpaceNode;
import org.xwiki.xoo.xwikilib.FileTooBigException;
import org.xwiki.xoo.xwikilib.XOOFileStorage;
import org.xwiki.xoo.xwikilib.cleaner.XOOHTMLCleaner;
import org.xwiki.xoo.xwikilib.cleaner.internal.DefaultXOOHTMLCleaner;
import org.xwiki.xoo.xwikilib.converter.BidirectionalConverter;
import org.xwiki.xoo.xwikilib.converter.internal.DefaultBidirectionalConverter;
import org.xwiki.xoo.xwikilib.convertersupport.XOOConverterSupport;
import org.xwiki.xoo.xwikilib.convertersupport.internal.DefaultConverterSupport;

import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;

/**
 * A class that implements the actions of the XWiki Extension
 * 
 * @version $Id: $
 * @since 1.0 M
 */

public class XWikiExtensionActions
{

    private XWikiExtension xWikiExtension;

    private String newPageText =
        "Hi! This is your new page. Please put your content here and then share it with others by saving it on the wiki.";

    private String startDocument = "<html>\n<head>\n</head>\n<body>";

    private String endDocument = "\n</body>\n</html>";

    public static int FLAG_OPEN_EDIT = 1;

    public static int FLAG_PUBLISH = 2;

    /**
     * Constructor.
     * 
     * @param xWikiExtension the XWiki Extension
     */
    public XWikiExtensionActions(XWikiExtension xWikiExtension)
    {

        this.xWikiExtension = xWikiExtension;
    }

    /**
     * Handles the edit page Toolbar command
     */
    public void cmdEditPage()
    {

        PageNode pageNode = checkPageSelect(true);
        if (pageNode == null)
            return;
        editPage(pageNode.pageSummary);
    }

    /**
     * Prepares a page for editing.
     * 
     * @param pageSummary the page summary of the page which will be opened for edit
     */
    public void editPage(PageSummary pageSummary)
    {
        // TODO verify if it is protected page
        XWikiXmlRpcClient client = xWikiExtension.getClient();
        try {

            XWikiPage xWikiPage = client.getPage(pageSummary.getId());
            String content = xWikiPage.getContent();
            String htmlContent = startDocument + getHTMLContent(content) + endDocument;

            XOOFileStorage filestorage =
                createStorage(getPageFullName(pageSummary), pageSummary.getTitle(), htmlContent);
            String location = filestorage.getTempDir().getPath() + File.separator;

            downloadImageAttachments(pageSummary, content, location);

            String fileurl = convertToURL(filestorage.getOutputFile().getPath());
            openPageForEdit(fileurl, xWikiPage);

        } catch (XmlRpcException e) {
            // TODO treat error
            e.printStackTrace();
        } catch (ConversionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Opens a page for edit with OpenOffice
     * 
     * @param fileurl the URL of the physical page
     * @param xWikiPage the associated XWiki object with the page
     */
    private void openPageForEdit(final String fileurl, XWikiPage xWikiPage)
    {
        try {
            if (xWikiExtension.getExtensionStatus().hasOpenedResource(fileurl))
                throw new PageInUseException();

            xWikiExtension.getExtensionStatus().addOpenedResource(fileurl, xWikiPage);
            OpenOfficeActions openOfficeActions = new OpenOfficeActions(this.xWikiExtension.getComponentContext());
            XComponent document = openOfficeActions.openFile(fileurl);
            document.addEventListener(new XEventListener()
            {
                public void disposing(EventObject e)
                {
                    xWikiExtension.getExtensionStatus().removeOpenedResource(fileurl);
                }
            });
        } catch (OpenFileException ex) {
            ex.printStackTrace();
        } catch (PageInUseException ex) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR, Constants.ERROR_ALREADYEDIT,
                Constants.TYPE_ERROR, false);
        }
    }

    /**
     * Handles the publish page Toolbar command
     */
    public void cmdPublishPage()
    {
        try {
            if (!checkLogin(true))
                return;

            OpenOfficeActions openOfficeActions = new OpenOfficeActions(this.xWikiExtension.getComponentContext());
            String currDocURL = openOfficeActions.getCurrentDocumentURL();

            if (currDocURL == null || currDocURL.isEmpty())
                throw new PageNullException();

            xWikiExtension.getExtensionStatus().setCurrentDocURL(currDocURL);
            if (xWikiExtension.getExtensionStatus().hasOpenedResource(currDocURL)) {
                Object res = xWikiExtension.getExtensionStatus().getOpenedResource(currDocURL);
                if (!(res instanceof XWikiPage))
                    throw new PagePublishException();
                XWikiPage page = (XWikiPage) res;
                publishPage(page);
            } else {
                List<SpaceSummary> spaces;
                if (xWikiExtension.getXWikiStructure() == null) {
                    XWikiXmlRpcClient client = xWikiExtension.getClient();
                    spaces = XWikiStructureActions.getSpaces(client);
                } else {
                    spaces = XWikiStructureActions.getSpaces(xWikiExtension.getXWikiStructure());
                }

                AddPageDialog addPageDialog =
                    new AddPageDialog(xWikiExtension.getComponentContext(), AddPageDialog.STEP_NEW_SPACE, spaces, null,
                        FLAG_PUBLISH);
                addPageDialog.show();
            }

        } catch (PageNullException ex) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR, Constants.ERROR_PAGEEMPTY,
                Constants.TYPE_ERROR, false);
        } catch (PagePublishException ex) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR, Constants.ERROR_PAGEPUBLISH,
                Constants.TYPE_ERROR, false);
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR, Constants.ERROR_PAGEPUBLISH,
                Constants.TYPE_ERROR, false);
        }
    }

    /**
     * Publishes a page to the XWiki server.
     * 
     * @param page the page to be published
     */
    public void publishPage(XWikiPage page)
    {
        try {

            XWikiXmlRpcClient client = xWikiExtension.getClient();
            OpenOfficeActions openOfficeActions = new OpenOfficeActions(this.xWikiExtension.getComponentContext());
            String curDocURL = xWikiExtension.getExtensionStatus().getCurrentDocURL();

            String htmlContent = openOfficeActions.getDocumentContent(curDocURL);
            String xwikiSyntaxContent = getXWikiSyntaxContent(htmlContent);

            String cleanedContent = uploadImageAttachments(page, xwikiSyntaxContent);

            page.setContent(cleanedContent);
            client.storePage(page);

            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_XWIKI, Constants.MESS_PUBLISHSUCC,
                Constants.TYPE_INFO, false);

            if (!NavigationDialog.isNull()) {
                NavigationDialog navigationDialog = NavigationDialog.getInstance();
                navigationDialog.refreshSpace(page.getSpace());
            }

        } catch (XmlRpcException e) {
            e.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR, Constants.ERROR_PAGEPUBLISH,
                Constants.TYPE_ERROR, false);
        } catch (ConversionException e) {
            e.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_FOMATNOTSUPPORTED, Constants.TYPE_ERROR, false);
        } catch (IllegalDataException e) {
            e.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_FOMATNOTSUPPORTED, Constants.TYPE_ERROR, false);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_FOMATNOTSUPPORTED, Constants.TYPE_ERROR, false);
        }

    }

    /**
     * Handles the add page Toolbar command
     */
    public void cmdAddPage()
    {
        if (!checkLogin(true))
            return;
        SpaceNode spaceNode = checkSpaceSelect(false);
        List<SpaceSummary> spaces;
        if (xWikiExtension.getXWikiStructure() == null) {
            XWikiXmlRpcClient client = xWikiExtension.getClient();
            spaces = XWikiStructureActions.getSpaces(client);
        } else {
            spaces = XWikiStructureActions.getSpaces(xWikiExtension.getXWikiStructure());
        }
        if (spaceNode == null) {
            AddPageDialog addPageDialog =
                new AddPageDialog(xWikiExtension.getComponentContext(), AddPageDialog.STEP_NEW_SPACE, spaces, null,
                    FLAG_OPEN_EDIT);
            addPageDialog.show();
        } else {
            AddPageDialog addPageDialog =
                new AddPageDialog(xWikiExtension.getComponentContext(), AddPageDialog.STEP_EXISTING_SPACE, spaces,
                    spaceNode.spaceSummary, FLAG_OPEN_EDIT);
            addPageDialog.show();
        }
    }

    /**
     * Adds a new page.
     * 
     * @param spaceName the space name where the page will be added
     * @param pageName the page name
     * @param pageTitle the page title
     * @param flags flags used specify the next action
     * @return the new added page
     */
    public XWikiPage addNewPage(String spaceName, String pageName, String pageTitle, int flags)
    {
        String pagefullName = getPageFullName(spaceName, pageName);

        XWikiPage xWikiPage = new XWikiPage(new HashMap<Object, Object>());
        xWikiPage.setSpace(spaceName);
        xWikiPage.setId(pagefullName);
        xWikiPage.setTitle(pageTitle);

        if ((flags & FLAG_OPEN_EDIT) != 0) {
            xWikiPage.setContent(newPageText);
            try {
                String htmlContent = startDocument + getHTMLContent(newPageText) + endDocument;
                XOOFileStorage filestorage = createStorage(pagefullName, pageTitle, htmlContent);
                String fileurl = convertToURL(filestorage.getOutputFile().getPath());
                openPageForEdit(fileurl, xWikiPage);

            } catch (ConversionException e) {
                // TODO Treat exception
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if ((flags & FLAG_PUBLISH) != 0) {
            publishPage(xWikiPage);
        }
        return xWikiPage;
    }

    /**
     * Handles the view in browser command
     */
    public void cmdViewInBrowser()
    {

        PageNode pageNode = checkPageSelect(true);
        if (pageNode == null)
            return;
        viewInBrowser(pageNode.pageSummary);
    }

    /**
     * Opens a specified page with the client default browser
     * 
     * @param pageSummary the page which will be opened
     */
    public void viewInBrowser(PageSummary pageSummary)
    {
        Utils.openWithDefaultBrowser(pageSummary.getUrl());
    }

    /**
     * Handles the dowload attachment Toolbar command
     */
    public void cmdDownloadAttachment()
    {
        Attachment att = checkAttSelect(true);
        if (att == null)
            return;
        DownloadAttDialog dialog = new DownloadAttDialog(xWikiExtension.getComponentContext(), att);
        dialog.show();
    }

    /**
     * Downloads an attachment.
     * 
     * @param att the attachment which will be downloaded
     * @param location the location used for saving the file
     */
    public void downloadAttachment(Attachment att, String location)
    {

        XWikiXmlRpcClient client = xWikiExtension.getClient();
        try {
            byte[] data = client.getAttachmentData(att.getPageId(), att.getFileName(), "0");
            File file = new File(location + att.getFileName());
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(data);
            fo.flush();
            fo.close();

            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_XWIKI, Constants.MESS_DOWNLOADSUCC,
                Constants.TYPE_INFO, false);

        } catch (XmlRpcException e) {
            e.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_DOWNLOAD_FAILED, Constants.TYPE_ERROR, false);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_DOWNLOAD_FAILED, Constants.TYPE_ERROR, false);
        }
    }

    /**
     * Handles the upload the current document as attachment command.
     */
    public void cmdUploadCurDocAsAttachment()
    {
        PageNode pageNode = checkPageSelect(true);
        if (pageNode == null)
            return;
        uploadCurDocAsAttachment(pageNode.pageSummary);

    }

    /**
     * Uploads the current document as attachment to a specified page
     * 
     * @param pageSummary the page where the current document will be attached
     */
    public void uploadCurDocAsAttachment(PageSummary pageSummary)
    {
        try {
            OpenOfficeActions openOfficeActions = new OpenOfficeActions(xWikiExtension.getComponentContext());
            String fileURL = openOfficeActions.getCurrentDocumentURL();
            if (fileURL == null)
                throw new FileNotFoundException();
            String filePath = openOfficeActions.convertFromURL(fileURL);

            uploadAttachment(pageSummary, filePath);

            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_XWIKI, Constants.MESS_UPLOADSUCC,
                Constants.TYPE_INFO, false);

            if (!NavigationDialog.isNull()) {
                NavigationDialog navigationDialog = NavigationDialog.getInstance();
                navigationDialog.refreshPageAtt(pageSummary);
            }

        } catch (FileNotFoundException e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_UPP_FILE_NOT_EXISTS, Constants.TYPE_ERROR, false);
            e.printStackTrace();

        } catch (XmlRpcException e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_UPPLOAD_FAILED, Constants.TYPE_ERROR, false);
            e.printStackTrace();

        } catch (IOException e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_UPPLOAD_FAILED, Constants.TYPE_ERROR, false);
            e.printStackTrace();

        } catch (FileTooBigException e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_UPP_FILE_TOO_BIG, Constants.TYPE_ERROR, false);
            e.printStackTrace();

        } catch (Exception e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_UPPLOAD_FAILED, Constants.TYPE_ERROR, false);
            e.printStackTrace();
        }
    }

    /**
     * Uploads a specified file as attachment at a page.
     * 
     * @param pageSummary the page where the file will be attached
     * @param filePath the path of the file that will become an attachment
     * @throws FileTooBigException
     * @throws IOException
     * @throws XmlRpcException
     */
    private void uploadAttachment(PageSummary pageSummary, String filePath) throws FileTooBigException, IOException,
        XmlRpcException
    {
        File file = new File(filePath);
        long len = file.length();
        if (len > Integer.MAX_VALUE) {
            throw new FileTooBigException();
        }
        byte data[] = new byte[(int) len];
        FileInputStream fi = new FileInputStream(file);
        fi.read(data);
        fi.close();

        XWikiXmlRpcClient client = xWikiExtension.getClient();
        Attachment att = new Attachment(new HashMap<Object, Object>());
        att.setFileName(file.getName());
        att.setContentType("text/plain");
        att.setPageId(pageSummary.getId());
        client.addAttachment(0, att, data);

    }

    /**
     * Handles the add page Toolbar command
     * 
     * @param pos the position of the navigation panel
     */
    public void cmdAddSpace(int pos)
    {
        if (!checkLogin(true))
            return;
        AddSpaceDialog dialog = new AddSpaceDialog(xWikiExtension.getComponentContext(), pos);
        dialog.show();

    }

    /**
     * Adds a new Space at the XWiki Server.
     * 
     * @param spaceName
     */
    public void addSpace(String spaceName)
    {
        try {
            XWikiXmlRpcClient client = xWikiExtension.getClient();
            Space space = new Space(new HashMap<Object, Object>());
            space.setKey(spaceName);
            client.addSpace(space);
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_XWIKI, Constants.MESS_ADDSPACESUCC,
                Constants.TYPE_INFO, false);

            if (!NavigationDialog.isNull()) {
                NavigationDialog navigationDialog = NavigationDialog.getInstance();
                navigationDialog.refreshSpace(spaceName);
            }

        } catch (XmlRpcException e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_ADDSPACE_FAILED, Constants.TYPE_ERROR, false);
            e.printStackTrace();
        } catch (Exception e) {
            Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                Constants.ERROR_ADDSPACE_FAILED, Constants.TYPE_ERROR, false);
            e.printStackTrace();
        }
    }

    /**
     * Checks if the user is loggedIn.
     * 
     * @param needed true, if the user must be logged in , false otherwise
     * @return true if the user is logged in , false otherwise
     */
    private boolean checkLogin(boolean needed)
    {
        try {
            XWikiXmlRpcClient client = xWikiExtension.getClient();
            if (client == null || !xWikiExtension.getExtensionStatus().isLoggedIn())
                throw new NotLoggedInException();
            return true;
        } catch (NotLoggedInException ex) {
            if (needed) {
                Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                    Constants.ERROR_LOGINREQUESTED, Constants.TYPE_ERROR, false);
            }
        }
        return false;

    }

    /**
     * Checks if a space is selected.
     * 
     * @param needed true if a space must be selected, false otherwise
     * @return the selected spaceNode
     */
    private SpaceNode checkSpaceSelect(boolean needed)
    {

        try {
            if (NavigationDialog.isNull())
                throw new SpaceNotSelectedException();
            NavigationDialog navigationDialog = NavigationDialog.getInstance();
            SpaceNode spaceNode = navigationDialog.getSelectedSpaceNode();
            if (spaceNode == null)
                throw new SpaceNotSelectedException();
            return spaceNode;
        } catch (SpaceNotSelectedException ex) {
            if (needed)
                Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                    Constants.ERROR_SPACESELECT, Constants.TYPE_ERROR, false);
        }
        return null;
    }

    /**
     * Checks if a page is selected.
     * 
     * @param needed true if a page must be selected, false otherwise
     * @return the selected pageNode
     */
    private PageNode checkPageSelect(boolean needed)
    {
        if (!checkLogin(true))
            return null;

        try {
            if (NavigationDialog.isNull())
                throw new PageNotSelectedException();
            NavigationDialog navigationDialog = NavigationDialog.getInstance();
            PageNode pageNode = navigationDialog.getSelectedPageNode();
            if (pageNode == null)
                throw new PageNotSelectedException();
            return pageNode;

        } catch (PageNotSelectedException ex) {
            if (needed)
                Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                    Constants.ERROR_PAGESELECT, Constants.TYPE_ERROR, false);
        }

        return null;

    }

    /**
     * Checks if an attachment is selected.
     * 
     * @param needed true if an attachment must be selected, false otherwise
     * @return the selected attachment
     */
    private Attachment checkAttSelect(boolean needed)
    {

        try {
            if (NavigationDialog.isNull())
                throw new AttachmentNotSelectedException();
            NavigationDialog navigationDialog = NavigationDialog.getInstance();
            Attachment attNode = navigationDialog.getSelectedAttNode();
            if (attNode == null)
                throw new AttachmentNotSelectedException();
            return attNode;

        } catch (AttachmentNotSelectedException ex) {
            if (needed)
                Utils.ShowMessage(xWikiExtension.getComponentContext(), Constants.TITLE_ERROR,
                    Constants.ERROR_ATTSELECT, Constants.TYPE_ERROR, false);
        }

        return null;
    }

    /**
     * Gets the converted input text from the XWiki2.0 Syntax in XHTML 1.0 Syntax
     * 
     * @param content the text to be converted
     * @return the converted content
     * @throws ConversionException
     */
    private String getHTMLContent(String content) throws ConversionException
    {
        if (content == null || content.length() == 0)
            return "";

        EmbeddableComponentManager ecm = xWikiExtension.getComponentManager();
        BidirectionalConverter converter = new DefaultBidirectionalConverter(ecm);
        String result = converter.toXHTML(content);

        return result;
    }

    /**
     * Gets the converted input text from HTML Syntax in XWiki 2.0 Syntax
     * 
     * @param htmlContent the text to be converted
     * @return the converted result
     * @throws Exception
     */
    private String getXWikiSyntaxContent(String htmlContent) throws Exception
    {
        if (htmlContent == null)
            return null;

        EmbeddableComponentManager ecm = xWikiExtension.getComponentManager();
        BidirectionalConverter converter = new DefaultBidirectionalConverter(ecm);
        XOOHTMLCleaner cleaner = new DefaultXOOHTMLCleaner(ecm);
        String result = converter.fromXHTML(cleaner.clean(htmlContent));

        return result;
    }

    /**
     * Creates a new XOOFileStorage
     * 
     * @param storageName the name of the storage folder
     * @param outputFileName the name of the page
     * @param content the content of the page
     * @return the created XOOFileStorage
     * @throws IOException
     */
    private XOOFileStorage createStorage(String storageName, String outputFileName, String content) throws IOException
    {
        XOOFileStorage fileStorage = new XOOFileStorage(storageName, outputFileName);
        FileWriter writer = new FileWriter(fileStorage.getOutputFile());
        writer.write(content);
        writer.close();
        return fileStorage;

    }

    /**
     * Converts a file path to a URL
     * 
     * @param filepath the input filepath
     * @return the associated URL
     */
    public String convertToURL(String filepath)
    {
        OpenOfficeActions openOfficeActions = new OpenOfficeActions(this.xWikiExtension.getComponentContext());
        String fileurl = openOfficeActions.convertToURL("", filepath);
        return fileurl;
    }

    /**
     * Downloads image attachments contained in a specified page.
     * 
     * @param page the information about the page
     * @param content the content of the page
     * @param location the location where the images will be downloaded
     */
    private void downloadImageAttachments(PageSummary page, String content, String location)
    {
        EmbeddableComponentManager ecm = xWikiExtension.getComponentManager();
        XOOConverterSupport converterSupport = new DefaultConverterSupport(ecm);

        List<Attachment> attachments = converterSupport.getAllImageAttachments(content, Syntax.XWIKI_2_0);
        String curPage = page.getId();
        XWikiXmlRpcClient client = xWikiExtension.getClient();
        for (Attachment att : attachments) {
            try {
                String pageId = att.getPageId();
                String fileName = att.getId();
                byte[] data = client.getAttachmentData(pageId == null ? curPage : pageId, fileName, "0");
                File file = new File(location + fileName);
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(data);
                fo.flush();
                fo.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Uploads images contained in a page.
     * 
     * @param pageSummary the information about the page
     * @param content the content of the page
     * @return the modified content of the page
     * @throws FileTooBigException
     * @throws IOException
     * @throws XmlRpcException
     */
    private String uploadImageAttachments(PageSummary pageSummary, String content) throws FileTooBigException,
        IOException, XmlRpcException
    {
        EmbeddableComponentManager ecm = xWikiExtension.getComponentManager();
        XOOConverterSupport converterSupport = new DefaultConverterSupport(ecm);

        List<Attachment> attachments = new ArrayList<Attachment>();
        String result = converterSupport.imageNameCleaner(content, Syntax.XWIKI_2_0, attachments);

        for (Attachment att : attachments) {
            try {
                uploadAttachment(pageSummary, att.getId());
            } catch (Exception e) {
            }
        }

        return result;

    }

    /**
     * Gets the complete name of a page.
     * 
     * @param pageSummary the page summary
     * @return the complete name of the page
     */
    private String getPageFullName(PageSummary pageSummary)
    {
        return pageSummary.getId();

    }

    /**
     * Gets the complete name of a page.
     * 
     * @param spaceName the space name
     * @param pageName the page name
     * @return the complete name of the page
     */
    private String getPageFullName(String spaceName, String pageName)
    {
        return spaceName + "." + pageName;
    }

}
