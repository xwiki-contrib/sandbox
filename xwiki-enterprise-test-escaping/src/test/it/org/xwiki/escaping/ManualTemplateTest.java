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

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;
import org.xwiki.escaping.framework.AbstractEscapingTest;
import org.xwiki.escaping.framework.XMLEscapingValidator;


/**
 * Runs additional escaping tests that need more complex manual setup. These tests are missed by the
 * automatic test builder.
 * 
 * @version $Id$
 * @since 2.5
 */
public class ManualTemplateTest extends AbstractEscapingTest
{
    /**
     * Create new ManualTemplateTest, needed for JUnit.
     */
    public ManualTemplateTest()
    {
        super(Pattern.compile(".*\\.vm"));
    }

    @Test
    public void testEditReflectedXSS()
    {
        if (!initialize("XWIKI-4758", null)) {
            return;
        }
        checkUnderEscaping(createUrl("edit", "Main", XMLEscapingValidator.getTestString(), null), "page");
    }

    /*
        HashMap<String, String> params = getParamsFor("copy", "targetdoc", "bla");
        params.put(parameter, XMLEscapingValidator.getTestString());
        String url = createUrl(null, null, null, params);
        checkUnderEscaping(url, "\"" + parameter + "\"");
     */

    @Test
    public void testErrorTraceEscaping()
    {
        if (!initialize("XWIKI-5170", null)) {
            return;
        }
        checkUnderEscaping(createUrl("viewrev", "Main", "WebHome", getTestParams("rev")), "error trace");
    }

    @Test
    public void testEditorEscaping()
    {
        if (!initialize("XWIKI-5164", null)) {
            return;
        }
        // tests for XWIKI-5164, XML symbols in editor parameter should be escaped
        checkUnderEscaping(createUrl("edit", "Main", "Page", getTestParams("editor")), "editor");
        checkUnderEscaping(createUrl("edit", "Main", "Page", getTestParams("editor", "wysiwyg", "section")), "section");
        checkUnderEscaping(createUrl("edit", "Main", "Page", getTestParams("editor", "wiki", "x-maximized")),
            "x-maximized");
    }

    @Test
    public void testAdminEditor()
    {
        if (!initialize("XWIKI-5190", null)) {
            return;
        }
        checkUnderEscaping(createUrl("admin", "XWiki", "AdminSheet", getTestParams("editor")), "admin editor");
        // same page after redirect
        checkUnderEscaping(createUrl("view", "Main", "WebHome", getTestParams("xpage", "admin", "editor")),
            "admin editor redirect");
    }

    @Test
    public void testAdminSection()
    {
        if (!initialize("XWIKI-5190", null)) {
            return;
        }
        // kind of covered (only the redirect version)
        checkUnderEscaping(createUrl("admin", "XWiki", "AdminSheet", getTestParams("section")), "admin section");
        // same page after redirect
        checkUnderEscaping(createUrl("view", "Main", "WebHome", getTestParams("xpage", "admin", "section")),
            "admin section redirect");
    }

    @Test
    public void testAttachmentsInline()
    {
        if (!initialize("XWIKI-5191", null)) {
            return;
        }
        // need a page with attachments, Sandbox has an image attached by default
        checkUnderEscaping(createUrl("view", "Sandbox", "WebHome", getTestParams("viewer", "attachments", "xredirect")),
            "attachments inline");
    }

    @Test
    public void testBrowseWysiwygSQL()
    {
        if (!initialize("XWIKI-5193", null)) {
            return;
        }
        // FIXME check for SQL escaping (i.e. additionally check for \ and ;)
        checkUnderEscaping(createUrl("view", "Sandbox", "WebHome", getTestParams("xpage", "browsewysiwyg", "text")),
            "browsewysiwyg sql");
        // TODO check that there is no error on the page
        // Assert.assertTrue(getDriver().findElements(By.xpath("//pre[@class='xwikierror']")).isEmpty());
    }

    @Test
    public void testBrowseWysiwygPage()
    {
        // also covers former testBrowseWysiwygPageLink()
        if (!initialize("XWIKI-5193", null)) {
            return;
        }
        // FIXME need an existing page with name = title = test string
        // createPage("Main", test, test, "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", "Test", getParamsFor("browsewysiwyg", null, null)),
            "browsewysiwyg");
    }

    @Test
    public void testWysiwygRecentViewsPage()
    {
        if (!initialize("XWIKI-5193", null)) {
            return;
        }
        // FIXME need an existing page with name = title = test string
        // createPage("Main", test, test, "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", "Test", getParamsFor("recentdocwysiwyg", null, null)),
            "wysiwyg recent docs");
    }

    @Test
    public void testSearchWysiwygSQL()
    {
        if (!initialize("XWIKI-5344", null)) {
            return;
        }
        // FIXME check for SQL escaping (i.e. additionally check for \ and ;)
        checkUnderEscaping(createUrl("view", "Main", "Test", getTestParams("xpage", "searchwysiwyg", "space")),
            "searchwysiwyg sql space");
        checkUnderEscaping(createUrl("view", "Main", "Test", getTestParams("xpage", "searchwysiwyg", "page")),
            "searchwysiwyg sql page");
        // TODO check that there is no error on the page
        // Assert.assertTrue(getDriver().findElements(By.xpath("//pre[@class='xwikierror']")).isEmpty());
    }

    @Test
    public void testSearchWysiwygPageLink()
    {
        if (!initialize("XWIKI-5344", null)) {
            return;
        }
        // FIXME need an existing page with name = title = test string
        // createPage("Main", test, test, "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", "Test", getParamsFor("searchwysiwyg", null, null)),
            "searchwysiwyg");
    }

    @Test
    public void testLoginRedirect()
    {
        if (!initialize("login redirect", null)) {
            return;
        }
        // FIXME need to be logged off
        // getUtil().setSession(null);
        checkUnderEscaping(createUrl("login", "XWiki", "XWikiLogin", getTestParams("xredirect")),
            "login redirect");
    }

    @Test
    public void testLoginSrid()
    {
        if (!initialize("login srid", null)) {
            return;
        }
        // FIXME need to be logged off
        // getUtil().setSession(null);
        checkUnderEscaping(createUrl("login", "XWiki", "XWikiLogin", getTestParams("srid")),
            "login srid");
    }

/*
    // allready covered
    // XWIKI-5205
    // XWIKI-5209
    // XWIKI-5193 (space)
    // XWIKI-5344 (space)
    // XWIKI-5204
    // testImported()

    // too complicated to cover
    // testVersionSummary()
 */

    @Test
    public void testEditActions()
    {
        if (!initialize("edit comment", null)) {
            return;
        }
        // FIXME need an existing page with name = title = test string
        // createPage("Main", test, test, "Bla bla");
        checkUnderEscaping(createUrl("edit", "Main", "WebHome", getTestParams("editor", "wiki", "comment")),
            "edit comment");
    }

    @Test
    public void testCopySourcedoc()
    {
        testCopy("sourcedoc");
    }

    @Test
    public void testCopyLanguage()
    {
        testCopy("language");
    }

    @Test
    public void testCopyExistingPage()
    {
        if (!initialize("XWIKI-5206", null)) {
            return;
        }
        // FIXME need an existing page with name = test string
        // createPage("Main", test, "", "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", XMLEscapingValidator.getTestString(),
            getTestParams("xpage", "copy", null)), "copy existing");
    }

    /**
     * Run escaping tests for copy.vm.
     * 
     * @param parameter parameter to test
     */
    private void testCopy(String parameter)
    {
        if (!initialize("templates/copy.vm", null)) {
            return;
        }
        // XWIKI-5206
        // copy.vm does not display the form if targetdoc is not set
        HashMap<String, String> params = getParamsFor("copy", "targetdoc", XMLEscapingValidator.getTestString());
        params.put(parameter, XMLEscapingValidator.getTestString());
        String url = createUrl(null, null, null, params);
        checkUnderEscaping(url, "\"" + parameter + "\"");
    }

    @Test
    public void testRename()
    {
        if (!initialize("templates/rename.vm", null)) {
            return;
        }
        // rename.vm is only used with step=2, otherwise renameStep1.vm is used
        for (String parameter : userInput) {
            HashMap<String, String> params = getParamsFor("rename", "step", "2");
            // HTTP 400 is returned if newPageName is empty, 409 if the new page exist
            if (!params.containsKey("newPageName")) {
                // TODO cleanup
                params.put("newPageName", "testRename" + System.nanoTime());
            }
            params.put(parameter, XMLEscapingValidator.getTestString());
            String url = createUrl(null, null, null, params);
            checkUnderEscaping(url, "\"" + parameter + "\"");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * The file reader is not used in manual tests, we just return a fixed set of parameters.
     * 
     * @see org.xwiki.escaping.TemplateTest#parse(java.io.Reader)
     */
    @Override
    protected Set<String> parse(Reader reader)
    {
        Set<String> parameters = new HashSet<String>();
        parameters.add("language");
        parameters.add("sourcedoc");
        parameters.add("targetdoc");
        parameters.add("newPageName");
        parameters.add("newSpaceName");
        parameters.add("parameterNames");
        return parameters;
    }

    /**
     * Create a parameter map for the given template and one optional parameter.
     * 
     * @param template template name
     * @param parameter parameter name, ignored if null
     * @param value value of the parameter
     * @return new parameter map
     */
    private HashMap<String, String> getParamsFor(String template, String parameter, String value)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("xpage", template);
        if (parameter != null) {
            params.put(parameter, value);
        }
        return params;
    }

    /**
     * Create a parameter map containing one parameter with the value set to the test string.
     * 
     * @param parameter parameter name
     * @return new parameter map
     */
    private HashMap<String, String> getTestParams(String parameter)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(parameter, XMLEscapingValidator.getTestString());
        return params;
    }

    /**
     * Create a parameter map containing two parameters, one with the given value and the second with the value set to
     * the test string.
     * 
     * @param parameter parameter name
     * @param value value of the parameter
     * @param testedParameter name of the tested parameter, its value will be the test string
     * @return new parameter map
     */
    private HashMap<String, String> getTestParams(String parameter, String value, String testedParameter)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(parameter, XMLEscapingValidator.getTestString());
        return params;
    }
}

