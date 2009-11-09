/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.mockiphone.web;

import org.apache.commons.lang.StringUtils;
import org.xwoot.mockiphone.MockIphone;
import org.xwoot.mockiphone.MockIphoneException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockIphoneSite
{
    // singleton instance
    private static MockIphoneSite instance;

    private MockIphone mockIphoneEngine;
    
    private boolean started = false;

    public static final String MOCKIPHONE_PROPERTIES_FILENAME = "mockiphone.properties";

    public static final String IWOOT_END_POINT = "iwoot_endpoint";
    
    public static final String MOCKIPHONE_DIR_NAME = "mockiphone_working_dir";

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static synchronized MockIphoneSite getInstance()
    {
        if (MockIphoneSite.instance == null) {
            MockIphoneSite.instance = new MockIphoneSite();
        }

        return MockIphoneSite.instance;
    }

    public Properties getProperties(String path)
    {
        Properties p = new Properties();
        try {
            FileInputStream fis = new FileInputStream(path);
            p.load(fis);
            fis.close();
        } catch (IOException ex) {
            // Cannot load properties, return empty properties.
        }
        return p;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public MockIphone getMockIphoneSiteEngine()
    {
        return this.mockIphoneEngine;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param siteId DOCUMENT ME!
     * @param peerId DOCUMENT ME!
     * @param wikiPropertiesPath DOCUMENT ME!
     * @param workingDirPath DOCUMENT ME!
     * @param messagesRound DOCUMENT ME!
     * @param maxNeighbors DOCUMENT ME!
     * @throws MockIphoneException 
     * @throws RuntimeException DOCUMENT ME!
     * @throws ClockException 
     * @throws WikiContentManagerException 
     * @throws WootEngineException 
     * @throws HttpServletLpbCastException 
     * @throws AntiEntropyException 
     * @throws XWootException 
     */
    public void init(String iwootEndPoint, String workingDirPath, int mockIphoneId) throws MockIphoneException
    {
        File mockIphoneDir = new File(workingDirPath + File.separator + MOCKIPHONE_DIR_NAME);
       

        if (!mockIphoneDir.exists() && !mockIphoneDir.mkdir()) {
            throw new RuntimeException("Can't create mockIphone directory: " + mockIphoneDir);
        }

        this.mockIphoneEngine = new MockIphone(mockIphoneDir.toString(),Integer.valueOf(mockIphoneId),iwootEndPoint);

        this.started = true;
        System.out.println("Site " + this.mockIphoneEngine.getId() + " initialisation");
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean isStarted()
    {
        return this.started;
    }

    public void savePropertiesInFile(String path, String comments, Properties p) throws IOException
    {
        File f = new File(path);
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        p.store(fos, comments);
        fos.flush();
        fos.close();
    }

    public String updatePropertiesFiles(HttpServletRequest request, String mockIphonePropertiesPath) throws IOException
    {
        String result = "";
        Properties p;

        // Update properties.
        p = updatePropertiesFromRequest(request, mockIphonePropertiesPath);
        result += this.validateProperties(p);
        if (result.equals("")) {
            this.savePropertiesInFile(mockIphonePropertiesPath, " -- Mock Iphone properties --", p);
        }

        return result;
    }

    public Properties updatePropertiesFromRequest(ServletRequest request, String mockIphonePropertiesPath)
    {
        Properties p = getProperties(mockIphonePropertiesPath);
        if (!StringUtils.isEmpty(request.getParameter(MockIphoneSite.IWOOT_END_POINT))) {
            p.put(MockIphoneSite.IWOOT_END_POINT, request.getParameter(MockIphoneSite.IWOOT_END_POINT));
        }
        if (!StringUtils.isEmpty(request.getParameter(MockIphoneSite.MOCKIPHONE_DIR_NAME))) {
            p.put(MockIphoneSite.MOCKIPHONE_DIR_NAME, request.getParameter(MockIphoneSite.MOCKIPHONE_DIR_NAME));
        }
        return p;
    }

    /**
     * Checks that the XWoot configuration is good.
     * 
     * @param p The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></strong> is returned.
     * @todo Message localization.
     */
    private String validateProperties(Properties p)
    {
        String result = "";

        // Check that the directory for storing data is valid and writable.
        if (p.get(MockIphoneSite.MOCKIPHONE_DIR_NAME) == null) {
            result += "Please enter a non-empty " + MockIphoneSite.MOCKIPHONE_DIR_NAME + " field.\n";
        } else {
            try {
                File f = new File((String) p.get(MockIphoneSite.MOCKIPHONE_DIR_NAME));
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        result +=
                            "The provided directory does not exist and cannot be created. Please enter a writable serialization folder.\n";
                    }
                } else if (!f.canRead() || !f.canWrite()) {
                    result += "Please enter a writable serialization folder.\n";
                }
            } catch (Exception ex) {
                result += "The provided directory cannot be accessed. Please enter a writable serialization folder.\n";
            }
        }

        // Check that the XWoot URL is valid.
        if (p.get(MockIphoneSite.IWOOT_END_POINT) == null) {
            result += "Please enter a non-empty IWoot address.\n";
        } else {
            try {
                new URL((String) p.get(MockIphoneSite.IWOOT_END_POINT));
            } catch (MalformedURLException e) {
                result += "Please enter a valid IWoot address (the given URL is malformed)\n";
            }

        }
        return result;
    }
}
