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
 * Unit tests for {@link CSSStreamFilter}.
 * 
 * @version $Id$
 */
public class CSSStreamFilterTest extends AbstractStreamFilterTest
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
        filter = new CSSStreamFilter("x", mockURLRewriter);
    }

    /**
     * Tests that URLs in import rules are rewritten.
     */
    @Test
    public void testRewriteImportRule()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("/style.css", RequestType.RESOURCE);
                will(returnValue("z/style.css"));
            }
        });

        assertFilterOutput("@import url('/style.css');", "@import url(z/style.css);");
    }

    /**
     * Tests that URLs in import rules are rewritten when media types are specified.
     */
    @Test
    public void testRewriteImportRuleWithMediaTypes()
    {
        mockery.checking(new Expectations()
        {
            {
                allowing(mockURLRewriter).rewrite("/bar.css", RequestType.RESOURCE);
                will(returnValue("foo/bar.css"));
            }
        });

        assertFilterOutput("@import url('/bar.css') projection, tv;", "@import url(foo/bar.css) projection, tv;");
    }

    /**
     * Tests how a simple selector is rewritten.
     */
    @Test
    public void testRewriteSimpleSelector()
    {
        assertFilterOutput(".foo{display:block}", "*#x *.foo { display: block }");
    }

    /**
     * Tests how a composite selector is rewritten.
     */
    @Test
    public void testRewriteCompositeSelector()
    {
        assertFilterOutput(".red,a:hover{color:red}", "*#x *.red, *#x a:hover { color: red }");
    }

    /**
     * Tests how an id selector is rewritten.
     */
    @Test
    public void testRewriteIdSelector()
    {
        assertFilterOutput("#y{color:blue}", "*#x *#x-y { color: blue }");
    }

    /**
     * Tests how selectors that include the HTML element are rewritten.
     */
    @Test
    public void testRewriteHTMLElementSelector()
    {
        assertFilterOutput("html{width:100%}", "html *#x { width: 100% }");
        assertFilterOutput("* html .foo{width:100%}", "* html *#x *.foo { width: 100% }");
        assertFilterOutput("*+html a{width:100%}", "* + html *#x a { width: 100% }");
        // Note: only HTML selectors that are followed by element, id or class selectors are correctly name-spaced.
        assertFilterOutput("html>foo{width:100%}", "*#x html > foo { width: 100% }");
    }

    /**
     * Tests how selectors that include the BODY element are rewritten.
     */
    @Test
    public void testRewriteBodyElementSelector()
    {
        assertFilterOutput("body em{float:left}", "*#x *#x-body em { float: left }");
        assertFilterOutput(".body-left{float:left}", "*#x *.body-left { float: left }");
        assertFilterOutput("body#left{float:left}", "*#x *#x-left { float: left }");
    }

    /**
     * Tests how selectors that include both the HTML and the BODY element are rewritten.
     */
    @Test
    public void testRewriteHTMLAndBodyElementSelectors()
    {
        assertFilterOutput("html body{color:yellow}", "html *#x *#x-body { color: yellow }");
        // Note: only HTML selectors that are followed by element, id or class selectors are correctly name-spaced.
        assertFilterOutput("html>body{color:yellow}", "*#x html > *#x-body { color: yellow }");
    }

    /**
     * Tests if filter CSS property is parsed when its value contains a colon.
     * 
     * @see http://sourceforge.net/tracker/?func=detail&atid=448266&aid=3106039&group_id=47038
     */
    @Test
    public void testParseFilterProperty()
    {
        assertFilterOutput(".foo{filter:\"progid:DXImageTransform.Microsoft.gradient"
            + "(startColorStr='#FFFFFF',EndColorStr='#FAFAFA')\";"
            + "-ms-filter:\"progid:DXImageTransform.Microsoft.gradient"
            + "(startColorStr='#FFFFFF',EndColorStr='#FAFAFA')\";}",
            "*#x *.foo { filter: \"progid:DXImageTransform.Microsoft.gradient"
                + "(startColorStr='#FFFFFF',EndColorStr='#FAFAFA')\"; "
                + "-ms-filter: \"progid:DXImageTransform.Microsoft.gradient"
                + "(startColorStr='#FFFFFF',EndColorStr='#FAFAFA')\" }");
    }

    /**
     * Tests how selectors that include pseudo-elements like :after are rewritten.
     */
    @Test
    public void testRewritePseudoElementSelector()
    {
        assertFilterOutput("a:after{content:'?'}", "*#x a:after { content: \"?\" }");
    }
}
