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

package org.xwoot.xwootApp.test;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xwoot.contentprovider.XWootContentProviderFactory;
import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.clockEngine.Clock;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class AbstractXWootTest
{
    protected final static String WORKINGDIR = FileUtil.getTestsWorkingDirectoryPathForModule("xwootApp");

    protected int round = 6;

    protected int logDelay = 60;

    protected int maxNeighbors = 5;
    
    XWootContentProviderInterface xwiki21;
    XWootContentProviderInterface xwiki22;
    XWootContentProviderInterface xwiki23;
    
    Clock opClock1;

    Clock opClock2;

    Clock opClock3;

    WootEngine wootEngine1;

    WootEngine wootEngine2;

    WootEngine wootEngine3;

    AntiEntropy ae1;

    AntiEntropy ae2;

    AntiEntropy ae3;

    ThomasRuleEngine tre1;

    ThomasRuleEngine tre2;

    ThomasRuleEngine tre3;

    XWoot3 xwoot31;

    XWoot3 xwoot32;

    XWoot3 xwoot33;

    Peer peer1;

    Peer peer2;

    Peer peer3;
    
    String peer1Name = "Concerto1";
    String peer2Name = "Concerto2";
    String peer3Name = "Concerto3";
    
    String xwoot1DirectoryName = "Site1";
    String xwoot2DirectoryName = "Site2";
    String xwoot3DirectoryName = "Site3";
    
    File xwoot1Directory;
    File xwoot2Directory;
    File xwoot3Directory;
    
    @Before
    public void setup() throws Exception
    {

        // Configure working dir for temporary files
        FileUtil.deleteDirectory(WORKINGDIR);
        FileUtil.checkDirectoryPath(WORKINGDIR);

        this.xwoot1Directory = new File(WORKINGDIR, xwoot1DirectoryName);
        this.xwoot2Directory = new File(WORKINGDIR, xwoot2DirectoryName);
        this.xwoot3Directory = new File(WORKINGDIR, xwoot3DirectoryName);
        
        FileUtil.checkDirectoryPath(xwoot1Directory);
        FileUtil.checkDirectoryPath(xwoot2Directory);
        FileUtil.checkDirectoryPath(xwoot3Directory);
        
        // Choose one:
        
        // a) Use real content providers.
//        this.xwiki21 = XWootContentProviderFactory.getXWootContentProvider("http://concerto1.devxwiki.com:8080/xwiki/xmlrpc","xwoot1",true,null);
//        this.xwiki22 = XWootContentProviderFactory.getXWootContentProvider("http://concerto2.devxwiki.com:8080/xwiki/xmlrpc","xwoot2",true,null);
//        this.xwiki23 = XWootContentProviderFactory.getXWootContentProvider("http://concerto3.devxwiki.com:8080/xwiki/xmlrpc","xwoot3",true,null);
        
        // b) Use mocks for content providers.
        this.xwiki21 = XWootContentProviderFactory.getXWootContentProvider(null,null,true,null);
        this.xwiki22 = XWootContentProviderFactory.getXWootContentProvider(null,null,true,null);
        this.xwiki23 = XWootContentProviderFactory.getXWootContentProvider(null,null,true,null);

        this.opClock1 = new Clock(WORKINGDIR + File.separator + "Site1" + File.separator + "WootClock");
        this.opClock2 = new Clock(WORKINGDIR + File.separator + "Site2" + File.separator + "WootClock");
        this.opClock3 = new Clock(WORKINGDIR + File.separator + "Site3" + File.separator + "WootClock");

        // 3 peers for 3 xwoots
        this.peer1 = PeerFactory.createMockPeer();
        this.peer1.configureNetwork(this.peer1Name, xwoot1Directory, ConfigMode.EDGE);
        
        this.peer2 = PeerFactory.createMockPeer();
        this.peer2.configureNetwork(this.peer2Name, xwoot2Directory, ConfigMode.EDGE);
        
        this.peer3 = PeerFactory.createMockPeer();
        this.peer3.configureNetwork(this.peer3Name, xwoot3Directory, ConfigMode.EDGE);
        
        //FIXME: use peerID.
        // 3 wootEngines
        this.wootEngine1 =
            new WootEngine(String.valueOf(1), new File(this.xwoot1Directory, "wootEngine").toString(), this.opClock1);
        this.wootEngine2 =
            new WootEngine(String.valueOf(2), new File(this.xwoot2Directory, "wootEngine").toString(), this.opClock2);
        this.wootEngine3 =
            new WootEngine(String.valueOf(3), new File(this.xwoot3Directory, "wootEngine").toString(), this.opClock3);

        // 3 antiEntropy
        this.ae1 = new AntiEntropy(WORKINGDIR + File.separator + "Site1" + File.separator + "ae");
        this.ae2 = new AntiEntropy(WORKINGDIR + File.separator + "Site2" + File.separator + "ae");
        this.ae3 = new AntiEntropy(WORKINGDIR + File.separator + "Site3" + File.separator + "ae");

        // FIXME: use peerID
        // 3 tre
        this.tre1 = new ThomasRuleEngine(String.valueOf(1), WORKINGDIR + File.separator + "Site1" + File.separator + "tre");
        this.tre2 = new ThomasRuleEngine(String.valueOf(2), WORKINGDIR + File.separator + "Site2" + File.separator + "tre");
        this.tre3 = new ThomasRuleEngine(String.valueOf(3), WORKINGDIR + File.separator + "Site3" + File.separator + "tre");

        // 3 xwoot
        
        this.xwoot31 =
            new XWoot3(this.xwiki21, this.wootEngine1, this.peer1, WORKINGDIR + File.separator + "Site1", this.tre1, this.ae1, "Admin", "admin");
        this.xwoot32 =
            new XWoot3(this.xwiki22, this.wootEngine2, this.peer2, WORKINGDIR + File.separator + "Site2", this.tre2, this.ae2, "Admin", "admin");
        this.xwoot33 =
            new XWoot3(this.xwiki23, this.wootEngine3, this.peer3, WORKINGDIR + File.separator + "Site3", this.tre3, this.ae3, "Admin", "admin");
    }

    @BeforeClass
    public static void initFile() throws Exception
    {
        FileUtil.deleteDirectory(WORKINGDIR);
        FileUtil.checkDirectoryPath(WORKINGDIR);
    }
    
    @AfterClass
    public static void cleanDirectory() throws Exception
    {
        FileUtil.deleteDirectory(WORKINGDIR);
    }
}
