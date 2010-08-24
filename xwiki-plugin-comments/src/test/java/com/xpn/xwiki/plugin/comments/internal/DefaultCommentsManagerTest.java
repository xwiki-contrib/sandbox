/**
 * 
 */
package com.xpn.xwiki.plugin.comments.internal;

import com.xpn.xwiki.plugin.comments.XWikiMockTestCase;

/**
 * @author Florin
 *
 */
public class DefaultCommentsManagerTest extends AbstractCommentsManagerTest
{

    /* (non-Javadoc)
     * @see com.xpn.xwiki.plugin.comments.XWikiMockTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        commentsmanager = new DefaultCommentsManager();
    }

    /* (non-Javadoc)
     * @see com.xpn.xwiki.plugin.comments.XWikiMockTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

}
