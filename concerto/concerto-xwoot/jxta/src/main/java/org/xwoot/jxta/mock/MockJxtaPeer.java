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
package org.xwoot.jxta.mock;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.jxta.DirectMessageReceiver;
import org.xwoot.jxta.NetworkManager;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.NetworkManager.ConfigMode;

import net.jxta.discovery.*;
import net.jxta.exception.JxtaException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.id.UUID.PeerGroupID;
import net.jxta.impl.membership.none.NoneMembershipService;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.protocol.PeerAdv;
import net.jxta.impl.protocol.PipeAdv;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.*;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.*;
import net.jxta.rendezvous.*;

import net.jxta.credential.Credential;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;

/**
 * Implementation handling the gory details of JXTA. 
 *              
 * @version $Id$
 */
public class MockJxtaPeer implements Peer, RendezvousListener {

    protected PeerGroup rootGroup;
    protected PeerGroup currentJoinedGroup;
    
    protected Credential groupCredential;
    protected JxtaCastEventListener jxtaCastListener;
    protected DirectMessageReceiver directMessageReceiver;
    protected Log logger = LogFactory.getLog(this.getClass());
    
    protected boolean networkConfigured;
    protected boolean connectedToNetwork;
    protected boolean connectedToGroup;
    
    
    /** The pipe name to be used when broadcasting messages. Interested peers will look for this. */
	public static final String PROPAGATE_PIPE_ADVERTISEMENT_NAME = "ConcertoMessageBroadcast";
	
	/** Number of ms to wait for the output pipe to be resolved when directly communicating through the back-channel. */
	public static final long BACK_CHANNEL_OUTPUT_PIPE_RESOLVE_TIMEOUT = 5000;
	
	/** Number of ms to wait between tries to discover pipe ADVs in a group and send an object to one of them. */ 
	public static final long WAIT_INTERVAL_BETWEEN_TRIES = 5000;
	
	/** Number tries to discover pipe ADVs in a group and send an object to one of them. */
	public static final int NUMBER_OF_TRIES = 5;
	
	/** Number of ms to wait for a rdv connection to a group. */ 
    public static final long WAIT_FOR_RDV_CONNECTION_PERIOD = 120000;
    
    /** This peer's peer advertisement. */
    protected PeerAdv myPeerAdv;          
    
    /** "NetPeerGroup" advertisement. */
    protected PeerGroupAdvertisement rootGroupAdv;  
	
	protected PeerAdv.Instantiator peerAdvInst;  // Instantiator of PeerAdv objects.
    protected PipeAdv.Instantiator pipeAdvInst;  // Instantiator of PipeAdv objects.
    
    // global registry
    public static Map<PeerGroupAdvertisement, Set<MockJxtaPeer>> groupsWithPeersMap =  new HashMap<PeerGroupAdvertisement, Set<MockJxtaPeer>>();;
    public static Set<PeerGroupAdvertisement> groupAdvRegistry = new HashSet<PeerGroupAdvertisement>();
    
    // local cache
    public Set<PeerGroupAdvertisement> localGroupAdvRegistry;
    public Set<PeerAdvertisement> localPeerAdvRegistry;
    public Set<Advertisement> localPipeAdvRegistry;
    
    public char[] localKeystorePassword;
    
    public static ID groupAuthenticationServiceParamID = IDFactory.newModuleSpecID(PeerGroup.membershipClassID);
    
    protected PipeAdvertisement myPipeAdv;
    
    public boolean rendezVousForGroup;
    
	
    public MockJxtaPeer() {
        this.connectedToNetwork = false;
        this.connectedToGroup = false;
        
        // Create a simulation of the "NetPeerGroup".
        rootGroupAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());
        rootGroupAdv.setName("NetPeerGroup");
        rootGroupAdv.setPeerGroupID(new PeerGroupID());

        // Create my peer advertisement.
        peerAdvInst = new PeerAdv.Instantiator();
        myPeerAdv = (PeerAdv)peerAdvInst.newInstance();
        myPeerAdv.setName("MyMockPeerName");
        myPeerAdv.setPeerID(IDFactory.newPeerID((PeerGroupID)rootGroupAdv.getPeerGroupID()));
        myPeerAdv.setPeerGroupID(rootGroupAdv.getPeerGroupID());
        
        // local cache.
        localGroupAdvRegistry = new HashSet<PeerGroupAdvertisement>();
        localPeerAdvRegistry = new HashSet<PeerAdvertisement>();
        localPipeAdvRegistry = new HashSet<Advertisement>();
        
        this.setRendezVousForGroup(false);
    }
    
    /**
     * @param group the group.
     * @return a list of peers in a group.
     */
    public Set<MockJxtaPeer> getPeersInGroup(PeerGroup group)
    {
        return groupsWithPeersMap.get(group);
    }
    
    
	/** {@inheritDoc} **/
	public void configureNetwork(String peerName, File jxtaCacheDirectoryPath, ConfigMode mode) throws JxtaException
	{
	    this.myPeerAdv.setName(peerName);
	    this.networkConfigured  = true;
	    /*
	    if (jxtaCacheDirectoryPath == null) {
            jxtaCacheDirectoryPath = new File(new File(".cache"), "ConcertoPeer"
                    + UUID.randomUUID().toString());
        }
        
	    try {
	        manager = new NetworkManager(mode, "ConcertoPeer",
	            jxtaCacheDirectoryPath.toURI());
	    } catch (Exception e) {
	        throw new JxtaException("Failed to initialize peer.\n", e);
	    }
	    
	    // Make sure the jxta platform will shut down tidely when the JVM does.
	    manager.registerShutdownHook();

        // Use JXTA default relay/rendezvous servers for now.
        // manager.setUseDefaultSeeds(true);
        //manager.getConfigurator().addSeedRelay(URI.create("tcp://192.18.37.39:9701"));
        //manager.getConfigurator().addSeedRendezvous(URI.create("tcp://192.18.37.39:9701"));
        
        // FIXME: Leave such configurations to be made from outside
        // after calling this method but before calling startNetworkAndConnect.
        //NetworkConfigurator config = manager.getConfigurator();
        //manager.getConfigurator().setUseMulticast(false);
        
        logger.info("Infrastructure ID: " + manager.getInfrastructureID());
        logger.info("Peer ID: " + manager.getPeerID());*/
	    logger.info("Network Configured.");
        
	}
    
    
    /** {@inheritDoc} */
    public NetworkManager getManager()
    {
        return null;//this.manager;
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
		this.connectedToNetwork = true;

		// Get the NetPeerGroup and use this for now.
		this.rootGroup = new SimPeerGroup(this.rootGroupAdv, this.myPeerAdv);
		
		this.currentJoinedGroup = this.rootGroup;

		// Contribute to the network's connectivity.
		//this.rootGroup.getRendezVousService().setAutoStart(true);

		// Simulate delay cause by connect to the Network entry-point (Rendezvous).
		// FIXME: do something like waitForRdvConnection for groups.
		/*try {
            Thread.sleep(3000);
        } catch (InterruptedException ignore) {
        }*/
		
		// Save the listeners.
		this.jxtaCastListener = jxtaCastListener;
		this.directMessageReceiver = directMessageReceiver;
	}
	
	
	/** {@inheritDoc} **/
	public void stopNetwork() {
	    this.logger.info("Stopping network.");
	    
		if (this.isConnectedToNetwork()) {
		    //PeerGroup currentGroup = this.currentJoinedGroup;
		    
			// Try to leave the current group nicely.
			try {
				this.leavePeerGroup(currentJoinedGroup);
			} catch (Exception e) {
				// ignore, we are shutting down anyway.
			}
			
			this.currentJoinedGroup = null;
			
			this.rootGroup = null;

			this.connectedToNetwork = false;
			
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
        return this.getMyPeerAdv().getName();
    }
    
    /** {@inheritDoc} **/
    public void setMyPeerName(String peerName)
    {
       this.myPeerAdv.setName(peerName);
    }
    
    
    /** {@inheritDoc} **/
    public PeerID getMyPeerID() {
        return this.getMyPeerAdv().getPeerID();
    }
    
    
    /** {@inheritDoc} **/
    public PeerAdvertisement getMyPeerAdv() {
        return this.myPeerAdv;
    }

    
    /** {@inheritDoc} */
    public String getMyDirectCommunicationPipeName() {

        // Use a complex delimiter to mark off the peer name and ID.
        // We need to parse this string later, so we need something that's
        // unlikely to appear in a peer name.  (A simple period is too risky.)
        //
        String name = getDirectCommunicationPipeNamePrefix() + JxtaCast.DELIM +
                      this.getMyPeerName()            + JxtaCast.DELIM +
                      this.getMyPeerID().toString();

        return name;
    }
    

    /** {@inheritDoc} */
    public String getDirectCommunicationPipeNamePrefix() {

        return "ConcertoPeerDirectCommunication." + this.getClass().getSimpleName();
    }
    
    
    /** {@inheritDoc} */
    public PipeAdvertisement getMyDirectCommunicationPipeAdvertisement() {
        if (this.isConnectedToGroup() && directMessageReceiver != null) {
            return this.myPipeAdv;
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

        // The peer ID is located after the second delimiter.
        int pos = pipeName.indexOf(JxtaCast.DELIM);
        if (pos < 0)
            return null;
        pos = pipeName.indexOf(JxtaCast.DELIM, ++pos);
        if (pos < 0)
            return null;

        return pipeName.substring(pos + JxtaCast.DELIM.length());
    }
    
    
    /** {@inheritDoc} **/
    public JxtaCast getJxtaCastInstance() {
        return null;
    }
    

    /** {@inheritDoc} **/
    public void discoverGroups(String targetPeerId, DiscoveryListener discoListener) {
        localGroupAdvRegistry.addAll(MockJxtaPeer.groupAdvRegistry);
    }


    /** {@inheritDoc} **/
    public void discoverPeers(String targetPeerId,
                              DiscoveryListener discoListener) {
        Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(this.currentJoinedGroup.getPeerGroupAdvertisement());
        if (peersInGroup != null) {
            for (MockJxtaPeer peer : peersInGroup) {
                localPeerAdvRegistry.add(peer.getMyPeerAdv());
            }
        }
    }


    /** {@inheritDoc} **/
    public void discoverAdvertisements(String targetPeerId,
                                       //PeerGroupAdvertisement group,
                                       DiscoveryListener discoListener,
                                       String attribute,
                                       String value) {
        // refresh only pipe advs. That is what we will be needing most from this method.
        Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(this.currentJoinedGroup.getPeerGroupAdvertisement());
        if (peersInGroup != null) {
            for (MockJxtaPeer peer : peersInGroup) {
                localPipeAdvRegistry.add(peer.getMyDirectCommunicationPipeAdvertisement());
            }
        }
    }


    /** {@inheritDoc} **/
	public Enumeration<PeerGroupAdvertisement> getKnownGroups() {

		if (!this.isConnectedToNetwork()) {
    		logger.warn("Not connected to network.");
    		// return empty enumeration.
            return Collections.enumeration(new ArrayList<PeerGroupAdvertisement>());
    	}
    	
		discoverGroups(null, null);
		
		return Collections.enumeration(this.localGroupAdvRegistry);
    }
    

    /** {@inheritDoc} **/
	public Enumeration<PeerAdvertisement> getKnownPeers() {
    	
    	if (!this.isConnectedToNetwork()) {
    	    logger.warn("Not connected to network.");
    	    // return empty enumeration.
            return Collections.enumeration(new ArrayList<PeerAdvertisement>());
    	}
    	
        discoverPeers(null, null);
        
        return Collections.enumeration(this.localPeerAdvRegistry);
    }


    /** {@inheritDoc} **/
    public Enumeration<Advertisement> getKnownAdvertisements(String attribute,
                                              String value) {
    	
        Enumeration<Advertisement> result = null;
    	if (!this.isConnectedToNetwork()) {
    	    logger.warn("Not connected to network.");
    	    // return empty enumeration.
    		result = Collections.enumeration(new ArrayList<Advertisement>());
    	}
        
    	result = Collections.enumeration(new HashSet<Advertisement>(this.localPipeAdvRegistry));
    	
        discoverAdvertisements(null, null, attribute, value);

        return result;
    }
    
    /** {@inheritDoc} **/
    public Enumeration<Advertisement> getKnownDirectCommunicationPipeAdvertisements()
    {
        ArrayList<Advertisement> pipeAdvs = new ArrayList<Advertisement>();
        
        Enumeration<Advertisement> en = this.getKnownAdvertisements(PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");
        
        while (en.hasMoreElements()) {
            Advertisement adv = en.nextElement();
            // Get only PipeAdvertisements that are different from this peer's.
            if (adv instanceof PipeAdvertisement) {
                PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;
                if (!pipeAdv.equals(this.getMyDirectCommunicationPipeAdvertisement())) {
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
    	
    	return null;//FIXME: this.rootGroup.getRendezVousService().getConnectedRendezVous();
    }


    /** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
    public PeerGroup createNewGroup(String groupName, String description, char[] keystorePassword, char[] groupPassword) throws Exception {
        
        PeerGroup pg;               // new peer group
        
        PeerGroupAdvertisement newGroupAdv  = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
            PeerGroupAdvertisement.getAdvertisementType());
        newGroupAdv.setName(groupName);
        newGroupAdv.setDescription(description);
        newGroupAdv.setPeerGroupID(new PeerGroupID());
    
        // If a keystorePassword and groupPassword has been specified, set the membershipService to PSEMembershipService.
        if (keystorePassword != null && keystorePassword.length != 0 && groupPassword != null && groupPassword.length != 0) {
            
            StructuredDocument groupPasswordParam = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, "Parm");
            Element grouPasswordElement = groupPasswordParam.createElement("groupPassword", String.valueOf(groupPassword));
            groupPasswordParam.appendChild(grouPasswordElement);
            
            newGroupAdv.putServiceParam(MockJxtaPeer.groupAuthenticationServiceParamID, groupPasswordParam);
        }
        
        // publish
        localGroupAdvRegistry.add(newGroupAdv);
        MockJxtaPeer.groupAdvRegistry.add(newGroupAdv);
        
        pg = new SimPeerGroup(newGroupAdv, this.getMyPeerAdv());
 
        // We join the new group as well.
        if (!authenticateMembership(pg, keystorePassword, groupPassword)) {
            throw new Exception("Authentication failed for the new group!");
        }

        // Become rdv for this new group. Peers will not be able to communicate if there is no rdv in this group.
        this.setRendezVousForGroup(true);
        
        // If we were previously a member of a group, we have to leave it now.
        this.leavePeerGroup(this.currentJoinedGroup);
        
        // Set the new group as the current joined group.
        this.currentJoinedGroup = pg;
        
        // Init direct communication for this group and register the listener.
        this.createDirectCommunicationServerSocket();
        
        // Init static registry for this group.
        Set<MockJxtaPeer> peersInGroup = new HashSet<MockJxtaPeer>();
        peersInGroup.add(this);
        MockJxtaPeer.groupsWithPeersMap.put(newGroupAdv, peersInGroup);

        return pg;
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
        PeerGroup newGroup = new SimPeerGroup(groupAdv, this.getMyPeerAdv());
    	
        if (!authenticateMembership(newGroup, keystorePassword, groupPassword)) {
        	throw new PeerGroupException("Authentication failed for joining the group.");
        }
        
        if (beRendezvous) {
            this.setRendezVousForGroup(true);
        } else {
            // Wait for a connection to a RDV of this group.
            if (!waitForRendezVousConnection(newGroup, 60000)) {
                // If none found, make this peer a RDV for the group in order to enable immediate communication.
                this.logger.debug("Could not connect to any RDV peer in this group. Promoting this peer to RDV.");
                this.setRendezVousForGroup(true);
            } else {
                this.logger.debug("RDV connection established for this group.");
            }
        }
        
        // Leave the old peer group.
        this.leavePeerGroup(currentJoinedGroup);
        
        // Set this group as the current one
        currentJoinedGroup = newGroup;
        
        // Init direct communication for this group and register the listener.
        this.createDirectCommunicationServerSocket();
        
        // Update static registry for this group.
        Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(groupAdv);
        peersInGroup.add(this);
        
        // Update local cache with peers and their private pipe advertisements from this group.
        discoverPeers(null, null);
        discoverAdvertisements(null, null, PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");

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
        
        if (group.equals(this.rootGroup)) {
            this.logger.debug("Connected to network rdv in mock mode.");
            return true;
        }
        
        long stopNow = System.currentTimeMillis();
        if (delay == 0) {
            stopNow = Long.MAX_VALUE;
        } else {
            stopNow += delay;
        }
        
        // TODO: Vulnerable to time changes to the past? quite unlikely.
        while (System.currentTimeMillis() < stopNow) {
            
            if (this.isRendezVousForGroup() || findRdvPeerInGroup(group.getPeerGroupAdvertisement())) {
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
    
    public boolean findRdvPeerInGroup(PeerGroupAdvertisement groupAdv)
    {
        Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(groupAdv);
        if (peersInGroup == null) {
            return false;
        }
        
        for(MockJxtaPeer peer : peersInGroup) {
            if (peer.isRendezVousForGroup()) {
                return true;
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
    
    protected void createDirectCommunicationServerSocket() throws IOException
    {
        this.logger.debug("Creating direct communication server socket.");
        
        if (!this.hasJoinedAGroup()) {
            this.logger.warn("Not connected to any group. Aborting.");
            return;
        }
        
        this.closeExistingDirectCommunicationServerSocket();
        
        this.myPipeAdv = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
            PipeAdvertisement.getAdvertisementType());

        PipeID id = (PipeID)IDFactory.newPipeID(currentJoinedGroup.getPeerGroupID());
        myPipeAdv.setPipeID(id);
        myPipeAdv.setName(getMyDirectCommunicationPipeName());
        myPipeAdv.setType(PipeService.UnicastType);

        this.logger.debug("Publishing pipe advertisement.");
        
        // If no listener registered, there is no point in starting a server socket and a connection handler thread.
        if (this.directMessageReceiver != null) {        
            // nothing
        } else {
            this.logger.warn("There is no listener registered. Direct communication server socket will not be created.");
        }
    }
    
    protected void closeExistingDirectCommunicationServerSocket() throws IOException
    {
        this.logger.debug("Closing existing direct communication server socket.");
        this.myPipeAdv = null;
    }
    
    // One thread per connection.
    class ConnectionThread extends Thread
    {
        Socket socket;
        DirectMessageReceiver receiver;
        
        ConnectionThread(Socket socket, DirectMessageReceiver receiver)
        {
            this.socket = socket;
            this.receiver = receiver;
        }
        
        /** {@inheritDoc} **/
        @Override
        public void run()
        {
            InputStream is = null;
            OutputStream os = null;
            ObjectInputStream ois = null;
            ObjectOutputStream oos = null;
            try {
                is = this.socket.getInputStream();
                os = this.socket.getOutputStream();
                ois = new ObjectInputStream(is);
                oos = new ObjectOutputStream(os);
                
                Object message = ois.readObject();
                receiver.receiveDirectMessage(message, oos);
                
                // Make sure to flush in case receiver did not.
                oos.flush();
                os.flush();
            } catch (Exception e) {
                this.receiver.getLog().error("Failed to receive message or to send reply.", e);
            } finally {
                try{
                    if (ois != null) {
                        ois.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                    
                    if (oos != null) {
                        oos.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    
                    if (this.socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                    // Just log it.
                    this.receiver.getLog().warn("Failed to close streams for this conenction.");
                }
            }
            
            // die.
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

        // clean local cache for current group (a joined group or even npg.)
        this.localPeerAdvRegistry.clear();
        this.localPipeAdvRegistry.clear();
        
        // See if it's the default group. Don`t think you can leave that.
        if (oldGroup.getPeerGroupID().equals(this.rootGroup.getPeerGroupID())) {
            this.logger.warn("Asked to leave the default NetPeerGroup. Ignoring request.");           
        	return;
        }
        
        // Stop being rdv for the gorup.
        this.setRendezVousForGroup(false);
        
        // Resign from the group.
        MockJxtaPeer.groupsWithPeersMap.get(oldGroup.getPeerGroupAdvertisement()).remove(this);
        
    	// See if it was the current joined group.
        if (this.currentJoinedGroup != null && oldGroup.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
        	this.currentJoinedGroup = this.rootGroup;
        }
        
        oldGroup = null;
        
        try {
            this.closeExistingDirectCommunicationServerSocket();
        } catch (IOException e) {
            // This will never happen in the current implementation but it's best to be sure.
            // Just log it.
            this.logger.warn("Failed to close existing direct communciation server socket after leaving a group.", e);
        }
    }
    
    
    /** {@inheritDoc} **/
    public void leavePeerGroup() throws PeerGroupException {
        leavePeerGroup(this.currentJoinedGroup);
    }
    

    /**
     * Authenticate membership in a peer group using {@link PSEMembershipService}'s \"StringAuthentication\" method.
     * </p>
     * If both passwords are not provided, the authentication is made using {@link NoneMembershipService} and no authentication data is provided.
     * 
     * @param keystorePassword the password of the local keystore.
     * @param identityPassword the group's password.
     * 
     * @return true if successful, false if the provided passwords were not correct or joining failed.
     * @throws PeerGroupException if problems occurred joining the group.
     * @throws ProtocolNotSupportedException if problems occur authenticating credentials.
     */
    @SuppressWarnings("unchecked")
    protected boolean authenticateMembership(PeerGroup group, char[] keystorePassword, char[] identityPassword) throws PeerGroupException, ProtocolNotSupportedException {
    	// FIXME: make authentication based on the actual membershipService of the group, not by the provided passwords.
    	
        String authenticationMethod = null;
        
        if (keystorePassword != null && identityPassword != null) {
            authenticationMethod = "StringAuthentication";
        }
        
        // private group authentication.
        if (authenticationMethod != null) {
            // keystore
            if (this.localKeystorePassword == null) {
                // set a keystore pass
                this.localKeystorePassword = keystorePassword;
            } else {
                // see if the keystore pass is good.
                if (!this.localKeystorePassword.equals(keystorePassword)) {
                    this.logger.error("Can't join the group yet. Keystore password incorrect.");
                    return false;
                }
            }
            
            // group password.
            PeerGroupAdvertisement groupAdv = group.getPeerGroupAdvertisement();
            
            StructuredDocument groupPasswordParam = groupAdv.getServiceParam(MockJxtaPeer.groupAuthenticationServiceParamID);
            Enumeration en = groupPasswordParam.getChildren("groupPassword");
            
            List elements = Collections.list(en);
            if (elements != null && elements.size() != 0) {
                Element groupPasswordElement = (Element) elements.get(0);
                char[] goodGroupPassword = ((String) groupPasswordElement.getValue()).toCharArray();
                if (!Arrays.equals(goodGroupPassword, identityPassword)) {
                    this.logger.error("Can't join the group yet. Group password incorrect.");
                    return false;
                }
                
            } else {
                this.logger.debug("No group password found in the group adv.");
            }
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
		/*if (!this.isConnectedToGroup()) {
			throw new IllegalStateException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.addJxtaCastEventListener(listener);*/
	    this.logger.warn("addJxtaCastEventListener not implemented!");
	}
	
	/** {@inheritDoc} **/
	public void removeJxtaCastEventListener(JxtaCastEventListener listener) {
		/*if (!this.isConnectedToGroup()) {
			throw new IllegalStateException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.removeJxtaCastEventListener(listener);*/
	    this.logger.warn("removeJxtaCastEventListener not implemented!");
	}

	/** {@inheritDoc} **/
	public void sendChatMsg(String text) throws PeerGroupException {
		/*if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendChatMsg(text);*/
	    this.logger.warn("sendChatMsg not implemented!");
	}

	/** {@inheritDoc} **/
	public void sendFile(File file, String caption) throws PeerGroupException {
		/*if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendFile(file, caption);*/
		this.logger.warn("sendFile not implemented!");
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
		
        Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(this.currentJoinedGroup.getPeerGroupAdvertisement());
        for (MockJxtaPeer peer : peersInGroup) {
            // send to all but this peer.
            if (!peer.equals(this)) {
                JxtaCastEvent broadcastObjectEvent = new JxtaCastEvent();
                broadcastObjectEvent.percentDone = 100;
                broadcastObjectEvent.transType = JxtaCastEvent.RECV;
                broadcastObjectEvent.transferedData = object;
                broadcastObjectEvent.caption = caption;
                broadcastObjectEvent.sender = this.getMyPeerName();
                broadcastObjectEvent.senderId = this.getMyPeerAdv().getPeerID().toString();
                
                peer.jxtaCastListener.jxtaCastProgress(broadcastObjectEvent);
            }
        }
	}
	
	/** {@inheritDoc} **/
	public Object sendObject(Object object, PipeAdvertisement pipeAdv) throws JxtaException {
	    if (!this.isConnectedToGroup()) {
            throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
        }
	    
	    if (!(object instanceof Serializable)) {
	        throw new IllegalArgumentException("The object does not implement the interface java.io.Serializable and can not be sent.");
	    }
	 
	    MockJxtaPeer destinationPeer = null;
	    Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(this.currentJoinedGroup.getPeerGroupAdvertisement());
	    for (MockJxtaPeer peer : peersInGroup) {
	        if (!peer.equals(this) &&
	            peer.getMyDirectCommunicationPipeAdvertisement().getPipeID().equals(pipeAdv.getPipeID())) {
	            destinationPeer = peer;
	            break;
	        }
	    }
	    
	    if (destinationPeer == null) {
	        throw new JxtaException("Failed to locate destination peer.");
	    }
	    
	    ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            
            destinationPeer.directMessageReceiver.receiveDirectMessage(object, oos);
        } catch (Exception e) {
            throw new JxtaException("Failed to send an object through a direct connection using the provided pipe advertisement.", e);
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                this.logger.warn("Failed to close streams for this conenction.");
            }
            
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                this.logger.warn("Failed to close streams for this conenction.");
            }
        }
        
        Object replyMessage = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);
            
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
                if (bais != null) {
                    bais.close();
                }
            } catch (Exception e) {
                // Just log it.
                this.logger.warn("Failed to close streams for this conenction.");
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
        
        this.logger.info("Trying to send an object to a random peer in the group.");
        
        Object reply = null;
        boolean success = false;
        
        for(int i=0; i<NUMBER_OF_TRIES && !success; i++) {
            this.logger.info("Try number: " + i);
            
            // simulate discovery process.
            List<Advertisement> knownPipeAdvs = Collections.list(this.getKnownDirectCommunicationPipeAdvertisements());
            
            Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(this.currentJoinedGroup.getPeerGroupAdvertisement());
            for (MockJxtaPeer peer : peersInGroup) {
                // if we discovered this peer and it is not our peer then try to send the message.
                if (!peer.equals(this) && knownPipeAdvs.contains(peer.getMyDirectCommunicationPipeAdvertisement())) {
                    PipeAdvertisement pipeAdv = peer.getMyDirectCommunicationPipeAdvertisement();
                    
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
                    break;
                }
            }
            
            // If success, don`t go to sleep anymore.
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
	public void rendezvousEvent(RendezvousEvent event) {
	    // If we've connected to a new RDV or just disconnected from one.  Launch discovery,
        // so we can see any ADVs (peers, groups, peer back-channel ADVs.) this RDV knows or used to know.
	    
		if (event.getType() == RendezvousEvent.RDVCONNECT    ||
        event.getType() == RendezvousEvent.RDVRECONNECT  ||
        event.getType() == RendezvousEvent.RDVDISCONNECT ||
        event.getType() == RendezvousEvent.RDVFAILED ||
        event.getType() == RendezvousEvent.BECAMERDV) {
		    
		    this.discoverGroups(event.getPeer(), null);
            this.discoverPeers(event.getPeer(), null);
            this.discoverAdvertisements(event.getPeer(), null, PipeAdvertisement.NameTag, this.getDirectCommunicationPipeNamePrefix() + "*");
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
		return this.connectedToNetwork; 
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToGroup() {
	    if (!hasJoinedAGroup()) {
	        return false;
	    }
	    
	    Set<MockJxtaPeer> peersInGroup = MockJxtaPeer.groupsWithPeersMap.get(this.getCurrentJoinedPeerGroup().getPeerGroupAdvertisement());
	    boolean knowsOtherPeers = peersInGroup != null && peersInGroup.size() > 1;
	    
		return (this.isGroupRendezVous() && knowsOtherPeers) || this.isConnectedToGroupRendezVous(); 
	}
	
	/** {@inheritDoc} **/
	public boolean isNetworkRendezVous() {
	    // FIXME: remove;
		return this.isJxtaStarted();
	}
	
	/** {@inheritDoc} **/
	public boolean isGroupRendezVous() {
		return this.hasJoinedAGroup() && this.isRendezVousForGroup();
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToNetworkRendezVous() {
		return this.isJxtaStarted() && true;//FIXME: this.rootGroup.getRendezVousService().isConnectedToRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToGroupRendezVous() {
		return this.hasJoinedAGroup() && findRdvPeerInGroup(this.currentJoinedGroup.getPeerGroupAdvertisement());
	}

    /**
     * @return the rendezVousForGroup
     */
    public boolean isRendezVousForGroup()
    {
        return this.rendezVousForGroup;
    }

    /**
     * @param rendezVousForGroup the rendezVousForGroup to set
     */
    public void setRendezVousForGroup(boolean rendezVousForGroup)
    {
        this.rendezVousForGroup = rendezVousForGroup;
    }

    /** {@inheritDoc} **/
    public boolean isNetworkConfigured() {
        return this.networkConfigured;
    }
    
    /**
     * @param networkConfigured the networkConfigured to set
     */
    public void setNetworkConfigured(boolean networkConfigured)
    {
        this.networkConfigured = networkConfigured;
    }

    /** {@inheritDoc} **/
    public void discoverDirectCommunicationPipeAdvertisements()
    {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} **/
    public void republishDirectCommunicationPipeAdvertisement()
    {
        // TODO Auto-generated method stub
        
    }

}
