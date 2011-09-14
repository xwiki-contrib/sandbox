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

import java.net.URL;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.Document;
import org.xwiki.model.Server;
import org.xwiki.model.UniqueReference;
import org.xwiki.model.Wiki;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class BridgedEntityManagerTest extends AbstractBridgedComponentTestCase
{
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        final XWiki xwiki = getMockery().mock(XWiki.class);
        getContext().setWiki(xwiki);
    }

    @Test
    public void getEntityForDocumentThatExists() throws Exception
    {
        final DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        final XWikiDocument xdoc = new XWikiDocument(documentReference);
        xdoc.setNew(false);
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getDocument(documentReference, getContext());
                will(returnValue(xdoc));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Document doc = em.getEntity(new UniqueReference(documentReference));
        Assert.assertNotNull(doc);
    }

    @Test
    public void getEntityForDocumentThatDoesntExist() throws Exception
    {
        final DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        final XWikiDocument xdoc = new XWikiDocument(documentReference);
        xdoc.setNew(true);
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getDocument(documentReference, getContext());
                will(returnValue(xdoc));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Document doc = em.getEntity(new UniqueReference(documentReference));
        Assert.assertNull(doc);
    }

    @Test
    public void getEntityForWikiThatExists() throws Exception
    {
        final WikiReference wikiReference = new WikiReference("wiki");
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getServerURL("wiki", getContext());
                will(returnValue(new URL("http://whatever/not/null")));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Wiki wiki = em.getEntity(new UniqueReference(wikiReference));
        Assert.assertNotNull(wiki);
    }

    @Test
    public void getEntityForWikiThatDoesntExists() throws Exception
    {
        final WikiReference wikiReference = new WikiReference("wiki");
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getServerURL("wiki", getContext());
                will(returnValue(null));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Wiki wiki = em.getEntity(new UniqueReference(wikiReference));
        Assert.assertNull(wiki);
    }

    @Test
    public void hasEntityForDocumentThatExists() throws Exception
    {
        final DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).exists(documentReference, getContext());
                will(returnValue(true));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Assert.assertTrue(em.hasEntity(new UniqueReference(documentReference)));
    }

    @Test
    public void hasEntityForWikiThatExists() throws Exception
    {
        final WikiReference wikiReference = new WikiReference("wiki");
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getServerURL("wiki", getContext());
                will(returnValue(new URL("http://whatever/not/null")));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Assert.assertTrue(em.hasEntity(new UniqueReference(wikiReference)));
    }

    @Test
    public void hasEntityForWikiThatDoesntExist() throws Exception
    {
        final WikiReference wikiReference = new WikiReference("wiki");
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getServerURL("wiki", getContext());
                will(returnValue(null));
        }});

        BridgedEntityManager em = new BridgedEntityManager(getContext());
        Assert.assertFalse(em.hasEntity(new UniqueReference(wikiReference)));
    }
}
