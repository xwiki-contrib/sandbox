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

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XMacroExpander;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.awt.XToolkit;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;

/**
 * The main class of the AddOn
 * 
 * @version $Id$
 * @since 1.0 M
 */
public final class XOO extends WeakBase implements com.sun.star.lang.XServiceInfo,
    com.sun.star.frame.XDispatchProvider, com.sun.star.lang.XInitialization, com.sun.star.frame.XDispatch
{

    private final XComponentContext m_xContext;

    private com.sun.star.frame.XFrame m_xFrame;

    private static final String m_implementationName = XOO.class.getName();

    private static final String[] m_serviceNames = {"com.sun.star.frame.ProtocolHandler"};

    private XToolkit m_xToolkit;

    private XWikiExtensionActions xWikiExtensionActions;

    /**
     * Constructor.
     * 
     * @param context the component context
     */
    public XOO(XComponentContext context)
    {
        m_xContext = context;
        try {
            // Create the toolkit to have access to it later
            m_xToolkit =
                (XToolkit) UnoRuntime.queryInterface(XToolkit.class, m_xContext.getServiceManager()
                    .createInstanceWithContext("com.sun.star.awt.Toolkit", m_xContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the factory to create instances of the implementation of the service Without this method the register won't
     * work
     * 
     * @param sImplementationName the name of the implementation
     * @return the factory
     */
    public static XSingleComponentFactory __getComponentFactory(String sImplementationName)
    {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName))
            xFactory = Factory.createComponentFactory(XOO.class, m_serviceNames);
        return xFactory;
    }

    /**
     * Writes the necessary information into the registry. Without this method the register won't work
     * 
     * @param xRegistryKey the registry key
     * @return true if it succeeds, false otherwise
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey)
    {
        return Factory.writeRegistryServiceInfo(m_implementationName, m_serviceNames, xRegistryKey);
    }

    /**
     * {@inheritDoc}
     */
    public String getImplementationName()
    {
        return m_implementationName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsService(String sService)
    {
        int len = m_serviceNames.length;

        for (int i = 0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getSupportedServiceNames()
    {
        return m_serviceNames;
    }

    /**
     * {@inheritDoc}
     */
    public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL, String sTargetFrameName,
        int iSearchFlags)
    {
        if (aURL.Protocol.compareTo(Constants.PROTOCOL_XOOMENU) == 0) {
            if (aURL.Path.compareTo(Constants.CMD_LOGIN) == 0 || aURL.Path.compareTo(Constants.CMD_TREE) == 0)
                return this;

        } else if (aURL.Protocol.compareTo(Constants.PROTOCOL_XOOTOOLBAR) == 0) {
            if (aURL.Path.compareTo(Constants.CMD_ADDPAGE) == 0 || aURL.Path.compareTo(Constants.CMD_EDITPAGE) == 0
                || aURL.Path.compareTo(Constants.CMD_PUBLISHPAGE) == 0
                || aURL.Path.compareTo(Constants.CMD_VIEWINBROWSER) == 0
                || aURL.Path.compareTo(Constants.CMD_DOWNATT) == 0 || aURL.Path.compareTo(Constants.CMD_UPATT) == 0)
                return this;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public com.sun.star.frame.XDispatch[] queryDispatches(com.sun.star.frame.DispatchDescriptor[] seqDescriptors)
    {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher = new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for (int i = 0; i < nCount; ++i) {
            seqDispatcher[i] =
                queryDispatch(seqDescriptors[i].FeatureURL, seqDescriptors[i].FrameName, seqDescriptors[i].SearchFlags);
        }
        return seqDispatcher;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(Object[] object) throws com.sun.star.uno.Exception
    {
        if (object.length > 0) {
            m_xFrame =
                (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(com.sun.star.frame.XFrame.class, object[0]);
        }
        Debug debug = Debug.getInstance();
        debug.setContext(m_xFrame, m_xToolkit);

        XWikiExtension xWikiExtension = XWikiExtension.getInstance();
        xWikiExtension.setComponentContext(m_xContext);
        xWikiExtension.setImagesDirUrl(getImageDirUrl());

        xWikiExtensionActions = new XWikiExtensionActions(xWikiExtension);
    }

    /**
     * @param _sImageName the name of the image
     */
    public String getImageDirUrl()
    {
        String sImageUrl = "";
        try {
            // retrive the configuration node of the extension
            XNameAccess xNameAccess = getRegistryKeyContent("org.xwiki.xoo.locations.ImageLocations");
            if (xNameAccess != null) {
                if (xNameAccess.hasByName("ImageDir")) {
                    // get the Image Url and process the Url by the macroexpander...
                    sImageUrl = (String) xNameAccess.getByName("ImageDir");
                    Object oMacroExpander =
                        this.m_xContext.getValueByName("/singletons/com.sun.star.util.theMacroExpander");
                    XMacroExpander xMacroExpander =
                        (XMacroExpander) UnoRuntime.queryInterface(XMacroExpander.class, oMacroExpander);
                    sImageUrl = xMacroExpander.expandMacros(sImageUrl);
                    sImageUrl = sImageUrl.substring(new String("vnd.sun.star.expand:").length(), sImageUrl.length());
                    sImageUrl = sImageUrl.trim();
                }
            }
        } catch (Exception ex) {
            /*
             * perform individual exception handling here. Possible exception types are:
             * com.sun.star.lang.IllegalArgumentException, com.sun.star.lang.WrappedTargetException,
             */
            ex.printStackTrace();
        }
        return sImageUrl;
    }

    /**
     * @param _sKeyName
     * @return
     */
    public XNameAccess getRegistryKeyContent(String _sKeyName)
    {
        try {
            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
            Object oConfigProvider;
            PropertyValue[] aNodePath = new PropertyValue[1];
            oConfigProvider =
                xMCF.createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", this.m_xContext);
            aNodePath[0] = new PropertyValue();
            aNodePath[0].Name = "nodepath";
            aNodePath[0].Value = "org.xwiki.xoo.locations.ImageLocations";
            XMultiServiceFactory xMSFConfig =
                (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oConfigProvider);
            Object oNode =
                xMSFConfig.createInstanceWithArguments("com.sun.star.configuration.ConfigurationAccess", aNodePath);
            XNameAccess xNameAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, oNode);
            return xNameAccess;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispatch(com.sun.star.util.URL aURL, com.sun.star.beans.PropertyValue[] aArguments)
    {
        if (aURL.Protocol.compareTo(Constants.PROTOCOL_XOOMENU) == 0) {
            if (aURL.Path.compareTo(Constants.CMD_LOGIN) == 0) {
                SettingsDialog set = new SettingsDialog(m_xContext);
                set.show();
                return;
            }
            if (aURL.Path.compareTo(Constants.CMD_TREE) == 0) {
                NavigationDialog nav = NavigationDialog.getInstance();
                XWikiExtension xwikiExtension = XWikiExtension.getInstance();
                if (xwikiExtension.getExtensionStatus().getNavBarStatus() == ExtensionStatus.NEW_NAVBAR)
                    nav.show();
                else
                    nav.setFocus();
                return;
            }

        } else if (aURL.Protocol.compareTo(Constants.PROTOCOL_XOOTOOLBAR) == 0) {
            if (aURL.Path.compareTo(Constants.CMD_ADDPAGE) == 0) {
                xWikiExtensionActions.cmdAddPage();
            } else if (aURL.Path.compareTo(Constants.CMD_EDITPAGE) == 0) {
                xWikiExtensionActions.cmdEditPage();
            } else if (aURL.Path.compareTo(Constants.CMD_PUBLISHPAGE) == 0) {
                xWikiExtensionActions.cmdPublishPage();
            } else if (aURL.Path.compareTo(Constants.CMD_VIEWINBROWSER) == 0) {
                xWikiExtensionActions.cmdViewInBrowser();
            } else if (aURL.Path.compareTo(Constants.CMD_DOWNATT) == 0) {
                xWikiExtensionActions.cmdDownloadAttachment();
            } else if (aURL.Path.compareTo(Constants.CMD_UPATT) == 0) {
                xWikiExtensionActions.cmdUploadCurDocAsAttachment();
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public void addStatusListener(com.sun.star.frame.XStatusListener xControl, com.sun.star.util.URL aURL)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void removeStatusListener(com.sun.star.frame.XStatusListener xControl, com.sun.star.util.URL aURL)
    {
    }

}
