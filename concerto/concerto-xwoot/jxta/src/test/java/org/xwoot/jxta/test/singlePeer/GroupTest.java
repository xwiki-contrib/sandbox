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

import java.util.UUID;

import junit.framework.Assert;

import net.jxta.impl.membership.none.NoneMembershipService;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.junit.Test;

/**
 * Test group actions, like create/join/leave.
 * 
 * @version $Id$
 */
public class GroupTest extends AbstractSinglePeerTestBase
{
    /** Name prefix for test groups. */
    protected static final String GROUP_NAME_PREFIX = "testGroup";

    /** Description for test groups. */
    protected static final String GROUP_DESCRIPTION = "A temporary test group.";

    /** Local keystore password. */
    protected static final char[] KEYSTORE_PASSWORD = "local password".toCharArray();

    /** Group password. */
    protected static final char[] GROUP_PASSWORD = "Open sessame!".toCharArray();

    /**
     * Create a public group and join it.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCreatePublicGroup() throws Exception
    {
        // Make sure we are connected to the network.
        Assert.assertTrue(PEER.isConnectedToNetwork());

        String groupName = GROUP_NAME_PREFIX + UUID.randomUUID().toString();

        PeerGroup group = PEER.createNewGroup(groupName, GROUP_DESCRIPTION);

        // Make sure we successfully joined the group.
        Assert.assertTrue(PEER.hasJoinedAGroup());
        Assert.assertTrue(PEER.isGroupRendezVous());
        Assert.assertEquals(group, PEER.getCurrentJoinedPeerGroup());

        // Make sure the group is public.
        Assert.assertTrue(group.getMembershipService() instanceof NoneMembershipService);
    }

    /**
     * Create a public group. Leave it, then join it back again.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testJoinPublicGroup() throws Exception
    {
        // Make sure we are connected to the network.
        Assert.assertTrue(PEER.isConnectedToNetwork());

        String groupName = GROUP_NAME_PREFIX + UUID.randomUUID().toString();

        PeerGroup group = PEER.createNewGroup(groupName, GROUP_DESCRIPTION);

        // Make sure we successfully joined the group.
        Assert.assertTrue(PEER.hasJoinedAGroup());
        Assert.assertTrue(PEER.isGroupRendezVous());
        Assert.assertEquals(group, PEER.getCurrentJoinedPeerGroup());

        // Make sure the group is public.
        Assert.assertTrue(group.getMembershipService() instanceof NoneMembershipService);

        // Save the peer group advertisement in order to join again. 
        PeerGroupAdvertisement groupAdv = group.getPeerGroupAdvertisement();
        
        // Leave the group so we can join it again with the joinPeerGroup method.
        PEER.leavePeerGroup(group);

        // Make sure we have successfully left the group.
        Assert.assertFalse(PEER.hasJoinedAGroup());
        Assert.assertEquals(PEER.getDefaultGroup(), PEER.getCurrentJoinedPeerGroup());

        PeerGroup joinedGroup = PEER.joinPeerGroup(groupAdv, false);

        // Make sure we successfully joined the group.
        Assert.assertTrue(PEER.hasJoinedAGroup());
        Assert.assertTrue(PEER.isGroupRendezVous());

        // Make sure we joined the right group.
        Assert.assertEquals(groupAdv.getPeerGroupID(), joinedGroup.getPeerGroupID());
        Assert.assertEquals(joinedGroup.getPeerGroupID(), PEER.getCurrentJoinedPeerGroup().getPeerGroupID());
    }

    /**
     * Create a public group and join it.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCreatePrivateGroup() throws Exception
    {
        // Make sure we are connected to the network.
        Assert.assertTrue(PEER.isConnectedToNetwork());

        String groupName = GROUP_NAME_PREFIX + UUID.randomUUID().toString();

        PeerGroup group = PEER.createNewGroup(groupName, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);

        // Make sure we successfully joined the group.
        Assert.assertTrue(PEER.hasJoinedAGroup());
        Assert.assertTrue(PEER.isGroupRendezVous());
        Assert.assertEquals(group, PEER.getCurrentJoinedPeerGroup());

        // Make sure the group is private.
        Assert.assertTrue(group.getMembershipService() instanceof PSEMembershipService);
    }

    /**
     * Create a private group. Leave it, then join it back again.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testJoinPrivateGroup() throws Exception
    {
        // Make sure we are connected to the network.
        Assert.assertTrue(PEER.isConnectedToNetwork());

        String groupName = GROUP_NAME_PREFIX + UUID.randomUUID().toString();

        PeerGroup group = PEER.createNewGroup(groupName, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);

        // Make sure we successfully joined the group.
        Assert.assertTrue(PEER.hasJoinedAGroup());
        Assert.assertTrue(PEER.isGroupRendezVous());
        Assert.assertEquals(group, PEER.getCurrentJoinedPeerGroup());

        // Make sure the group is private.
        Assert.assertTrue(group.getMembershipService() instanceof PSEMembershipService);

        // Save the peer group advertisement in order to join again. 
        PeerGroupAdvertisement groupAdv = group.getPeerGroupAdvertisement();
        
        // Leave the group so we can join it again.
        PEER.leavePeerGroup(group);

        // Make sure we have successfully left the group.
        Assert.assertFalse(PEER.hasJoinedAGroup());
        Assert.assertEquals(PEER.getDefaultGroup(), PEER.getCurrentJoinedPeerGroup());

        PeerGroup joinedGroup =
            PEER.joinPeerGroup(groupAdv, KEYSTORE_PASSWORD, GROUP_PASSWORD, false);

        // Make sure we successfully joined the group.
        Assert.assertTrue(PEER.hasJoinedAGroup());
        Assert.assertTrue(PEER.isGroupRendezVous());

        // Make sure we joined the right group.
        Assert.assertEquals(groupAdv.getPeerGroupID(), joinedGroup.getPeerGroupID());
        Assert.assertEquals(joinedGroup.getPeerGroupID(), PEER.getCurrentJoinedPeerGroup().getPeerGroupID());
    }
}
