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

package org.xwiki.xoo.xwikilib.cleaner.internal;

import junit.framework.TestCase;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.xoo.xwikilib.XWikiComponentContext;
import org.xwiki.xoo.xwikilib.cleaner.internal.DefaultXOOHTMLCleaner;

/**
 * Tests the default HTML cleaner.
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class TestDefaultXOOHTMLCleaner extends TestCase
{

    private DefaultXOOHTMLCleaner cleaner;

    public TestDefaultXOOHTMLCleaner()
    {

        XWikiComponentContext xcmp = new XWikiComponentContext();
        xcmp.initializeComponentContext();
        EmbeddableComponentManager ecm = xcmp.getComponentManager();
        cleaner = new DefaultXOOHTMLCleaner(ecm);
    }

    public void testClean()
    {

        String input = "<HTML><P>test1</P><BR></BR></HTML>";
        String expected =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html><head></head><body><p>test1</p><div class=\"wikimodel-emptyline\"></div></body></html>\n";

        String result = cleaner.clean(input);
        assertEquals(result, expected);
    }
}
