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
import org.junit.*;
import org.xwiki.model.*;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class BridgedServerTest extends AbstractBridgedComponentTestCase
{
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        final XWiki xwiki = getMockery().mock(XWiki.class);
        getContext().setWiki(xwiki);
    }

    @Test
    public void addWiki() throws Exception
    {
        getMockery().checking(new Expectations() {{
            oneOf(getContext().getWiki()).getServerURL("wiki", getContext());
                will(returnValue(new URL("http://whatever/not/null")));
        }});

        Server server = new BridgedServer(getContext(), new BridgedEntityManager(getContext()));
        Wiki wiki = server.addWiki("wiki");
        Assert.assertSame(wiki, server.getWiki("wiki"));
    }
}
