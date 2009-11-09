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
package org.xwoot.xwootApp.web.servlets;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.XMLElement;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.impl.protocol.HTTPAdv;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.impl.protocol.RdvConfigAdv;
import net.jxta.impl.protocol.RelayConfigAdv;
import net.jxta.impl.protocol.TCPAdv;
import net.jxta.impl.protocol.RdvConfigAdv.RendezVousConfiguration;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.TransportAdvertisement;

import org.xwoot.jxta.NetworkManager;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * Servlet handling network setup.
 * 
 * @version $Id$
 */
public class BootstrapNetwork extends HttpServlet
{
    /** TODO DOCUMENT ME! */
    public static final String JXTA_PUBLIC_NETWORK_RELAY_SEEDING_URI = "http://rdv.jxtahosts.net/cgi-bin/relays.cgi";

    /** TODO DOCUMENT ME! */
    public static final String JXTA_PUBLIC_NETWORK_RDV_SEEDING_URI = "http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi";

    /** TODO DOCUMENT ME! */
    public static final String CONCERTO_NETWORK_RELAY_SEEDING_URI = "http://concerto.xwiki.org/xwiki/bin/view/Network/ConcertoRendezvousSeeds?xpage=plain";

    /** TODO DOCUMENT ME! */
    public static final String CONCERTO_NETWORK_RDV_SEEDING_URI = "http://concerto.xwiki.org/xwiki/bin/view/Network/ConcertoRelaySeeds?xpage=plain";

    /** Join custom network option. */
    private static final String CUSTOM_NETWORK = "custom";

    /** Join public jxta network option. */
    private static final String JXTA_PUBLIC_NETWORK = "publicJxta";

    /** Join concerto network option. */
    private static final String CONCERTO_NETWORK = "concerto";

    /** Used for serialization. */
    private static final long serialVersionUID = -3758874922535817475L;

    /** The value of the join network button. */
    private static final String JOIN_BUTTON = "Join";

    /** The value of the create network button. */
    private static final String CREATE_BUTTON = "Create";

    /** The value of a checked checkbox. */
    private static final String TRUE = "true";
    
    /** The infrastructure ID of the Concerto network. */
    private static final String CONCERTO_NETWORK_INFRASTRUCTURE_ID = "urn:jxta:uuid-C79D17467D584790985FF99F06CCF4FB02";
    
    /** The MSID of the COncerto network. */
    private static final String CONCERTO_NETWORK_MSID = "urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE000000017DCDE6F257D0421A8FD9E3F13A06F93806";

    /** The XWootEngine instance to manage. */
    private XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();

    private boolean useMulticast;

    private int tcpPort;

    private boolean useOnlyPublicTcpAddress;

    private int httpPort;

    private boolean useOnlyPublicHTTPAddress;

    private String publicAddress;

    private String rdvSeedingUris;

    private String rdvSeeds;

    private boolean beRendezVous;

    private String relaySeedingUris;

    private String relaySeeds;

    private boolean beRelay;

    private boolean usePublicAddress;

    private boolean useTcp;
    
    private boolean useTcpIncomming;

    private boolean useHttp;
    
    private boolean useHttpIncomming;

    private boolean useOnlyPublicAddress;

    private boolean usePublicJxtaNetwork;

    private boolean useConcertoNetwork;

    private boolean useCustomNetwork;

    /** {@inheritDoc} */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        String errors = "";
        
        NetworkManager networkManager = xwootEngine.getPeer().getManager();
        NetworkConfigurator networkConfig = networkManager.getConfigurator();

        String networkChoice = request.getParameter("networkChoice");
        
        File platformConfigFile = new File(networkManager.getConfigurator().getHome(), "PlatformConfig");
        
        // If networkChoice button has been pressed, validate entered data.
        if (CREATE_BUTTON.equals(networkChoice) || JOIN_BUTTON.equals(networkChoice)) {
            if (JOIN_BUTTON.equals(networkChoice)) {
                errors += this.validateJoinFormFieldsFromRequest(request); 
            }
            errors += this.validateCommonFormFieldsFromRequest(request);
        

            // If the entered values are good, process data.
            if (errors == null || errors.trim().length() == 0){
                
                // Disconnect from any connected network.
                if (xwootEngine.isConnectedToP2PNetwork()) {
                    try {
                        xwootEngine.disconnectFromP2PNetwork();
                    } catch (Exception e) {
                        // TODO: remove the exception throwing of disconnectFromP2PNetwork.
                        // This should never happen.
                        this.log("Failed to disconnect from existing network.", e);
                    }
                }
                
                // Clear previous locally cached configuration because we do all necesary configuration in the UI.
                if (platformConfigFile.exists()) {
                    platformConfigFile.delete();
                }
                
                // Initialize the proper peer mode.
                ConfigMode mode = ConfigMode.EDGE;
                if (JOIN_BUTTON.equals(networkChoice) && CUSTOM_NETWORK.equals(request.getParameter("useNetwork"))) {
                    boolean beRendezVous = TRUE.equals(request.getParameter("beRendezVous"));
                    boolean beRelay = TRUE.equals(request.getParameter("beRelay"));
                    
                    if (beRendezVous && beRelay) {
                        mode = ConfigMode.RENDEZVOUS_RELAY;
                    } else if (beRendezVous) {
                        mode = ConfigMode.RENDEZVOUS;
                    } else if (beRelay) {
                        mode = ConfigMode.RELAY;
                    }
                } else if (CREATE_BUTTON.equals(networkChoice)) {
                    mode = ConfigMode.RENDEZVOUS_RELAY;
                }
                
                this.log("Setting this peer to " + mode + " mode.");
                networkManager.setMode(mode);
                
                // Get the now updated networkConfig or the old one if the mode remained the same.
                networkConfig = networkManager.getConfigurator();
                
                
                // Continue with common settings.
                
                boolean useExternalIp = TRUE.equals(request.getParameter("useExternalIp"));
                String externalIp = request.getParameter("externalIp");
                boolean useOnlyExternalIp = TRUE.equals(request.getParameter("useOnlyExternalIp"));
    
                boolean useTcp = TRUE.equals(request.getParameter("useTcp"));
                boolean useTcpIncomming = TRUE.equals(request.getParameter("useTcpIncomming"));
    
                boolean useHttp = TRUE.equals(request.getParameter("useHttp"));
                boolean useHttpIncomming = TRUE.equals(request.getParameter("useHttpIncomming"));
    
                boolean useMulticast = TRUE.equals(request.getParameter("useMulticast"));
    
                networkConfig.setTcpEnabled(useTcp);
    
                if (useTcp) {
                    this.log("Using TCP");
                    
                    int tcpPort = Integer.parseInt(request.getParameter("tcpPort"));
    
                    networkConfig.setTcpIncoming(useTcpIncomming);
                    networkConfig.setTcpOutgoing(true);
                    networkConfig.setTcpPort(tcpPort);
    
                    String tcpPublicAddress = externalIp;
                    if (useExternalIp) {
                        // disable dynamic ports because we use a fixed ip:port combination now.
                        networkConfig.setTcpStartPort(-1);
                        networkConfig.setTcpEndPort(-1);
    
                        if (!tcpPublicAddress.contains(":")) {
                            tcpPublicAddress += ":" + tcpPort;
                        }
                        networkConfig.setTcpPublicAddress(tcpPublicAddress, useOnlyExternalIp);
                        this.log("Using TCP External IP : " + tcpPublicAddress + " exclusively? " + useOnlyExternalIp);
                    }
                    
                    networkConfig.setUseMulticast(useMulticast);
                    this.log("Using Multicast? " + useMulticast);
                }
    
                networkConfig.setHttpEnabled(useHttp);
    
                if (useHttp) {
                    this.log("Using HTTP");
                    
                    int httpPort = Integer.parseInt(request.getParameter("httpPort"));
    
                    networkConfig.setHttpIncoming(useHttpIncomming);
                    networkConfig.setHttpOutgoing(true);
                    networkConfig.setHttpPort(httpPort);
    
                    String httpPublicAddress = externalIp;
                    if (useExternalIp) {
                        if (!httpPublicAddress.contains(":")) {
                            httpPublicAddress += ":" + httpPort;
                        }
                        networkConfig.setHttpPublicAddress(httpPublicAddress, useOnlyExternalIp);
                        this
                            .log("Using HTTP External IP : " + httpPublicAddress + " exclusively? " + useOnlyExternalIp);
                    }
                }
                
                // Create network settings.
                if (CREATE_BUTTON.equals(networkChoice)) {
                    this.getServletContext().log("Create network requested.");
    
                    try {
                        // Can`t use this because setmode overrides our settings. 
                        //xwootEngine.createNetwork();
                        
                        networkConfig.clearRelaySeedingURIs();
                        networkConfig.clearRelaySeeds();
                        networkConfig.clearRendezvousSeedingURIs();
                        networkConfig.clearRendezvousSeeds();
                        
                        networkManager.setUseDefaultSeeds(false);
                        
                        xwootEngine.getPeer().startNetworkAndConnect((XWoot3) xwootEngine, (XWoot3) xwootEngine);
                        // request.getSession().setAttribute("join", Boolean.valueOf(false));
                        // response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
                    } catch (Exception e) {
                        errors += "Can't create network:" + e.getMessage() + "\n";
                    }
    
                // Join Network settings
                } else if (JOIN_BUTTON.equals(networkChoice)) {
                    this.getServletContext().log("Join network requested.");
                        
                    String useNetwork = request.getParameter("useNetwork");
                
                    try {
                        // Clean any previously entered seeds and seedingUris.
                        networkConfig.clearRendezvousSeeds();
                        networkConfig.clearRendezvousSeedingURIs();
                        networkConfig.clearRelaySeeds();
                        networkConfig.clearRelaySeedingURIs();
                        
                        // Reset the infrastructure id to default.
                        networkConfig.setInfrastructureID(PeerGroupID.defaultNetPeerGroupID);

                        if (CONCERTO_NETWORK.equals(useNetwork)) {
                            
                            // Specify how to contact the Concerto network.
                            networkConfig.addRdvSeedingURI(CONCERTO_NETWORK_RDV_SEEDING_URI);
                            networkConfig.addRelaySeedingURI(CONCERTO_NETWORK_RELAY_SEEDING_URI);
                            
                            // Specify the infrastructure id of the Concerto netowrk and a name.
                            networkConfig.setInfrastructureName("Concerto Network");
                            networkConfig.setInfrastructureID(CONCERTO_NETWORK_INFRASTRUCTURE_ID);

                        } else if (JXTA_PUBLIC_NETWORK.equals(useNetwork)) {
                            
                            networkManager.setUseDefaultSeeds(true);
                            networkConfig.addRdvSeedingURI(JXTA_PUBLIC_NETWORK_RDV_SEEDING_URI);
                            networkConfig.addRelaySeedingURI(JXTA_PUBLIC_NETWORK_RELAY_SEEDING_URI);

                        } else if (CUSTOM_NETWORK.equals(useNetwork)) {
                            
                            String rdvSeedingUriString = request.getParameter("rdvSeedingUri");
                            String relaySeedingUriString = request.getParameter("relaySeedingUri");
                            String rdvSeeds = request.getParameter("rdvSeeds");
                            String relaySeeds = request.getParameter("relaySeeds");
                
                            String[] rdvSeedingUrisList = rdvSeedingUriString.split("\\s*,\\s*");
                            String[] relaySeedingUrisList = relaySeedingUriString.split("\\s*,\\s*");
                            
                            String[] rdvSeedsList = rdvSeeds.split("\\s*,\\s*");
                            String[] relaySeedsList = relaySeeds.split("\\s*,\\s*");
                            
                            // Rdv Seeding URIs
                            for (String rdvSeedingUri : rdvSeedingUrisList) {
                                if (rdvSeedingUri.trim().length() != 0) {
                                    networkConfig.addRdvSeedingURI(URI.create(rdvSeedingUri));
                                }
                            }
                            
                            // Relay Seeding URIs
                            for (String relaySeedingUri : relaySeedingUrisList) {
                                if (relaySeedingUri.trim().length() != 0) {
                                    networkConfig.addRelaySeedingURI(URI.create(relaySeedingUri));
                                }
                            }

                            // Rdv Seeds.
                            for (String rdvSeed : rdvSeedsList) {
                                if (rdvSeed.trim().length() != 0) {
                                    networkConfig.addSeedRendezvous(URI.create(rdvSeed));
                                }
                            }

                            // Relay Seeds.
                            for (String relaySeed : relaySeedsList) {
                                if (relaySeed.trim().length() != 0) {
                                    networkConfig.addSeedRelay(URI.create(relaySeed));
                                }
                            }
                            
                        }

                        xwootEngine.joinNetwork(null);

                        // Catch silent exceptions jxta is not throwing but just warning about.
                        if (!xwootEngine.isConnectedToP2PNetwork()) {
                            errors += "Can't join network. Failed to contact a RendezVous peer for the given network.";
                        }
                    } catch (Exception e) {
                        // If exceptions come along the way or if joinNetwork() fails.
                        errors += "Can't join network: " + e.getMessage() + "\n";
                    }                    
                }
            }
            
            // If no errors were encountered and successfully joined/created a network, go to next step.
            if (errors.length() == 0) {
                this.getServletContext().log("No errors occured.");
                
//                // Stop the autosynch thread if it is running.
//                XWootSite.getInstance().getAutoSynchronizationThread().stopThread();
                
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
                return;
            } else {
                this.getServletContext().log("Errors occured.");
                
                // re-save the current config because it was earlier deleted.
                if (!networkConfig.exists()) {
                    networkConfig.save();
                }
            }
        } else {
            // No button pressed, check if network is already configured
            if (platformConfigFile.exists()) {
                // Auto-join network by using existing network configuration.
                networkChoice = "AUTO_START_NETWORK";

                if (!xwootEngine.isConnectedToP2PNetwork()) {
                    try {
                        this.log("Automatically restarting existing and configured P2P network.");
                        xwootEngine.reconnectToP2PNetwork();
                    } catch (Exception e) {
                        errors += "Failed to automatically restart the P2P network. Reason: " + e.getMessage(); 
                    }
                } else {
                    this.log("Already connected to P2P network. Moving on.");
                }
                
                // If successfully auto-started network, go to group bootstrap.
                if (errors == null || errors.trim().length() == 0) {
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
                    return;
                }
            }
        }
        
        // We are here if page just opened, no button was pressed or an error occurred while processing data.
        
        // Fill in existing configuration.
        if (networkConfig.exists()) {
            this.readCurrentSettings(networkConfig);
        } else {
            // Hint default basic settings to the GUI.
            this.setDefaultSettingsToGUI();
        }
        this.populateRequest(request);

        // If any.
        errors = errors.replaceAll("\n", "<br/>");
        request.setAttribute("errors", errors);

        // Show the form.
        request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
        return;

    }

    private void setDefaultSettingsToGUI()
    {
        this.useTcp = true;
        this.tcpPort = 9701;
        this.useTcpIncomming = true;
        this.useMulticast = true;
        this.useHttp = true;
        this.httpPort = 9700;
        this.useHttpIncomming = true;
    }

    public String validateCommonFormFieldsFromRequest(HttpServletRequest request)
    {
        String errors = "";
        
        boolean useExternalIp = TRUE.equals(request.getParameter("useExternalIp"));
        String externalIp = request.getParameter("externalIp");

        boolean useTcp = TRUE.equals(request.getParameter("useTcp"));
        String tcpPortString = request.getParameter("tcpPort");
        boolean useTcpIncomming = TRUE.equals(request.getParameter("useTcpIncomming"));

        boolean useHttp = TRUE.equals(request.getParameter("useHttp"));
        String httpPortString = request.getParameter("httpPort");
        boolean useHttpIncomming = TRUE.equals(request.getParameter("useHttpIncomming"));
        
        if (useExternalIp) {
            if (externalIp == null || externalIp.trim().length() == 0) {
                errors += "No external IP provided.\n";
            }
        }
        
        if (!useTcp && !useHttp) {
            errors += "At least one communication method (TCP and/or HTTP) must be chosen.\n";
        } else {
            if (useTcp) {
                if (tcpPortString != null && tcpPortString.trim().length() != 0) {
                    try {
                        int port = Integer.parseInt(tcpPortString); 
                        if (port <= 0) {
                            errors += "TCP port number must be greater than 0.\n";
                        }
                        // TODO: check if port is busy/usable.
                    } catch (NumberFormatException e) {
                        errors += "Invalid TCP port.\n";
                    }
                } else {
                    errors += "No TCP port provided.\n";
                }
            }
            
            if (useHttp) {
                if (httpPortString != null && httpPortString.trim().length() != 0) {
                    try {
                        int port = Integer.parseInt(httpPortString); 
                        if (port <= 0) {
                            errors += "HTTP port number must be greater than 0.\n";
                        }
                        // TODO: check if port is busy/usable.
                    } catch (NumberFormatException e) {
                        errors += "Invalid HTTP port.\n";
                    }
                } else {
                    errors += "No HTTP port provided.\n";
                }
            }
        }
        
        String networkChoice = request.getParameter("networkChoice");
        String useNetwork = request.getParameter("useNetwork");
        boolean beRendezVous = TRUE.equals(request.getParameter("beRendezVous"));
        boolean beRelay = TRUE.equals(request.getParameter("beRelay"));
        
        if (CREATE_BUTTON.equals(networkChoice) 
            || (JOIN_BUTTON.equals(networkChoice) && CUSTOM_NETWORK.equals(useNetwork) && (beRendezVous || beRelay))) {
            if (!useTcpIncomming && !useHttpIncomming) {
                errors += "Incomming connections must be accepted for at least one of the selected communication method.";
            }
        }
            
        
        return errors;
    }
    
    public String validateJoinFormFieldsFromRequest(HttpServletRequest request)
    {
        String errors = "";
        
        String useNetwork = request.getParameter("useNetwork");
        
        if (!CONCERTO_NETWORK.equals(useNetwork) && !JXTA_PUBLIC_NETWORK.equals(useNetwork) && !CUSTOM_NETWORK.equals(useNetwork)) {
            return "No network specified.\n";
        }
        
        if (CUSTOM_NETWORK.equals(useNetwork)) {
            
            String rdvSeedingUriString = request.getParameter("rdvSeedingUri");
            String relaySeedingUriString = request.getParameter("relaySeedingUri");
            String rdvSeeds = request.getParameter("rdvSeeds");
            String relaySeeds = request.getParameter("relaySeeds");
            
            String[] rdvSeedingUrisList = rdvSeedingUriString.split("\\s*,\\s*");
            String[] relaySeedingUrisList = relaySeedingUriString.split("\\s*,\\s*");

            String[] rdvSeedsList = rdvSeeds.split("\\s*,\\s*");
            String[] relaySeedsList = relaySeeds.split("\\s*,\\s*");
            
            // Check if at least one rdv seed/seedingUri exists.
            if ((rdvSeedingUrisList.length == 0 || (rdvSeedingUrisList.length == 1 && rdvSeedingUrisList[0].trim().length() == 0)) &&
                (rdvSeedsList.length == 0 || (rdvSeedsList.length == 1 && rdvSeedsList[0].length() == 0))) {
                errors += "Must specify at least one RendezVous seed or RendezVous seeding URI.\n";
            }
            
            // Rdv Seeding Uris
            for (String rdvSeedingUri : rdvSeedingUrisList) {
                if (rdvSeedingUri != null && rdvSeedingUri.trim().length() != 0) {
                    try {
                        URI seedingUri = new URI(rdvSeedingUri);
                        String scheme = seedingUri.getScheme();
                        String host = seedingUri.getHost();
                        if (host == null || scheme == null) {
                            errors += rdvSeedingUri + " is not a valid location for retrieving RendezVous seeds.\n";
                        }
                    } catch (Exception e) {
                        errors += rdvSeedingUri + " is not a valid location for retrieving RendezVous seeds.\n";
                    }
                }
            }
            
            // Relay Seeding Uris
            for (String relaySeedingUri : relaySeedingUrisList) {
                if (relaySeedingUri != null && relaySeedingUri.trim().length() != 0) {
                    try {
                        URI seedUri = new URI(relaySeedingUri);
                        String scheme = seedUri.getScheme();
                        String host = seedUri.getHost();
                        if (host == null || scheme == null) {
                            errors += relaySeedingUri + " is not a valid location for retrieving relay seeds.\n";
                        }
                    } catch (Exception e) {
                        errors += relaySeedingUri + " is not a valid location for retrieving relay seeds.\n";
                    }
                }
            }
         
            // Rdv Seeds
            for (String rdvSeed : rdvSeedsList) {
                if (rdvSeed != null && rdvSeed.trim().length() != 0) {
                    try {
                        URI seedUri = new URI(rdvSeed);
                        String scheme = seedUri.getScheme();
                        String host = seedUri.getHost();
                        if (host == null || scheme == null) {
                            errors += rdvSeed + " is not a valid RendezVous seed.\n";
                        } else if (seedUri.getPort() < 1) {
                            errors += rdvSeed + " has no port specified.\n";
                        }
                    } catch (Exception e) {
                        errors += rdvSeed + " is not a valid RendezVous seed.\n";
                    }
                }
            }
        
            // Relay Seeds
            for (String relaySeed : relaySeedsList) {
                if (relaySeed != null && relaySeed.trim().length() != 0) {
                    try {
                        URI seedUri = new URI(relaySeed);
                        String scheme = seedUri.getScheme();
                        String host = seedUri.getHost();
                        if (host == null || scheme == null) {
                            errors += relaySeed + " is not a valid Relay seed.\n";
                        } else if (seedUri.getPort() < 1) {
                            errors += relaySeed + " has no port specified.\n";
                        }
                    } catch (Exception e) {
                        errors += relaySeed + " is not a valid Relay seed.\n";
                    }
                }
            }
        }
        
        return errors;
    }
    
    @SuppressWarnings("unchecked")
    public void readCurrentSettings(NetworkConfigurator networkConfigurator) 
    {
        PlatformConfig platformConfig = (PlatformConfig) networkConfigurator.getPlatformConfig();
        
        TCPAdv tcpAdvertisement = null;
        HTTPAdv httpAdvertisement = null;
        
        // TCP
        XMLElement param = (XMLElement) platformConfig.getServiceParam(PeerGroup.tcpProtoClassID);
        useTcp = platformConfig.isSvcEnabled(PeerGroup.tcpProtoClassID);
        Enumeration tcpChildren = param.getChildren(TransportAdvertisement.getAdvertisementType());
        
        // get the TransportAdv from either TransportAdv or tcpConfig
        if (tcpChildren.hasMoreElements()) {
            param = (XMLElement) tcpChildren.nextElement();
        } else {
            throw new IllegalStateException("Missing TCP Advertisment");
        }
        tcpAdvertisement = (TCPAdv) AdvertisementFactory.newAdvertisement(param);

        // HTTP
        try {
            param = (XMLElement) platformConfig.getServiceParam(PeerGroup.httpProtoClassID);
            useHttp = platformConfig.isSvcEnabled(PeerGroup.httpProtoClassID);
            
            Enumeration httpChildren = param.getChildren(TransportAdvertisement.getAdvertisementType());
            
            // get the TransportAdv from either TransportAdv
            if (httpChildren.hasMoreElements()) {
                param = (XMLElement) httpChildren.nextElement();
            } else {
                throw new IllegalStateException("Missing HTTP Advertisment");
            }
            // Read-in the adv as it is now.
            httpAdvertisement = (HTTPAdv) AdvertisementFactory.newAdvertisement(param);
        } catch (Exception failure) {
            // TODO: handle;
            log("Error processing the HTTP config advertisement", failure);
//            IOException ioe = new IOException();
//            ioe.initCause(failure);
//            throw ioe;
        }
        
//        // Rendezvous
//        try {
//            param = (XMLElement) platformConfig.getServiceParam(PeerGroup.rendezvousClassID);
//            // backwards compatibility
//            param.addAttribute("type", RdvConfigAdv.getAdvertisementType());
//            rdvConfig = (RdvConfigAdv) AdvertisementFactory.newAdvertisement(param);
//        } catch (Exception failure) {
//            IOException ioe = new IOException("error processing the rendezvous config advertisement");
//            ioe.initCause(failure);
//            throw ioe;
//        }
//        
//        // Relay
//        try {
//            param = (XMLElement) platformConfig.getServiceParam(PeerGroup.relayProtoClassID);
//            if (param != null && !platformConfig.isSvcEnabled(PeerGroup.relayProtoClassID)) {
//                mode = mode | RELAY_OFF;
//            }
//            // backwards compatibility
//            param.addAttribute("type", RelayConfigAdv.getAdvertisementType());
//            relayConfig = (RelayConfigAdv) AdvertisementFactory.newAdvertisement(param);
//        } catch (Exception failure) {
//            IOException ioe = new IOException("error processing the relay config advertisement");
//            ioe.initCause(failure);
//            throw ioe;
//        }
 
        
        
//        TCPAdv tcpAdvertisement = (TCPAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.tcpProtoClassID);
//        HTTPAdv httpAdvertisement = (HTTPAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.httpProtoClassID);
        RelayConfigAdv relayAdvertisement = (RelayConfigAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.relayProtoClassID);
        RdvConfigAdv rdvAdvertisement = (RdvConfigAdv) platformConfig.getSvcConfigAdvertisement(PeerGroup.rendezvousClassID);
        
        // TCP
        useMulticast = tcpAdvertisement.getMulticastState();
        tcpPort = tcpAdvertisement.getPort();
        useOnlyPublicTcpAddress = tcpAdvertisement.getPublicAddressOnly();
        String tcpPublicAddress = tcpAdvertisement.getServer();
        boolean beTcpClient = tcpAdvertisement.isClientEnabled();
        boolean beTcpServer = tcpAdvertisement.isServerEnabled();
        
        useTcp = useTcp && (beTcpClient || beTcpServer);
        useTcpIncomming = beTcpServer;
        
        // HTTP
        httpPort = httpAdvertisement.getPort();
        useOnlyPublicHTTPAddress = httpAdvertisement.getPublicAddressOnly();
        String httpPublicAddress = httpAdvertisement.getServer();
        boolean beHttpClient = httpAdvertisement.isClientEnabled();
        boolean beHttpServer = httpAdvertisement.isServerEnabled();
        
        useHttp = useHttp && (beHttpClient || beHttpServer);
        useHttpIncomming = beHttpServer;
        
        if (tcpPublicAddress != null && tcpPublicAddress.length() > 0) {
            publicAddress = tcpPublicAddress.substring(0, tcpPublicAddress.lastIndexOf(":"));
            useOnlyPublicAddress = useOnlyPublicTcpAddress;
        } else if (httpPublicAddress != null && httpPublicAddress.length() > 0) {
            publicAddress = httpPublicAddress.substring(0, httpPublicAddress.lastIndexOf(":"));
            useOnlyPublicAddress = useOnlyPublicHTTPAddress;
        }
        
        usePublicAddress = (publicAddress != null && publicAddress.length() > 0);
        
        usePublicJxtaNetwork = false;
        useConcertoNetwork = false;
        useCustomNetwork = false;
        
        // RendezVous
        rdvSeedingUris = "";
        for(URI seedingURI : rdvAdvertisement.getSeedingURIs()) {
            if (JXTA_PUBLIC_NETWORK_RDV_SEEDING_URI.equals(seedingURI.toString())) {
                usePublicJxtaNetwork = true;
            } else if (CONCERTO_NETWORK_RDV_SEEDING_URI.equals(seedingURI.toString())) {
                useConcertoNetwork = true;
            }
            
            rdvSeedingUris += seedingURI.toString() + ", ";
        }
        if (rdvSeedingUris.length() > 0) {
            rdvSeedingUris = rdvSeedingUris.substring(0, rdvSeedingUris.length()-2);
        }
        
        rdvSeeds = "";
        for(URI seed : rdvAdvertisement.getSeedRendezvous()) {
            rdvSeeds += seed.toString() + ", ";
        }
        if (rdvSeeds.length() > 0) {
            rdvSeeds = rdvSeeds.substring(0, rdvSeeds.length()-2);
        }
        
        beRendezVous = rdvAdvertisement.getConfiguration().equals(RendezVousConfiguration.RENDEZVOUS);
        
        // Relays
        relaySeedingUris = "";
        for(URI seedingURI : relayAdvertisement.getSeedingURIs()) {
            if (JXTA_PUBLIC_NETWORK_RELAY_SEEDING_URI.equals(seedingURI.toString())) {
                usePublicJxtaNetwork = true;
            } else if (CONCERTO_NETWORK_RELAY_SEEDING_URI.equals(seedingURI.toString())) {
                useConcertoNetwork = true;
            }
            
            relaySeedingUris += seedingURI.toString() + ", ";
        }
        if (relaySeedingUris.length() > 0) {
            relaySeedingUris = relaySeedingUris.substring(0, relaySeedingUris.length()-2);
        }
        
        relaySeeds = "";
        for(EndpointAddress seed : relayAdvertisement.getSeedRelays()) {
            relaySeeds += seed.toString() + ", ";
        }
        if (relaySeeds.length() > 0) {
            relaySeeds = relaySeeds.substring(0, relaySeeds.length()-2);
        }
        
        beRelay = relayAdvertisement.isServerEnabled();
        
        if (!useConcertoNetwork && !usePublicJxtaNetwork) {
            useCustomNetwork = true;
        }
    }
    
    public void populateRequest(HttpServletRequest request)
    {
        request.setAttribute("useTcp", isChecked(useTcp));
        request.setAttribute("tcpPort", tcpPort);
        request.setAttribute("useTcpIncomming", isChecked(useTcpIncomming));
        request.setAttribute("useMulticast", isChecked(useMulticast));
        
        request.setAttribute("useHttp", isChecked(useHttp));
        request.setAttribute("httpPort", httpPort);
        request.setAttribute("useHttpIncomming", isChecked(useHttpIncomming));

        request.setAttribute("publicAddress", publicAddress);
        request.setAttribute("usePublicAddress", isChecked(usePublicAddress));
        request.setAttribute("useOnlyPublicAddress", isChecked(useOnlyPublicAddress));
        
        request.setAttribute("rdvSeedingUris", rdvSeedingUris);
        request.setAttribute("rdvSeeds", rdvSeeds);
        request.setAttribute("beRendezVous", isChecked(beRendezVous));
        
        request.setAttribute("relaySeedingUris", relaySeedingUris);
        request.setAttribute("relaySeeds", relaySeeds);
        request.setAttribute("beRelay", isChecked(beRelay));
        
        request.setAttribute("useConcertoNetwork", isChecked(useConcertoNetwork));
        request.setAttribute("usePublicJxtaNetwork", isChecked(usePublicJxtaNetwork));
        request.setAttribute("useCustomNetwork", isChecked(useCustomNetwork));
    }
    
    private String isChecked(boolean checked)
    {
        if (checked) {
            return "checked=\"checked\"";
        }
        
        return "";
    }
}
