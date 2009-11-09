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
package org.xwoot.jxta.test.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import junit.framework.Assert;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.xwoot.jxta.test.multiplePeers.MultiplePeersTestCase;

/**
 * Utility class for launching tests that are instances of {@link MultiplePeersTestCase}.
 *
 * @version $Id$
 */
public class MultiplePeersTestCaseLauncher
{
    /** TODO DOCUMENT ME! */
    public static final String DISCONNECT_METHODS_VALUE = "disconnectMethods";

    /** TODO DOCUMENT ME! */
    public static final String START_METHODS_VALUE = "startMethods";

    /** TODO DOCUMENT ME! */
    public static final String CONNECT_METHODS_VALUE = "connectMethods";

    /** TODO DOCUMENT ME! */
    public static final String INIT_METHODS_VALUE = "initMethods";

    /** TODO DOCUMENT ME! */
    public static final String TEST_CASES_VALUE = "testCases";

    public static final String MAIN_THREAD_LOCK = "wait for peers to finish";

    public static PeerGroupAdvertisement GROUP_ADV;

    public static final String GROUP_ADV_LOCK = "wait for group adv to be created/published";

    public static final String TEST_GROUP_PREFIX = "testGoup";

    public static final String classpath = getClassPath();

    public static final String pathElement[] = MultiplePeersTestCaseLauncher.split(classpath);

    public static final Class[] VOID_PARAMETERS_TYPE = new Class[0];

    public static final Object[] VOID_PARAMETERS = new Object[0];

    public static final Class[] initMethodParametersTypes = {String.class, Boolean.class};

    public static final Class[] startMethodParametersTypes = {Boolean.class, String.class};

    public static final String ERRORS_PROPERTY_NAME = "errors";

    public static final String SUCCESS_PROPERTY_NAME = "success";

    public static final String SUCCESS_PROPERTY_VALUE = "true";

    public static final char[] KEYSTORE_PASSWORD = "keystorePass".toCharArray();

    public static final char[] GROUP_PASSWORD = "groupPass".toCharArray();

    public static Map<String, Object[]> launchTest(String className, int numberOfPeers, String peerName) throws Exception
    {
        boolean specialInstance = numberOfPeers == 1 && peerName != null && peerName.length() != 0;
        
        Map<String, Object[]> testCasesAndMethods = new HashMap<String, Object[]>();
        
        Object[] testCases = new Object[numberOfPeers];
        Method[] initMethods = new Method[numberOfPeers];
        Method[] connectMethods = new Method[numberOfPeers];
        Method[] startMethods = new Method[numberOfPeers];
        Method[] disconnectMethods = new Method[numberOfPeers];

        String groupName = TEST_GROUP_PREFIX + UUID.randomUUID().toString();

        for (int i = 0; i < numberOfPeers; i++) {

            PeerClassLoader peerLoader = new PeerClassLoader(pathElement);
            Class discoverTestPeerClass = peerLoader.loadClass(className);

            Constructor constructor = discoverTestPeerClass.getConstructor(VOID_PARAMETERS_TYPE);
            testCases[i] = constructor.newInstance(VOID_PARAMETERS);

            initMethods[i] = discoverTestPeerClass.getMethod("init", initMethodParametersTypes);
            String thisPeersName = (specialInstance ? peerName : String.valueOf(i));
            Boolean networkCreator = (specialInstance ? Boolean.TRUE : Boolean.FALSE);
            Object[] initParameters = new Object[] {thisPeersName, networkCreator};
            Boolean inited = (Boolean) initMethods[i].invoke(testCases[i], initParameters);
            Assert.assertTrue(inited.booleanValue());

            connectMethods[i] = discoverTestPeerClass.getMethod("connect", VOID_PARAMETERS_TYPE);
            Boolean connected = (Boolean) connectMethods[i].invoke(testCases[i], VOID_PARAMETERS);
            Assert.assertTrue(connected.booleanValue());

            startMethods[i] = discoverTestPeerClass.getMethod("start", startMethodParametersTypes);
            Object[] startParameters = new Object[] {(i == 0 ? Boolean.TRUE : Boolean.FALSE), groupName};
            Boolean started = (Boolean) startMethods[i].invoke(testCases[i], startParameters);
            Assert.assertTrue(started.booleanValue());
            
            disconnectMethods[i] = discoverTestPeerClass.getMethod("disconnect", VOID_PARAMETERS_TYPE);
        }

        // check if we had errors before waiting.
        MultiplePeersTestCaseLauncher.checkForErrors();

        if (!specialInstance) {
            System.out.println("Keeping main thread alive for max 2 minutes.");
            
            synchronized (MAIN_THREAD_LOCK) {
                MAIN_THREAD_LOCK.wait(120000);
                System.out.println("(possibly) A peer finished.");
    
                MultiplePeersTestCaseLauncher.checkForErrors();
                MultiplePeersTestCaseLauncher.checkForSuccess();
            }
            
            // stop and disconnect all peers involved in this test.
            for (int i = 0; i < numberOfPeers; i++) {
                disconnectMethods[i].invoke(testCases[i], MultiplePeersTestCaseLauncher.VOID_PARAMETERS);
            }
        }
        
        testCasesAndMethods.put(TEST_CASES_VALUE, testCases);
        testCasesAndMethods.put(INIT_METHODS_VALUE, initMethods);
        testCasesAndMethods.put(CONNECT_METHODS_VALUE, connectMethods);
        testCasesAndMethods.put(START_METHODS_VALUE, startMethods);
        testCasesAndMethods.put(DISCONNECT_METHODS_VALUE, disconnectMethods);
        
        return testCasesAndMethods;
    }
    
    public static Map<String, Object[]> launchTest(String className, int numberOfPeers) throws Exception
    {
        return MultiplePeersTestCaseLauncher.launchTest(className, numberOfPeers, null);
    }

    private static String[] split(String classpath)
    {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        String pathElement[] = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreElements(); i++) {
            pathElement[i] = (String) tokenizer.nextElement();
        }
        return pathElement;
    }

    private static void checkForErrors()
    {
        String errors = System.getProperty(ERRORS_PROPERTY_NAME);
        System.out.println("Errors: " + errors);
        Assert.assertNull(errors);
    }

    private static void checkForSuccess()
    {
        String success = System.getProperty(SUCCESS_PROPERTY_NAME);
        Assert.assertNotNull(success);
        Assert.assertEquals(SUCCESS_PROPERTY_VALUE, success);
    }

    public static String getClassPath()
    {
        String defaultClassPath = System.getProperty("java.class.path");
        String surefireClassPath = System.getProperty("surefire.test.class.path");

        return defaultClassPath + File.pathSeparator + surefireClassPath;
    }
}
