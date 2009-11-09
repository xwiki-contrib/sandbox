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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.jxta.NetworkManager;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;

import net.jxta.exception.JxtaException;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

/**
 * Implements a Super peer that will ensure connectivity and for a network.
 * <p>
 * This peer does nothing except route communication, log events and ensures network existence.
 * 
 * @version $Id$
 */
public class JxtaSuperPeer implements SuperPeer, RendezvousListener
{
    /** This peer's jxta manager. */
    private Peer peer;

    /** Used for logging. */
    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * Constructs a new Super peer instance.
     * 
     * @param peerName the name of this peer.
     * @param cacheLocation the location where to create the jxta cache directory.
     * @param mode the mode to run in.
     * @throws JxtaException if problems occur.
     */
    public JxtaSuperPeer(String peerName, String cacheLocation, ConfigMode mode) throws JxtaException
    {
        peer = PeerFactory.createPeer();
        peer.configureNetwork(peerName, (cacheLocation == null ? null : new File(cacheLocation)), mode);
    }

    /** {@inheritDoc} **/
    public NetworkConfigurator getConfigurator()
    {
        NetworkConfigurator configurator = null;
        try {
            configurator = this.getManager().getConfigurator();
        } catch (Exception e) {
            this.logger.error("Failed to get the configurator. Returning null.", e);
        }

        return configurator;
    }

    /** {@inheritDoc} **/
    public NetworkManager getManager()
    {
        return this.peer.getManager();
    }

    /** {@inheritDoc} */
    public void startNetwork() throws JxtaException
    {
        this.logger.info(this.getPeerName() + " : Starting network.");

        try {
            peer.startNetworkAndConnect(null, null);
        } catch (Exception e) {
            throw new JxtaException("Problems starting the network.", e);
        }

        this.logger.info(this.getPeerName() + " : Network started.");
    }

    /** {@inheritDoc} **/
    public void stopNetwork()
    {
        this.logger.info(this.getPeerName() + " : Stoping network.");
        peer.stopNetwork();
        this.logger.info(this.getPeerName() + " : Network stoped.");
    }

    /** {@inheritDoc} */
    public Peer getPeer()
    {
        return peer;
    }

    /** {@inheritDoc} */
    public String getPeerName()
    {
        return this.peer.getMyPeerName();
    }

    /** {@inheritDoc} **/
    public void rendezvousEvent(RendezvousEvent event)
    {
        this.logger.info(this.getPeerName() + event.toString());
    }

    /*
     * private String rdvEventToString(int eventType) { switch (eventType) { case RendezvousEvent.BECAMEEDGE : return
     * "BECAMEEDGE"; case RendezvousEvent.BECAMERDV : return "BECAMERDV"; case RendezvousEvent.CLIENTCONNECT : return
     * "CLIENTCONNECT"; case RendezvousEvent.CLIENTDISCONNECT : return "CLIENTDISCONNECT"; case
     * RendezvousEvent.CLIENTFAILED : return "CLIENTFAILED"; case RendezvousEvent.CLIENTRECONNECT : return
     * "CLIENTRECONNECT"; case RendezvousEvent.RDVCONNECT : return "RDVCONNECT"; case RendezvousEvent.RDVDISCONNECT :
     * return "RDVDISCONNECT"; case RendezvousEvent.RDVFAILED : return "RDVFAILED"; case RendezvousEvent.RDVRECONNECT :
     * return "RDVRECONNECT"; default: return "UNKNOWN"; } }
     */

}
