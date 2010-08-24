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

import com.sun.star.awt.WindowAttribute;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * The base class for the non-modal dialogs designed with the Basic Editor
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class XWikiModelessDialog extends XWikiDialog
{

    protected XWindowPeer xPeer;

    /**
     * Constructor. Creates a non - modal dialog
     * 
     * @param c The OpenOffice component context
     * @param DialogURL the URL of the dialog from the dialog library
     */
    public XWikiModelessDialog(XComponentContext c, String DialogURL)
    {
        this.m_xContext = c;
        this.xMCF = m_xContext.getServiceManager();
        try {
            Object obj;
            obj = xMCF.createInstanceWithContext("com.sun.star.awt.DialogProvider2", m_xContext);
            XDialogProvider2 xDialogProvider =
                (XDialogProvider2) UnoRuntime.queryInterface(XDialogProvider2.class, obj);
            XDialog xDialog = xDialogProvider.createDialogWithHandler(DialogURL, this);
            XControl dialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, xDialog);
            XControlModel xBasicDialogModel = dialogControl.getModel();

            Object dialog = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, dialog);
            xControl.setModel(xBasicDialogModel);

            XToolkit xToolkit =
                (XToolkit) UnoRuntime.queryInterface(XToolkit.class, xMCF.createInstanceWithContext(
                    "com.sun.star.awt.Toolkit", m_xContext));

            Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
            XFrame m_xFrame = xDesktop.getCurrentFrame();
            XWindow xdefaultWindow = m_xFrame.getComponentWindow();
            WindowDescriptor aDescriptor = new WindowDescriptor();
            aDescriptor.Type = WindowClass.TOP;
            aDescriptor.WindowServiceName = "";
            aDescriptor.ParentIndex = -1;
            aDescriptor.Parent = xToolkit.getDesktopWindow();
            aDescriptor.Bounds = xdefaultWindow.getPosSize();
            aDescriptor.WindowAttributes =
                WindowAttribute.BORDER | WindowAttribute.MOVEABLE | WindowAttribute.SIZEABLE
                    | WindowAttribute.CLOSEABLE;

            xPeer = xToolkit.createWindow(aDescriptor);
            XWindow xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, xPeer);
            xWindow.setVisible(false);
            xControl.createPeer(xToolkit, xPeer);

            m_xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, dialog);
            m_xControlContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, m_xDialog);

        } catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace();
        }
    }

}
