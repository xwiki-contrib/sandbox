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

import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.awt.XTopWindow;
import com.sun.star.awt.XTopWindowListener;
import com.sun.star.awt.XWindow;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;

/**
 * The base class for the dialogs designed with the Basic Editor
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class XWikiDialog implements XDialogEventHandler, XTopWindowListener
{

    XComponentContext m_xContext;

    XControlContainer m_xControlContainer;

    XMultiComponentFactory xMCF;

    XDialog m_xDialog;

    String[] m_aMethods;

    boolean m_bAction = false;

    protected Thread m_aThread;

    protected boolean m_bThreadFinished = false;

    /**
     * Constructor. Creates an empty object.
     */
    public XWikiDialog()
    {
    }

    /**
     * Constructor. Creates a dialog from the basic dialog library.
     * 
     * @param c The OpenOffice component context
     * @param DialogURL the URL of the dialog from the dialog library
     */
    public XWikiDialog(XComponentContext c, String DialogURL)
    {
        this.m_xContext = c;
        this.xMCF = m_xContext.getServiceManager();
        try {
            Object obj = xMCF.createInstanceWithContext("com.sun.star.awt.DialogProvider2", m_xContext);
            XDialogProvider2 xDialogProvider =
                (XDialogProvider2) UnoRuntime.queryInterface(XDialogProvider2.class, obj);

            m_xDialog = xDialogProvider.createDialogWithHandler(DialogURL, this);
            m_xControlContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, m_xDialog);
            XTopWindow xTopWindow = (XTopWindow) UnoRuntime.queryInterface(XTopWindow.class, m_xDialog);
            if (xTopWindow != null)
                xTopWindow.addTopWindowListener(this);
        } catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Shows the Dialog.
     */
    public void show()
    {
        if (m_xDialog != null) {
            m_xDialog.execute();
            disposeDialog();
        }

    }

    /**
     * @param sControl a control from the current dialog
     * @return the set of its properties
     */
    protected XPropertySet getPropSet(String sControl)
    {
        return getPropSet(m_xControlContainer, sControl);
    }

    /**
     * @param sControl the name of a control from the current dialog
     * @return the XControl implementation of the given name
     */
    protected XControl getControl(String sControl)
    {
        return m_xControlContainer.getControl(sControl);
    }

    /**
     * Gets the set of properties of a control contained by a control container
     * 
     * @param xControlContainer the control container with the specified control.
     * @param sControl the name of the control
     * @return the set of the control's properties
     */
    protected static XPropertySet getPropSet(XControlContainer xControlContainer, String sControl)
    {
        XPropertySet xPS = null;

        if (xControlContainer != null && sControl != null) {
            XControl xControl = xControlContainer.getControl(sControl);
            xPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControl.getModel());
        }

        if (xPS == null)
            throw new com.sun.star.uno.RuntimeException();

        return xPS;
    }

    protected XPropertySet getPropSet()
    {
        XControl dialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, m_xDialog);
        XControlModel xBasicDialogModel = dialogControl.getModel();
        XPropertySet xDialogPropertySet =
            (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xBasicDialogModel);
        return xDialogPropertySet;
    }

    public void disposeDialog()
    {
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xDialog);
        xComponent.dispose();
    }

    /**
     * Sets the focus on a specified control.
     * 
     * @param aControl the name of the control
     */
    public void setFocusTo(String aControl)
    {
        if (m_xControlContainer != null) {
            try {
                XWindow xWindow =
                    (XWindow) UnoRuntime.queryInterface(XWindow.class, m_xControlContainer.getControl(aControl));
                if (xWindow != null)
                    xWindow.setFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setFocus()
    {
        XControl dialogControl = (XControl) UnoRuntime.queryInterface(XControl.class, m_xDialog);
        XWindow xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, dialogControl);
        if (xWindow != null)
            xWindow.setFocus();

    }

    /**
     * {@inheritDoc}
     */
    public boolean callHandlerMethod(XDialog arg0, Object arg1, String arg2)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getSupportedMethodNames()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void windowActivated(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void windowClosed(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void windowClosing(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void windowDeactivated(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void windowMinimized(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void windowNormalized(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void windowOpened(EventObject arg0)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void disposing(EventObject arg0)
    {
    }

}
