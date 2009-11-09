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
package org.xwoot.xwootApp;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.jxta.protocol.PeerGroupAdvertisement;

import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.jxta.Peer;

public interface XWootAPI
{

    // TODO : Verifier le rechargement des donnees (fichiers) en cas d'arret de l'application
    // (rechargement de l'instance)

    // State management

    /**
     * To create/update model. The given file is the woot storage computed by the network creator (computed with
     * initialiseWootStorage and exported with exportWootStorage()). All data are uncompressed in the model directory.
     * No need to be connected with P2P/contentManager. You have to call synchronize method to update xwiki.
     * 
     * @param wst : a zip file (must be computed by an XWoot neighbor)
     * @return true if import success
     */
    boolean importState(File wst) throws XWootException;

    public File computeState() throws XWootException;

    /**
     * To get woot storage. No need to be connected with P2P/contentManager.
     * 
     * @return : the woot storage : a zip file.
     */
    File getState();

    boolean isStateComputed();

    /**
     * Asks the state from the current group and stores it locally in the Operating System's temporary directory.
     * 
     * @return a file object pointing to the location on disk where the state file was saved.
     * @throws XWootException if failed to contact a member of the group to get the state from or if getting the state
     *             failed for some other reason.
     */
    File askStateToGroup() throws XWootException;

    /** @return the name of the state file for the current group. */
    String getStateFileName();

    void doAntiEntropy(Object neighbor) throws XWootException;

    void doAntiEntropyWithAllNeighbors() throws XWootException;

    // P2P Network Data

    Peer getPeer();

    boolean joinNetwork(String neighborURL) throws XWootException;

    boolean createNetwork() throws XWootException;

    void reconnectToP2PNetwork() throws XWootException;

    void disconnectFromP2PNetwork() throws XWootException;

    boolean isConnectedToP2PNetwork();

    boolean isConnectedToP2PGroup();

    // P2P Group Data

    /**
     * @return a list of {@link PeerGroupAdvertisement} instances representing the known groups inside the joined P2P
     *         network. An empty collection will be returned if not connected to network.
     * @throws XWootException
     */
    @SuppressWarnings("unchecked")
    public Collection getGroups();
    
    /** @return true if this node has joined a group. */
    public boolean hasJoinedAP2PGroup();
    
    /**
     * Create a new group in the current P2P network.
     * <p/>
     * In order to create a public group, keystorePassword and/or groupPassword need(s) to be null or empty.
     * <p/>
     * To create a password protected group, they need to be both need to not be empty or null.
     * 
     * @param name the name of the group.
     * @param description short description of the group.
     * @param keystorePassword the local keystore's password.
     * @param groupPassword the group's password.
     * @return the jxta advertisement of the new group.
     * @throws XWootException if creating the group failed.
     */
    public PeerGroupAdvertisement createNewGroup(String name, String description, char[] keystorePassword,
        char[] groupPassword) throws XWootException;

    /** @return true if this peer created the group he currently is member of; false otherwise. */
    boolean isGroupCreator();

    /**
     * Join a group of of the P2P network.
     * 
     * @param groupAdvertisement the jxta group advertisement of the group to join
     * @param keystorePassword the password for the local keystore. This will only be used if the group is password
     *            protected.
     * @param groupPassword the group's password. This will only be used if the group is password protected.
     * @param beRendezVous whether to explicitly be a RDV for the group after joining.
     * @throws XWootException if joining the group failed.
     */
    public void joinGroup(PeerGroupAdvertisement groupAdvertisement, char[] keystorePassword, char[] groupPassword,
        boolean beRendezVous) throws XWootException;

    /**
     * Convenience method to join a P2P group.
     * <p/>
     * Equivalent to {@code joinGroup(advertisement, keystorePassword, groupPassword, false)}.
     * 
     * @param groupAdvertisement the jxta group advertisement of the group to join
     * @param keystorePassword the password for the local keystore. This will only be used if the group is password
     *            protected.
     * @param groupPassword the group's password. This will only be used if the group is password protected.
     * @throws XWootException if joining the group failed.
     * @see {@link #joinGroup(PeerGroupAdvertisement, char[], char[], boolean)}
     */
    public void joinGroup(PeerGroupAdvertisement groupAdvertisement, char[] keystorePassword, char[] groupPassword)
        throws XWootException;

    /**
     * @return a collection of {@link PipeAdvertisement} instances describing the members of the current group and how
     *         to contact them. If not connected to a group or getting the data failed, an empty collection will be
     *         returned.
     */
    @SuppressWarnings("unchecked")
    Collection getNeighborsList();

    Object receiveMessage(Object message) throws XWootException;

    // ContentProvider

    XWootContentProviderInterface getContentProvider();

    void connectToContentManager() throws XWootException;

    void disconnectFromContentManager() throws XWootException;

    boolean isContentManagerConnected();

    void synchronize() throws XWootException;

    void synchronize(boolean generatePatches) throws XWootException;

    List<String> getLastPages(String id) throws XWootException;

    Date getLastSynchronizationDate();

    String getLastSynchronizationFailure();

    /** @return the unique ID of this XWoot. */
    String getXWootPeerId();

    String getContentManagerURL();

    /** @return the workingDir */
    public String getWorkingDir();

}
