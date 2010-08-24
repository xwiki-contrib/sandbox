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

import org.codehaus.swizzle.confluence.Attachment;
import org.xwiki.xoo.openoffice.OpenOfficeActions;

import com.sun.star.awt.XDialog;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFolderPicker;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * The dialog used to choose the download location.
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class DownloadAttDialog extends XWikiDialog
{
    /* The attachment which is downloaded */
    private Attachment attachment;

    /* Method name for the btnCancel Clicked event */
    private final String sCancelMethod = "btnCancel_Clicked";

    /* Method name for the btnOK Clicked event */
    private final String sOKMethod = "btnOK_Clicked";

    /* Method name for the btnBrowse Clicked event */
    private final String sBrowseMethod = "btnBrowse_Clicked";

    /**
     * Constructor.
     * 
     * @param c the OpenOffice component context
     * @param att the attachment which is downloaded
     */
    public DownloadAttDialog(XComponentContext c, Attachment att)
    {
        super(c, Constants.DOWLOAD_ATT_DIALOG);
        this.attachment = att;
        
        XWikiExtension xWikiExtension = XWikiExtension.getInstance();
        
        String urlOkButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_OK_BUTTON;
        String urlCancelButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_CANCEL_BUTTON;
        String urlBrowseButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_FOLDER;
        
        try {

            getPropSet("cmdOK").setPropertyValue("ImageURL", urlOkButton);
            getPropSet("cmdCancel").setPropertyValue("ImageURL", urlCancelButton);
            getPropSet("cmdBrowse").setPropertyValue("ImageURL", urlBrowseButton);

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    public boolean callHandlerMethod(XDialog xDialog, Object EventObject, String MethodName)
    {

        if (MethodName.equals(sOKMethod)) {
            btnOK_Clicked();
        } else if (MethodName.equals(sCancelMethod)) {
            btnCancel_Clicked();
        } else if (MethodName.equals(sBrowseMethod)) {
            btnBrowse_Clicked();
        }
        return true;
    }

    /**
     * The handler for the clicked event of the Browse button.
     */
    private void btnBrowse_Clicked()
    {

        OpenOfficeActions openOfficeActions = new OpenOfficeActions(m_xContext);
        String urlLocation = raiseFolderPicker("", "Save location");
        String location = openOfficeActions.convertFromURL(urlLocation);
        try {
            getPropSet("txtLocation").setPropertyValue("Text", location);
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * The handler for the clicked event of the Cancel button.
     */
    private void btnCancel_Clicked()
    {
        m_xDialog.endExecute();
    }

    /**
     * The handler for the clicked event of the OK button.
     */
    private void btnOK_Clicked()
    {
        try {

            String location = (String) (getPropSet("txtLocation").getPropertyValue("Text"));
            String defaultsep = File.separator;
            if (!location.endsWith(defaultsep))
                location = location + defaultsep;

            m_xDialog.endExecute();

            XWikiExtension xWikiExtension = XWikiExtension.getInstance();
            XWikiExtensionActions xWikiExtensionActions = new XWikiExtensionActions(xWikiExtension);
            File file = new File(location + attachment.getFileName());
            if (file.exists()) {
                if (Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_ERROR, Constants.ERROR_FILE_EXISTS,
                    Constants.TYPE_ERROR, true))
                    xWikiExtensionActions.downloadAttachment(attachment, location);
            } else {
                xWikiExtensionActions.downloadAttachment(attachment, location);
            }
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Raises a folderpicker in which the user can browse and select a path
     * 
     * @param _sDisplayDirectory the path to the directory that is initially displayed
     * @param _sTitle the title of the folderpicker
     * @return the path to the folder that the user has selected. if the user has closed the folderpicker by clicking
     *         the "Cancel" button an empty string is returned
     * @see com.sun.star.ui.dialogs.FolderPicker
     */
    private String raiseFolderPicker(String _sDisplayDirectory, String _sTitle)
    {
        String sReturnFolder = "";
        XComponent xComponent = null;
        try {
            Object oFolderPicker = xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FolderPicker", m_xContext);
            XFolderPicker xFolderPicker = (XFolderPicker) UnoRuntime.queryInterface(XFolderPicker.class, oFolderPicker);
            XExecutableDialog xExecutable =
                (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, oFolderPicker);
            xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, oFolderPicker);
            xFolderPicker.setDisplayDirectory(_sDisplayDirectory);
            xFolderPicker.setTitle(_sTitle);
            short nResult = xExecutable.execute();

            // User has clicked "Select" button
            if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK) {
                sReturnFolder = xFolderPicker.getDirectory();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (xComponent != null) {
                xComponent.dispose();
            }
        }
        return sReturnFolder;
    }

}
