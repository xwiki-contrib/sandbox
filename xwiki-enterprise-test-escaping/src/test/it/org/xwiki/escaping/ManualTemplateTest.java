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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Before;
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
    /** List of URLs that should be deleted in {@link #tearDown()}.
     *  @see #createPage(String, String, String, String) */
    private List<String> toDeleteURLs = new LinkedList<String>();

    /**
     * Create new ManualTemplateTest, needed for JUnit.
     */
    public ManualTemplateTest()
    {
        super(Pattern.compile(".*"));
    }

    // already covered
    // XWIKI-5205
    // XWIKI-5209
    // XWIKI-5193 (space)
    // XWIKI-5344 (space)
    // XWIKI-5204
    // testImported()

    // too complicated to cover
    // testVersionSummary()

    @Test
    public void testEditReflectedXSS()
    {
        if (!initialize("XWIKI-5190", null)) {
            return;
        }
        checkUnderEscaping(createUrl("edit", "Main", XMLEscapingValidator.getTestString(), null), "XWIKI-4758");
    }

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
    public void testBrowseWysiwygSQL() throws IOException
    {
        if (!initialize("XWIKI-5193", null)) {
            return;
        }
        // TODO check for SQL escaping (i.e. additionally put \ and ;)
        String url = createUrl("view", "Sandbox", "WebHome", getTestParams("xpage", "browsewysiwyg", "text"));
        checkUnderEscaping(url, "browsewysiwyg sql");
        checkForErrorTrace(url);
    }

    @Test
    public void testBrowseWysiwygPage()
    {
        // also covers former testBrowseWysiwygPageLink()
        if (!initialize("XWIKI-5193", null)) {
            return;
        }
        // need an existing page with name = title = test string
        createPage("Main", XMLEscapingValidator.getTestString(), XMLEscapingValidator.getTestString(), "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", "Test", getParamsFor("browsewysiwyg", null, null)),
            "browsewysiwyg");
    }

    @Test
    public void testWysiwygRecentViewsPage()
    {
        if (!initialize("XWIKI-5193", null)) {
            return;
        }
        // need an existing page with name = title = test string
        createPage("Main", XMLEscapingValidator.getTestString(), XMLEscapingValidator.getTestString(), "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", "Test", getParamsFor("recentdocwysiwyg", null, null)),
            "wysiwyg recent docs");
    }

    @Test
    public void testSearchWysiwygSQL() throws IOException
    {
        if (!initialize("XWIKI-5344", null)) {
            return;
        }
        // TODO check for SQL escaping (i.e. additionally put \ and ;)
        String spaceUrl = createUrl("view", "Main", "Test", getTestParams("xpage", "searchwysiwyg", "space"));
        checkUnderEscaping(spaceUrl, "searchwysiwyg sql space");
        checkForErrorTrace(spaceUrl);

        String pageUrl = createUrl("view", "Main", "Test", getTestParams("xpage", "searchwysiwyg", "page"));
        checkUnderEscaping(pageUrl, "searchwysiwyg sql page");
        checkForErrorTrace(pageUrl);
    }

    @Test
    public void testSearchWysiwygPageLink()
    {
        if (!initialize("XWIKI-5344", null)) {
            return;
        }
        // need an existing page with name = title = test string
        createPage("Main", XMLEscapingValidator.getTestString(), XMLEscapingValidator.getTestString(), "Bla bla");
        checkUnderEscaping(createUrl("view", "Main", "Test", getParamsFor("searchwysiwyg", null, null)),
            "searchwysiwyg");
    }

    @Test
    public void testLoginRedirect()
    {
        if (!initialize("login redirect", null)) {
            return;
        }
        // need to be logged off
        setLoggedIn(false);
        try {
            checkUnderEscaping(createUrl("login", "XWiki", "XWikiLogin", getTestParams("xredirect")),
                "login redirect");
        } finally {
            setLoggedIn(true);
        }
    }

    @Test
    public void testLoginSrid()
    {
        if (!initialize("login srid", null)) {
            return;
        }
        // need to be logged off
        setLoggedIn(false);
        try {
            checkUnderEscaping(createUrl("login", "XWiki", "XWikiLogin", getTestParams("srid")),
                "login srid");
        } finally {
            setLoggedIn(true);
        }
    }

    @Test
    public void testEditActions()
    {
        if (!initialize("edit comment", null)) {
            return;
        }
        // need an existing page with name = title = test string
        createPage("Main", XMLEscapingValidator.getTestString(), XMLEscapingValidator.getTestString(), "Bla bla");
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
        // need an existing page with name = test string
        createPage("Main", XMLEscapingValidator.getTestString(), "", "Bla bla");
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
                String page = "testRename" + System.nanoTime();
                params.put("newPageName", page);
                // the above may create a page, schedule for deletion
                this.toDeleteURLs.add(createUrl("delete", null, page, getTestParams("confirm", "1", null)));
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
        params.put(parameter, value);
        if (testedParameter != null) {
            params.put(testedParameter, XMLEscapingValidator.getTestString());
        }
        return params;
    }

    /**
     * Check that there is no error trace on the given URL.
     * TODO do not download the same URL twice (usually {@link #checkUnderEscaping(String, String)} is also used)
     * 
     * @param url the URL to download
     * @throws IOException on connection errors
     */
    private void checkForErrorTrace(String url) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(AbstractEscapingTest.getUrlContent(url)));
        String line;
        while ((line = reader.readLine()) != null) {
            Assert.assertFalse("The page contains a error trace", line.matches("^.*<pre\\s+class=\"xwikierror\">.*$"));
        }
    }

    /**
     * Create a page with the given data. This page is automatically deleted in {@link #tearDown()}.
     * 
     * @param space space name
     * @param page page name
     * @param title document title
     * @param content document content
     */
    private void createPage(String space, String page, String title, String content)
    {
        // create
        Map<String, String> params = getTestParams("title", title, null);
        params.put("content", content);
        params.put("action_save", "Save+%26+View");
        String url = createUrl("save", space, page, params);
        AbstractEscapingTest.getUrlContent(url);
        // schedule for deletion
        this.toDeleteURLs.add(createUrl("delete", space, page, getTestParams("confirm", "1", null)));
    }

    /**
     * Clean up.
     */
    @Before
    public void tearDown()
    {
        // delete all created pages
        for (String url : this.toDeleteURLs) {
            AbstractEscapingTest.getUrlContent(url);
        }
    }
}

