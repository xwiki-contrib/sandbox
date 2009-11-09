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

public interface MultiplePeersTestCase extends Runnable
{

    /**
     * Initialize and configure the peer instance associated to this test.
     * 
     * @param peerName the name of the peer.
     * @param networkCreator if the peer creates a network.
     * @return {@link Boolean#TRUE} if initialization was successful, {@link Boolean#FALSE} otherwise.
     */
    Boolean init(String peerName, Boolean networkCreator);

    /** Connects the peer to the network. */
    Boolean connect();

    /**
     * Start test-specific actions in a new thread.
     * 
     * @param groupCreator if this peer will be the group creator.
     * @param groupName the name of the group to join/create. This name has to be unique to avoid collisions.
     * @return {@link Boolean#TRUE} if start was successful, {@link Boolean#FALSE} otherwise.
     */
    Boolean start(Boolean groupCreator, String groupName);
    
    /** Disconnects this peer from the network and stops the jxta platform. */
    void disconnect();
    
    /**
     * Notify that the test failed at one point.
     * 
     * @param message the associated error message.
     */
    void fail(String message);
    
    /** Notify that the test has successfully completed. */
    void pass();
}
