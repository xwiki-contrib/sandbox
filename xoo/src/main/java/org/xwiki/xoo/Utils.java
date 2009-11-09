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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * A general class with useful methods
 * 
 * @version $Id: $
 * @since 1.0 M
 */

public class Utils
{

    /**
     * Gets the text to be displayed in an information or error messagebox
     * 
     * @param nMessID the id of the message
     * @param nTypeID the type of the message (error or information)
     * @return the text to be displayed
     */
    private static String getText(int nMessID, int nTypeID)
    {
        String sMessage = null;

        switch (nTypeID) {
            case Constants.TYPE_ERROR:
                sMessage =
                    (Constants.errorMessages.length > nMessID) ? Constants.errorMessages[nMessID] : "Error: " + nMessID;
                break;
            case Constants.TYPE_INFO:
                sMessage = (Constants.Messages.length > nMessID) ? Constants.Messages[nMessID] : "Error: " + nMessID;
                break;
        }
        return sMessage;
    }

    protected static void ShowMessage(XComponentContext xContext, String sTitle, int nMessID, int nTypeID,
        boolean bQuery)
    {

        XMultiComponentFactory xmcf = xContext.getServiceManager();

        Object desktop;
        try {
            desktop = xmcf.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
            XFrame m_xFrame = xDesktop.getCurrentFrame();
            XWindowPeer xPeer =
                (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, m_xFrame.getContainerWindow());
            ShowMessage(xContext, xPeer, sTitle, nMessID, nTypeID, bQuery);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    protected static boolean ShowMessage(XComponentContext xContext, XDialog xDialog, String sTitle, int nMessID,
        int nTypeID, boolean bQuery)
    {
        XWindowPeer xPeer = null;
        XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, xDialog);
        if (xControl != null)
            xPeer = xControl.getPeer();
        return ShowMessage(xContext, xPeer, sTitle, nMessID, nTypeID, bQuery);
    }

    protected static boolean ShowMessage(XComponentContext xContext, XWindowPeer xParentPeer, String sTitle,
        int nMessID, int nTypeID, boolean bQuery)
    {
        boolean bResult = false;

        if (xContext != null) {
            String sMessage = getText(nMessID, nTypeID);

            if (xParentPeer != null) {
                XMessageBoxFactory xMBFactory = null;
                XMessageBox xMB = null;
                try {
                    XMultiComponentFactory xFactory = xContext.getServiceManager();
                    if (xFactory != null)
                        xMBFactory =
                            (XMessageBoxFactory) UnoRuntime.queryInterface(XMessageBoxFactory.class, xFactory
                                .createInstanceWithContext("com.sun.star.awt.Toolkit", xContext));

                    if (xMBFactory != null) {
                        if (bQuery) {
                            xMB =
                                xMBFactory.createMessageBox(xParentPeer, new com.sun.star.awt.Rectangle(), "querybox",
                                    MessageBoxButtons.BUTTONS_YES_NO | MessageBoxButtons.DEFAULT_BUTTON_NO, sTitle,
                                    sMessage);
                        } else if (nTypeID == Constants.TYPE_INFO) {
                            xMB =
                                xMBFactory.createMessageBox(xParentPeer, new com.sun.star.awt.Rectangle(), "infobox",
                                    MessageBoxButtons.BUTTONS_YES_NO, sTitle, sMessage);
                        }

                        else {
                            xMB =
                                xMBFactory.createMessageBox(xParentPeer, new com.sun.star.awt.Rectangle(), "errorbox",
                                    MessageBoxButtons.BUTTONS_OK, sTitle, sMessage);
                        }
                        if (xMB != null) {
                            short ret = xMB.execute();
                            if (bQuery && ret == (short) 3)
                                bResult = false;
                            else
                                bResult = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    if (xMB != null)
                        Dispose(xMB);
                }
            }
        }

        return bResult;
    }

    /**
     * Disposes a component
     * 
     * @param oObject the object to dispose
     */
    public static void Dispose(Object oObject)
    {
        if (oObject != null) {
            XComponent xComp = (XComponent) UnoRuntime.queryInterface(XComponent.class, oObject);
            if (xComp != null)
                xComp.dispose();

        }
    }

    public static void openWithDefaultBrowser(String url)
    {

        if (!Desktop.isDesktopSupported()) {
            System.out.println("Desktop is not supported (fatal)");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {

            System.out.println("Desktop doesn't support the browse action (fatal)");
            return;
        }

        try {
            URI uri = new URI(url);
            desktop.browse(uri);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

    }
}
