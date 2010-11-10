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

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JavaScriptStreamFilter}.
 * 
 * @version $Id$
 */
public class JavaScriptStreamFilterTest extends AbstractStreamFilterTest
{
    /**
     * Setup the tests.
     */
    @Before
    public void setUp()
    {
        filter = new JavaScriptStreamFilter("x");
    }

    /**
     * Tests that HTML element identifiers are name-spaced.
     */
    @Test
    public void testNamespaceHTMLElementIdentifiers()
    {
        assertFilterOutput("$('foo').value=7;", "$('x-foo').value = 7;\n");
        assertFilterOutput("var x=ID('foo')", "var x = ID('x-foo');\n");
    }

    /**
     * Tests that a variable declaration AST node is properly decompiled.
     * 
     * @see https://bugzilla.mozilla.org/show_bug.cgi?id=491621
     * @see https://sourceforge.net/tracker/?func=detail&atid=448266&aid=3105264&group_id=47038
     */
    @Test
    public void testDecompileVariableDeclaration()
    {
        assertFilterOutput("var x=0;x++;", "var x = 0;\nx++;\n");
        assertFilterOutput("for(var i=0;i<10;i++)x[i]=i;a++;", "for (var i = 0; i < 10; i++) \n  x[i] = i;\na++;\n");
        assertFilterOutput("var a;if(true)a=1;", "var a;\nif (true) \na = 1;\n");
        assertFilterOutput("switch(x){case 1:var y;z++}", "switch (x) {\n  case 1:\n    var y;\n    z++;\n}\n");
        assertFilterOutput("for(var p in o)s+=o[p]", "for (var p in o) \n  s += o[p];\n");
        assertFilterOutput("if(c)var a=0;else a=1", "if (c) \nvar a = 0; else a = 1;\n");
        assertFilterOutput("for(var i=0;i<10;i++)var x=i;x++;", "for (var i = 0; i < 10; i++) \n  var x = i;\nx++;\n");
    }

    /**
     * Tests special JavaScript identifier names are properly parsed.
     */
    @Test
    public void testSpecialIdentifierName()
    {
        // "float" has been removed from the list of reserved keywords.
        assertFilterOutput("style.float='left'", "style.float = 'left';\n");
    }

    /**
     * Tests that the void operator is properly serialized into JavaScript source code.
     */
    @Test
    public void testVoidOperator()
    {
        assertFilterOutput("x=void f()", "x = void f();\n");
    }
}
