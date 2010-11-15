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
package org.xwiki.model.internal;

import org.jmock.Expectations;
import org.junit.*;
import org.xwiki.model.Document;
import org.xwiki.model.Server;
import org.xwiki.model.Wiki;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class BridgeServerTest extends AbstractBridgedComponentTestCase
{
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        final XWiki xwiki = getMockery().mock(XWiki.class);
        getContext().setWiki(xwiki);
    }

    @Test
    public void testGetReferenceForDocument() throws Exception
    {
        final DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getDocument(documentReference, getContext());
                will(returnValue(new XWikiDocument(documentReference)));
        }});

        Server server = new BridgedServer(getContext());
        Document doc = server.getEntity(documentReference);
        Assert.assertNotNull(doc);
    }

    @Test
    public void testGetReferenceForWiki() throws Exception
    {
        final WikiReference wikiReference = new WikiReference("wiki");

        Server server = new BridgedServer(getContext());
        Wiki wiki = server.getEntity(wikiReference);
        Assert.assertNotNull(wiki);
    }
}
