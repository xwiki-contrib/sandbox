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
package org.xwoot.contentprovider;

import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Attachment;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.contentprovider.Constants;
import org.xwoot.contentprovider.Utils;
import org.xwoot.contentprovider.XWootContentProvider;
import org.xwoot.contentprovider.XWootContentProviderException;
import org.xwoot.contentprovider.XWootId;
import org.xwoot.contentprovider.XWootObject;

public class XWootContentProviderTest extends TestCase
{
    private XWootContentProvider xwc;

    @Override
    protected void setUp() throws Exception
    {
        System.out.format("*****************************\n");
        xwc = new XWootContentProvider("http://localhost:8080/xwiki/xmlrpc/confluence", "DB", true, null);
        xwc.login("Admin", "admin");
    }

    @Override
    protected void tearDown() throws Exception
    {
        xwc.logout();
        xwc.dispose();
        System.out.format("*****************************\n\n");
    }

    public void testGetModifiedPagesIds() throws XWootContentProviderException
    {
        System.out.format("*** testGetModifiedPagesIds()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("Total modifications: %d\n", result.size());
        assertTrue(result.size() > 0);
    }

    public void testClearModification() throws XWootContentProviderException
    {
        System.out.format("*** testClearModification()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();

        XWootId xwootId = (XWootId) result.toArray()[0];
        
        xwc.clearModification(xwootId);
        Set<XWootId> resultAfter = xwc.getModifiedPagesIds();

        System.out.format("Total modifications before: %d\n", result.size());
        System.out.format(" Total modifications after: %d\n", resultAfter.size());

        assertEquals(resultAfter.size() + 1, result.size());
    }

    public void testClearAllModifications() throws XWootContentProviderException
    {
        System.out.format("*** testClearAllModification()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        xwc.clearAllModifications();
        Set<XWootId> resultAfter = xwc.getModifiedPagesIds();

        System.out.format("Total modifications before: %d\n", result.size());
        System.out.format(" Total modifications after: %d\n", resultAfter.size());

        assertEquals(0, resultAfter.size());
    }

    public void testPageModification() throws Exception
    {
        final String pageName = "Main.WebHome";
        final String content = String.format("Modified at %s\n", System.currentTimeMillis());

        System.out.format("*** testPageModification()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();

        XWikiXmlRpcClient rpc = xwc.getRpc();
        XWikiPage page = rpc.getPage(pageName);
        page.setContent(content);
        page = rpc.storePage(page);

        Set<XWootId> result = xwc.getModifiedPagesIds();

        assertEquals(1, result.size());
        XWootId xwootId = (XWootId) result.toArray()[0];
        System.out.format("Modification: %s\n", xwootId);
        assertEquals(pageName, xwootId.getPageId());

        /*
         * Set the last cleared modification to the previous one so that the getModifiedEntities will have a version to
         * which compare the differences
         */
        xwc.getStateManager().clearModification(xwc.getStateManager().getPreviousModification(xwootId));
        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(xwootId);
        assertTrue(modifiedEntities.size() >= 1);
        XWootObject xwootObject = modifiedEntities.get(0);
        System.out.format("%s\n", xwootObject);
        assertTrue(xwootObject.getGuid().startsWith(Constants.PAGE_NAMESPACE));
        assertFalse(xwootObject.isNewlyCreated());
        assertTrue(xwootObject.getFieldValue("content").equals(content));
    }

    public void testNewPageModification() throws XWootContentProviderException, XmlRpcException
    {
        final String pageName = String.format("Test.%d", System.currentTimeMillis());
        final String content = String.format("Modified at %s\n", System.currentTimeMillis());

        System.out.format("*** testNewPageModification()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();

        XWikiXmlRpcClient rpc = xwc.getRpc();
        XWikiPage page = new XWikiPage();
        page.setId(pageName);
        page.setContent(content);
        rpc.storePage(page);

        page = rpc.getPage(pageName);
        assertEquals(pageName, page.getId());
        assertEquals(content, page.getContent());

        xwc.getStateManager().dumpDbLines();

        Set<XWootId> result = xwc.getModifiedPagesIds();

        xwc.getStateManager().dumpDbLines();

        System.out.format("************** Result: %s\n", result);
        assertEquals(1, result.size());
        XWootId xwootId = (XWootId) result.toArray()[0];
        System.out.format("Modification: %s\n", xwootId);
        assertEquals(pageName, xwootId.getPageId());

        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(xwootId);
        assertTrue(modifiedEntities.size() == 1);
        XWootObject xwootObject = modifiedEntities.get(0);
        System.out.format("%s\n", xwootObject);
        assertTrue(xwootObject.getGuid().startsWith(Constants.PAGE_NAMESPACE));
        assertTrue(xwootObject.isNewlyCreated());
        assertTrue(xwootObject.getFieldValue("content").equals(content));
    }

    public void testStore() throws XWootContentProviderException, XmlRpcException
    {
        final String content = String.format("Modified by XWoot at %s\n", System.currentTimeMillis());

        XWikiXmlRpcClient rpc = xwc.getRpc();

        System.out.format("*** testStore()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();

        XWikiPage page = rpc.getPage("Main.WebHome");
        XWootObject xwootObject = Utils.xwikiPageToXWootObject(page, false);
        xwootObject.setFieldValue("content", content);

        xwc.store(xwootObject, null);
        page = rpc.getPage("Main.WebHome");
        System.out.format("XWiki page stored by XWoot. At version %d.%d\n", page.getVersion(), page.getMinorVersion());

        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("%s\n", result);
        assertEquals(0, result.size());
    }

    /* WARNING: This test works only if the optimized version of the updateModifiedPages is used in getModifiedPagesIds */
    public void testMultipleModifications() throws Exception
    {
        XWikiXmlRpcClient rpc = xwc.getRpc();

        System.out.format("*** testMultipleModifications()\n");
        XWikiPage page = rpc.getPage("Main.WebHome");
        page.setContent(String.format("%d\n", System.currentTimeMillis()));
        page = rpc.storePage(page);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        page.setContent(String.format("%d\n", System.currentTimeMillis()));
        page = rpc.storePage(page);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        page.setContent(String.format("%d\n", System.currentTimeMillis()));
        page = rpc.storePage(page);

        xwc.getModifiedPagesIds();
        List<XWootId> result = xwc.getStateManager().getModificationsFor("Main.WebHome", false);
        System.out.format("Not cleared items for Main.WebHome: %s\n", result);

        assertEquals(1, result.size());
        assertEquals(page.getVersion(), result.get(0).getVersion());
        assertEquals(page.getMinorVersion(), result.get(0).getMinorVersion());

        System.out.format("*****************************\n\n");
    }
    
    public void testAttachments() throws Exception {
        XWikiXmlRpcClient rpc = xwc.getRpc();
        Random random = new Random();
        
        System.out.format("*** testAttachments()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();
        
        String attachmentName = String.format("test_attachment_%d.png", random.nextInt());
        byte[] data = (new String("This is a test").getBytes());
        Attachment attachment = new Attachment();
        attachment.setPageId("Main.WebHome");
        attachment.setFileName(attachmentName);
        attachment = rpc.addAttachment(0, attachment, data);
        
        System.out.format("%s\n", attachment);
        
        Set<XWootId> modifiedPagesIds = xwc.getModifiedPagesIds();
        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(modifiedPagesIds.iterator().next());
        System.out.format("%s\n%s\n", modifiedPagesIds, modifiedEntities);
        
        assertTrue(modifiedPagesIds.size() == 1);
        
        XWootObject attachmentObject = null;
        for(XWootObject xwo : modifiedEntities) {
            if(xwo.getGuid().startsWith(Constants.ATTACHMENT_NAMESPACE)) {
                attachmentObject = xwo;
            }
        }
                
        assertEquals(attachmentName, attachmentObject.getFieldValue("fileName"));
    }

}
