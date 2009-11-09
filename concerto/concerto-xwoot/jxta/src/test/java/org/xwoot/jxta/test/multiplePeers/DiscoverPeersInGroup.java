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
import java.util.Collections;
import java.util.List;

import net.jxta.document.Advertisement;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.commons.logging.Log;
import org.xwoot.jxta.test.util.MultiplePeersTestCaseLauncher;

/**
 * TODO DOCUMENT ME!
 *
 * @version $Id$
 */
public class DiscoverPeersInGroup extends AbstractMultiplePeersTestCase
{

    /** {@inheritDoc} **/
    public void run()
    {
        System.out.println(this.peerName + " : Thread started.");
        
        if (this.groupCreator) {
            PeerGroup group = null;
            try {
                group = this.peer.createNewGroup(this.groupName, "A test group.", MultiplePeersTestCaseLauncher.KEYSTORE_PASSWORD, MultiplePeersTestCaseLauncher.GROUP_PASSWORD);
            } catch (Exception e) {
                System.out.println(this.peerName + " : Thread Failed. Stopping.");
                e.printStackTrace();
                //Assert.fail("Failed to crete group: " + e.getMessage());
                
                synchronized (MultiplePeersTestCaseLauncher.GROUP_ADV_LOCK) {
                    // notify other peers that the group adv will not be published.
                    MultiplePeersTestCaseLauncher.GROUP_ADV_LOCK.notifyAll();
                }
                
                // stop this thread
                this.fail("Failed to create group: " + e.getMessage());
                /*synchronized (MultiplePeersTestCaseLauncher.MAIN_THREAD_LOCK) {
                    // notify main thread not to wait for this thread anymore.
                    MultiplePeersTestCaseLauncher.MAIN_THREAD_LOCK.notifyAll();
                }
                
                return;*/
            }
            
            System.out.println(this.peerName + " : group created. : " + group.getPeerGroupName());
            
            synchronized (MultiplePeersTestCaseLauncher.GROUP_ADV_LOCK) {
                // notify other peers that the group adv has been published.
                MultiplePeersTestCaseLauncher.GROUP_ADV_LOCK.notifyAll();
                System.out.println(this.peerName + " : Listeners notified.");
            }
            
        } else {
            PeerGroupAdvertisement joinGroupAdv = this.searchForGroup(this.groupName);
            
            System.out.println(this.peerName + " : Joining group.");
            
            PeerGroup group = null;
            try {
                group = this.peer.joinPeerGroup(joinGroupAdv, MultiplePeersTestCaseLauncher.KEYSTORE_PASSWORD, MultiplePeersTestCaseLauncher.GROUP_PASSWORD, false);
            } catch (Exception e) {
                e.printStackTrace();
                this.fail(e.getMessage());
            }
            
            if (this.peer.isGroupRendezVous()) {
                this.fail("Failed to contact existing group rendezvous.");
            }
            
            System.out.println(this.peerName + " : Joied group " + group.getPeerGroupName() + ".");
            
            int numberOfPipeAdvs = 0;
            for (int i=0; i<3; i++) {
                System.out.println(this.peerName + " : Try " + i + " to discover pipeAdv");
                List<Advertisement> pipeAdvs = Collections.list(peer.getKnownDirectCommunicationPipeAdvertisements());
                System.out.println(this.peerName + " : pipeAdvs :\n" + pipeAdvs);
                numberOfPipeAdvs = pipeAdvs.size();
                System.out.println("Found " + numberOfPipeAdvs + " PipeAdvertisements");
                if (numberOfPipeAdvs == 1) {
                    break;
                }
                
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ignore) {
                }
            }
            
            if (numberOfPipeAdvs != 1) {
                this.fail("Expecting 1 pipe Adv, actual: " + numberOfPipeAdvs);
            }
            
            this.pass();
        }
        
        System.out.println(this.peerName + " : Thread finished.");
    }

    /** {@inheritDoc} **/
    public void jxtaCastProgress(JxtaCastEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} **/
    public Log getLog()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} **/
    public void receiveDirectMessage(Object message, ObjectOutputStream oos)
    {
        // TODO Auto-generated method stub
        
    }

}
