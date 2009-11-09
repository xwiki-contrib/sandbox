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

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xwoot.jxta.test.multiplePeers.ServerPeerUsedForTests;
import org.xwoot.jxta.test.util.MultiplePeersTestCaseLauncher;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Base implementation for every jxta test.
 * <p>
 * It starts a concurrent thread running a jxta peer that creates a network. Other peers that are contained in the tests
 * will not be able to connect to localhost instead of using the public jxta network.
 * 
 * @version $Id$
 */
public abstract class AbstractJxtaTestBase
{
    /** Working dir for tests. */
    public static final String WORKING_DIR = FileUtil.getTestsWorkingDirectoryPathForModule("jxta");

    /**
     * The methods from the classloader used to launch the "server peer". We need to be able to stop the peer when we
     * are done.
     */
    private static Map<String, Object[]> networkPeerTestCase;

    /**
     * Cleans the tests directory and starts the "server peer".
     * 
     * @throws Exception if problems occur.
     */
    @BeforeClass
    public static void startNetwork() throws Exception
    {
        if (networkPeerTestCase == null) {
            // Cleanup.
            FileUtil.deleteDirectory(WORKING_DIR);
            FileUtil.checkDirectoryPath(WORKING_DIR);
    
            System.out.println("Starting Serv0rPeer.");
            
            // Start the supporting peer.
            networkPeerTestCase = MultiplePeersTestCaseLauncher.launchTest(ServerPeerUsedForTests.class.getName(), 1, "Serv0rPeer");
            
            System.out.println("Serv0rPeer started.");
        } else {
            System.out.println("Serv0rPeer already started.");
        }
    }

    /**
     * Stops the server peer and cleans the tests directory.
     * 
     * @throws Exception if problems occur.
     */
    @AfterClass
    public static void stopNetwork() throws Exception
    {
        /*System.out.println("Stopping Serv0rPeer.");
        
        // Stop the supporting peer.
        Method disconnectMethod =
            (Method) ((Object[]) networkPeerTestCase.get(MultiplePeersTestCaseLauncher.DISCONNECT_METHODS_VALUE))[0];
        Object instance = (Object) ((Object[]) networkPeerTestCase.get(MultiplePeersTestCaseLauncher.TEST_CASES_VALUE))[0];
        disconnectMethod.invoke(instance, MultiplePeersTestCaseLauncher.VOID_PARAMETERS);
        
        System.out.println("Serv0rPeer stopped.");

        // Cleanup.
        FileUtil.deleteDirectory(WORKING_DIR);*/
    }
}
