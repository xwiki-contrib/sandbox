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

package org.xwoot.lpbcast.test;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.lpbcast.util.NetUtil;

/**
 * Tests the NetUtil class.
 * <p>
 * Note: These tests can run only with another xwoot instance running.
 * 
 * @version $Id:$
 */
public class NetUtilTest
{
    /** The url to get a file from. */
    private static URL testURL;

    /**
     * Initialize the url to test.
     * 
     * @throws Exception if problems occur.
     */
    @BeforeClass
    public static void initUrl() throws Exception
    {
        testURL =
            new URL(
                "http://localhost:8080/xwootApp/sendState.do?neighbor=http://localhost:8082/xwootApp&file=stateFile");
    }

    /**
     * Tests if the url was propperly initialized.
     */
    public void testInitUtl()
    {
        Assert.assertNotNull(testURL);
    }
    
    /**
     * Test getting a state file from a running xwoot instance.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testGetFileViaHTTPRequest() throws Exception
    {
        File downloadedFile = NetUtil.getFileViaHTTPRequest(testURL);
        Assert.assertNotNull(downloadedFile);
        Assert.assertTrue(downloadedFile.exists());
        Assert.assertTrue(downloadedFile.isFile());
        Assert.assertTrue(downloadedFile.length() > 0);
    }
}
