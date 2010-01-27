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
package com.xpn.xwiki.watch.it.selenium;

import com.xpn.xwiki.it.selenium.framework.AbstractXWikiTestCase;
import com.xpn.xwiki.it.selenium.framework.AlbatrossSkinExecutor;
import com.xpn.xwiki.it.selenium.framework.XWikiTestSuite;

import junit.framework.Test;

/**
 * Verify the Feed Reader features in Watch
 * 
 * @version $Id$
 */
public class WatchFeedReaderTest extends AbstractXWikiTestCase
{
    public static Test suite()
    {
        XWikiTestSuite suite = new XWikiTestSuite("Verify Feeds Reader in Watch");
        suite.addTestSuite(WatchFeedReaderTest.class, AlbatrossSkinExecutor.class);
        return suite;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
    }

    /**
     * This method makes the following tests :
     * 
     * <ul>
     * <li>Opens the Feeds Reader for XWiki instance.</li>
     * <li>Creates a new Feed - Click configure icon - Add a RSS Feed - Add a RSS Feed URL.</li>
     * <li>Completes the form with details for the newly created feed.</li>
     * <li>Goes back to feeds list and all the articles from the newly created feed.</li>
     * <li>Check if in the article list is present at least one.</li>
     * <li>Check in the status table is the newly created feeds appear.</li>
     * </ul>
     */
    public void testAddRSS()
    {
        open("/xwiki/bin/view/Watch/Reader");
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Welcome to XWiki Watch') != -1;", "2000");
        clickLinkWithXPath("//img[@title='Configure']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Add a RSS Feed') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Add a RSS Feed']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Add a RSS Feed URL') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Add a RSS Feed URL']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Next') != -1;", "10000");
        getSelenium().type("feedname", "internfeed1");
        getSelenium().type("feedurl", " http://localhost:8080/xwiki/bin/view/Main/WebRss?xpage=rdf");
        clickLinkWithXPath("//button[text()='Next']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('All') != -1;", "10000");

        clickLinkWithXPath("//img[@title='Configure']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Add a RSS Feed') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Add a RSS Feed']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Add a RSS Feed URL') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Add a RSS Feed URL']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Next') != -1;", "10000");
        getSelenium().type("feedname", "intern_feed1");
        getSelenium().type("feedurl", " http://localhost:8080/xwiki/bin/view/Main/BlogRss?xpage=rdf");
        clickLinkWithXPath("//button[text()='Next']", false);
        getSelenium()
            .waitForCondition("selenium.isElementPresent(\"//div[@title='intern_feed1']\") != false;", "10000");

        clickLinkWithXPath("//div[@title='All']", false);
        getSelenium()
            .waitForCondition(
                "selenium.isElementPresent(\"//div[@class='watch-articlelist']/div[4]/div[@class='watch-article-headline']/table[@class='watch-article-header']/tbody/tr/td/div[@class='watch-article-title']/div[@class='gwt-HTML']\") != false;",
                "10000");
        // making sure that there is at least 1 element
        assertElementPresent("//div[@class='watch-articlelist']/div[4]/div[@class='watch-article-headline']/table[@class='watch-article-header']/tbody/tr/td/div[@class='watch-article-title']/div[@class='gwt-HTML']");
        // Checking if the newly created feeds appear in status table
        clickLinkWithXPath("//img[@title='Configure']", false);
        getSelenium()
            .waitForCondition("selenium.page().bodyText().indexOf('Check the loading status') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Check the loading status']", false);
        // check if Update active is on
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('internfeed1') != -1;", "10000");
        assertTextPresent("internfeed1");
        assertTextPresent("intern_feed1");
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Cancel') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Cancel']", false);
    }

    /**
     * This method makes the following tests :
     * 
     * <ul>
     * <li>Opens the Feeds Reader for XWiki instance.</li>
     * <li>Checks the status of Feeds Reader.</li>
     * </ul>
     */
    public void testCheckStatus()
    {
        open("/xwiki/bin/view/Watch/Reader");
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Welcome to XWiki Watch') != -1;", "2000");
        clickLinkWithXPath("//img[@title='Configure']", false);
        getSelenium().waitForCondition(
            "selenium.isElementPresent(\"//button[text()='Check the loading status']\") != false;", "10000");
        clickLinkWithXPath("//button[text()='Check the loading status']", false);
        // check if Update active is on
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Update active: Yes') != -1;", "10000");
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Cancel') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Cancel']", false);
    }

    /**
     * This method makes the following tests :
     * 
     * <ul>
     * <li>Opens the Feeds Reader for XWiki instance.</li>
     * <li>Adds a new keyword to all feeds.</li>
     * </ul>
     */
    public void testAddKeyword()
    {
        open("/xwiki/bin/view/Watch/Reader");
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Welcome to XWiki Watch') != -1;", "2000");
        clickLinkWithXPath("//img[@title='Configure']", false);
        getSelenium().waitForCondition("selenium.isElementPresent(\"//button[text()='Add a Keyword']\") != false;",
            "10000");
        clickLinkWithXPath("//button[text()='Add a Keyword']", false);
        getSelenium().waitForCondition("selenium.isElementPresent(\"//button[text()='Next']\")!= false;", "10000");
        setFieldValue("keyword", "wiki");
        clickLinkWithXPath("//button[text()='Next']", false);
        // check if the keyword appears in the keywords list
        getSelenium().waitForCondition("selenium.isElementPresent(\"//div[@title='wiki']\")!= false;", "10000");
    }

    /**
     * This method makes the following tests :
     * 
     * <ul>
     * <li>Opens the Feeds Reader for XWiki instance.</li>
     * <li>Creates a new group of feeds.</li>
     * </ul>
     */
    public void testCreateGroup()
    {
        open("/xwiki/bin/view/Watch/Reader");
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Welcome to XWiki Watch') != -1;", "2000");
        clickLinkWithXPath("//img[@title='Configure']", false);
        getSelenium().waitForCondition("selenium.page().bodyText().indexOf('Create a Group') != -1;", "10000");
        clickLinkWithXPath("//button[text()='Create a Group']", false);
        getSelenium().waitForCondition("selenium.isElementPresent(\"//button[text()='Next']\")!= false;", "10000");
        getSelenium().type("group", "intern_feeds");
        clickLinkWithXPath("//button[text()='Next']", false);
        // Since yet we could not find a workaround to unfold and work with GWT tree widget in Selenium,
        // we currently cannot verify the group has been properly added to the left feed tree,
        // This will have to be reworked when the feed tree will be revamped.
    }

}
