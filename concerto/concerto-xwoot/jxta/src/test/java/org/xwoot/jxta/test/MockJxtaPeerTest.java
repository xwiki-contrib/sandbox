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
package org.xwoot.jxta.test;

import java.io.ObjectOutputStream;

import junit.framework.Assert;

import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.xwoot.jxta.DirectMessageReceiver;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.message.Message;
import org.xwoot.jxta.message.MessageFactory;
import org.xwoot.jxta.mock.MockJxtaPeer;

/**
 * Tests for the jxta module mockup(fake).
 *
 * @version $Id$
 */
public class MockJxtaPeerTest
{    
    Peer peer1;
    Peer peer2;
    Peer peer3;
    
    String peerName1 = "Concerto1";
    String peerName2 = "Concerto2";
    String peerName3 = "Concerto3";
    
    @Before
    public void initPeers() throws Exception
    {        
        if (MockJxtaPeer.groupAdvRegistry != null) {
            MockJxtaPeer.groupAdvRegistry.clear();
        }
        
        if (MockJxtaPeer.groupsWithPeersMap != null) {
            MockJxtaPeer.groupsWithPeersMap.clear();
        }
        
        peer1 = PeerFactory.createMockPeer();
        peer1.configureNetwork(peerName1, null, null);
        
        peer2 = PeerFactory.createMockPeer();
        peer2.configureNetwork(peerName2, null, null);
        
        peer3 = PeerFactory.createMockPeer();
        peer3.configureNetwork(peerName3, null, null);
    }
       
    @Test
    public void testStartNetwork() throws Exception
    {
        peer1.startNetworkAndConnect(null, null);
        peer2.startNetworkAndConnect(null, null);
        peer3.startNetworkAndConnect(null, null);
    }
    
    @Test
    public void testCreateGroup() throws Exception
    {
        peer1.startNetworkAndConnect(null, null);
        PeerGroup group = peer1.createNewGroup("testGroup", "testDescription");
        
        Assert.assertNotNull(group);
        Assert.assertTrue(peer1.getCurrentJoinedPeerGroup().equals(group));
        Assert.assertTrue(peer1.isConnectedToGroup());
        
        PeerGroupAdvertisement groupAdv = group.getPeerGroupAdvertisement();
        
        Assert.assertTrue(MockJxtaPeer.groupAdvRegistry.contains(groupAdv));
        
        Assert.assertNotNull(MockJxtaPeer.groupsWithPeersMap.get(groupAdv));
        Assert.assertEquals(1, MockJxtaPeer.groupsWithPeersMap.get(groupAdv).size());
        Assert.assertTrue(MockJxtaPeer.groupsWithPeersMap.get(groupAdv).contains(peer1));
    }
    
    @Test
    public void testCreateGroupAndOthersJoin() throws Exception
    {
        peer1.startNetworkAndConnect(null, null);
        peer2.startNetworkAndConnect(null, null);
        
        PeerGroup createdGroup = peer1.createNewGroup("testGroup", "testDescription");
        Assert.assertNotNull(createdGroup);
        Assert.assertEquals(createdGroup, peer1.getCurrentJoinedPeerGroup());
        Assert.assertTrue(peer1.isConnectedToGroup());
        
        PeerGroup joinedGroup = peer2.joinPeerGroup(createdGroup.getPeerGroupAdvertisement(), false);
        Assert.assertNotNull(joinedGroup);
        Assert.assertEquals(joinedGroup, peer2.getCurrentJoinedPeerGroup());
        Assert.assertTrue(peer2.isConnectedToGroup());
        
        Assert.assertEquals(createdGroup.getPeerGroupID(), joinedGroup.getPeerGroupID());
        
        PeerGroupAdvertisement groupAdv = joinedGroup.getPeerGroupAdvertisement();

        Assert.assertTrue(MockJxtaPeer.groupAdvRegistry.contains(groupAdv));
        
        Assert.assertNotNull(MockJxtaPeer.groupsWithPeersMap.get(groupAdv));
        Assert.assertEquals(2, MockJxtaPeer.groupsWithPeersMap.get(groupAdv).size());
        Assert.assertTrue(MockJxtaPeer.groupsWithPeersMap.get(groupAdv).contains(peer1));
        Assert.assertTrue(MockJxtaPeer.groupsWithPeersMap.get(groupAdv).contains(peer2));
    }
    
    @Test
    public void testBroadcastMessageToGroup() throws Exception
    {
        peer1.startNetworkAndConnect(null, null);
        
        JxtaCastEventListenerHandler1 broadcastHandler1 = new JxtaCastEventListenerHandler1();
        peer2.startNetworkAndConnect(broadcastHandler1, null);
        
        PeerGroup createdGroup = peer1.createNewGroup("testGroup", "testDescription");
        
        PeerGroup joinedGroup = peer2.joinPeerGroup(createdGroup.getPeerGroupAdvertisement(), false);
        
        Message broadcastedMessage = MessageFactory.createMessage(peer1.getMyPeerAdv().getPeerID(), "a test message", Message.Action.BROADCAST_PATCH);
        
        peer1.sendObject(broadcastedMessage, broadcastedMessage.getAction().toString());
        
        Message receivedMessage = (Message) broadcastHandler1.getReceivedMessage();
        
        Assert.assertEquals(broadcastedMessage, receivedMessage);
    }
    
    @Test
    public void testDirectMessageToPeer() throws Exception
    {
        peer1.startNetworkAndConnect(null, null);
        
        DirectMessageReceiverHandler directMessageHandler1 = new DirectMessageReceiverHandler();
        peer2.startNetworkAndConnect(null, directMessageHandler1);
        
        PeerGroup createdGroup = peer1.createNewGroup("testGroup", "testDescription");
        
        PeerGroup joinedGroup = peer2.joinPeerGroup(createdGroup.getPeerGroupAdvertisement(), false);
        
        Message broadcastedMessage = MessageFactory.createMessage(peer1.getMyPeerAdv().getPeerID(), "a test message", Message.Action.BROADCAST_PATCH);
        
        peer1.sendObject(broadcastedMessage, peer2.getMyDirectCommunicationPipeAdvertisement());
        
        Message receivedMessage = (Message) directMessageHandler1.getReceivedMessage();
        
        Assert.assertEquals(broadcastedMessage, receivedMessage);
    }
    
    @Test
    public void testDirectMessageToRandomPeer() throws Exception
    {
        peer1.startNetworkAndConnect(null, null);
        
        DirectMessageReceiverHandler directMessageHandler1 = new DirectMessageReceiverHandler();
        peer2.startNetworkAndConnect(null, directMessageHandler1);
        
        PeerGroup createdGroup = peer1.createNewGroup("testGroup", "testDescription");
        
        PeerGroup joinedGroup = peer2.joinPeerGroup(createdGroup.getPeerGroupAdvertisement(), false);
        
        Message broadcastedMessage = MessageFactory.createMessage(peer1.getMyPeerAdv().getPeerID(), "a test message", Message.Action.BROADCAST_PATCH);
        
        peer1.sendObjectToRandomPeerInGroup(broadcastedMessage, false);        
        Message receivedMessage = (Message) directMessageHandler1.getReceivedMessage();
        
        Assert.assertEquals(broadcastedMessage, receivedMessage);
    }
    
    class DirectMessageReceiverHandler implements DirectMessageReceiver
    {
        private Object receivedMessage;
        
        /** {@inheritDoc} **/
        public Log getLog()
        {
            // TODO Auto-generated method stub
            return null;
        }

        /** {@inheritDoc} **/
        public void receiveDirectMessage(Object message, ObjectOutputStream oos)
        {
            this.receivedMessage = message;
        }
        
        /**
         * @return the receivedMessage
         */
        public Object getReceivedMessage()
        {
            return this.receivedMessage;
        }
        
    }
    
    class JxtaCastEventListenerHandler1 implements JxtaCastEventListener
    {
        Object receivedMessage;

        /** {@inheritDoc} **/
        public void jxtaCastProgress(JxtaCastEvent e)
        {
            if (e.transType == JxtaCastEvent.RECV && e.percentDone == 100) {
                this.receivedMessage = e.transferedData;
            }
        }

        /**
         * @return the receivedMessage
         */
        public Object getReceivedMessage()
        {
            return this.receivedMessage;
        }
    }
}
