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
package org.xwoot.jxta.test.multiplePeers;

import java.io.ObjectOutputStream;

import junit.framework.Assert;

import net.jxta.exception.PeerGroupException;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.commons.logging.Log;
import org.xwoot.jxta.message.Message;
import org.xwoot.jxta.message.MessageFactory;
import org.xwoot.jxta.test.util.MultiplePeersTestCaseLauncher;

/**
 * Test message broadcasting between 2 peers that have joined a group.
 * 
 * @version $Id$
 */
public class BroadcastMessageToGroup extends AbstractMultiplePeersTestCase
{
    /**
     * The system property to be used by the sending peer to communicate to the destination peer the message id to
     * expect.
     */
    public static final String MESSAGE_ID_PROPERTY_NAME = "broadcastedMessageID";
    
    /** Lock to wait for the reception of an object. */
    private String receiveObjectLock = "wait for a propagate message to be received.";

    /** {@inheritDoc} **/
    public void run()
    {
        System.out.println(this.peerName + " : Thread started.");

        if (this.groupCreator) {
            PeerGroup group = null;
            try {
                group =
                    this.peer.createNewGroup(this.groupName, "A test group.", MultiplePeersTestCaseLauncher.KEYSTORE_PASSWORD,
                        MultiplePeersTestCaseLauncher.GROUP_PASSWORD);
            } catch (Exception e) {
                System.out.println(this.peerName + " : Thread Failed. Stopping.");
                e.printStackTrace();

                Assert.fail("Failed to create group: " + e.getMessage());
            }

            System.out.println(this.peerName + " : group created. : " + group.getPeerGroupName());

            synchronized (MultiplePeersTestCaseLauncher.GROUP_ADV_LOCK) {
                // notify other peers that the group adv has been published.
                MultiplePeersTestCaseLauncher.GROUP_ADV_LOCK.notifyAll();
                System.out.println(this.peerName + " : Listeners notified.");
            }

            // lock until the message is received.
            synchronized (receiveObjectLock) {
                try {
                    receiveObjectLock.wait();
                    System.setProperty("success", "true");
                } catch (InterruptedException e) {
                }
            }

            // if a message is received, this thread will pass as well.
            this.pass();

        } else {
            PeerGroupAdvertisement joinGroupAdv = searchForGroup(this.groupName);

            System.out.println(this.peerName + " : Joining group.");

            PeerGroup group = null;
            try {
                group =
                    this.peer.joinPeerGroup(joinGroupAdv, MultiplePeersTestCaseLauncher.KEYSTORE_PASSWORD,
                        MultiplePeersTestCaseLauncher.GROUP_PASSWORD, false);
            } catch (Exception e) {
                e.printStackTrace();
                this.fail("Failed to join group: " + e.getMessage());
            }

            System.out.println(this.peerName + " : Joied group " + group.getPeerGroupName() + ".");

            Message messageToBroadcast =
                MessageFactory.createMessage(this.peer.getMyDirectCommunicationPipeAdvertisement().getPipeID()
                    .toString(), "test broadcasted content", Message.Action.BROADCAST_PATCH);
            System.setProperty(MESSAGE_ID_PROPERTY_NAME, messageToBroadcast.getId().toString());

            try {
                this.peer.sendObject(messageToBroadcast, "a broadcasted message.");
            } catch (PeerGroupException e) {
                e.printStackTrace();
                this.fail("Failed to send message : " + e.getMessage());
            }
        }

        System.out.println(this.peerName + " : Thread finished.");
        
        //this.disconnect();
    }

    /** {@inheritDoc} **/
    public void jxtaCastProgress(JxtaCastEvent e)
    {
        try {
            if (e.percentDone == 100 && e.transType == JxtaCastEvent.RECV) {
                Message receivedMessage = (Message) e.transferedData;
                String expectedMessageID = System.getProperty(BroadcastMessageToGroup.MESSAGE_ID_PROPERTY_NAME);
                if (!receivedMessage.getId().toString().equals(expectedMessageID)) {
                    this.fail("Expected : " + expectedMessageID + " but was : " + receivedMessage.getId().toString());
                }

                synchronized (receiveObjectLock) {
                    receiveObjectLock.notifyAll();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.fail("Failed to receive message : " + ex.getMessage());
        }

    }

    /** {@inheritDoc} **/
    public Log getLog()
    {
        // not interested;
        return null;
    }

    /** {@inheritDoc} **/
    public void receiveDirectMessage(Object message, ObjectOutputStream oos)
    {
        // not interested;
    }

}
