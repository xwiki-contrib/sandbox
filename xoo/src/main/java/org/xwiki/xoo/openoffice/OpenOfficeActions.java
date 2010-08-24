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

package org.xwiki.xoo.openoffice;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * This class implements the actions related to the OpenOffice
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class OpenOfficeActions
{
    private XComponentContext xContext;

    /**
     * Constructor.
     * 
     * @param xContext the OpenOffice component context
     */
    public OpenOfficeActions(XComponentContext xContext)
    {
        this.xContext = xContext;

    }

    /**
     * Opens a file from a specified path
     * 
     * @param filepath the path of the file
     * @throws OpenFileException
     */
    public XComponent openFile(String loadUrl) throws OpenFileException
    {
        try {
            XMultiComponentFactory xMultiComponentFactory = xContext.getServiceManager();
            XComponentLoader xcomponentloader =
                (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, xMultiComponentFactory
                    .createInstanceWithContext("com.sun.star.frame.Desktop", xContext));

            PropertyValue[] loadProps = new PropertyValue[0];

            Object objectDocumentToStore = xcomponentloader.loadComponentFromURL(loadUrl, "_default", 0, loadProps);
            if (objectDocumentToStore == null)
                throw new OpenFileException();
            XComponent document = (XComponent) objectDocumentToStore;
            return document;

        } catch (Throwable t) {
            t.printStackTrace();
            throw new OpenFileException("OpenOffice failed to load the file from " + loadUrl);
        }
    }

    /**
     * Converts an URL into a system path using OOo API
     * 
     * @param sURLPath the URL of the file
     * @return the system path associated with the URL
     */
    public String convertFromURL(String sURLPath)
    {
        String sSystemPath = null;
        try {
            XMultiComponentFactory m_xMCF = xContext.getServiceManager();
            XFileIdentifierConverter xFileConverter =
                (XFileIdentifierConverter) UnoRuntime.queryInterface(XFileIdentifierConverter.class, m_xMCF
                    .createInstanceWithContext("com.sun.star.ucb.FileContentProvider", xContext));
            sSystemPath = xFileConverter.getSystemPathFromFileURL(sURLPath);

        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

        return sSystemPath;

    }

    /**
     * Converts a system path into an URL using OOo API
     * 
     * @param sBase the base of the path
     * @param sSystemPath the system path
     * @return the URL associated with the specified path
     */
    public String convertToURL(String sBase, String sSystemPath)
    {
        String sURL = null;
        try {
            XMultiComponentFactory m_xMCF = xContext.getServiceManager();
            XFileIdentifierConverter xFileConverter =
                (XFileIdentifierConverter) UnoRuntime.queryInterface(XFileIdentifierConverter.class, m_xMCF
                    .createInstanceWithContext("com.sun.star.ucb.FileContentProvider", xContext));
            sURL = xFileConverter.getFileURLFromSystemPath(sBase, sSystemPath);
        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }catch (java.lang.Exception e)
        {
            e.printStackTrace();
        }

        return sURL;

    }

    /**  
     * @return the current Document URL opened with OpenOffice
     */
    public String getCurrentDocumentURL()
    {
        try {
            XMultiComponentFactory xmcf = xContext.getServiceManager();
            Object desktop = xmcf.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);

            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class, desktop);
            XComponent document = xDesktop.getCurrentComponent();
            XModel xmodel = (XModel) UnoRuntime.queryInterface(XModel.class, document);

            if (xmodel != null) {
                return xmodel.getURL();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 
     * @return the current Frame 
     */
    public XFrame getCurrentFrame()
    {

        try {
            XMultiComponentFactory xmcf = xContext.getServiceManager();
            Object desktop = xmcf.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class, desktop);

            return xDesktop.getCurrentFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the content of a specified file
     * @param URL the URL of the input file
     * @return the content of the file
     */
    public String getDocumentContent(String URL)
    {
        String path = convertFromURL(URL);
        byte[] buffer = null;
        DataInputStream in = null;

        try {
            File f = new File(path);
            buffer = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            in.readFully(buffer);
            return new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
