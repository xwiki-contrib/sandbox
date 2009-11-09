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

import java.util.List;

import org.codehaus.swizzle.confluence.SpaceSummary;

import com.sun.star.awt.XDialog;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.XComponentContext;

/**
 * The dialog used for adding a new page.
 * 
 * @version $Id: $
 * @since 1.0 M
 */
public class AddPageDialog extends XWikiDialog
{
    /* The textbox for writing the name of new space is visible */
    static short STEP_NEW_SPACE = 1;

    /* The combobox for choosing the name of the existing space is visible */
    static short STEP_EXISTING_SPACE = 2;

    /* Method name for the btnCancel Clicked event */
    private final String sCancelMethod = "btnCancel_Clicked";

    /* Method name for the btnOK Clicked event */
    private final String sOKMethod = "btnOK_Clicked";

    /* Method name for the optNewSpace Clicked event */
    private final String sNewSpaceMethod = "optNewSpace_Clicked";

    /* Method name for the optExSpace Clicked event */
    private final String sExSpaceMethod = "optExSpace_Clicked";

    /* The space names that are displayed in the combobox for choosing an existing space */
    private String[] sSpaces;

    /* Flags used for specify the next action */
    private int flags;

    /**
     * Constructor.
     * 
     * @param c the OpenOffice component context
     * @param step the dialog step which is used for displaying either a textbox either a combobox
     * @param spaces the space names that are displayed in the combobox for choosing an existing space
     * @param selectedSpace the space name which is selected in combobox
     * @param flags specifies the next action
     */
    public AddPageDialog(XComponentContext c, short step, List<SpaceSummary> spaces, SpaceSummary selectedSpace,
        int flags)
    {
        super(c, Constants.ADD_PAGE_DIALOG);
        this.flags = flags;
        try {
            sSpaces = new String[spaces.size()];
            for (int i = 0; i < spaces.size(); i++)
                sSpaces[i] = spaces.get(i).getKey();
            getPropSet("cmbSpaceName").setPropertyValue("StringItemList", sSpaces);

            if (selectedSpace != null) {
                getPropSet("cmbSpaceName").setPropertyValue("Text", selectedSpace.getKey());
                getPropSet().setPropertyValue("Step", AddPageDialog.STEP_EXISTING_SPACE);
                getPropSet("optExSpace").setPropertyValue("State", (short) 1);

            } else {
                getPropSet().setPropertyValue("Step", AddPageDialog.STEP_NEW_SPACE);
                getPropSet("optNewSpace").setPropertyValue("State", (short) 1);
            }
            XWikiExtension xWikiExtension = XWikiExtension.getInstance();

            String urlOkButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_OK_BUTTON;
            String urlCancelButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_CANCEL_BUTTON;

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
        } catch (Throwable t) {
            t.printStackTrace();
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
        } else if (MethodName.equals(sExSpaceMethod)) {
            optEXSpace_Clicked();
        } else if (MethodName.equals(sNewSpaceMethod)) {
            optNewSpace_Clicked();
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

            short nExChecked = ((Short) (getPropSet("optExSpace").getPropertyValue("State"))).shortValue();
            short nNewChecked = ((Short) (getPropSet("optNewSpace").getPropertyValue("State"))).shortValue();

            String sSpaceName = null;
            if (nExChecked == 1) {
                sSpaceName = (String) (getPropSet("cmbSpaceName").getPropertyValue("Text"));
            } else if (nNewChecked == 1) {
                sSpaceName = (String) (getPropSet("txtSpaceName").getPropertyValue("Text"));
            }
            if (sSpaceName == null || sSpaceName.length() == 0) {
                Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_ERROR, Constants.ERROR_SPACENAMENULL,
                    Constants.TYPE_ERROR, false);
            }
            String sPageName = (String) getPropSet("txtPageName").getPropertyValue("Text");
            if (sPageName == null || sPageName.length() == 0) {
                Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_ERROR, Constants.ERROR_PAGENAMENULL,
                    Constants.TYPE_ERROR, false);
            }
            String sPageTitle = (String) getPropSet("txtPageTitle").getPropertyValue("Text");
            if (sPageTitle == null || sPageTitle.length() == 0) {
                Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_ERROR, Constants.ERROR_PAGETITLENULL,
                    Constants.TYPE_ERROR, false);
            }
            m_xDialog.endExecute();
            XWikiExtension xWikiExtension = XWikiExtension.getInstance();
            XWikiExtensionActions xWikiExtensionActions = new XWikiExtensionActions(xWikiExtension);
            xWikiExtensionActions.addNewPage(sSpaceName, sPageName, sPageTitle, flags);

        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        }

    }

    /**
     * The handler for the clicked event of the New Space radio button.
     */
    private void optNewSpace_Clicked()
    {
        try {
            getPropSet().setPropertyValue("Step", STEP_NEW_SPACE);
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
     * The handler for the clicked event of the Existing Space radio button.
     */
    private void optEXSpace_Clicked()
    {
        try {
            getPropSet().setPropertyValue("Step", STEP_EXISTING_SPACE);
            if (sSpaces.length > 0) {
                getPropSet("cmbSpaceName").setPropertyValue("Text", sSpaces[0]);
            }
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
}
