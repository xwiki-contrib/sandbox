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
package org.xwoot.jxta.test.singlePeer;

import java.io.File;

import junit.framework.Assert;

import net.jxta.exception.JxtaException;
import net.jxta.platform.NetworkConfigurator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Test the network.
 *
 * @version $Id$
 */
public class NetworkTest extends AbstractSinglePeerTestBase
{
    /**
     * Start and connect the peer if needed.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void init() throws Exception
    {
        if (!PEER.isConnectedToNetwork()) {
            PEER.startNetworkAndConnect(null, null);
        }
    }
    
    /**
     * Leave any joined peer group and restart the peer if needed.
     * 
     * @throws Exception if problems occur.
     */
    @After
    public void destroy() throws Exception
    {
        PEER.leavePeerGroup();
    }
    
    /**
     * Can't start network before we configure our network and local settings.
     * 
     * @throws Exception expected to throw an IllegalStateException.
     */
    @Test(expected=IllegalStateException.class)
    public void testStartNetwork() throws Exception
    {
        Peer aPeer = PeerFactory.createPeer();
        try {
            aPeer.startNetworkAndConnect(null, null);
        } finally {
            if (aPeer != null) {
                aPeer.stopNetwork();
            }
        }
    }
    
    /**
     * Connect to our custom rdv.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testConnectToNetwork() throws Exception
    {
        Assert.assertTrue(PEER.isConnectedToNetwork());
    }
    
    /**
     * Fail to connect because seed did not respond or no seed was provided.
     * Result: a {@link JxtaException} will be thrown.
     * 
     * @throws Exception if problems occur.
     */
    /*FIXME: jxta 2.5 bug affects this too. Try 2.6-snapshot
     * @Test(expected = JxtaException.class)
    public void testFailConnectToNetwork() throws Exception
    {
        Assert.assertTrue(peer.isConnectedToNetwork());
        
        // disconnect.
        peer.stopNetwork();
        Assert.assertFalse(peer.isConnectedToNetwork());
        
        // remove any seeds.
        NetworkConfigurator networkConfig = peer.getManager().getConfigurator();
        networkConfig.clearRelaySeedingURIs();
        networkConfig.clearRelaySeeds();
        networkConfig.clearRendezvousSeedingURIs();
        networkConfig.clearRendezvousSeeds();
        
        peer.getManager().setUseDefaultSeeds(false);
        
        try {
            // try to connect to nothing and fail.
            peer.startNetworkAndConnect(null, null);
        } catch(Exception e) {
            Assert.assertFalse(peer.isJxtaStarted());
            Assert.assertFalse(peer.isConnectedToNetwork());
            throw e;
        }
    }*/
    @Test(expected = JxtaException.class)
    public void testFailConnectToNetwork() throws Exception
    {
        Assert.assertTrue(PEER.isConnectedToNetwork());

        // disconnect the main peer.
        PEER.stopNetwork();
        Assert.assertFalse(PEER.isConnectedToNetwork());
        Assert.assertFalse(PEER.hasJoinedAGroup());
        Assert.assertFalse(PEER.isConnectedToGroup());
        Assert.assertFalse(PEER.isJxtaStarted());
        
        // build a second peer so that we don`t disturb the main peer's settings.
        Peer aPeer = PeerFactory.createPeer();
        String peerName = "concerto_conenction_failed";
        aPeer.configureNetwork(peerName, new File(WORKING_DIR), ConfigMode.EDGE);
        
        aPeer.getManager().setUseDefaultSeeds(false);
        
        NetworkConfigurator networkConfig = aPeer.getManager().getConfigurator();
        networkConfig.clearRelaySeedingURIs();
        networkConfig.clearRelaySeeds();
        networkConfig.clearRendezvousSeedingURIs();
        networkConfig.clearRendezvousSeeds();
        
        networkConfig.setUseMulticast(false);
                
        networkConfig.setTcpPort(9711);
        networkConfig.setHttpPort(9710);

        try {
            // try to connect to nothing and fail.
            aPeer.startNetworkAndConnect(null, null);
        } catch (Exception e) {
            Assert.assertFalse(aPeer.isConnectedToNetwork());
            Assert.assertFalse(aPeer.hasJoinedAGroup());
            Assert.assertFalse(aPeer.isConnectedToGroup());
            Assert.assertFalse(aPeer.isJxtaStarted());
            throw e;
        } finally {
            // cleanup for this test
            aPeer.stopNetwork();
            FileUtil.deleteDirectory(new File(WORKING_DIR, peerName));
        }
    }
    
    /**
     * Connect to a network, disconnect and then reconnect, two times in a row.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testReConnectToNetwork() throws Exception
    {
        /*
        //System.out.println("Connection test.");
        Assert.assertTrue(PEER.isConnectedToNetwork());
        
        //System.out.println("STOP");
        PEER.stopNetwork();
//        System.out.println("Connection test.");
//        System.out.println("Status: ");
//        System.out.println("jxta.isNetworkConfigured() : " + peer.isNetworkConfigured());
//        System.out.println("jxta.isJxtaStarted() : " + peer.isJxtaStarted()); 
//        System.out.println("jxta.isConnectedToNetworkRendezVous() : " + peer.isConnectedToNetworkRendezVous());
//        System.out.println("jxta.isConnectedToNetwork() : " + peer.isConnectedToNetwork());
//        System.out.println("jxta.hasJoinedAGroup() : " + peer.hasJoinedAGroup());
//        System.out.println("jxta.isGroupRendezVous() : " + peer.isGroupRendezVous());
//        System.out.println("jxta.isConnectedToGroupRendezVous() : " + peer.isConnectedToGroupRendezVous());
//        System.out.println("jxta.isConnectedToGroup() : " + peer.isConnectedToGroup());
        Assert.assertFalse(PEER.isConnectedToNetwork());
        
//        System.out.println("START");
        PEER.startNetworkAndConnect(null, null);
//        System.out.println("Connection test.");
        Assert.assertTrue(PEER.isConnectedToNetwork());
        */
      
        Assert.assertTrue(PEER.isConnectedToNetwork());

        PEER.stopNetwork();
        Assert.assertFalse(PEER.isConnectedToNetwork());
        Assert.assertFalse(PEER.hasJoinedAGroup());
        Assert.assertFalse(PEER.isConnectedToGroup());
        Assert.assertFalse(PEER.isJxtaStarted());
        
        PEER.startNetworkAndConnect(null, null);
        Assert.assertTrue(PEER.isConnectedToNetwork());
        Assert.assertTrue(PEER.isJxtaStarted());
        
        PEER.stopNetwork();
        Assert.assertFalse(PEER.isConnectedToNetwork());
        Assert.assertFalse(PEER.isJxtaStarted());
        
        PEER.startNetworkAndConnect(null, null);
        Assert.assertTrue(PEER.isConnectedToNetwork());
        Assert.assertTrue(PEER.isJxtaStarted());
        
        PEER.stopNetwork();
        Assert.assertFalse(PEER.isConnectedToNetwork());
        Assert.assertFalse(PEER.isJxtaStarted());
    }
}
