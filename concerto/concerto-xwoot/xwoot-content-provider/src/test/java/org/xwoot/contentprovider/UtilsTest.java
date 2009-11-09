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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Attachment;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.contentprovider.Utils;
import org.xwoot.contentprovider.XWootContentProviderConfiguration;
import org.xwoot.contentprovider.XWootObject;

public class UtilsTest extends TestCase
{
    protected XWikiXmlRpcClient rpc;

    protected Random random;

    protected XWootContentProviderConfiguration configuration;

    public void setUp() throws Exception
    {
        rpc = new XWikiXmlRpcClient(TestConstants.ENDPOINT);
        rpc.login(TestConstants.USERNAME, TestConstants.PASSWORD);
        random = new Random();
        configuration = new XWootContentProviderConfiguration();
    }

    public void tearDown() throws Exception
    {
        rpc.logout();
        rpc = null;
    }

    public void testXWikiObjectToXWootObject() throws XmlRpcException
    {
        List<XWikiObjectSummary> objectSummaries = rpc.getObjects("Main.WebHome");

        for (XWikiObjectSummary objectSummary : objectSummaries) {
            XWikiObject object = rpc.getObject("Main.WebHome", objectSummary.getGuid());

            XWootObject xwo = Utils.xwikiObjectToXWootObject(object, false, configuration);

            System.out.format("%s\n", xwo);
        }
    }

    public void testXWootObjectToXWikiPageAndViceversa() throws XmlRpcException
    {
        String content = String.format("This is a modified content %d\n", random.nextInt());

        XWikiPage page = rpc.getPage("Main.WebHome");

        XWootObject xwootObject = Utils.xwikiPageToXWootObject(page, false);
        System.out.format("%s\n", xwootObject);
        xwootObject.setFieldValue("content", content);

        page = Utils.xwootObjectToXWikiPage(xwootObject);
        rpc.storePage(page);

        page = rpc.getPage("Main.WebHome");

        assertTrue(content.equals(page.getContent()));
    }

    public void testXWootObjectToXWikiObjectAndViceversa() throws XmlRpcException
    {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        String value = Utils.listToString(list, Utils.LIST_CONVERSION_SEPARATOR);

        XWikiObject object = rpc.getObject("Main.WebHome", "XWiki.TagClass", 0);
        XWootObject xwootObject = Utils.xwikiObjectToXWootObject(object, false, configuration);

        xwootObject.setFieldValue("tags", value); 

        object = Utils.xwootObjectToXWikiObject(xwootObject);

        rpc.storeObject(object);

        object = rpc.getObject("Main.WebHome", "XWiki.TagClass", 0);

        assertTrue(list.equals(object.getProperty("tags")));
    }

    public void testWootableListConversion()
    {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        XWikiObject object = new XWikiObject();
        object.setClassName("XWiki.TagClass");
        object.setProperty("tags", list);

        XWootObject xwootObject = Utils.xwikiObjectToXWootObject(object, false, configuration);
        object = Utils.xwootObjectToXWikiObject(xwootObject);

        assertTrue(List.class.isAssignableFrom(object.getProperty("tags").getClass()));
    }
    
    public void testAttachmentToXWootObjectAndViceversa() {
        final String PAGE_ID = "Main.WebHome";
        final String FILENAME = "test.png";
        final String DATA_STRING = "THIS IS A TEST";
        
        Attachment attachment = new Attachment();
        attachment.setPageId(PAGE_ID);
        attachment.setFileName(FILENAME);
        byte[] data = DATA_STRING.getBytes();
        
        XWootObject xwootObject = Utils.attachmentToXWootObject(attachment, 0, 0, data, false);
        attachment = Utils.xwootObjectToAttachment(xwootObject);
        data = Utils.xwootObjectToAttachmentData(xwootObject);
        
        assertEquals(PAGE_ID, attachment.getPageId());
        assertEquals(FILENAME, attachment.getFileName());
        assertEquals(DATA_STRING, new String(data));
    }
}
