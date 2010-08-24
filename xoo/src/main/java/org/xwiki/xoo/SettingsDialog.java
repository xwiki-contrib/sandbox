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

import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import com.sun.star.awt.XDialog;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.XComponentContext;

/**
 * The dialog with the user settings used for the communication with the XE server
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class SettingsDialog extends XWikiDialog
{
    /* Method name for the btnOK Clicked event */
    private final String sOKMethod = "btnOK_Clicked";

    /* Method name for the btnCancel Clicked event */
    private final String sCancelMethod = "btnCancel_Clicked";

    /* Method name for the btnCustom Clicked event */
    private final String sCustomMethod = "btnCustom_Clicked";

    private String[] credentials = null;

    /**
     * Constructor.
     * 
     * @param c the OpenOffice component context
     */

    public SettingsDialog(XComponentContext c)
    {
        super(c, Constants.SETTINGS_DIALOG);
        LoginData loginData = new LoginData();
        XWikiExtension xWikiExtension = XWikiExtension.getInstance();
       
        if (loginData.canAutoLogin()) {
            credentials = loginData.getCredentials();
            ConnectionSettings set = xWikiExtension.getSettings();

            set.setServerURL(credentials[0]);
            set.setPassword(credentials[1]);
            set.setPassword(credentials[2]);
            set.setWikiURL(credentials[3]);
            set.setXmlRpcURL(credentials[4]);

            try {
                getPropSet("txtServerURL").setPropertyValue("Text", credentials[0]);
                getPropSet("txtUsername").setPropertyValue("Text", credentials[1]);
                // TODO secure store for the password
                getPropSet("txtPassword").setPropertyValue("Text", credentials[2]);

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

        String urlOkButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_OK_BUTTON;
        String urlCancelButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_CANCEL_BUTTON;
        String urlCustomButton = xWikiExtension.getImagesDirUrl() + "/" + Constants.IMG_CONN_SETTINGS;

        try {

            getPropSet("btnOK").setPropertyValue("ImageURL", urlOkButton);
            getPropSet("btnCancel").setPropertyValue("ImageURL", urlCancelButton);
            getPropSet("cmdCustom").setPropertyValue("ImageURL", urlCustomButton);

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
        } else if (MethodName.equals(sCustomMethod)) {
            btnCustom_Clicked();
        }

        return true;
    }

    /**
     * The handler for the clicked event of the OK button.
     */
    private void btnOK_Clicked()
    {

        boolean bResult = false;

        try {
            String sServerURL = (String) getPropSet("txtServerURL").getPropertyValue("Text");
            String sUserName = (String) getPropSet("txtUsername").getPropertyValue("Text");
            String sPassword = (String) getPropSet("txtPassword").getPropertyValue("Text");
            Short nState = (Short) getPropSet("chkRemember").getPropertyValue("State");

            if (sServerURL.endsWith("/"))
                sServerURL = sServerURL.substring(0, sServerURL.length() - 1);

            XWikiExtension xWikiExtension = XWikiExtension.getInstance();
            ConnectionSettings set = xWikiExtension.getSettings();
            set.setServerURL(sServerURL);
            xWikiExtension.resetClient();
            set.setUsername(sUserName);
            set.setPassword(sPassword);

            if (nState == 0) {
                LoginData loginData = new LoginData();
                loginData.clearCredentials();
            } else if (nState == 1) {
                String new_credentials[] =
                    {sServerURL, sUserName, sPassword, xWikiExtension.getSettings().getWikiURL(),
                    xWikiExtension.getSettings().getXmlRpcURL()};
                LoginData loginData = new LoginData();
                loginData.writeCredentials(new_credentials);
            }

            XWikiXmlRpcClient client = xWikiExtension.getClient();
            if (client != null) {
                bResult = xWikiExtension.doLogin();
            }
            if (bResult) {
                m_xDialog.endExecute();
                xWikiExtension.getExtensionStatus().setLoginStatus(true);
                Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_XWIKI, Constants.MESS_LOGINSUCC,
                    Constants.TYPE_INFO, false);
            } else {
                Utils.ShowMessage(m_xContext, m_xDialog, Constants.TITLE_ERROR, Constants.ERROR_LOGINFAILED,
                    Constants.TYPE_ERROR, false);
            }
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

    /**
     * The handler for the clicked event of the Custom button.
     */
    private void btnCustom_Clicked()
    {
        CustomSettingsDialog cset = new CustomSettingsDialog(m_xContext);
        cset.show();
    }
}
