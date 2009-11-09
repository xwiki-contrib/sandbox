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

import com.sun.star.awt.XDialog;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.XComponentContext;

/**
 * The dialog which allows the user to specify the customization he/she made at the XE install
 * 
 * @version $Id: $
 * @since 1.0 M
 */
public class CustomSettingsDialog extends XWikiDialog
{

    /* Method name for the btnOK Clicked event */
    private final String sOKMethod = "btnOK_Clicked";

    /* Method name for the btnCancel Clicked event */
    private final String sCancelMethod = "btnCancel_Clicked";

    /**
     * Constructor.
     * 
     * @param c the OpenOffice component context
     */
    public CustomSettingsDialog(XComponentContext c)
    {
        super(c, Constants.CUSTOM_SETTINGS_DIALOG);

        
        try {
            XWikiExtension xWikiExtension = XWikiExtension.getInstance();
            ConnectionSettings set = xWikiExtension.getSettings();
            getPropSet("txtWiki").setPropertyValue("Text", set.getWikiURL());
            getPropSet("txtXMLRPC").setPropertyValue("Text", set.getXmlRpcURL());

            String urlOkButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_OK_BUTTON;
            String urlCancelButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_CANCEL_BUTTON;

            getPropSet("cmdOK").setPropertyValue("ImageURL", urlOkButton);
            getPropSet("cmdCancel").setPropertyValue("ImageURL", urlCancelButton);

        
        } catch (UnknownPropertyException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (WrappedTargetException e) {
            e.printStackTrace();
        } catch (Exception e){
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
     * The handler for the clicked event of the OK button.
     */
    private void btnOK_Clicked()
    {
        try {
            String sWiki = (String) getPropSet("txtWiki").getPropertyValue("Text");
            String sXmlRpc = (String) getPropSet("txtXMLRPC").getPropertyValue("Text");

            if (sWiki.endsWith("/"))
                sWiki = sWiki.substring(0, sWiki.length() - 1);

            if (sXmlRpc.endsWith("/"))
                sXmlRpc = sXmlRpc.substring(0, sXmlRpc.length() - 1);

            XWikiExtension xWikiExtension = XWikiExtension.getInstance();
            ConnectionSettings set = xWikiExtension.getSettings();
            set.setWikiURL(sWiki);
            set.setXmlRpcURL(sXmlRpc);
            xWikiExtension.resetClient();

            m_xDialog.endExecute();

        } catch (UnknownPropertyException e) {
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
}
