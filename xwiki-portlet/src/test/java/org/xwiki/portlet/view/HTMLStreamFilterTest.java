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
package org.xwiki.portlet.view;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.url.URLRewriter;

/**
 * Unit tests for {@link HTMLStreamFilter}.
 * 
 * @version $Id$
 */
public class HTMLStreamFilterTest extends AbstractStreamFilterTest
{
    /**
     * A mock URL rewriter.
     */
    private URLRewriter mockURLRewriter;

    /**
     * Setup the tests.
     */
    @Before
    public void setUp()
    {
        mockURLRewriter = mockery.mock(URLRewriter.class);
        filter = new HTMLStreamFilter(mockURLRewriter, "x", false);
    }

    /**
     * Tests that element identifiers are name-spaced.
     */
    @Test
    public void testNamespaceId()
    {
        assertFilterOutput("<p id=\"foo\">bar</p>", "<p id=\"x-foo\">bar</p>");
        assertFilterOutput("<label id=\"one\" for=\"two\">Title</label><input id=\"two\"/>",
            "<label id=\"x-one\" for=\"x-two\">Title</label><input id=\"x-two\"/>");
        assertFilterOutput("<table><thead><tr><th headers=\"one two\">Test</th></tr></thead></table>",
            "<table><thead><tr><th headers=\"x-one x-two\">Test</th></tr></thead></table>");
        assertFilterOutput("<div id=\"\">content</div>", "<div id=\"x-\">content</div>");
    }

    /**
     * Tests that anchor's {@code href} attribute is properly rewritten.
     */
    @Test
    public void testRewriteAnchorHref()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("p", (RequestType) null);
                will(returnValue("q"));
            }
        });

        assertFilterOutput("<a href=\"p\">label</a>", "<a href=\"q\">label</a>");
    }

    /**
     * Tests that script's {@code src} attribute is properly rewritten.
     */
    @Test
    public void testRewriteScriptSrc()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("1", RequestType.RESOURCE);
                will(returnValue("2"));
            }
        });

        assertFilterOutput("<script src=\"1\"></script>", "<script src=\"2\"></script>");
    }

    /**
     * Tests that link's {@code href} attribute is properly rewritten.
     */
    @Test
    public void testRewriteLinkHref()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("m", RequestType.RESOURCE);
                will(returnValue("n"));
            }
        });

        String link = "<link href=\"url\"/>";
        assertFilterOutput(link, link);

        assertFilterOutput("<link href=\"m\" rel=\"stylesheet\"/>", "<link href=\"n\" rel=\"stylesheet\"/>");
    }

    /**
     * Tests that form's {@code action} attribute is properly rewritten.
     */
    @Test
    public void testRewriteFormAction()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("v", RequestType.ACTION);
                will(returnValue("w"));
            }
        });

        assertFilterOutput("<form action=\"v\"></form>", "<form action=\"w\"></form>");
    }

    /**
     * Tests that CSS code inside the style element is rewritten.
     */
    @Test
    public void testRewriteCSS()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("i", RequestType.RESOURCE);
                will(returnValue("j"));
            }
        });

        assertFilterOutput("<style>@import url('i');</style>", "<style>@import url(j);</style>");
    }

    /**
     * Tests that JavaScript code inside the script element is rewritten.
     */
    @Test
    public void testRewriteJavaScript()
    {
        assertFilterOutput("<script>$('bar').foo()</script>", "<script>x$('x-bar').foo();\n</script>");
    }

    /**
     * Tests that in-line code filter input is properly reset.
     */
    @Test
    public void testResetInlineCodeFilterInput()
    {
        assertFilterOutput("<script>a++</script>foo<script>a--</script>",
            "<script>xa++;\n</script>foo<script>xa--;\n</script>");
    }

    /**
     * Tests that HTML event attributes (which hold JavaScript code) are rewritten.
     */
    @Test
    public void testRewriteEventAttributes()
    {
        assertFilterOutput("<div onclick=\"doAction(); return false;\">text</div>",
            "<div onclick=\"xdoAction();\n  return false;\n\">text</div>");
    }

    /**
     * Tests that URLs inside IE conditional comments are rewritten.
     */
    @Test
    public void testRewriteURLsInConditionalComments()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("1", RequestType.RESOURCE);
                will(returnValue("1/1"));
                allowing(mockURLRewriter).rewrite("2", RequestType.RESOURCE);
                will(returnValue("2/2"));
            }
        });

        assertFilterOutput("<!--[if IE]><link href=\"1\" rel=\"stylesheet\"/><![endif]-->",
            "<!--[if IE]><link href=\"1/1\" rel=\"stylesheet\"/><![endif]-->");
        assertFilterOutput("<!--[if IE]><link href=\"1\" rel=\"stylesheet\"/>"
            + "<link href=\"2\" rel=\"stylesheet\"/><![endif]-->",
            "<!--[if IE]><link href=\"1/1\" rel=\"stylesheet\"/>"
                + "<link href=\"2/2\" rel=\"stylesheet\"/><![endif]-->");
    }

    /**
     * Tests that URL fragments in anchor URLs relative to the current page are rewritten.
     */
    @Test
    public void testRewriteURLFragment()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("#x-top", (RequestType) null);
                will(returnValue("#x-bottom"));
            }
        });

        assertFilterOutput("<a href=\"#top\">label</a>", "<a href=\"#x-bottom\">label</a>");
    }

    /**
     * Tests the HTML generated when {@code wrapOutput} flag is set.
     */
    @Test
    public void testWrapOutput()
    {
        filter = new HTMLStreamFilter(mockURLRewriter, "z", true);
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("", RequestType.RESOURCE);
                will(returnValue("resource/url"));
            }
        });

        assertFilterOutput("test",
            "<div id=\"z\"><input id=\"z-resourceURL\" type=\"hidden\" value=\"resource/url\"/>test</div>");
    }
}
