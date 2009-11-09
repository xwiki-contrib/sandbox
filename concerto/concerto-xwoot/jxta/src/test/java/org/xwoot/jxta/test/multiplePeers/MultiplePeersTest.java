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

import org.junit.Before;
import org.junit.Test;
import org.xwoot.jxta.test.AbstractJxtaTestBase;
import org.xwoot.jxta.test.util.MultiplePeersTestCaseLauncher;

/**
 * Launches various multiple-peer tests while having a background peer to serve as network seed.
 */
public class MultiplePeersTest extends AbstractJxtaTestBase
{
    
    @Before
    public void clearSystemProperties()
    {
        System.clearProperty(MultiplePeersTestCaseLauncher.ERRORS_PROPERTY_NAME);
        System.clearProperty(MultiplePeersTestCaseLauncher.SUCCESS_PROPERTY_NAME);
    }
    
    @Test
    public void testDiscoverPeersInGroup() throws Exception
    {
        MultiplePeersTestCaseLauncher.launchTest(DiscoverPeersInGroup.class.getName(), 2);
    }

    @Test
    public void testBroadcastMessageToGroup() throws Exception
    {
        MultiplePeersTestCaseLauncher.launchTest(BroadcastMessageToGroup.class.getName(), 2);
    }
    
}
