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
package org.xwiki.escaping;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwiki.escaping.framework.AbstractEscapingTest;
import org.xwiki.escaping.framework.AbstractManualTest;
import org.xwiki.escaping.framework.XMLEscapingValidator;



/**
 * Manual tests for the object editor and class editor. Some of these tests need an existing class with specific field
 * names.
 * 
 * @version $Id$
 * @since 2.5
 */
public class ObjectAndClassEditorTest extends AbstractManualTest
{
    /**
     * Set up the test class.
     */
    @BeforeClass
    public static void init()
    {
        // the tests need a special class and a document containing objects of that class, we put both into the same doc
        String test = XMLEscapingValidator.getTestString();
        // create a new class
        AbstractEscapingTest.getUrlContent(createUrl("propadd", test, test, params(kv("propname", "test"),
                                                                                   kv("proptype", "com.xpn.xwiki.objects.classes.StringClass"))));
        // add a new property to that class
        AbstractEscapingTest.getUrlContent(createUrl("propupdate", test, test, params(kv("test_disabled", "0"),
                                                                                      kv("test_name", "test"),
                                                                                      kv("test_prettyName", test),
                                                                                      kv("test_validationRegExp", test),
                                                                                      kv("test_validationMessage", test),
                                                                                      kv("test_number", "1"),
                                                                                      kv("test_size", "30"),
                                                                                      kv("test_picker", "0"),
                                                                                      kv("xeditaction", "edit"))));
        // add a new object of that class to the class document
        AbstractEscapingTest.getUrlContent(createUrl("view", test, test, params(kv("xpage", "editobject"),
                                                                                kv("xaction", "addObject"),
                                                                                kv("className", test + "." + test))));
        // set the property of that object
        AbstractEscapingTest.getUrlContent(createUrl("save", test, test, params(kv("classname", "-"),
                                                                                kv(escapeUrl(test + "." + test + "_0_test"), test),
                                                                                kv("xeditaction", "edit"))));
    }

    /**
     * Clean up after the tests.
     */
    @AfterClass
    public static void shutdown()
    {
        // delete the created class
        String test = XMLEscapingValidator.getTestString();
        AbstractEscapingTest.getUrlContent(createUrl("delete", test, test, params(kv("confirm", "1"))));
    }

    @Test
    public void testClassEditor()
    {
        skipIfIgnored("templates/editclass.vm");
        String test = XMLEscapingValidator.getTestString();
        checkUnderEscaping(createUrl("edit", test, test, params(kv("editor", "class"))), "XWIKI-5404");
    }

    @Test
    public void testObjectEditor()
    {
        skipIfIgnored("templates/editobject.vm");
        String test = XMLEscapingValidator.getTestString();
        checkUnderEscaping(createUrl("edit", test, test, params(kv("editor", "object"))), "XWIKI-5242");
    }
}

