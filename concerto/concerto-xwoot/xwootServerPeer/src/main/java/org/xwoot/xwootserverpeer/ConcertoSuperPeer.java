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
package org.xwoot.xwootserverpeer;

import java.io.File;
import java.net.URI;

import net.jxta.exception.JxtaException;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Implements a Super peer that will ensure connectivity and for a network.
 * <p>
 * This peer does nothing except route communication, log events and ensures network existence.
 * 
 * @version $Id$
 */
public class ConcertoSuperPeer
{
    /** Get help. */
    public static final String HELP_PARAMETER = "h";

    /** Location of a list of seeding rdvs. */
    public static final String RDV_SEEDING_URI_PARAMETER = "rdvSeedingUris";

    /** Location of a list of seeding relays. */
    public static final String RELAY_SEEDING_URI_PARAMETER = "relaySeedingUris";

    /** Relay mode for this peer. */
    public static final String MODE_RELAY_PARAMETER = "relay";

    /** Rendezvous mode for this peer. */
    public static final String MODE_RENDEZVOUS_PARAMETER = "rendezvous";

    /** Comma separated list of rdv seeds. */
    public static final String RDV_SEEDS_PARAMETER = "rdvSeeds";

    /** Comma separated list of relay seeds. */
    public static final String RELAY_SEEDS_PARAMETER = "relaySeeds";

    /** Name of this peer. */
    public static final String PEER_NAME_PARAMETER = "name";

    /** Location where to store the jxta cache directory. */
    public static final String HOME_PARAMETER = "home";

    /** If to use TCP for communication. */
    public static final String USE_TCP_PARAMETER = "useTcp";

    /** The TCP port to use. */
    public static final String TCP_PORT_PARAMETER = "tcpPort";

    /** If to use HTTP for communication. */
    public static final String USE_HTTP_PARAMETER = "useHttp";

    /** The HTTP port to use. */
    public static final String HTTP_PORT_PARAMETER = "httpPort";

    /** The external ip to use for this peer. */
    public static final String EXTERNAL_IP_PARAMETER = "externalIp";

    /** If to use only the external ip in advertisements. */
    public static final String ONLY_EXTERNAL_IP_PARAMETER = "useOnlyExternalIp";

    /** To clean any existing configuration from the home directory. */
    public static final String CLEAN_EXISTING_CONFIG_PARAMETER = "clean";

    /** To enable multicast for LAN communication. */
    public static final String USE_MULTICAST_PARAMETER = "useMulticast";
    
    /** To specify an infrastructure id for a private network. */
    public static final String INFRASTRUCTURE_ID = "infrastructureID";
    
    /** To specify an infrastructure name for the private network. */
    public static final String INFRASTRUCTURE_NAME = "infrastructureName";

    /** Name of this peer. */
    private String peerName;

    /** Location where to store the jxta cache directory. */
    private String homePath;

    /** Location of a list of seeding rdvs. */
    private String[] rdvSeedingUris;

    /** Location of a list of seeding relays. */
    private String[] relaySeedingUris;

    /** list of rdv seeds. */
    private String[] rdvSeeds;

    /** list of relay seeds. */
    private String[] relaySeeds;

    /** Rendezvous mode for this peer. */
    private boolean modeRendezvous;

    /** Relay mode for this peer. */
    private boolean modeRelay;

    /** The TCP port to use. */
    private int tcpPort;

    /** The HTTP port to use. */
    private int httpPort;

    /** If to use TCP for communication. */
    private boolean useTcp;

    /** If to use HTTP for communication. */
    private boolean useHttp;

    /** If to use only the external ip in advertisements. */
    private boolean useOnlyExternalIp;

    /** The external ip to use for this peer. */
    private String externalIp;

    /** To clean any existing configuration from the home directory. */
    private boolean clean;

    /** To enable multicast for LAN communication. */
    private boolean useMulticast;

    /** This super peer instance. */
    private SuperPeer superPeer;

    /** Options for command line. */
    private Options options;

    /** Used to print the usage. */
    private HelpFormatter formatter;

    private PeerGroupID infrastructureID;

    private String infrastructureName;

    /**
     * Constructor.
     */
    public ConcertoSuperPeer()
    {
        this.options = new Options();
        this.addOptions(options);
        formatter = new HelpFormatter();
    }

    /**
     * Main method.
     * 
     * @param args main method arguments.
     */
    public static void main(String[] args)
    {
        ConcertoSuperPeer concertoSuperPeer = new ConcertoSuperPeer();

        try {
            concertoSuperPeer.parseArgs(args);
        } catch (ParseException e) {
            System.err.println("Error parsing parameters: " + e.getMessage());
            concertoSuperPeer.showUsage();
            System.exit(-1);
        }

        try {
            concertoSuperPeer.configureNetwork();
        } catch (JxtaException e) {
            System.err.println("Error configuring the peer's network settings: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            concertoSuperPeer.getSuperPeer().startNetwork();
        } catch (JxtaException e) {
            System.err.println("Error starting the network on this peer: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        synchronized (concertoSuperPeer) {

            try {
                concertoSuperPeer.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse the command line arguments.
     * 
     * @param args the arguments to parse.
     * @throws ParseException if problems occur while parsing.
     */
    public void parseArgs(String[] args) throws ParseException
    {

        BasicParser parser = new BasicParser();
        CommandLine cl = parser.parse(this.options, args);

        if (cl.hasOption(HELP_PARAMETER)) {
            showUsage();
            System.exit(0);
        }

        String rdvSeedingUriString = cl.getOptionValue(RDV_SEEDING_URI_PARAMETER);
        String relaySeedingUriString = cl.getOptionValue(RELAY_SEEDING_URI_PARAMETER);
        this.modeRendezvous =
            (cl.hasOption(MODE_RENDEZVOUS_PARAMETER) != this.modeRendezvous ? cl.hasOption(MODE_RENDEZVOUS_PARAMETER)
                : this.modeRendezvous);
        this.modeRelay = cl.hasOption(MODE_RELAY_PARAMETER);
        this.peerName = cl.getOptionValue(PEER_NAME_PARAMETER);
        this.homePath = cl.getOptionValue(HOME_PARAMETER);
        String rdvSeedsString = cl.getOptionValue(RDV_SEEDS_PARAMETER);
        String relaySeedsString = cl.getOptionValue(RELAY_SEEDS_PARAMETER);

        this.useTcp = cl.hasOption(USE_TCP_PARAMETER);
        String tcpPortString = cl.getOptionValue(TCP_PORT_PARAMETER);

        this.useHttp = cl.hasOption(USE_HTTP_PARAMETER);
        String httpPortString = cl.getOptionValue(HTTP_PORT_PARAMETER);

        this.externalIp = cl.getOptionValue(EXTERNAL_IP_PARAMETER);
        this.useOnlyExternalIp = cl.hasOption(ONLY_EXTERNAL_IP_PARAMETER);

        this.clean = cl.hasOption(CLEAN_EXISTING_CONFIG_PARAMETER);
        this.useMulticast = cl.hasOption(USE_MULTICAST_PARAMETER);
        
        String infrastructureIDString = cl.getOptionValue(INFRASTRUCTURE_ID);
        this.infrastructureName = cl.getOptionValue(INFRASTRUCTURE_NAME);

        if (!useTcp && !useHttp) {
            throw new ParseException("At least one communication method (TCP and/or HTTP) must be chosen.");
        }

        if (useTcp) {
            if (tcpPortString != null && tcpPortString.trim().length() != 0) {
                try {
                    this.tcpPort = Integer.parseInt(tcpPortString);
                    if (this.tcpPort <= 0) {
                        throw new ParseException("TCP port number must be greater than 0.");
                    }
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid TCP port: " + tcpPortString);
                }
            } else {
                throw new ParseException("No TCP port provided.");
            }
        }

        if (useHttp) {
            if (httpPortString != null && httpPortString.trim().length() != 0) {
                try {
                    this.httpPort = Integer.parseInt(httpPortString);
                    if (this.httpPort <= 0) {
                        throw new ParseException("HTTP port number must be greater than 0.");
                    }
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid HTTP port.");
                }
            } else {
                throw new ParseException("No HTTP port provided.");
            }
        }

        if (!modeRendezvous && !modeRelay) {
            throw new ParseException("This peer must run in relay, rendezvous or both modes.");
        }

        String delimiter = ",";
        if (rdvSeedsString != null) {
            this.rdvSeeds = rdvSeedsString.split(delimiter);
        }

        if (relaySeedsString != null) {
            this.relaySeeds = relaySeedsString.split(delimiter);
        }

        if (rdvSeedingUriString != null) {
            this.rdvSeedingUris = rdvSeedingUriString.split(delimiter);
        }

        if (relaySeedsString != null) {
            this.relaySeedingUris = relaySeedingUriString.split(delimiter);
        }

        // Check if at least one rdv seed/seedingUri exists if this peer is a relay.
        if (!modeRendezvous
            && (rdvSeedingUriString == null || rdvSeedingUris.length == 0 
                || (rdvSeedingUris.length == 1 && rdvSeedingUris[0].trim().length() == 0))
            && (rdvSeedsString == null || rdvSeeds.length == 0
                || (rdvSeeds.length == 1 && rdvSeeds[0].length() == 0))) {
            throw new ParseException("Must specify at least one RendezVous seed or RendezVous seeding URI.");
        }

        // Rdv Seeding Uris
        if (this.rdvSeedingUris != null) {
            for (String rdvSeedingUri : rdvSeedingUris) {
                if (rdvSeedingUri != null && rdvSeedingUri.trim().length() != 0) {
                    try {
                        URI seedingUri = new URI(rdvSeedingUri);
                        String scheme = seedingUri.getScheme();
                        String host = seedingUri.getHost();
                        if (host == null || scheme == null) {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        throw new ParseException(rdvSeedingUri
                            + " is not a valid location for retrieving RendezVous seeds.");
                    }
                }
            }
        }

        // Relay Seeding Uris
        if (relaySeedingUris != null) {
            for (String relaySeedingUri : relaySeedingUris) {
                if (relaySeedingUri != null && relaySeedingUri.trim().length() != 0) {
                    try {
                        URI seedUri = new URI(relaySeedingUri);
                        String scheme = seedUri.getScheme();
                        String host = seedUri.getHost();
                        if (host == null || scheme == null) {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        throw new ParseException(relaySeedingUri
                            + " is not a valid location for retrieving relay seeds.");
                    }
                }
            }
        }

        // Rdv Seeds
        if (rdvSeeds != null) {
            for (String rdvSeed : rdvSeeds) {
                if (rdvSeed != null && rdvSeed.trim().length() != 0) {
                    try {
                        URI seedUri = new URI(rdvSeed);
                        String scheme = seedUri.getScheme();
                        String host = seedUri.getHost();
                        if (host == null || scheme == null) {
                            throw new Exception();
                        } else if (seedUri.getPort() < 1) {
                            throw new ParseException(rdvSeed + " has no port specified.");
                        }
                    } catch (Exception e) {
                        throw new ParseException(rdvSeed + " is not a valid RendezVous seed.");
                    }
                }
            }
        }

        // Relay Seeds
        if (relaySeeds != null) {
            for (String relaySeed : relaySeeds) {
                if (relaySeed != null && relaySeed.trim().length() != 0) {
                    try {
                        URI seedUri = new URI(relaySeed);
                        String scheme = seedUri.getScheme();
                        String host = seedUri.getHost();
                        if (host == null || scheme == null) {
                            throw new Exception();
                        } else if (seedUri.getPort() < 1) {
                            throw new ParseException(relaySeed + " has no port specified.");
                        }
                    } catch (Exception e) {
                        throw new ParseException(relaySeed + " is not a valid Relay seed.");
                    }
                }
            }
        }

        if (infrastructureIDString != null && infrastructureIDString.length() != 0) {
            try {
                this.infrastructureID = PeerGroupID.create(URI.create(infrastructureIDString));
            } catch (Exception e) {
                throw new ParseException(infrastructureIDString + " is not a valid infrastructure ID.");
            }
        }
    }

    /**
     * Add the options for the command line.
     * 
     * @param options the options object to populate.
     */
    public void addOptions(Options options)
    {
        options.addOption(HELP_PARAMETER, false, "Prints the usage for this application.");
        options.addOption(RDV_SEEDING_URI_PARAMETER, true,
            "A comma separated list of locations where to get RDV seeds for the network.");
        options.addOption(RELAY_SEEDING_URI_PARAMETER, true,
            "A comma separated list of locations where to get Relay seeds for the network.");
        options.addOption(MODE_RENDEZVOUS_PARAMETER, false, "Run this super peer in rendezvous mode.");
        options.addOption(MODE_RELAY_PARAMETER, false, "Run this super peer in relay mode.");
        options.addOption(RDV_SEEDS_PARAMETER, true, "A comma separated list of RDV seeds to use for the network.");
        options.addOption(RELAY_SEEDS_PARAMETER, true, "A comma separated list of Relay seeds to use for the network.");
        options.addOption(PEER_NAME_PARAMETER, true, "The name of this peer. Default is " + Peer.DEFAULT_PEER_NAME);
        options.addOption(HOME_PARAMETER, true,
            "The absolute location on drive where to store the jxta cache directory. Default is a directory named "
                + Peer.DEFAULT_DIR_NAME + " created in the current directory.");

        options.addOption(USE_TCP_PARAMETER, false, "To use TCP protocol for communication.");
        options.addOption(TCP_PORT_PARAMETER, true, "The port to use for TCP communication.");

        options.addOption(USE_HTTP_PARAMETER, false, "To use HTTP protocol for communication.");
        options.addOption(HTTP_PORT_PARAMETER, true, "The port to use for HTTP communication.");

        options.addOption(EXTERNAL_IP_PARAMETER, true,
            "An un-firewalled/un-NAT-ed IP address or DNS to use for communication instead of the local one.");
        options.addOption(ONLY_EXTERNAL_IP_PARAMETER, false, "To use HTTP protocol for communication.");

        options.addOption(CLEAN_EXISTING_CONFIG_PARAMETER, false,
            "To clean any existing configuration from the home directory.");

        options.addOption(USE_MULTICAST_PARAMETER, false, "To enable multicast for LAN communication.");
        
        options.addOption(INFRASTRUCTURE_ID, true, "The infrastructure id, in jxta uuid format, if it is a private network.");
        options.addOption(INFRASTRUCTURE_NAME, true, "The infrastructure name of the network if it is private.");
    }

    /**
     * Configure this peer's network settings.
     * 
     * @throws JxtaException if problems occur while configuring the peer.
     */
    public void configureNetwork() throws JxtaException
    {
        ConfigMode mode = null;
        if (modeRendezvous && modeRelay) {
            mode = ConfigMode.RENDEZVOUS_RELAY;
        } else if (modeRendezvous) {
            mode = ConfigMode.RENDEZVOUS;
        } else {
            mode = ConfigMode.RELAY;
        }

        if (this.clean) {
            String theName = (this.peerName != null ? this.peerName : Peer.DEFAULT_PEER_NAME);
            File homeLocation =
                (this.homePath != null ? new File(this.homePath, theName) : new File(Peer.DEFAULT_DIR_NAME, theName));

            FileUtil.deleteDirectory(homeLocation);
        }

        this.superPeer = new JxtaSuperPeer(peerName, this.homePath, mode);

        NetworkConfigurator networkConfigurator = this.superPeer.getConfigurator();

        // RDV Seeding URIs
        if (rdvSeedingUris != null) {
            for (String rdvSeedingUri : rdvSeedingUris) {
                if (rdvSeedingUri.trim().length() != 0) {
                    networkConfigurator.addRdvSeedingURI(URI.create(rdvSeedingUri));
                }
            }
        }

        // Relay Seeding URIs
        if (relaySeedingUris != null) {
            for (String relaySeedingUri : relaySeedingUris) {
                if (relaySeedingUri.trim().length() != 0) {
                    networkConfigurator.addRelaySeedingURI(URI.create(relaySeedingUri));
                }
            }
        }

        // Rdv Seeds.
        if (rdvSeeds != null) {
            for (String rdvSeed : rdvSeeds) {
                if (rdvSeed.trim().length() != 0) {
                    networkConfigurator.addSeedRendezvous(URI.create(rdvSeed));
                }
            }
        }

        // Relay Seeds.
        if (relaySeeds != null) {
            for (String relaySeed : relaySeeds) {
                if (relaySeed.trim().length() != 0) {
                    networkConfigurator.addSeedRelay(URI.create(relaySeed));
                }
            }
        }

        String addressPortSeparator = ":";

        networkConfigurator.setTcpEnabled(useTcp);

        if (useTcp) {
            networkConfigurator.setTcpIncoming(true);
            networkConfigurator.setTcpOutgoing(true);
            networkConfigurator.setTcpPort(tcpPort);
            if (this.externalIp != null) {
                // disable dynamic ports because we use a fixed ip:port combination now.
                networkConfigurator.setTcpStartPort(-1);
                networkConfigurator.setTcpEndPort(-1);

                String tcpPublicAddress = this.externalIp;
                if (!tcpPublicAddress.contains(addressPortSeparator)) {
                    tcpPublicAddress += addressPortSeparator + String.valueOf(tcpPort);
                }

                networkConfigurator.setTcpPublicAddress(tcpPublicAddress, this.useOnlyExternalIp);
            }

            this.superPeer.getManager().setUseDefaultSeeds(this.useMulticast);
        }

        networkConfigurator.setHttpEnabled(useHttp);

        if (useHttp) {
            networkConfigurator.setHttpIncoming(true);
            networkConfigurator.setHttpOutgoing(true);
            networkConfigurator.setHttpPort(httpPort);
            if (this.externalIp != null) {
                String httpPublicAddress = this.externalIp;
                if (!httpPublicAddress.contains(addressPortSeparator)) {
                    httpPublicAddress += addressPortSeparator + String.valueOf(httpPort);
                }

                networkConfigurator.setHttpPublicAddress(httpPublicAddress, this.useOnlyExternalIp);
            }
        }
        
        // Private network setup.
        if (this.infrastructureID != null) {
            networkConfigurator.setInfrastructureID(this.infrastructureID);
            if (this.infrastructureName != null && this.infrastructureName.length() != 0) {
                networkConfigurator.setInfrastructureName(this.infrastructureName);
            }
        }

    }

    /**
     * @return the superPeer
     */
    public SuperPeer getSuperPeer()
    {
        return this.superPeer;
    }

    /**
     * Prints the usage.
     */
    public void showUsage()
    {
        this.formatter.printHelp("Available parameters", this.options);
    }

}
