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

import com.sun.star.awt.PosSize;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * The dialog used for adding a new space.
 * 
 * @version $Id: $
 * @since 1.0 M
 */
public class AddSpaceDialog extends XWikiDialog
{
    /* Method name for the btnCancel Clicked event */
    private final String sCancelMethod = "btnCancel_Clicked";

    /* Method name for the btnOK Clicked event */
    private final String sOKMethod = "btnOK_Clicked";

    /**
     * Constructor.
     * 
     * @param c the OpenOffice component context
     * @param pos the position of the navigation panel
     */
    public AddSpaceDialog(XComponentContext c, int pos)
    {
        super(c, Constants.ADD_SPACE_DIALOG);

        XControl dialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, m_xDialog);
        XWindow dialogWin = (XWindow) UnoRuntime.queryInterface(XWindow.class, dialogControl);
        dialogWin.setPosSize((-1) * pos / 2, 0, 0, 0, PosSize.X);

        XWikiExtension xWikiExtension = XWikiExtension.getInstance();
        
        String urlOkButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_OK_BUTTON;
        String urlCancelButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_CANCEL_BUTTON;
        

        try {

            getPropSet("btnOK").setPropertyValue("ImageURL", urlOkButton);
            getPropSet("btnCancel").setPropertyValue("ImageURL", urlCancelButton);

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
        }
        return true;
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
            String sSpaceName = (String) (getPropSet("txtSpaceName").getPropertyValue("Text"));
            XWikiExtension xWikiExtension = XWikiExtension.getInstance();
            if (sSpaceName == null || sSpaceName.length() == 0) {
                Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_ERROR, Constants.ERROR_SPACENAMENULL,
                    Constants.TYPE_ERROR, false);
            }
            XWikiExtensionActions xWikiExtensionActions = new XWikiExtensionActions(xWikiExtension);
            xWikiExtensionActions.addSpace(sSpaceName);

            m_xDialog.endExecute();
            
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }
    }
}
