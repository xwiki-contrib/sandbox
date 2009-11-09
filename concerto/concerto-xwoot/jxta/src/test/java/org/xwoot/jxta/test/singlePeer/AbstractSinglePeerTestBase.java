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
import java.net.URI;

import net.jxta.platform.NetworkConfigurator;

import org.junit.BeforeClass;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.jxta.test.AbstractJxtaTestBase;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Common behavior for jxta tests.
 * <p>
 * Just add tests to subclasses.
 * 
 * @version $Id$
 */
public abstract class AbstractSinglePeerTestBase extends AbstractJxtaTestBase
{
    /** Working dir for tests. */
    public static final String WORKING_DIR = FileUtil.getTestsWorkingDirectoryPathForModule("jxta");

    /** Name of test peer. */
    protected static String PEER_NAME = "concerto1";

    /** Local repository for test peer. */
    protected static File PEER_HOME = new File(WORKING_DIR, PEER_NAME);

    /** The test peer. */
    protected static Peer PEER;

    /**
     * Initializes the working directory.
     * 
     * @throws Exception if problems occur.
     */
    @BeforeClass
    public static void createAndConnect() throws Exception
    {
        if (PEER == null) {
            FileUtil.deleteDirectory(PEER_HOME);
            FileUtil.checkDirectoryPath(PEER_HOME);

            PEER = PeerFactory.createPeer();
            PEER.configureNetwork(PEER_NAME, new File(WORKING_DIR), ConfigMode.EDGE);

            // Set the test peer to use the "server peer" that is running in the background.

            PEER.getManager().setUseDefaultSeeds(false);

            NetworkConfigurator configurator = PEER.getManager().getConfigurator();

            configurator.addSeedRendezvous(new URI("tcp://localhost:9701"));
            configurator.addSeedRelay(new URI("tcp://localhost:9701"));
            configurator.addSeedRendezvous(new URI("http://localhost:9700"));
            configurator.addSeedRelay(new URI("http://localhost:9700"));

            configurator.setUseMulticast(false);
            
            // Use different ports than the Serv0r peer to avoid conflict
            configurator.setTcpPort(9711);
            configurator.setHttpPort(9710);

            // Connect to the network.
            PEER.startNetworkAndConnect(null, null);
        } else if (!PEER.isConnectedToNetwork()) {
            PEER.startNetworkAndConnect(null, null);
        }
    }

    /**
     * Stop the network.
     * 
     * @throws Exception if problems occur.
     */
    /*
     * @AfterClass public static void clean() throws Exception { if (PEER != null) { PEER.stopNetwork();
     * FileUtil.deleteDirectory(PEER_HOME); PEER = null; } }
     */
}
