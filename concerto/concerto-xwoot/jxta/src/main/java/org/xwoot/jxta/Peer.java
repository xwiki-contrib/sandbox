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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

import org.xwoot.jxta.NetworkManager.ConfigMode;

import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.exception.JxtaException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;

/**
 * Peer: Peer-to-Peer protocol interface. Provides a generic interface to a set of (very JXTA-like) p2p protocols.
 * Implementations are used to transparently access either JXTA itself or a simulation of JXTA.
 * 
 * @version $Id$
 */
public interface Peer
{
    /** The default name for a peer. */
    final String DEFAULT_PEER_NAME = "ConcertoPeer";

    /** The default jxta related cache directory name. */
    final String DEFAULT_DIR_NAME = ".cache";

    /**
     * Configure the network.
     * 
     * @param peerName the name of this peer. If none provided, the default will be used. Default is "ConcertoPeer"
     * @param jxtaCacheDirectoryPath The location on drive where to save the directory containing jxta related
     *            information for this peer. The jxta directory will have the name of the peer or the default name if
     *            none provided. If this parameter is null, the default value is a subdir in the current directory by
     *            the name of \".cache\"
     * @param mode The mode in which this peer will be running.
     * @see ConfigMode
     * @throws IOException if problems occur while initializing.
     **/
    void configureNetwork(String peerName, File jxtaCacheDirectoryPath, ConfigMode mode) throws JxtaException;

    /**
     * @return the NetworkManager instance created by {@link #configureNetwork(File)} that allows tweaking the peer's
     *         configuration.
     **/
    NetworkManager getManager();

    /**
     * Start the network, connect to a RDV and set some parameters.
     * 
     * @param jxtaCastListener event listener interested in {@link JxtaCastEvent}s.
     * @param directMessageReceiver event listener interested in Objects directly sent to this peer only.
     * @throws JxtaException if could not contact any RDV peers to join the network.
     * @throws IllegalStateException if this is called before {@link #configureNetwork(File)}.
     * @throws PeerGroupException if problems occur while starting the Jxta platform.
     * @throws IOException if problems occur while starting the Jxta platform.
     **/
    void startNetworkAndConnect(JxtaCastEventListener jxtaCastListener, DirectMessageReceiver directMessageReceiver)
        throws IllegalStateException, PeerGroupException, IOException, JxtaException;

    /**
     * Stops the JXTA platform and disconnects from the network.
     */
    void stopNetwork();

    /** @return my own peer name. */
    String getMyPeerName();

    /** @param peerName the name to set for this peer. */
    void setMyPeerName(String peerName);

    /** @return my own peer ID. */
    PeerID getMyPeerID();

    /** @return advertisement for the default (initial) peer group. */
    PeerGroupAdvertisement getDefaultAdv();

    /** @return advertisement for my peer. */
    PeerAdvertisement getMyPeerAdv();

    /** @return the pipe name prefix used for the direct communication server socket. */
    String getDirectCommunicationPipeNamePrefix();

    /**
     * @return the pipe advertisement used by this peer for the direct communication server socket. {@code null} is
     *         returned if the peer has not yet joined a group or if he did but does not have a direct communication
     *         listener registered.
     */
    PipeAdvertisement getMyDirectCommunicationPipeAdvertisement();

    /** @return the String value of this peer's Pipe ID used for direct communication. */
    public String getMyDirectCommunicationPipeIDAsString();

    /** @return the pipe name used by this peer for the direct communication server socket. */
    String getMyDirectCommunicationPipeName();

    /** @return the {@link JxtaCast} instance associated with this peer. */
    JxtaCast getJxtaCastInstance();

    /** @return the default (initial) peer group. */
    PeerGroup getDefaultGroup();

    /**
     * Launch a peer group discovery.
     * <p>
     * If this peer did not connect to the network, nothing will happen.
     * 
     * @param targetPeerId - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events. May be null, if you don't want the notification.
     * @see #isConnectedToNetwork()
     */
    void discoverGroups(String targetPeerId, DiscoveryListener discoListener);

    /**
     * Launch peer discovery, for the joined group.
     * <p>
     * If this peer did not join a group and did not contact(or is not) a group RDV, nothing will happen.
     * 
     * @param targetPeerId - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events. May be null, if you don't want the notification.
     * @see #isConnectedToGroup()
     */
    void discoverPeers(String targetPeerId, DiscoveryListener discoListener);

    /**
     * Launch advertisement discovery, for the currently joined group.
     * <p>
     * If this peer did not join a group and did not contact(or is not) a group RDV, nothing will happen.
     * 
     * @param targetPeerId - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events. May be null, if you don't want the notification.
     * @param attribute - Limit responses to advertisements with this attribute/value pair. Set to null to place no
     *            limit.
     * @param value - See 'attribute', above.
     * @see #isConnectedToGroup()
     */
    void discoverAdvertisements(String targetPeerId, DiscoveryListener discoListener, String attribute, String value);
    
    /**
     * Convenience method to discover pipe advertisements used for direct communication.
     * <p/>
     * Has the same effect as {@code discoverAdvertisements(null, null, PipeAdvertisement.NameTag, getDirectCommunicationPipeNamePrefix() + "*");}
     */
    void discoverDirectCommunicationPipeAdvertisements();

    /**
     * @return PeerGroupAdvertisement objects representing the groups known so far or an empty enumeration if this peer
     *         is not connected to the network. <b>Note:</b> this doesn't include the default "NetPeerGroup"
     *         advertisement.
     * @see #isConnectedToNetwork()
     */
    Enumeration<PeerGroupAdvertisement> getKnownGroups();

    /**
     * @return an enumerator to an array of PeerAdvertisement objects representing the peers known so far for the
     *         currently joined group or an empty enumeration if this peer did not join a group or has not contacted a
     *         RDV peer.
     * @see #isConnectedToNetwork()
     */
    Enumeration<PeerAdvertisement> getKnownPeers();

    /**
     * @return Advertisement objects representing the advs known so far, that were created within the joined peer group
     *         or an empty enumeration if this peer has not joined a group. The list can be narrowed to advs matching an
     *         attribute/value pair.
     * @param attribute - Limit responses to advertisements with this attribute/value pair. Set to null to place no
     *            limit.
     * @param value - See 'attribute', above.
     * @see #isConnectedToGroup()
     */
    Enumeration<Advertisement> getKnownAdvertisements(String attribute, String value);

    /**
     * Convenience method for returning just direct communication pipe advertisements in the current group.
     * <p>
     * It can be used to see and communicate with other peers in the current group.
     * 
     * @return {@link PipeAdvertisement}s representing group members' direct communication channels excluding this
     *         peer's pipe advertisement..
     */
    Enumeration<Advertisement> getKnownDirectCommunicationPipeAdvertisements();

    /**
     * @return an enumeration of connected RDV ids or null if this peer is not connected to a group.
     * @see #isConnectedToGroup()
     */
    Enumeration<ID> getConnectedRdvsIDs();

    /**
     * Create and join a new PeerGroup. Also publishes the group advertisement.
     * <p>
     * This peer will automatically become a RDV for this group in order to enable communication in the new group.
     * <p>
     * If keystorePassword and identityPassword parameters are not null or empty, the new group will be a secure group
     * using {@link net.jxta.impl.membership.pse.PSEMembershipService PSEMembershipService}.
     * 
     * @param groupName Name for the new group.
     * @param description Group description.
     * @param keystorePassword the password of the local keystore.
     * @param groupPassword the group's password.
     * @return The new peer group if successful, otherwise null.
     * @throws Exception if problems occur.
     */
    PeerGroup createNewGroup(String groupName, String description, char[] keystorePassword, char[] groupPassword)
        throws Exception;

    /**
     * Convenience method for creating a public peer group.
     * 
     * @see #createNewGroup(String, String, char[], char[])
     * @param groupName Name for the new group.
     * @param description Group description.
     * @return The new peer group if successful, otherwise null.
     * @throws Exception if problems occur.
     */
    PeerGroup createNewGroup(String groupName, String description) throws Exception;

    /**
     * Join the specified PeerGroup.
     * <p>
     * Upon join, the peer will automatically become a RDV for this group in order to enable communication. If {@code
     * beRendezvous} was not set to true, the peer will be demoted back to a normal Edge peer when the network can
     * support this but will also be promoted back to RDV when the network will need it.
     * <p>
     * If keystorePassword and identityPassword parameters are not null or empty, the new group will be a secure group
     * using {@link NetworkManager.. {@link net.jxta.impl.membership.pse.PSEMembershipService PSEMPSEMembershipService}
     * , else it will be a public group and use {@link net.jxta.impl.membership.none.NoneMembershipService
     * NoneMembershipService}.
     * 
     * @param groupAdv Advertisement of the group to join.
     * @param keystorePassword The local keystore password.
     * @param groupPassword The group's password required for joining.
     * @param beRendezvous If true, act as a rendezvous for this group, else the peer will be automatically promoted to
     *            a RDV or demoted back to an EDGE peer when needed.
     * @return PeerGroup if we were successfully able to join the group, or if we had already joined it. null if we were
     *         unable to join the group.
     * @throws PeerGroupException if the group could not be joined.
     * @throws IOException if problems occur publishing the group.
     * @throws ProtocolNotSupportedException if problems occur while authenticating.
     */
    PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, char[] keystorePassword, char[] groupPassword,
        boolean beRendezvous) throws PeerGroupException, IOException, ProtocolNotSupportedException;

    /**
     * Convenience method for joining a public group.
     * 
     * @see #joinPeerGroup(PeerGroupAdvertisement, char[], char[], boolean)
     * @param groupAdv Advertisement of the group to join.
     * @param beRendezvous If true, act as a rendezvous for this group, else the peer will be automatically promoted to
     *            a RDV when needed.
     * @return PeerGroup if we were successfully able to join the group, or if we had already joined it. null if we were
     *         unable to join the group.
     * @throws PeerGroupException if the group could not be joined.
     * @throws IOException if problems occur publishing the group.
     * @throws ProtocolNotSupportedException if problems occur while authenticating.
     */
    PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, boolean beRendezvous) throws PeerGroupException,
        IOException, ProtocolNotSupportedException;

    /**
     * Leave a peer group.
     * <p>
     * Normally, this would be called after joining a new group to ensure that we are in only one peer group at a one
     * time.
     * <p>
     * If we leave the current peer group, {@link #isConnectedToGroup()} should return false until we join another
     * group.
     * <p>
     * If we try to leave the default peer group, nothing will happen. Also, nothing will happen if we try to leave a
     * group when we are not connected to the network.
     * 
     * @param oldGroup the group to leave.
     * @throws PeerGroupException if problems occur while leaving the group.
     */
    void leavePeerGroup(PeerGroup oldGroup) throws PeerGroupException;

    /**
     * Convenince method to leave the joined peer group.
     * 
     * @see #leavePeerGroup(PeerGroup)
     * @throws PeerGroupException if problems occur while leaving the group.
     */
    void leavePeerGroup() throws PeerGroupException;

    /**
     * Republishes the direct communication pipe advertisement for the currently joined peer group.
     * <p/>
     * This is normally used by the {@link PresenceTask} task that periodically republishes the pipe advertisement.
     * <p/>
     * If no group is joined ({@link #hasJoinedAGroup()} returns false) or if for some reason the server socket is not
     * initialized, a warning will be issued and the method will return.
     */
    void republishDirectCommunicationPipeAdvertisement();

    /**
     * @param peerAdv the peer advertisement of the peer to check.
     * @return true if the peer is a RDV, false otherwise or if it is not from the current joined group.
     */
    boolean isRendezvous(PeerAdvertisement peerAdv);

    /**
     * Register a listener for the messages broadcasted inside the joined group.
     * 
     * @param listener the listener.
     * @see net.jxta.jxtacast.JxtaCast#addJxtaCastEventListener(net.jxta.jxtacast.event.JxtaCastEventListener)
     */
    void addJxtaCastEventListener(JxtaCastEventListener listener);

    /**
     * Remove a listener that will ignore messages broadcasted inside the joined group.
     * 
     * @param listener the listener.
     * @see net.jxta.jxtacast.JxtaCast#removeJxtaCastEventListener(net.jxta.jxtacast.event.JxtaCastEventListener)
     */
    void removeJxtaCastEventListener(JxtaCastEventListener listener);

    /**
     * Unreliably send a message to the whole joined group.
     * 
     * @param text the text message to send.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @see net.jxta.jxtacast.JxtaCast#sendChatMsg(java.lang.String)
     */
    void sendChatMsg(String text) throws PeerGroupException;

    /**
     * Reliably send a variable-sized file to the whole joined group.
     * 
     * @param file the file to send.
     * @param caption the caption describing the file.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @see net.jxta.jxtacast.JxtaCast#sendFile(java.io.File, java.lang.String)
     */
    void sendFile(File file, String caption) throws PeerGroupException;

    /**
     * Reliably send a variable-sized object to the whole joined group.
     * 
     * @param object the object to send.
     * @param caption the caption describing the object.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @throws IllegalArgumentException if the object does not implement {@link Serializable}.
     * @see net.jxta.jxtacast.JxtaCast#sendObject(java.lang.Object, java.lang.String)
     */
    void sendObject(Object object, String caption) throws PeerGroupException;

    /**
     * Send a variable-sized object to a peer.
     * 
     * @param object the object to send.
     * @param pipeAdv the pipe advertisement where the destination peer listens for messages.
     * @return the object replied by the destination peer or null if it did not reply anything.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @throws IllegalArgumentException if the object does not implement {@link Serializable}.
     * @see net.jxta.jxtacast.JxtaCast#sendObject(java.lang.Object, java.lang.String)
     */
    Object sendObject(Object object, PipeAdvertisement pipeAdv) throws JxtaException;

    /**
     * Send a variable-sized object to a random peer in the currently joined group.
     * 
     * @param object the object to send.
     * @param expectReply whether to explicitly expect a reply from a peer.
     * @return the replied object returned from the receiving peer. {@code null} can also be returned, depending on the
     *         context, but if expectReply is {@code true}, {@code null} will never be returned.
     * @throws JxtaException if the message could not be sent after {@link JxtaPeer#NUMBER_OF_TRIES}, either because of
     *             transfer problems or because there was no peer in the group to receive it. If expectReply is true, an
     *             exception will also be thrown if no peer replied to this message.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @throws IllegalArgumentException if the object does not implement {@link Serializable} or is {@code null}.
     * @see net.jxta.jxtacast.JxtaCast#sendObject(java.lang.Object, java.lang.String)
     */
    Object sendObjectToRandomPeerInGroup(Object object, boolean expectReply) throws PeerGroupException,
        IllegalArgumentException, JxtaException;

    /**
     * Handles RendezVous events in the joined group.
     * <p>
     * If it detects a new RDV, it will launch a peer and group discovery to that RDV to get his known peers/groups.
     * 
     * @param event the event generated by a RDV.
     */
    void rendezvousEvent(RendezvousEvent event);

    /**
     * @return the currently joined peer group. <b>Note:</b> This will never return the default "netPeerGroup". To get
     *         that, use {@link #getDefaultGroup()}.
     */
    PeerGroup getCurrentJoinedPeerGroup();

    /** @return true if this peer has started the jxta platform. */
    boolean isJxtaStarted();

    /** @return true if this peer has joined a group, other than the default netPeerGroup. */
    boolean hasJoinedAGroup();

    /** @return true if this peer is connected to the network and can start querying it. */
    boolean isConnectedToNetwork();

    /** @return true if this peer is connected to the joined group and can start querying it. */
    boolean isConnectedToGroup();

    /** @return true if this peer is a RendezVous for the network. */
    boolean isNetworkRendezVous();

    /** @return true if this peer is a RendezVous for the joined group, if it has joined a group. */
    boolean isGroupRendezVous();

    /**
     * @return true if this peer is connected to a RendezVous of the network or is itself a RendezVous for the network.
     **/
    boolean isConnectedToNetworkRendezVous();

    /**
     * @return true if this peer has connected to a group's RendezVous or is itself a RendezVous for the joined group.
     */
    boolean isConnectedToGroupRendezVous();

    /**
     * @return true if the jxta platform has been configured by calling the {@link #configureNetwork(String)} method and
     *         can be started or stopped.
     */
    boolean isNetworkConfigured();
}
