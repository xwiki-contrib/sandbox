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
package org.xwoot.jxta;

import java.security.cert.X509Certificate;
import java.util.*;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

import javax.crypto.EncryptedPrivateKeyInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;

import net.jxta.discovery.*;
import net.jxta.exception.JxtaException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.*;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.*;
import net.jxta.rendezvous.*;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLDocument;

/**
 * Implementation handling the gory details of JXTA. 
 *              
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class JxtaPeer implements Peer, RendezvousListener, DiscoveryListener {

    protected PeerGroup rootGroup;
    protected PeerGroup currentJoinedGroup;
    protected NetworkManager manager;
    protected JxtaCast jc;
    protected Credential groupCredential;
    protected JxtaCastEventListener jxtaCastListener;
    protected DirectMessageReceiver directMessageReceiver;
    protected Log logger = LogFactory.getLog(this.getClass());
    
    protected Timer timer = new Timer("XWoot:JxtaModule:CommonTimer", true);
    protected TimerTask presenceTask = null;
    
    /** The interval in ms by which to update peer presence data (to run the presenceTask). */
    public static final int PEER_PRESENCE_UPDATE_INTERVAL = 5 * 60 * 1000;
    
    /** How long until the peer presence (pipe advertisements) will expire if not republished. */ 
    public static final int PEER_PRESENCE_ADVERTISEMENT_EXPIRATION = 6 * 60 * 1000;
    
    /** The pipe name to be used when broadcasting messages. Interested peers will look for this. */
	public static final String PROPAGATE_PIPE_ADVERTISEMENT_NAME = "ConcertoMessageBroadcast";
	
	/** Number of ms to wait for the output pipe to be resolved when directly communicating through the back-channel. */
	public static final long BACK_CHANNEL_OUTPUT_PIPE_RESOLVE_TIMEOUT = 5000;
	
	/** Number of ms to wait between tries to discover pipe ADVs in a group and send an object to one of them. */ 
	public static final long WAIT_INTERVAL_BETWEEN_TRIES = 5000;
	
	/** Number of ms to wait for a reply to be sent back. */
	public static final int WAIT_INTERVAL_FOR_DIRECT_COMMUNICATION_CONNECTIONS = 60 * 1000;
	
	/** Number tries to discover pipe ADVs in a group and send an object to one of them. */
	public static final int NUMBER_OF_TRIES = 5;
	
	/** Number of ms to wait for a rdv connection to a group. */ 
    public static final long WAIT_FOR_RDV_CONNECTION_PERIOD = 120 * 1000;
	
	/** Server socket used to accept incoming direct messages in a reliable way. This could also be made secure.*/
	protected JxtaServerSocket serverSocket;
    
	/** {@inheritDoc} **/
	public void configureNetwork(String peerName, File jxtaCacheDirectoryPath, ConfigMode mode) throws JxtaException
	{
	    // FIXME: normalize the peerName before setting it as directory name. Some names could cause filesystem problems.
	    
	    if (peerName == null || peerName.length() == 0) {
	        peerName = Peer.DEFAULT_PEER_NAME;
	    }
	    
	    if (jxtaCacheDirectoryPath == null) {
            jxtaCacheDirectoryPath = new File(new File(Peer.DEFAULT_DIR_NAME), peerName);
        } else {
            jxtaCacheDirectoryPath = new File(jxtaCacheDirectoryPath, peerName);
        }
        
	    try {
	        manager = new NetworkManager(mode, peerName,
	            jxtaCacheDirectoryPath.toURI());
	    } catch (Exception e) {
	        throw new JxtaException("Failed to initialize peer.\n", e);
	    }
	    
	    // Make sure the jxta platform will shut down tidely when the JVM does.
	    manager.registerShutdownHook();
        
        logger.info("Infrastructure ID: " + manager.getInfrastructureID());
        logger.info("Peer ID: " + manager.getPeerID());
        logger.info("Peer Name: " + manager.getInstanceName());
	}
    
    
    /** {@inheritDoc} */
    public NetworkManager getManager()
    {
        return this.manager;
    }

    
    /** {@inheritDoc} **/
	public void startNetworkAndConnect(JxtaCastEventListener jxtaCastListener, DirectMessageReceiver directMessageReceiver) throws IllegalStateException,
			PeerGroupException, IOException, JxtaException {
	    
	    this.logger.info("Starting network.");
	    
		if (!this.isNetworkConfigured()) {
			throw new IllegalStateException(
					"The manager has not yet been instantiated and configured. Call configureNetwork() first.");
		}
		
		if (this.isConnectedToNetwork()) {
		    logger.warn("Already connected to the network.");
			return;
		}

		// Start the network.
		this.manager.startNetwork();

		// Get the NetPeerGroup and use this for now.
		this.rootGroup = manager.getNetPeerGroup();
		
		//this.joinedGroups.add(this.rootGroup);
		this.currentJoinedGroup = this.rootGroup;

		// Connect to the Network entry-point (Rendezvous).
		if (!this.waitForRendezVousConnection(this.rootGroup, 120000)) {
		    logger.error("Unable to connect to rendezvous server. Stopping.");
			this.stopNetwork();
			throw new JxtaException("Unable to connect to rendezvous server. Network stopped.");
		}

		// TODO: See if this can still be added. Currently is causing problems when Network RDV disconnects.
		//  fixed and no is no longer an issue?
		
		// Contribute to the network's connectivity if we don`t already.
		if (!(manager.getMode().equals(ConfigMode.RENDEZVOUS) || 
		    manager.getMode().equals(ConfigMode.RENDEZVOUS_RELAY) || 
		    manager.getMode().equals(ConfigMode.SUPER))) {
		    
		    this.rootGroup.getRendezVousService().setAutoStart(true);
		}

		// Register ourselves to detect new RDVs that broadcast their presence and resources.
		// FIXME: reenable this . this.rootGroup.getRendezVousService().addListener(this);
		//  maybe netPeerGroup is not that important.
		
		// Save the listeners.
		this.jxtaCastListener = jxtaCastListener;
		this.directMessageReceiver = directMessageReceiver;
		
		// Clean the local cache of known groups.
		//this.flushExistingAdvertisements(this.rootGroup, DiscoveryService.GROUP);
		// Not good for rdvs, and probably not good for normal peers too.
		
		// Do a discovery for available groups.
		discoverGroups(null, null);
	}
	
	
	/** {@inheritDoc} **/
	public void stopNetwork() {
	    this.logger.info("Stopping network.");
	    
		if (this.isConnectedToNetwork() || this.isJxtaStarted()) {
		    //PeerGroup currentGroup = this.currentJoinedGroup;
		    
			// Try to leave the current group nicely.
			try {
				this.leavePeerGroup(currentJoinedGroup);
			} catch (Exception e) {
			    // ignore, we are shutting down anyway.
			    this.logger.warn("Failed to leave group " + this.currentJoinedGroup + " while stopping jxta network.");
			}
			
//			currentGroup.stopApp();
//			currentGroup.unref();
//			currentGroup = null;
			
			this.currentJoinedGroup = null;
			
//			this.rootGroup.stopApp();
//			this.rootGroup.unref();
			this.rootGroup = null;
			
			manager.stopNetwork();
			
			System.runFinalization();
			System.gc();
			
		} else {
		    this.logger.warn("Network already stopped.");
		}
	}

	
	/** {@inheritDoc} **/
    public PeerGroupAdvertisement getDefaultAdv() {
        return rootGroup.getPeerGroupAdvertisement();
    }

    
    /** {@inheritDoc} **/
    public PeerGroup getDefaultGroup() {
        return rootGroup;
    }
    
    
    /** {@inheritDoc} **/
    public String getMyPeerName() {
        return this.manager.getInstanceName();
    }
    
    /** {@inheritDoc} **/
    public void setMyPeerName(String peerName)
    {
        this.manager.setInstanceName(peerName);
    }
    
    /** {@inheritDoc} **/
    public PeerID getMyPeerID()
    {
        return this.manager.getPeerID();
    }
    
    /** {@inheritDoc} **/
    public PeerAdvertisement getMyPeerAdv() {
        return rootGroup.getPeerAdvertisement();
    }

//    /** {@inheritDoc} **/
//    public String getBackChannelPipeNamePrefix() {
//        return jc.getBackChannelPipePrefix();
//    }
//    
//    
//    /** {@inheritDoc} **/
//    public PipeAdvertisement getMyBackChannelPipeAdvertisement() {
//        return jc.getBackChannelPipeAdvertisement();
//    }
//    
//    
//    /** {@inheritDoc} **/
//    public String getMyBackChannelPipeName() {
//        return jc.getBackChannelPipeName();
//    }

    
    /** {@inheritDoc} */
    public String getMyDirectCommunicationPipeName() {

        // Use a complex delimiter to mark off the peer name and ID.
        // We need to parse this string later, so we need something that's
        // unlikely to appear in a peer name.  (A simple period is too risky.)
        //
        String name = getDirectCommunicationPipeNamePrefix() + JxtaCast.DELIM +
                      this.getMyPeerName()            + JxtaCast.DELIM +
                      this.getMyPeerID().toString() + JxtaCast.DELIM +
                      new Date().getTime();

        return name;
    }
    

    /** {@inheritDoc} */
    public String getDirectCommunicationPipeNamePrefix() {

        return "ConcertoPeerDirectCommunication." + this.getClass().getSimpleName();
    }
    
    
    /** {@inheritDoc} */
    public PipeAdvertisement getMyDirectCommunicationPipeAdvertisement() {
        if (this.serverSocket != null) {
            return this.serverSocket.getPipeAdv();
        }
        
        this.logger.warn("Server socket not instantiated. Returning null pipe advertisement.");
        return null;
    }
    
    
    /** {@inheritDoc} */
    public String getMyDirectCommunicationPipeIDAsString()
    {
        PipeAdvertisement pipeAdv = this.getMyDirectCommunicationPipeAdvertisement();
        if (pipeAdv != null) {
            return pipeAdv.getPipeID().toString();
        }
        
        this.logger.warn("Server socket not instantiated. Returning null pipe ID.");
        
        return null;
    }
    

    /** @return the name of the peer from a direct communication pipe name. */
    public static String getPeerNameFromBackChannelPipeName(String pipeName) {

        // The peer name is located between the first and second delimiters.
        int start = pipeName.indexOf(JxtaCast.DELIM);
        if (start < 0)
            return null;

        int end = pipeName.indexOf(JxtaCast.DELIM, start + 1);
        if (end < 0)
            return null;

        // Extract the peer name.
        start += JxtaCast.DELIM.length();
        if (start > end)
            return null;
        return pipeName.substring(start, end);
    }
    
    
    /** @return the peer ID of the peer from a direct communication pipe name. */
    public static String getPeerIdFromBackChannelPipeName(String pipeName) {

        // The peer ID is located between the second and the third delimiter.
        int start = pipeName.indexOf(JxtaCast.DELIM);
        if (start < 0)
            return null;
        
        start = pipeName.indexOf(JxtaCast.DELIM, ++start);
        if (start < 0)
            return null;

        int end = pipeName.indexOf(JxtaCast.DELIM, start + 1);
        if (end < 0)
            return null;

        // Extract the peer name.
        start += JxtaCast.DELIM.length();
        if (start > end)
            return null;
        return pipeName.substring(start, end);
    }
    
    
    /** @return the creation time of the direct communication channel from its pipe name. */
    public static String getCreationTimeFromBackChannelPipeName(String pipeName) {

        // The peer ID is located after the second delimiter.
        int pos = pipeName.lastIndexOf(JxtaCast.DELIM);
        if (pos < 0)
            return null;

        return pipeName.substring(pos + JxtaCast.DELIM.length());
    }
    
    
    /** {@inheritDoc} **/
    public JxtaCast getJxtaCastInstance() {
        return jc;
    }
    

    /** {@inheritDoc} **/
    public void discoverGroups(String targetPeerId, DiscoveryListener discoListener) {

    	if (!this.isConnectedToNetwork()) {
    		return;
    	}
    	
        DiscoveryService disco = rootGroup.getDiscoveryService();
		
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.GROUP,
                                             null,
                                             null,
                                             discoListener);
        thread.start();
    }


    /** {@inheritDoc} **/
    public void discoverPeers(String targetPeerId,
                              //PeerGroupAdvertisement group,
                              DiscoveryListener discoListener) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.)
        //
//        PeerGroup pg = findJoinedGroup(group);
//        if (pg == null)
//            return;
    	
    	if (!this.isConnectedToGroup()) {
    		return;
    	}

        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();
        /*
        Enumeration<PeerAdvertisement> peers = getKnownPeers(group);
        while (peers.hasMoreElements()) {
        	try {
				disco.flushAdvertisement(peers.nextElement());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        */
        
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.PEER,
                                             null,
                                             null,
                                             discoListener);
        thread.start();
    }


    /** {@inheritDoc} **/
    public void discoverAdvertisements(String targetPeerId,
                                       //PeerGroupAdvertisement group,
                                       DiscoveryListener discoListener,
                                       String attribute,
                                       String value) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.)
        //
//        PeerGroup pg = findJoinedGroup(group);
//        if (pg == null)
//            return;
    	
    	if (!this.isConnectedToGroup()) {
    		return;
    	}

        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.ADV,
                                             attribute,
                                             value,
                                             discoListener);
        thread.start();
    }
    
    
    /** {@inheritDoc} */
    public void discoverDirectCommunicationPipeAdvertisements()
    {
        this.discoverAdvertisements(null, null, PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");
    }


    /** {@inheritDoc} **/
	@SuppressWarnings("unchecked")
	public Enumeration<PeerGroupAdvertisement> getKnownGroups() {

		if (!this.isConnectedToNetwork()) {
    		logger.warn("Not conencted to network.");
    		// return empty enumeration.
            return Collections.enumeration(new ArrayList<PeerGroupAdvertisement>());
    	}
    	
        Enumeration en = null;
        DiscoveryService disco = rootGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.GROUP, null, null);
        } catch (Exception e) {
            logger.warn("Failed to get local group advertisements.\n", e);
            return Collections.enumeration(new ArrayList<PeerGroupAdvertisement>());
        }
        
        // Look for new groups to add to the local repository.
        discoverGroups(null, null);

        return (Enumeration<PeerGroupAdvertisement>) en;
    }
    

    /** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public Enumeration<PeerAdvertisement> getKnownPeers(/*PeerGroupAdvertisement group*/) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.
        //
        /*PeerGroup pg = findJoinedGroup(group);
        if (pg == null)
            return null;*/
    	
    	if (!this.isConnectedToNetwork()) {
    	    logger.warn("Not conencted to network.");
    	    // return empty enumeration.
            return Collections.enumeration(new ArrayList<PeerAdvertisement>());
    	}

        Enumeration en = null;
        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.PEER, null, null);
        } catch (Exception e) {
            logger.warn("Failed to get locally stored known peers.\n", e);
            return Collections.enumeration(new ArrayList<PeerAdvertisement>());
        }
        
        discoverPeers(null, null);

        return (Enumeration<PeerAdvertisement>) en;
    }


    /** {@inheritDoc} **/
    public Enumeration<Advertisement> getKnownAdvertisements(/*PeerGroupAdvertisement group,
                                              */String attribute,
                                              String value) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.
        //
//        PeerGroup pg = findJoinedGroup(group);
//        if (pg == null)
//            return null;
    	
    	if (!this.isConnectedToNetwork()) {
    	    logger.warn("Not conencted to network.");
    	    // return empty enumeration.
    		return Collections.enumeration(new ArrayList<Advertisement>());
    	}

        Enumeration<Advertisement> en = Collections.enumeration(new ArrayList<Advertisement>());
        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.ADV, attribute, value);
        } catch (Exception e) {
            logger.warn("Failed to get locally stored known advertisements.\n", e);
            return Collections.enumeration(new ArrayList<Advertisement>());
        }
        
        // Flush advertisements to always be up to date.
        /*while(en.hasMoreElements()) {
            Advertisement adv = en.nextElement();
            try {
                disco.flushAdvertisement(adv);
            } catch (Exception e) {
                logger.warn("Failed to flush advertisement:\n" + adv, e);
            }
        }*/
        
        // Rediscover advertisements to always be up to date.
        discoverAdvertisements(null, null, attribute, value);

        return en;
    }
    
    /** {@inheritDoc} **/
    public Enumeration<Advertisement> getKnownDirectCommunicationPipeAdvertisements()
    {

        Enumeration<Advertisement> en = this.getKnownAdvertisements(PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");
        
        ArrayList<Advertisement> pipeAdvs = new ArrayList<Advertisement>();
        
        while (en.hasMoreElements()) {
            Advertisement adv = en.nextElement();
            // Get only PipeAdvertisements that are different from this peer's.
            if (adv instanceof PipeAdvertisement) {
                PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;
                if (!pipeAdv.equals(this.getMyDirectCommunicationPipeAdvertisement())
                    && pipeAdv.getName().startsWith(this.getDirectCommunicationPipeNamePrefix())) {
                    
                    pipeAdvs.add(adv);
                }
            }
        }
        
        return Collections.enumeration(pipeAdvs);
    }
    
    
    /** {@inheritDoc} **/
    public Enumeration<ID> getConnectedRdvsIDs() {
    	if (!this.isConnectedToGroup()) {
    		return null;
    	}
    	
    	return this.rootGroup.getRendezVousService().getConnectedRendezVous();
    }


    /** {@inheritDoc} **/
    public PeerGroup createNewGroup(String groupName, String description, char[] keystorePassword, char[] groupPassword) throws Exception {
    	
        PeerGroup newGroup;               // new peer group
        
        DiscoveryService rootGroupDiscoveryService = this.rootGroup.getDiscoveryService();
        
        // Create a new all purpose peergroup.
        ModuleImplAdvertisement newGroupImpl = null;
        
        // If a keystorePassword and groupPassword has been specified, set the membershipService to PSEMembershipService.
        if (keystorePassword != null && keystorePassword.length != 0 && groupPassword != null && groupPassword.length != 0) {
	       
        	newGroupImpl = buildNewGroupImplAdvertisementWithPSE(rootGroup, groupName);
       
	        //System.out.println("ALTERED PEER GROUP IMPL ADV CONTAINING PSEMEMBERSHIPSERVICE, NO PSECONFIG:\n" + newGroupImpl);
	        
	        // Advertise this altered module impl adv so we can instantiate the group.
	        rootGroupDiscoveryService.remotePublish(newGroupImpl, DiscoveryService.DEFAULT_LIFETIME);
	        rootGroupDiscoveryService.publish(newGroupImpl, DiscoveryService.DEFAULT_LIFETIME, DiscoveryService.DEFAULT_LIFETIME);
	        
	        // Generate self-signed certificate and encrypt the private key from this certificate.
	        PSEUtils.IssuerInfo groupAuthenticationData = PSEUtils.genCert(manager.getInstanceName(), null);
	        EncryptedPrivateKeyInfo encryptedGroupPrivateKey = PSEUtils.pkcs5_Encrypt_pbePrivateKey(
	        		groupPassword, groupAuthenticationData.issuerPkey, 1000);

	        // Build PeerGroupAdvertisement for the new group with PSE authentication data in it.
	        X509Certificate[] certificateChain = { groupAuthenticationData.cert };
	        
	        PeerGroupAdvertisement newGroupAdv = buildGroupAdvWithPSE(
	        		groupName, description, newGroupImpl, certificateChain, encryptedGroupPrivateKey);
	  
//	        // Publish it.
//	        rootGroupDiscoveryService.publish(newGroupAdv, DiscoveryService.DEFAULT_LIFETIME, DiscoveryService.DEFAULT_LIFETIME);
//	        rootGroupDiscoveryService.remotePublish(newGroupAdv, DiscoveryService.DEFAULT_LIFETIME);
	  
	        // create a group from it.
	        // rootGroup.loadModule(newGroupImpl.getID(), newGroupImpl);
	        newGroup = rootGroup.newGroup(newGroupAdv);
        } else {
        	// Create a public group.
        	newGroupImpl = rootGroup.getAllPurposePeerGroupImplAdvertisement();
        	newGroup = rootGroup.newGroup(null,   // Assign new group ID
        							newGroupImpl, // The implem. adv
        							groupName,    // The name
        							description); // Helpful descr.
        }
        
        // We join the new group as well.
        if (!authenticateMembership(newGroup, keystorePassword, groupPassword)) {
        	throw new Exception("Authentication failed for the new group!");
        }
        
        // Listen to discovery events and try to eliminate duplicates.
        DiscoveryService newGroupDiscoveryService = newGroup.getDiscoveryService();
        newGroupDiscoveryService.addDiscoveryListener(this);

        // Become rdv for this new group. Peers will not be able to communicate if there is no rdv in this group.
        RendezVousService newGroupRendezvousService = newGroup.getRendezVousService();
        newGroupRendezvousService.addListener(this);
        newGroupRendezvousService.startRendezVous();

  /*      System.out.println("Connected RDVs: ");
        Enumeration<ID> rdvs = pg.getRendezVousService().getConnectedRendezVous();
        while (rdvs.hasMoreElements()) {
        	System.out.println("Rdv: " + rdvs.nextElement());
        }
        
        System.out.println("Connected Peers: ");
        Enumeration<ID> peers = pg.getRendezVousService().getConnectedPeers();
        while (peers.hasMoreElements()) {
        	System.out.println("Peer: " + peers.nextElement());
        }
        
        System.out.println("Connected to RDV?" + pg.getRendezVousService().isConnectedToRendezVous());
        System.out.println("Is RDV?" + pg.getRendezVousService().isRendezVous());
     */
        
        // Republish the group with increased expiration times.
        this.publishGroup(this.rootGroup, newGroup);
        
        // If we were previously a member of a group, we have to leave it now.
        if (this.hasJoinedAGroup()) {
        	this.leavePeerGroup(this.currentJoinedGroup);
        }
        
        // Set the new group as the current joined group.
        this.currentJoinedGroup = newGroup;
        
        // Init JxtaCast if null.
        if (jc == null ) {
        	jc = new JxtaCast(currentJoinedGroup.getPeerAdvertisement(), currentJoinedGroup, PROPAGATE_PIPE_ADVERTISEMENT_NAME);
        	jc.addJxtaCastEventListener(this.jxtaCastListener);
        	JxtaCast.logEnabled = true;
        }
        
        // Set as JxtaCast peer group.
        jc.setPeerGroup(newGroup);
        
        // Init direct communication for this group and register the listener.
        this.createDirectCommunicationServerSocket();
        
        // Schedule presence maintenance task.
        this.presenceTask = new PresenceTask(this);
        this.timer.schedule(this.presenceTask, PEER_PRESENCE_UPDATE_INTERVAL, PEER_PRESENCE_UPDATE_INTERVAL);

        return newGroup;
    }
    
    
    /** {@inheritDoc} **/
    public PeerGroup createNewGroup(String groupName, String description) throws Exception {
        return this.createNewGroup(groupName, description, null, null);
    }
    
    
    /** {@inheritDoc} **/
    public synchronized PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, char[] keystorePassword, char[] groupPassword,
                                                boolean beRendezvous) throws PeerGroupException, IOException, ProtocolNotSupportedException {

        // See if it's a group we've already joined.
        if (this.currentJoinedGroup != null && groupAdv.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
            logger.warn("Already joined.");
        	return this.currentJoinedGroup;
        }
        	
    	// Join the group.  This is done by creating a PeerGroup object for
        // the group and initializing it with the group advertisement.
        //
        PeerGroup newGroup = null;
    	try {
    		newGroup = rootGroup.newGroup(groupAdv);
    	} catch (PeerGroupException e) {
    	    logger.error("Failed to get the group from peer group advertisement.", e);
    		throw e;
    	}
    	
    	// Clean the local cache for this group just in case we have previously joined it.
        this.flushExistingAdvertisements(newGroup, DiscoveryService.PEER);
        this.flushExistingAdvertisements(newGroup, DiscoveryService.ADV);
    	
        if (!authenticateMembership(newGroup, keystorePassword, groupPassword)) {
        	throw new PeerGroupException("Authentication failed for joining the group.");
        }
        
        // Listen to rendezvous events.
        RendezVousService rendezvousService = newGroup.getRendezVousService();
        rendezvousService.addListener(this);
        
        // Listen to discovery events and try to eliminate duplicates.
        DiscoveryService disco = newGroup.getDiscoveryService();
        disco.addDiscoveryListener(this);
        
        // Decide the rendezvous status of this peer. 
        if (beRendezvous) {
            rendezvousService.startRendezVous();
        } else {
            // Wait for a connection to a RDV of this group.
            if (!waitForRendezVousConnection(newGroup, 60000)) {
                // If none found, make this peer a RDV for the group in order to enable immediate communication.
                this.logger.debug("Could not connect to any RDV peer in this group. Promoting this peer to RDV.");
                rendezvousService.startRendezVous();
            } else {
                this.logger.debug("RDV connection established for this group.");
            }
            
            // Let jxta decide when to promote edge peers to rdvs.
            rendezvousService.setAutoStart(true);
        }
        
        // Leave the old peer group.
        this.leavePeerGroup(currentJoinedGroup);

        // Re-Publish the group to mark it as still alive.
        this.publishGroup(this.rootGroup, newGroup);
        
        // Set this group as the current one
        currentJoinedGroup = newGroup;
        
        // Update local cache with peers and their private pipe advertisements from this group.
        discoverPeers(null, null);
        discoverAdvertisements(null, null, PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");
        
 /*       
        System.out.println("Connected RDVs: ");
        Enumeration<ID> rdvs = newGroup.getRendezVousService().getConnectedRendezVous();
        while (rdvs.hasMoreElements()) {
        	System.out.println("Rdv: " + rdvs.nextElement());
        }
        
        System.out.println("Connected Peers: ");
        Enumeration<ID> peers = newGroup.getRendezVousService().getConnectedPeers();
        while (peers.hasMoreElements()) {
        	System.out.println("Peer: " + peers.nextElement());
        }
        
        System.out.println("Connected to RDV?" + newGroup.getRendezVousService().isConnectedToRendezVous());
        System.out.println("Is RDV?" + newGroup.getRendezVousService().isRendezVous());
    */
        
        // Init JxtaCast if null.
        if (jc == null ) {
        	jc = new JxtaCast(currentJoinedGroup.getPeerAdvertisement(), currentJoinedGroup, PROPAGATE_PIPE_ADVERTISEMENT_NAME);
        	jc.addJxtaCastEventListener(this.jxtaCastListener);
        	JxtaCast.logEnabled = true;
        }
        
        // Set the group as JxtaCast's group.
        jc.setPeerGroup(newGroup);
        
        // Initialize direct communication for this group and register the listener.
        this.createDirectCommunicationServerSocket();
        
        // Schedule presence maintenance task.
        this.presenceTask = new PresenceTask(this);
        this.timer.schedule(this.presenceTask, PEER_PRESENCE_UPDATE_INTERVAL, PEER_PRESENCE_UPDATE_INTERVAL);
        
        return newGroup;
    }
    
    /**
     * Wait for a rendezvous connection to any group, including NetPeerGroup if given.
     * 
     * @param group the group
     * @param delay the time in ms to wait for a connection. 0 for forever.
     * @return true if a connection has been established in the default wait time period.
     * @throws PeerGroupException if problems occur while waiting.
     * @see #WAIT_FOR_RDV_CONNECTION_PERIOD
     */
    public boolean waitForRendezVousConnection(PeerGroup group, long delay) throws PeerGroupException
    {
        if (group == null) {
            throw new NullPointerException("Null group given.");
        }
        
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay given.");
        }
        
        this.logger.debug("Waiting for rdv connection of group " + group.getPeerGroupName());
        
        RendezVousService rdvService = group.getRendezVousService();
        if (rdvService == null) {
            throw new PeerGroupException("Failed to get the RendezVousService for this group.");
        }
        
        long stopNow = System.currentTimeMillis();
        if (delay == 0) {
            stopNow = Long.MAX_VALUE;
        } else {
            stopNow += delay;
        }
        
        // TODO: Vulnerable to time changes to the past? quite unlikely.
        while (System.currentTimeMillis() < stopNow) {
            if (rdvService.isConnectedToRendezVous() || rdvService.isRendezVous()) {
                this.logger.debug("Successfuly connected to RDV peer of group " + group.getPeerGroupName());
                return true;
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // just log.
                this.logger.debug("Interrupt received while waiting for RDV connection. Ignoring.");
            }
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    public PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, boolean beRendezvous) throws PeerGroupException,
        IOException, ProtocolNotSupportedException
    {
        return joinPeerGroup(groupAdv, null, null, beRendezvous);
    }
    
    protected void publishGroup(PeerGroup parentGroup, PeerGroup childrenGroup) throws IOException {
        DiscoveryService parentGroupDiscoveryService = parentGroup.getDiscoveryService();
        
        Advertisement childGroupPeerGroupAdvertisement = childrenGroup.getPeerGroupAdvertisement();
        Advertisement childGroupImplementationAdvertisement = childrenGroup.getImplAdvertisement();
        
        // Use 1 year for both.
        long groupExpirationTime = DiscoveryService.DEFAULT_LIFETIME;
        long groupLifetime = DiscoveryService.DEFAULT_LIFETIME;
        
        parentGroupDiscoveryService.remotePublish(childGroupPeerGroupAdvertisement, groupLifetime);
        parentGroupDiscoveryService.publish(childGroupPeerGroupAdvertisement, groupLifetime, groupExpirationTime);
        parentGroupDiscoveryService.remotePublish(childGroupImplementationAdvertisement, groupLifetime);
        parentGroupDiscoveryService.publish(childGroupImplementationAdvertisement, groupLifetime, groupExpirationTime);
    }
    
    protected void createDirectCommunicationServerSocket() throws IOException
    {
        this.logger.debug("Creating direct communication server socket.");
        
        if (!this.hasJoinedAGroup()) {
            this.logger.warn("Not joined a group. Aborting.");
            return;
        }
        
        this.closeExistingDirectCommunicationServerSocket();
        
        PipeAdvertisement pipeAdv = this.createDirectCommunicationPipeAdvertisement();

        this.logger.debug("Publishing pipe advertisement.");
        this.publishDirectCommunicationPipeAdvertisement(pipeAdv);
        
        // If no listener registered, there is no point in starting a server socket and a connection handler thread.
        if (this.directMessageReceiver != null) {
            this.serverSocket = new JxtaServerSocket(this.currentJoinedGroup, pipeAdv);
            
            // Block waiting for connections indefinitely.
            this.serverSocket.setSoTimeout(0);
        
            new ConnectionHandler(this.serverSocket, directMessageReceiver).start();
        } else {
            this.logger.warn("There is no listener registered. Direct communication server socket will not be created.");
        }
    }
    
    protected PipeAdvertisement createDirectCommunicationPipeAdvertisement()
    {
        if (!this.hasJoinedAGroup()) {
            this.logger.warn("Not joined a group. Returning null pipe advertisement.");
            return null;
        }
        
        PipeAdvertisement pipeAdv = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
            PipeAdvertisement.getAdvertisementType());

        PipeID id = (PipeID)IDFactory.newPipeID(currentJoinedGroup.getPeerGroupID());
        pipeAdv.setPipeID(id);
        pipeAdv.setName(getMyDirectCommunicationPipeName());
        pipeAdv.setType(PipeService.UnicastType);
        
        return pipeAdv;
    }
    
    protected void publishDirectCommunicationPipeAdvertisement(PipeAdvertisement pipeAdv, long lifetime, long expirationTime) throws IOException
    {
        if (!this.hasJoinedAGroup()) {
            this.logger.warn("Not joined a group. Publish aborted.");
            return;
        }
        
        if (pipeAdv == null) {
            this.logger.warn("Refusing to publish null pipe advertisement.");
            return;
        }
        
        DiscoveryService discoveryService = this.currentJoinedGroup.getDiscoveryService();
        
        discoveryService.publish(pipeAdv, lifetime, expirationTime);
        discoveryService.remotePublish(pipeAdv, expirationTime);
    }
    
    protected void publishDirectCommunicationPipeAdvertisement(PipeAdvertisement pipeAdv) throws IOException
    {
        this.publishDirectCommunicationPipeAdvertisement(pipeAdv, PEER_PRESENCE_ADVERTISEMENT_EXPIRATION, PEER_PRESENCE_ADVERTISEMENT_EXPIRATION);
    }
    
    protected void republishDirectCommunicationPipeAdvertisement(long lifetime, long expirationTime)
    {
        this.logger.debug("Republishing pipe advertisement.");
        
        if (!this.hasJoinedAGroup()) {
            this.logger.warn("No group joined. Republish aborted.");
            return;
        }
        
        if (this.serverSocket == null) {
            this.logger.warn("Server socket not initialized. Republish abotred.");
            return;
        }
        
        try {
            this.publishDirectCommunicationPipeAdvertisement(getMyDirectCommunicationPipeAdvertisement(), lifetime, expirationTime);
        } catch (Exception e) {
            this.logger.error("Failed to publish pipe advertisement.", e);
        }
    }
    
    /** {@inheritDoc} */
    public void republishDirectCommunicationPipeAdvertisement()
    {
        this.republishDirectCommunicationPipeAdvertisement(PEER_PRESENCE_ADVERTISEMENT_EXPIRATION, PEER_PRESENCE_ADVERTISEMENT_EXPIRATION);
    }
    
    /**
     * Invalidate the direct communication pipe advertisement by republishing with an immediate expiration time which
     * will cause it to immediately expire on remote peers that will discover it.
     */
    protected void invalidateDirectCommunicationPipeAdvertisement()
    {
        this.republishDirectCommunicationPipeAdvertisement(1, 1);
    }
    
    protected void closeExistingDirectCommunicationServerSocket() throws IOException
    {
        this.logger.debug("Closing existing direct communication server socket.");
        
        if (this.serverSocket != null) {
            this.serverSocket.close();
            
            this.invalidateDirectCommunicationPipeAdvertisement();
            
            this.serverSocket = null;
        }
    }
    
    
    /** {@inheritDoc} **/
    public void leavePeerGroup(PeerGroup oldGroup) throws PeerGroupException {

        this.logger.debug("Leaving peer group: " + oldGroup);
        
        if (oldGroup == null) {
           this.logger.warn("Null group provided. Ignoring request.");
           return; 
        }
        
    	// If not connected to the network there is nothing to leave from.
        if (!this.isConnectedToNetwork()) {
            this.logger.warn("Not connected to network. Ignoring request.");
        	return;
        }
        
        // See if it's the default group. Don`t think you can leave that.
        if (oldGroup.getPeerGroupID().equals(this.rootGroup.getPeerGroupID())) {
            this.logger.warn("Asked to leave the default NetPeerGroup. Ignoring request.");
        	return;
        }
        
        // Stop being rdv for the gorup.
        RendezVousService oldGroupRendezvousService = oldGroup.getRendezVousService();
        oldGroupRendezvousService.stopRendezVous();
        
        // Resign from the group.
        MembershipService oldGroupMembershipService = oldGroup.getMembershipService();
        oldGroupMembershipService.resign();
        
        // Stop listening to rendezvous events.
        oldGroupRendezvousService.removeListener(this);
        
        // Stop listening to discovery events.
        DiscoveryService oldGroupDiscoveryService = oldGroup.getDiscoveryService();
        oldGroupDiscoveryService.removeDiscoveryListener(this);
        
        try {
            this.closeExistingDirectCommunicationServerSocket();
        } catch (IOException e) {
            // This will never happen in the current implementation but it's best to be sure.
            // Just log it.
            this.logger.warn("Failed to close existing direct communciation server socket after leaving a group.", e);
        }
        
        // See if it was the current joined group.
        if (this.currentJoinedGroup != null && oldGroup.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
            this.currentJoinedGroup = this.rootGroup;
        }
        
        // Clean the local cache for this group.
        this.flushExistingAdvertisements(oldGroup, DiscoveryService.PEER);
        this.flushExistingAdvertisements(oldGroup, DiscoveryService.ADV);
        
        // Stop peer presence task.
        this.presenceTask.cancel();
        this.timer.purge();
        
        // Stop group services and free used memory
        oldGroup.stopApp();
        oldGroup.unref();
        oldGroup = null;
        
        // Dispose of the the JxtaCast instance.
        this.jc.setPeerGroup(null);
        this.jc = null;
        
        System.runFinalization();
        System.gc();
    }
    
    
    /** {@inheritDoc} **/
    public void leavePeerGroup() throws PeerGroupException {
        leavePeerGroup(this.currentJoinedGroup);
    }
    

    /**
     * Build a Module Implementation Advertisement suitable for the PSE Sample
     * Peer Group. The <tt>ModuleImplAdvertisement</tt> is built using the
     * result of <tt>base.getAllPurposePeerGroupImplAdvertisement()</tt> to
     * ensure that the result will be appropriate for running as a child
     * peer group of <tt>base</tt>.
     * <p/>
     * <p/>The default advertisement is modified to use the PSE Membership
     * Service as it's membership service replacing whatever membership
     * service was originally specified (except if it already is PSE of course).
     * <p/>
     * <p/>The Module Spec ID of the ModuleImplAdvertisement is set to a new and
     * random value in order not to collide with the base group's
     * ModuleImplAdvertisement.
     *
     * @param base The Peer Group from which we will retrieve the default
     *             Module Implementation Advertisement.
     * @return The Module Implementation Advertisement for the PSE Sample
     *         Peer Group.
     */
    @SuppressWarnings("unchecked")
	static ModuleImplAdvertisement buildNewGroupImplAdvertisementWithPSE(PeerGroup base, String newGroupName) {
        ModuleImplAdvertisement newGroupImpl;

        try {
            newGroupImpl = base.getAllPurposePeerGroupImplAdvertisement();
        } catch (Exception unlikely) {
            // getAllPurposePeerGroupImplAdvertisement() doesn't really throw exceptions.
            throw new IllegalStateException("Could not get All Purpose Peer Group Impl Advertisement.");
        }

        newGroupImpl.setDescription(newGroupName + " Peer Group Implementation");
        newGroupImpl.setModuleSpecID(IDFactory.newModuleSpecID(PeerGroup.peerGroupClassID));

        // FIXME Use something else to edit the params. Could be no longer needed in jxta 2.6
        StdPeerGroupParamAdv params = new StdPeerGroupParamAdv(newGroupImpl.getParam());

        Map<ModuleClassID, Object> newGroupServices = params.getServices();

        ModuleImplAdvertisement baseGroupMembershipModuleAdv = (ModuleImplAdvertisement) newGroupServices.get(PeerGroup.membershipClassID);

        newGroupServices.remove(PeerGroup.membershipClassID);

        // The ModuleImplAdvertisement of the PSEMembershipService we want to set to the group.
        ModuleImplAdvertisement pseMembershipServiceImplAdv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(
                ModuleImplAdvertisement.getAdvertisementType());

        pseMembershipServiceImplAdv.setModuleSpecID(PSEMembershipService.pseMembershipSpecID);
        pseMembershipServiceImplAdv.setCompat(baseGroupMembershipModuleAdv.getCompat());
        pseMembershipServiceImplAdv.setCode(PSEMembershipService.class.getName());
        pseMembershipServiceImplAdv.setUri(baseGroupMembershipModuleAdv.getUri());
        pseMembershipServiceImplAdv.setProvider(baseGroupMembershipModuleAdv.getProvider());
        pseMembershipServiceImplAdv.setDescription("PSE Membership Service");

        // Add our selected membership service to the peer group service as the
        // group's default membership service.
        newGroupServices.put(PeerGroup.membershipClassID, pseMembershipServiceImplAdv);

        // Save the group impl parameters
        newGroupImpl.setParam((Element) params.getDocument(MimeMediaType.XMLUTF8));

        return newGroupImpl;
    }
    
    
    /**
     * Build the Peer Group Advertisement for the PSE Sample Peer Group.
     * <p/>
     * <p/>The Peer Group Advertisement will be generated to contain an
     * invitation certificate chain and encrypted private key. Peers which
     * know the password for the Peer Group Root Certificate Key can generate
     * their own invitation otherwise peers must get an invitation from
     * another group member.
     * <p/>
     * <p/>The invitation certificate chain appears in two forms:
     * <ul>
     * <li>Self Invitation : PSE Sample Group Root Certificate + Encrypted Private Key</li>
     * <li>Regular Invitation :
     * <ul>
     * <li>Invitation Certificate + Encrpyted Private Key</li>
     * <li>Peer Group Member Certificate</li>
     * <li>Peer Group Administrator Certificate</li>
     * <li>PSE Sample Group Root Certificate</li>
     * </ul></li>
     * </ul>
     * <p/>
     * <p/>Invitations are provided to prospective peer group members. You can
     * use a unique invitation for each prospective member or a single
     * static invitation for every prospective member. If you use a static
     * invitation certificate keep in mind that every copy will use the same
     * shared password and thus the invitation will provide only very limited
     * security.
     * <p/>
     * <p/>In some applications the invitation password will be built in to the
     * application and the human user will never have to know of it's use.
     * This can be useful if you wish your PSE Peer Group used only by a single
     * application.
     *
     * @param pseImpl              The Module Impl Advertisement which the Peer Group
     *                             Advertisement will reference for its Module Spec ID.
     * @param certificateChain  The certificate chain which comprises the
     *                             PeerGroup Invitation.
     * @param encryptedGroupPrivateKey The private key of the invitation.
     * @return The Peer Group Advertisement.
     */
    @SuppressWarnings("unchecked")
	static PeerGroupAdvertisement buildGroupAdvWithPSE(String groupName, String description, 
    		ModuleImplAdvertisement pseImpl, X509Certificate[] certificateChain, EncryptedPrivateKeyInfo encryptedGroupPrivateKey) {
    	
    	// TODO:The invitation based group join should be investigated deeper. It could provide a nicer solution than the
    	// (not-necesarily) simple password based authentication.
    	
        PeerGroupAdvertisement newPGAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());

        newPGAdv.setPeerGroupID(IDFactory.newPeerGroupID());
        newPGAdv.setModuleSpecID(pseImpl.getModuleSpecID());
        newPGAdv.setName(groupName);
        newPGAdv.setDescription(description);

        PSEConfigAdv pseConfAdv = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());

        pseConfAdv.setCertificateChain(certificateChain);
        pseConfAdv.setEncryptedPrivateKey(encryptedGroupPrivateKey, certificateChain[0].getPublicKey().getAlgorithm());

        XMLDocument pseDoc = (XMLDocument) pseConfAdv.getDocument(MimeMediaType.XMLUTF8);

        newPGAdv.putServiceParam(PeerGroup.membershipClassID, pseDoc);

        return newPGAdv;
    }
    

    /**
     * Authenticate membership in a peer group using {@link PSEMembershipService}'s \"StringAuthentication\" method.
     * </p>
     * If the group is a private group and uses {@link PSEMembershipService}, the provided passwords will be used. If it is a public group, the passwords will be ignored.
     * 
     * @param keystorePassword the password of the local keystore.
     * @param identityPassword the group's password.
     * 
     * @return true if successful, false if the provided passwords were not correct or joining failed.
     * @throws PeerGroupException if problems occurred joining the group.
     * @throws ProtocolNotSupportedException if problems occur authenticating credentials.
     */
    protected boolean authenticateMembership(PeerGroup group, char[] keystorePassword, char[] identityPassword) throws PeerGroupException, ProtocolNotSupportedException {
    	
        // Get the MembershipService from the peer group.
        MembershipService membershipService = group.getMembershipService();
        
        this.logger.info("Current Membership service: " + membershipService);
    	
        //StructuredDocument creds = null;
        Authenticator memberAuthenticator = null;
        
        String authenticationMethod = null;
        
        if (membershipService instanceof PSEMembershipService/*keystorePassword != null && identityPassword != null*/) {
        	authenticationMethod = "StringAuthentication";
        }
        
        try {

        	// Generate the credentials for the Peer Group.
	        AuthenticationCredential authCred = 
	            new AuthenticationCredential(group, authenticationMethod, null);
	
	        // Get the Authenticator from the Authentication creds.
	        memberAuthenticator = membershipService.apply(authCred);
        
        } catch (ProtocolNotSupportedException noAuthenticator) {
            this.logger.error("Could not create authenticator:\n", noAuthenticator);
        	return false;
        }

        if (memberAuthenticator instanceof StringAuthenticator) {
            
            ID identity = group.getPeerGroupID();
            
            StringAuthenticator stringAuthenticator = ((StringAuthenticator) memberAuthenticator);
	        
	        stringAuthenticator.setAuth1_KeyStorePassword(keystorePassword);
	        stringAuthenticator.setAuth2Identity(identity);
	        stringAuthenticator.setAuth3_IdentityPassword(identityPassword);
        }
        
        // Check if everything is okay to join the group.
        if (memberAuthenticator.isReadyForJoin()) {
        	try {
        		this.groupCredential = membershipService.join(memberAuthenticator);
        		this.logger.info("Member authentication successful.");
        	} catch (PeerGroupException failed) {
        	    this.logger.error("Member authentication failed:\n", failed);
        		return false;
        	}
        }
        else {
            this.logger.error("Can't join the group yet. Authentication data incorrent or incomplete.");
            return false;
        }

        return true;
    }


    /** {@inheritDoc} **/
    public boolean isRendezvous(PeerAdvertisement peerAdv) {

        // If this peer is not from the currentJoinedGroup, bail. 
    	if (peerAdv.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
    		return false;
    	}
    	
    	// Find the PeerGroup object for this group.
        /*PeerGroup group = findJoinedGroup(peerAdv.getPeerGroupID());
        if (group == null)
            return false;
        */

        // Are we checking for our own peer?  If so, we can just ask the
        // PeerGroup object if we are a rendezvous.
        if (peerAdv.getPeerID().equals(rootGroup.getPeerAdvertisement().getPeerID())) {
            return this.isGroupRendezVous();
        }

        
        // Get the RendezVousService from the PeerGroup.
        /*RendezVousService rdv = (RendezVousService)group.getRendezVousService();
        if (rdv == null)
            return false;*/

        // Get a list of the connected rendezvous peers for this group, and
        // search it for the requested peer.
        //
        PeerID peerID = null;
        Enumeration<ID> rdvs = null;
        rdvs = this.getConnectedRdvsIDs();
        while (rdvs.hasMoreElements()) {
            try {
                peerID = (PeerID)rdvs.nextElement();
                if (peerID.equals(peerAdv.getPeerID()))
                    return true;
            } catch (Exception e) {}
        }

        // Didn't find it, the peer isn't a rendezvous.
        return false;
    }

//
//    /** Search our array of joined groups for the requested group.
//     *  @return PeerGroup, or null if not found.
//     */
//    protected PeerGroup findJoinedGroup(PeerGroupAdvertisement groupAdv) {
//        return findJoinedGroup(groupAdv.getPeerGroupID());
//    }
//
//
//    /** Search our array of joined groups for the requested group.
//     *  @return PeerGroup, or null if not found.
//     */
//    protected PeerGroup findJoinedGroup(PeerGroupID groupID) {
//
//        PeerGroup group = null;
//
//        // Step thru the groups we've created, looking for one that has the
//        // same peergroup ID as the requested group.
//        //
//        Enumeration<PeerGroup> myGroups = joinedGroups.elements();
//        while (myGroups.hasMoreElements()) {
//            group = (PeerGroup)myGroups.nextElement();
//
//            // If these match, we found it.
//            if (group.getPeerGroupID().equals(groupID))
//                return group;
//        }
//
//        // Didn't find it.
//        return null;
//    }


    /** {@inheritDoc} **/
	public void addJxtaCastEventListener(JxtaCastEventListener listener) {
		if (!this.hasJoinedAGroup()) {
			throw new IllegalStateException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.addJxtaCastEventListener(listener);
	}
	
	/** {@inheritDoc} **/
	public void removeJxtaCastEventListener(JxtaCastEventListener listener) {
		if (!this.hasJoinedAGroup()) {
			throw new IllegalStateException("The peer has not yet joined a group.");
		}
		
		if (jc != null) {
		    jc.removeJxtaCastEventListener(listener);
		} else {
		    this.logger.warn("JxtaCast module not initiated. Remove listener request aborded.");
		}
	}

	/** {@inheritDoc} **/
	public void sendChatMsg(String text) throws PeerGroupException {
		if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendChatMsg(text);
	}

	/** {@inheritDoc} **/
	public void sendFile(File file, String caption) throws PeerGroupException {
		if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendFile(file, caption);
	}

	/** {@inheritDoc} **/
	public void sendObject(Object object, String caption) throws PeerGroupException {
		if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException("The object does not implement the interface java.io.Serializable and can not be sent.");
        }
		
		this.logger.debug("Sending object of type " + object.getClass() + " to group. Caption: " + caption);
		
		jc.sendObject(object, caption);
	}
	
	/** {@inheritDoc} **/
	public Object sendObject(Object object, PipeAdvertisement pipeAdv) throws JxtaException {
	    if (!this.isConnectedToGroup()) {
            throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
        }
	    
	    if (!(object instanceof Serializable)) {
	        throw new IllegalArgumentException("The object does not implement the interface java.io.Serializable and can not be sent.");
	    }

	    boolean failed = false;
	    
	    Socket socket = null;
	    
	    InputStream is = null;
        OutputStream os = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            socket = new JxtaSocket(this.currentJoinedGroup, pipeAdv);     
            socket.setSoTimeout(WAIT_INTERVAL_FOR_DIRECT_COMMUNICATION_CONNECTIONS);
        } catch (Exception e) {
            throw new JxtaException("Failed to create a direct connection using the provided pipe advertisement.", e);
        }
        
        try {
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
            
            oos.writeObject(object);
            
            oos.flush();
            os.flush();
        } catch (Exception e) {
            failed = true;
            throw new JxtaException("Failed to send an object through a direct connection using the provided pipe advertisement.", e);
        } finally {
            try {                
                if (oos != null) {
                    oos.close();
                }
                if (os != null) {
                    os.close();
                }
                
                // Close the socket only if this step has failed.
                if (failed && socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                // Just log it.
                this.logger.warn("Failed to close streams for this conenction.");
            }
        }
        
        Object replyMessage = null;
        try {
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
            
            replyMessage = ois.readObject(); 
            
        } catch (EOFException eof) {
            this.logger.debug("There is no reply to this message. Returning null.");
            replyMessage = null;
        } catch (Exception e) {
            throw new JxtaException("Failed to receive reply message.", e);
        } finally {
            try{
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {
            	// Just log it.
                this.logger.warn("Failed to close object input stream for this conenction.", e);
            }
            
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            	// Just log it.
                this.logger.warn("Failed to close input stream for this conenction.", e);
            }
                
            try {
                // Close the socket, we are done.
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                // Just log it.
                this.logger.warn("Failed to close socket for this conenction.");
            }
        }
        
        return replyMessage;
	}
	
	/** {@inheritDoc} **/
	public Object sendObjectToRandomPeerInGroup(Object object, boolean expectReply) throws PeerGroupException, IllegalArgumentException, JxtaException {
	    if (!this.isConnectedToGroup()) {
            throw new PeerGroupException("The peer has not yet joined a group and contacted a group RDV peer.");
        }
        
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException("The object does not implement the interface java.io.Serializable and can not be sent.");
        }
        
        this.logger.info("Trying to send an object to a random peer in the group. Expecting reply: " + expectReply);
        
        Object reply = null;
        PipeAdvertisement myPipeAdvertisement = getMyDirectCommunicationPipeAdvertisement();
        boolean success = false;
        
        for(int i=0; i<NUMBER_OF_TRIES && !success; i++) {
            this.logger.info("Try number: " + i);
            
            Enumeration<Advertisement> pipeAdvertisements = this.getKnownDirectCommunicationPipeAdvertisements();
            
            // Try all available pipe ADVs until one succeeds.
            while (pipeAdvertisements.hasMoreElements() && !success) {
                Advertisement adv = pipeAdvertisements.nextElement();
                
                if (!(adv instanceof PipeAdvertisement) || adv.equals(myPipeAdvertisement)) {
                    // Not interested, skip.
                    continue;
                }
                
                PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;
                
                this.logger.info("Trying to send through this pipe advertisement:\n" + pipeAdv);
                
                try {
                    reply = this.sendObject(object, pipeAdv);
                } catch (Exception e) {
                    this.logger.error("Failed to send object to this peer.\n", e);
                    continue;
                }
                
                if (expectReply && reply == null) {
                    this.logger.warn("Peer contacted but no reply given. Skipping");
                    continue;
                }
                
                success = true;
            }
            
            if (success) {
                break;
            }
            
            // Wait for advertisement discovery to get more possible pipe advs.
            try {
                Thread.sleep(WAIT_INTERVAL_BETWEEN_TRIES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        this.logger.info("Object successfuly sent: " + success);
        if (!success) {
            throw new JxtaException("Failed to send message. No peer group member found or none of them could receive it. Try again later.");
        }
        
        return reply;
    }

	/** {@inheritDoc} **/
	public void rendezvousEvent(RendezvousEvent event) 
	{
		if (event.getType() == RendezvousEvent.RDVCONNECT ||
        event.getType() == RendezvousEvent.RDVRECONNECT ||
        event.getType() == RendezvousEvent.RDVDISCONNECT ||
        event.getType() == RendezvousEvent.RDVFAILED ||
        event.getType() == RendezvousEvent.BECAMERDV) {
		    
		    // If we've connected to a new RDV or just disconnected from one.  Launch discovery,
	        // so we can see any ADVs (peers, groups, peer back-channel ADVs.) this RDV knows or used to know.
		    
		    this.discoverGroups(event.getPeer(), null);
            this.discoverPeers(event.getPeer(), null);
            this.discoverAdvertisements(event.getPeer(), null, PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");
		} else if (event.getType() == RendezvousEvent.CLIENTDISCONNECT ||
		    event.getType() == RendezvousEvent.CLIENTFAILED) {
		    
		    // If we are RDV and a client just disconnected (gracefully or not), clean his direct communication pipe adv
		    // and his peer adv.
		    
		    String leavingClientPeerId = event.getPeer();
		    this.logger.debug("Peer " + leavingClientPeerId + " disconnected.");
		    
		    DiscoveryService discoveryService = this.currentJoinedGroup.getDiscoveryService();
		    Enumeration<Advertisement> pipeAdvertisements = this.getKnownDirectCommunicationPipeAdvertisements();
		    while (pipeAdvertisements.hasMoreElements()) {
		        PipeAdvertisement pipeAdvertisement = (PipeAdvertisement) pipeAdvertisements.nextElement();
		        
		        String pipeName = pipeAdvertisement.getName();
		        String ownerPeerId = JxtaPeer.getPeerIdFromBackChannelPipeName(pipeName);
		        if (leavingClientPeerId.equals(ownerPeerId)) {
		            try {
		                this.logger.debug("Flushing pipe advertisement of disconnected peer named " + pipeName);
                        discoveryService.flushAdvertisement(pipeAdvertisement);
                    } catch (IOException e) {
                        String peerName = JxtaPeer.getPeerNameFromBackChannelPipeName(pipeName);
                        this.logger.warn("Failed to flush the pipe advertisement of the peer named "
                            + peerName + " after he has disconnected.\n", e);
                    }
		        }
		    }
		    
		    Enumeration<PeerAdvertisement> peerAdvertisements = this.getKnownPeers();
            while (peerAdvertisements.hasMoreElements()) {
                PeerAdvertisement peerAdvertisement = (PeerAdvertisement) peerAdvertisements.nextElement();
                String peerName = peerAdvertisement.getName();
                
                String peerId = peerAdvertisement.getPeerID().toString();
                if (leavingClientPeerId.equals(peerId)) {
                    try {
                        this.logger.debug("Flushing pipe advertisement of disconnected peer named " + peerName);
                        discoveryService.flushAdvertisement(peerAdvertisement);
                    } catch (IOException e) {
                        this.logger.warn("Failed to flush the peer advertisement of the peer named "
                            + peerName + " after he has disconnected.\n", e);
                    }
                }
            }
		}
		    
		
	}
	
	/** {@inheritDoc} **/
	public PeerGroup getCurrentJoinedPeerGroup() {
		return this.currentJoinedGroup;
	}
	
	/** {@inheritDoc} **/
	public boolean isJxtaStarted() {
		return this.rootGroup != null;
	}
	
	/** {@inheritDoc} **/
	public boolean hasJoinedAGroup() {
		return this.currentJoinedGroup != null && !this.currentJoinedGroup.equals(this.rootGroup);
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToNetwork() {
		return (this.isNetworkRendezVous()
		    /* && !this.rootGroup.getRendezVousService().getRendezVousStatus().equals(RendezVousStatus.AUTO_RENDEZVOUS)*/
		    /*&& (this.getManager().getMode().equals(ConfigMode.RENDEZVOUS_RELAY) || this.getManager().getMode().equals(ConfigMode.RENDEZVOUS))*/
		    ) || this.isConnectedToNetworkRendezVous(); 
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToGroup() {
	    if (!this.hasJoinedAGroup()) {
	        return false;
	    }
	    
	    RendezVousService rdvService = this.currentJoinedGroup.getRendezVousService();
	    boolean knowsOtherRdvs = rdvService.getLocalWalkView().size() > 0;
	    boolean hasClientPeersConnected = rdvService.getConnectedPeerIDs().size() > 0;
	    
		return (this.isGroupRendezVous() && (knowsOtherRdvs || hasClientPeersConnected)) ||
		    this.isConnectedToGroupRendezVous(); 
	    
//	    return this.isGroupRendezVous() || this.isConnectedToGroupRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isNetworkRendezVous() {
		return this.isJxtaStarted() && this.rootGroup.getRendezVousService().isRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isGroupRendezVous() {
		return this.hasJoinedAGroup() && this.currentJoinedGroup.getRendezVousService().isRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToNetworkRendezVous() {
		return this.isJxtaStarted() && this.rootGroup.getRendezVousService().isConnectedToRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToGroupRendezVous() {
		return this.hasJoinedAGroup() && this.currentJoinedGroup.getRendezVousService().isConnectedToRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isNetworkConfigured() {
		return this.manager != null;
	}

//
//    /**
//     * @return the directMessageReceiver
//     */
//    public DirectMessageReceiver getDirectMessageReceiver()
//    {
//        return this.directMessageReceiver;
//    }
//
//
//    /**
//     * To be used from the outside.
//     * 
//     * @param directMessageReceiver the directMessageReceiver to set
//     */
//    public void setDirectMessageReceiver(DirectMessageReceiver directMessageReceiver) throws JxtaException
//    {
//        // If this is an unset (unregister), kill the connection handler thread.
//        if (directMessageReceiver == null) {
//            try {
//                this.closeExistingDirectCommunicationServerSocket();
//            } catch (IOException e) {
//                throw new JxtaException("Unable to unregister the listener.", e);
//            }
//        } else {
//            
//            // If this is the first time a listener registers, create the connection handler thread.
//            if (this.directMessageReceiver == null && this.serverSocket == null) {
//                try {
//                    this.directMessageReceiver = directMessageReceiver;
//                    this.createDirectCommunicationServerSocket();
//                } catch (IOException e) {
//                    throw new JxtaException("Unable to register the listener.", e);
//                }
//            }
//        }
//        
//        this.directMessageReceiver = directMessageReceiver;
//    }
	
	/**
	 * Flush advertisements of a given type for a certain group. If something fails along the way, it will just be
	 * logged and no exception will be thrown.
	 *  
	 * @param group the group whos advertisements to flush.
	 * @param advertisementType the type of advertisements to flush.
	 * @see DiscoveryService
	 */
    public void flushExistingAdvertisements(PeerGroup group, int advertisementType)
    {
        DiscoveryService discoveryService = group.getDiscoveryService();
        if (discoveryService == null) {
            this.logger.warn("DiscoveryService for group " + group.getPeerGroupName() + " could not be obtained. Flushing advertisements canceled.");
            return;
        }
                
        Enumeration<Advertisement> advertiements = null;
        try {
            advertiements = discoveryService.getLocalAdvertisements(advertisementType, null, null);
        } catch (Exception e) {
            // just log it.
            this.logger.warn("Failed to get local advertisements of type " + advertisementType + " for group " + group, e);
            return;
        }
             
        while (advertiements.hasMoreElements()) {
            Advertisement adv = advertiements.nextElement();
            try {
                discoveryService.flushAdvertisement(adv);
            } catch (Exception e) {
                // just log it.
                this.logger.warn("Failed to flush advertisement:\n" + adv + "\n", e);
            }
        }
    }


    /**
     * {@inheritDoc}
     * <p/>
     * Make sure we never have duplicate direct communication channels (pipe advertisements) for one peer.
     **/
    public void discoveryEvent(DiscoveryEvent event)
    {
        // FIXME: this only fixes the problem on the group RDV side and on the joining peer's side.
        // Existing group members still have this problem.      
        //  UPDATE: existing group members will update their local repo when a disconnected peer rejoins, but at least there will
        //  be no duplicates.
        // Existing group members need to be notified of disconnecting peers.
        
        DiscoveryResponseMsg response = event.getResponse();
        String queryAttribute = response.getQueryAttr();
        String queryValue = response.getQueryValue();
        String desiredQueryValue = this.getDirectCommunicationPipeNamePrefix() + "*";
        if (!this.isGroupRendezVous() && (response.getDiscoveryType() != DiscoveryService.ADV || !PipeAdvertisement.NameTag.equals(queryAttribute) || !desiredQueryValue.equals(queryValue))) {
            // Not interested in other advertisements, responses to other queries.
            return;
        }
        
        // A response to a direct communication channel advertisements discovery request or a remote publish by an EDGE peer when we are RDV.
        
        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();
        
        Enumeration<Advertisement> receivedAdvertisements = response.getAdvertisements();
        while (receivedAdvertisements.hasMoreElements()) {
            Advertisement receivedAdvertisement = receivedAdvertisements.nextElement();
            // Make sure there aren't other types of advertisements.
            if (!(receivedAdvertisement instanceof PipeAdvertisement)) {
                // Not interested in other advertisements.
                continue;
            }
            
            PipeAdvertisement receivedPipeAdvertisement = (PipeAdvertisement) receivedAdvertisement;
            if (!receivedPipeAdvertisement.getName().startsWith(this.getDirectCommunicationPipeNamePrefix())) {
                // Not interested in other advertisements than direct communication channel advs.
                continue;
            }
            
            String receivedPipeName = receivedPipeAdvertisement.getName();
            ID receivedPipeId = receivedPipeAdvertisement.getPipeID();
            String receivedPeerId = JxtaPeer.getPeerIdFromBackChannelPipeName(receivedPipeName);
            long receivedCreationTime = 0;
            try {
                receivedCreationTime = Long.parseLong(JxtaPeer.getCreationTimeFromBackChannelPipeName(receivedPipeName));
            } catch (Exception failedGetCreationTime) {
                this.logger.warn("Removing invalid received pipe advertisement named " + receivedPipeName, failedGetCreationTime);
                try {
                    disco.flushAdvertisement(receivedPipeAdvertisement);
                } catch (Exception failedFlushAdvertisement) {
                    this.logger.warn("Failed to remove invalid received pipe advertisement named " + receivedPipeName, failedFlushAdvertisement);    
                } 
                
                // Go to next received advertisement.
                continue;
            }
            
            // Get locally stored Pipe advertisements. 
            // Note: we don't use this.getKnownDirectCommunicationAdvertisements because we don`t want to trigger a new discovery event and enter an infinite loop.
            List<Advertisement> existingLocalAdvertisements = null;
            try {
                // get only existing direct communication channel advs.
                existingLocalAdvertisements = Collections.list(disco.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.NameTag, desiredQueryValue));
            } catch (Exception e) {
                logger.warn("Failed to get locally stored known advertisements while checking for duplicate direct communication advertisements.\n", e);
                // hope others won't have this problem.
                return;
            }
            
            // Reset flag.
            boolean receivedFoundInExisting = false;
            
            // Search through the local direct communication advertisements if we already have an advertisement with the same pipeName of the new advertisement.
            for (Advertisement existingLocalAdvertisement : existingLocalAdvertisements) {
                // Make sure there aren't other types of advertisements.
                if (!(existingLocalAdvertisement instanceof PipeAdvertisement)) {
                    // Not interested in other advertisements.
                    continue;
                }
                
                PipeAdvertisement existingPipeAdvertisement = (PipeAdvertisement) existingLocalAdvertisement;
                String existingPipeName = existingPipeAdvertisement.getName();
                ID existingPipeId = existingPipeAdvertisement.getPipeID();
                String existingPeerId = JxtaPeer.getPeerIdFromBackChannelPipeName(existingPipeName);
                
                if (!existingPipeName.startsWith(this.getDirectCommunicationPipeNamePrefix())) {
                    // Not interested in other pipe advertisements.
                    continue;
                }
            
                if (receivedPipeId.equals(existingPipeId)) {
                    if (receivedFoundInExisting) {
                        // Duplicate advertisement found. Deleting.
                        try {
                            disco.flushAdvertisement(existingPipeAdvertisement);
                        } catch (Exception failedFlushAdvertisement) {
                            this.logger.warn("Failed to remove duplicate existing pipe advertisement named " + receivedPipeName, failedFlushAdvertisement);    
                        }
                    } else {
                        // Mark that we found the received advertisement trough the existing ones.
                        receivedFoundInExisting = true;
                    }
                    
                    // Ignore retransmissions of the same advertisement.
                    continue;
                }
                
                if (!receivedPeerId.equals(existingPeerId)) {
                    // Ignore owning peer ID non-collisions. 
                    continue;
                }
                
                // It's not the same advertisement retransmitted, but it's actually a new one, for the same owner(name collision)
                // Different pipeIDs but same owning peerId.
                
                long existingCreationTime = 0;
                try {
                    existingCreationTime = Long.parseLong(JxtaPeer.getCreationTimeFromBackChannelPipeName(existingPipeName));
                } catch (Exception failedGetCreationTime) {
                    this.logger.warn("Removing invalid existing pipe advertisement named " + receivedPipeName, failedGetCreationTime);
                    try {
                        disco.flushAdvertisement(existingPipeAdvertisement);
                    } catch (Exception failedFlushAdvertisement) {
                        this.logger.warn("Failed to remove invalid existing pipe advertisement named " + receivedPipeName, failedFlushAdvertisement);    
                    }
                    
                    // Go to next existing advertisement.
                    continue;
                }
                
                PipeAdvertisement toFlush = null;
                try {
                    // update the communication channel for the owner by deleting the old and outdated one and keeping the new one.
                    if (receivedCreationTime > existingCreationTime) {
                        toFlush = existingPipeAdvertisement;
                    } else if (existingCreationTime > receivedCreationTime){
                        toFlush = receivedPipeAdvertisement;
                    }
                    
                    if (toFlush != null) {
                        this.logger.debug("Flushing outdated pipe advertisement by the name: " + toFlush.getName());
                        disco.flushAdvertisement(toFlush);
                    }
                } catch (Exception e) {
                    // Leave it as duplicate. The user will see the same peer twice but we hope that the next time it will be successfuly flushed.
                    this.logger.warn("Failed to flush outdated pipe advertisement by the name: " + toFlush.getName() + "\n", e);
                }
                
                // don't stop looking because we might have more outdated advertisements for the same peer (more collisions)
            }
        }   
    }
}
