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

package org.xwiki.xoo.xwikilib.converter.internal;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.xoo.xwikilib.XWikiComponentContext;
import org.xwiki.xoo.xwikilib.converter.internal.DefaultBidirectionalConverter;
import org.xwiki.xoo.xwikilib.cleaner.internal.DefaultXOOHTMLCleaner;

import junit.framework.TestCase;

/**
 * Tests the default bidirectional converter xhtml - XWiki syntax .
 * 
 * @version $Id: $
 * @since 1.0 M
 */

public class TestDefaultBidirectionalConverter extends TestCase
{
    private DefaultBidirectionalConverter converter;

    private DefaultXOOHTMLCleaner cleaner;

    public TestDefaultBidirectionalConverter()
    {

        XWikiComponentContext xcmp = new XWikiComponentContext();
        xcmp.initializeComponentContext();
        EmbeddableComponentManager ecm = xcmp.getComponentManager();
        converter = new DefaultBidirectionalConverter(ecm);
        cleaner = new DefaultXOOHTMLCleaner(ecm);

    }

    public void testFromXHTML()
    {

        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html><head></head><body><p>test1</p><div class=\"wikimodel-emptyline\"></div></body></html>\n";

        String expected = "test1\n";
        String result = null;
        try {
            result = converter.fromXHTML(input);
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        assertEquals(result, expected);

    }

    public void testToXHTML()
    {

        String input = "test1\n";
        String expected = "<p>test1</p>";
        String result = null;
        try {
            result = converter.toXHTML(input);
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        assertEquals(result, expected);

    }

    public void testCleanAndConvert()
    {

        String input =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" + "<HTML><HEAD>"
                + "<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=utf-8\">" + "<TITLE></TITLE>"
                + "<META NAME=\"GENERATOR\" CONTENT=\"OpenOffice.org 3.0  (Linux)\">"
                + "<META NAME=\"CREATED\" CONTENT=\"0;0\">" + "<META NAME=\"CHANGED\" CONTENT=\"20090627;12430300\">"
                + "</HEAD>" + "<BODY LANG=\"en-US\" DIR=\"LTR\">"
                + "<UL> <LI><P STYLE=\"margin-bottom: 0in\">list1</P><UL><LI><P>list 2</P></UL></UL>" + "<P>test</P>"
                + "</BODY></HTML>";
        String expected = "* list1\n** list 2\n\ntest";
        String result = null;
        try {
            result = converter.fromXHTML(cleaner.clean(input));
        } catch (ConversionException e) {
            e.printStackTrace();
        }

        assertEquals(result, expected);
    }
}
