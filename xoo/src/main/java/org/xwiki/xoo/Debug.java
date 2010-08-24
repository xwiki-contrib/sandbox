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

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowAttribute;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.frame.XFrame;

/**
 * A class used for displaying messages for debugging
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class Debug
{

    private static Debug _instance;

    private XFrame m_xFrame;

    private XToolkit m_xToolkit;

    /**
     * Constructor. It is private because the class is a singleton
     */
    private Debug()
    {
    }

    /**
     * Sets the context of the debug dialog window
     * 
     * @param m_xFrame the parent frame
     * @param m_xToolkit the toolkit component
     */
    public void setContext(XFrame m_xFrame, XToolkit m_xToolkit)
    {
        this.m_xFrame = m_xFrame;
        this.m_xToolkit = m_xToolkit;
    }

    /**
     * Calls the constructor or gets a stored instance.
     * 
     * @return A instance of this class
     */
    public static Debug getInstance()
    {
        if (null == _instance) {
            _instance = new Debug();
        }
        return _instance;
    }

    /**
     * Shows a dialog used for debugging
     * 
     * @param sTitle the title of the dialog
     * @param sMessage the message to be displayed
     */
    public void showMessageBox(String sMessage)
    {
        try {
            if (null != m_xFrame && null != m_xToolkit) {

                // describe window properties.
                WindowDescriptor aDescriptor = new WindowDescriptor();
                aDescriptor.Type = WindowClass.MODALTOP;
                aDescriptor.WindowServiceName = new String("infobox");
                aDescriptor.ParentIndex = -1;
                aDescriptor.Parent =
                    (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, m_xFrame.getContainerWindow());
                aDescriptor.Bounds = new Rectangle(0, 0, 300, 200);
                aDescriptor.WindowAttributes =
                    WindowAttribute.BORDER | WindowAttribute.MOVEABLE | WindowAttribute.CLOSEABLE;

                XWindowPeer xPeer = m_xToolkit.createWindow(aDescriptor);
                if (null != xPeer) {
                    XMessageBox xMsgBox = (XMessageBox) UnoRuntime.queryInterface(XMessageBox.class, xPeer);
                    if (null != xMsgBox) {
                        xMsgBox.setCaptionText("Debug");
                        xMsgBox.setMessageText(sMessage);
                        xMsgBox.execute();
                    }
                }
            }
        } catch (com.sun.star.uno.Exception e) {
            // TODO error handling
        }
    }

}
