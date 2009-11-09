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
package org.xwoot.xwootApp.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.jxta.exception.JxtaException;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.impl.protocol.RdvConfigAdv;
import net.jxta.impl.protocol.RelayConfigAdv;
import net.jxta.impl.protocol.RdvConfigAdv.RendezVousConfiguration;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.contentprovider.Utils;
import org.xwoot.contentprovider.XWootContentProviderException;
import org.xwoot.contentprovider.XWootContentProviderFactory;
import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.antiEntropy.AntiEntropyException;
import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.WootEngineException;
import org.xwoot.xwootApp.AutoSynchronizationThread;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootUtil.FileUtil;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWootSite
{
    /** Logging helper object. */
    private static final Log LOG = LogFactory.getLog(XWootSite.class);

    // singleton instance
    private static XWootSite instance = new XWootSite();

    private boolean started = false;

    private XWootAPI XWootEngine;

    public static final String XWIKI_PROPERTIES_FILENAME = "xwiki.properties";

    public static final String XWOOT_PROPERTIES_FILENAME = "xwoot.properties";

    public static final String CONTENT_MANAGER_PROPERTIES_FILENAME = "xwoot-content-provider.properties";

    public static final String XWIKI_ENDPOINT = "xwiki_endpoint";

    public static final String XWIKI_USERNAME = "xwiki_username";

    public static final String XWIKI_PASSWORD = "xwiki_password";

    public static final String XWOOT_WORKING_DIR = "xwoot_working_dir";

    public static final String XWOOT_SERVER_NAME = "xwoot_server_name";

    private static final String WOOT_CLOCK_DIR_NAME = "wootclock";

    private static final String JXTA_DIR_NAME = "jxta";

    private static final String WOOTENGINE_DIR_NAME = "wootEngine";

    private static final String TRE_DIR_NAME = "tre";

    private static final String AE_DIR_NAME = "antientropy";

    private static final String XWOOT_DIR_NAME = "xwoot";

    private static final String CONTENT_PROVIDER_DIR_NAME = "contentProvider";

    private AutoSynchronizationThread autoSynchronizationThread;

    // FIXME: 60 seconds for now. Read this from a properties file.
    private static final int AUTO_SYNCHRONIZE_INTERVAL = 60000;

    /** @return the singleton instance. */
    public static XWootSite getInstance()
    {
        return instance;
    }

    public static Properties getProperties(String path)
    {
        Properties p = new Properties();
        try {
            FileInputStream fis = new FileInputStream(path);
            p.load(fis);
            fis.close();
        } catch (IOException ex) {
            // Cannot load properties, return empty properties.
        }
        return p;
    }

    /** @return the XWoot engine managed by this XWoot site. */
    public XWootAPI getXWootEngine()
    {
        return this.XWootEngine;
    }

    public AutoSynchronizationThread getAutoSynchronizationThread()
    {
        return this.autoSynchronizationThread;
    }

    /**
     * @param siteName
     * @param workingDirPath
     * @param contentProviderXmlRpcUrl
     * @param contentProviderLogin
     * @param contentProviderPassword
     * @param contenProviderPropertiesFilePath
     * @throws RuntimeException
     * @throws ClockException
     * @throws WikiContentManagerException
     * @throws WootEngineException
     * @throws JxtaException
     * @throws AntiEntropyException
     * @throws XWootException
     * @throws ThomasRuleEngineException
     * @throws XWootContentProviderException
     */
    public void init(String siteName, String workingDirPath, String contentProviderXmlRpcUrl,
        String contentProviderLogin, String contentProviderPassword, String contenProviderPropertiesFilePath)
        throws RuntimeException, ClockException, WootEngineException, JxtaException, AntiEntropyException,
        XWootException, ThomasRuleEngineException, XWootContentProviderException
    {
        // Module directories.
        File jxtaDir = new File(workingDirPath, JXTA_DIR_NAME);
        File wootEngineDir = new File(workingDirPath, WOOTENGINE_DIR_NAME);
        File wootEngineClockDir = new File(workingDirPath, WOOT_CLOCK_DIR_NAME);
        File treDir = new File(workingDirPath, TRE_DIR_NAME);
        File aeDir = new File(workingDirPath, AE_DIR_NAME);
        File xwootDir = new File(workingDirPath, XWOOT_DIR_NAME);
        File contentProviderDir = new File(workingDirPath, CONTENT_PROVIDER_DIR_NAME);

        try {
            // Check and/or create the working dir.
            FileUtil.checkDirectoryPath(workingDirPath);

            // Do the same for all the components.
            FileUtil.checkDirectoryPath(jxtaDir.toString());
            FileUtil.checkDirectoryPath(wootEngineDir.toString());
            FileUtil.checkDirectoryPath(wootEngineClockDir.toString());
            FileUtil.checkDirectoryPath(treDir.toString());
            FileUtil.checkDirectoryPath(aeDir.toString());
            FileUtil.checkDirectoryPath(xwootDir.toString());
        } catch (Exception e) {
            throw new RuntimeException("The provided working directory is not usable.", e);
        }

        // Init modules.
        Clock wootEngineClock = new Clock(wootEngineClockDir.toString());

        AntiEntropy ae = new AntiEntropy(aeDir.toString());

        Peer peer = PeerFactory.createPeer();
        
        ConfigMode initMode = ConfigMode.EDGE;
        NetworkConfigurator existingNetworkConfigurator = new NetworkConfigurator();
        existingNetworkConfigurator.setStoreHome(new File(jxtaDir, siteName).toURI());
        if (existingNetworkConfigurator.exists()) {
            try {
                existingNetworkConfigurator.load();
                
                PlatformConfig platformConfig = (PlatformConfig) existingNetworkConfigurator.getPlatformConfig();
                RelayConfigAdv relayAdvertisement = (RelayConfigAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.relayProtoClassID);
                boolean isRelay = relayAdvertisement.isServerEnabled();
                
                RdvConfigAdv rdvAdvertisement = (RdvConfigAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.rendezvousClassID);
                boolean isRendezVous = rdvAdvertisement.getConfiguration().equals(RendezVousConfiguration.RENDEZVOUS);
                
                if (isRendezVous && isRelay) {
                    initMode = ConfigMode.RENDEZVOUS_RELAY;
                } else if (isRendezVous) {
                    initMode = ConfigMode.RENDEZVOUS;
                } else if (isRelay) {
                    initMode = ConfigMode.RELAY;
                }
            } catch (Exception e) {
                LOG.warn("Failed to read existing network configuration.", e);
            } 
        }
        
        peer.configureNetwork(siteName, jxtaDir, initMode);
        if (existingNetworkConfigurator.exists()) {
            // Set the peerID to the one of the existing configuration.
            peer.getManager().setPeerID(existingNetworkConfigurator.getPeerID());
        }
        PlatformConfig platformConfig = (PlatformConfig) existingNetworkConfigurator.getPlatformConfig();
        RdvConfigAdv rdvAdvertisement = (RdvConfigAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.rendezvousClassID);
        rdvAdvertisement.setLeaseDuration(11 * 60 * 1000L);
        rdvAdvertisement.setLeaseMargin(11 * 60 * 1000L);
        
        String peerName = peer.getMyPeerName();
        String peerId = peer.getMyPeerID().getUniqueValue().toString();

        // TODO better properties management
        Properties contentProviderProperties = XWootSite.getProperties(contenProviderPropertiesFilePath);
        LOG.debug(contentProviderProperties);

        String dbLocation = new File(contentProviderDir, peerName).toString();
        XWootContentProviderInterface xwiki =
            XWootContentProviderFactory.getXWootContentProvider(contentProviderXmlRpcUrl, dbLocation, false,
                contentProviderProperties);

        WootEngine wootEngine = new WootEngine(peerId, wootEngineDir.toString(), wootEngineClock);

        ThomasRuleEngine tre = new ThomasRuleEngine(peerId, treDir.toString());

        this.XWootEngine = new XWoot3(xwiki, wootEngine, peer, xwootDir.toString(), tre, ae, contentProviderLogin, contentProviderPassword);

        // FIXME: read the interval from the properties file.
        this.autoSynchronizationThread = new AutoSynchronizationThread(this.XWootEngine, AUTO_SYNCHRONIZE_INTERVAL);

        // Mark as started.
        this.started = true;
        LOG.debug("Site " + this.XWootEngine.getXWootPeerId() + " initialisation");
    }

    /** @return true if this instance is initialized. */
    public boolean isStarted()
    {
        return this.started;
    }

    public static void savePropertiesInFile(String path, String comments, Properties p) throws IOException
    {
        File f = new File(path);
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        p.store(fos, comments);
        fos.flush();
        fos.close();
    }

    public String updatePropertiesFiles(HttpServletRequest request, String xwikiPropertiesPath,
        String xwootPropertiesPath) throws IOException
    {
        String result = "";
        Properties properties;

        // Update XWiki connection properties.
        properties = updateXWikiPropertiesFromRequest(request, xwikiPropertiesPath);
        result += this.validateXWikiProperties(properties);
        if (result.equals("")) {
            XWootSite.savePropertiesInFile(xwikiPropertiesPath, " -- XWiki XML-RPC connection properties --", properties);
        }

        // Update XWoot properties.
        properties = updateXWootPropertiesFromRequest(request, xwootPropertiesPath);
        result += this.validateXWootProperties(properties);
        if (result.equals("")) {
            XWootSite.savePropertiesInFile(xwootPropertiesPath, " -- XWoot properties --", properties);
        }
        return result;
    }

    public Properties updateXWikiPropertiesFromRequest(ServletRequest request, String xwikiPropertiesPath)
    {
        Properties p = getProperties(xwikiPropertiesPath);
        p.put(XWootSite.XWIKI_ENDPOINT, request.getParameter(XWootSite.XWIKI_ENDPOINT));
        p.put(XWootSite.XWIKI_USERNAME, request.getParameter(XWootSite.XWIKI_USERNAME));
        p.put(XWootSite.XWIKI_PASSWORD, request.getParameter(XWootSite.XWIKI_PASSWORD));
        
        return p;
    }

    public Properties updateXWootPropertiesFromRequest(ServletRequest request, String xwootPropertiesPath)
    {
        Properties p = getProperties(xwootPropertiesPath);
        p.put(XWootSite.XWOOT_WORKING_DIR, request.getParameter(XWootSite.XWOOT_WORKING_DIR));
        p.put(XWootSite.XWOOT_SERVER_NAME, request.getParameter(XWootSite.XWOOT_SERVER_NAME));
        
        return p;
    }

    /**
     * Checks that the XWiki connection configuration is good: the connection URL is a valid URL, and the username and
     * password are provided.
     * 
     * @param properties The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></strong> is returned.
     * @todo Message localization.
     * @todo Make a simple call to the wiki to verify that there is a wiki at that address, and that the
     *       username/password are valid.
     */
    private final String validateXWikiProperties(Properties properties)
    {
        String result = "";
        String xwikiEndpoint = properties.getProperty(XWootSite.XWIKI_ENDPOINT);
        String xwikiUserName = properties.getProperty(XWootSite.XWIKI_USERNAME);
        String xwikiPassword = properties.getProperty(XWootSite.XWIKI_PASSWORD);

        // Check that the XWiki endpoint is a valid URL.
        if (xwikiEndpoint == null || xwikiEndpoint.trim().length() == 0) {
            result += "Please enter a non-empty XWiki endpoint URL.\n";
        } else {
            try {
                new URL(xwikiEndpoint);
            } catch (MalformedURLException e) {
                result += "Please enter a valid XWiki endpoint URL (the given URL is malformed)\n";
            }

        }

        // Check that the username and password are provided.
        
        if (xwikiUserName == null || xwikiUserName.trim().length() == 0) {
            result += "Please enter a non-empty username.\n";
        }

        if (xwikiPassword == null || xwikiPassword.length() == 0) {
            result += "Please enter a non-empty password.\n";
        }
        
        if (result != null && result.length() == 0) {
            boolean connectionFailed = false;
            try {
                HttpURLConnection connection = (HttpURLConnection) (new URL(xwikiEndpoint).openConnection());
                connection.setConnectTimeout(15000);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_BAD_METHOD) {
                    result += "An XWiki server is not available at the provided address.";
                    connectionFailed = true;
                }
            } catch (Exception e) {
                connectionFailed = true;
                result += "Failed to contact the remote XWiki server. Make sure XWiki is started at the provided address and that it is reachable.";
            }
            
            if (!connectionFailed) {
                try {
                    if (!Utils.checkLogin(xwikiEndpoint, xwikiUserName, xwikiPassword)) {
                        result += "The remote XWiki server refused the provided username/password combination.\n";
                    }
                } catch (Exception e) {
                    result += "Please enter a valid XWiki endpoint URL (the given URL is malformed)\n";
                }
            }
        }

        return result;
    }

    /**
     * Checks that the XWoot configuration is good.
     * 
     * @param properties The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></string> is returned.
     * @todo Message localization.
     */
    private String validateXWootProperties(Properties properties)
    {
        String result = "";
        String wootWorkingDir = properties.getProperty(XWootSite.XWOOT_WORKING_DIR);
        String xwootServerName = properties.getProperty(XWootSite.XWOOT_SERVER_NAME);

        // Check that the directory for storing data is valid and writable.
        if (wootWorkingDir == null || wootWorkingDir.trim().length() == 0) {
            result += "Please enter a non-empty " + XWootSite.XWOOT_WORKING_DIR + " field.\n";
        } else {
            try {
                File dir = new File((String) properties.get(XWootSite.XWOOT_WORKING_DIR));
                FileUtil.checkDirectoryPath(dir);
//                if (!f.exists()) {
//                    if (!f.mkdirs()) {
//                        result +=
//                            "The provided directory does not exist and cannot be created. Please enter a writable serialization folder.\n";
//                    }
//                } else if (!f.canRead() || !f.canWrite()) {
//                    result += "Please enter a writable serialization folder.\n";
//                }
            } catch (Exception ex) {
                result += ex.getMessage() + " Please enter a writable serialization folder.\n";
            }
        }

        // Check the server name
        if (xwootServerName == null || xwootServerName.trim().length() == 0) {
            result += "Please enter a non-empty server name.\n";
        }

        return result;
    }
}
