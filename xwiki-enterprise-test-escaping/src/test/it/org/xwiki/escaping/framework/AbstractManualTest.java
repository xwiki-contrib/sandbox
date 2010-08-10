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
package org.xwiki.escaping.framework;

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

import org.junit.After;

/**
 * Abstract base class for manual tests. Provides several helper methods and default implementation for less likely
 * needed superclass methods.
 * 
 * @version $Id$
 * @since 2.5
 */
public abstract class AbstractManualTest extends AbstractEscapingTest
{
    /**
     * List of URLs that should be deleted in {@link #tearDown()}.
     * @see #createPage(String, String, String, String)
     */
    private List<String> toDeleteURLs = new LinkedList<String>();

    /**
     * Create new {@link AbstractManualTest}. JUnit needs a default constructor, we provide a default implementation
     * that accepts all file names.
     */
    public AbstractManualTest()
    {
        super(Pattern.compile(".*"));
    }

    /**
     * {@inheritDoc}
     * 
     * Parsing should not be needed in manual tests, we return an empty set.
     * 
     * @see org.xwiki.escaping.framework.AbstractEscapingTest#parse(java.io.Reader)
     */
    @Override
    protected Set<String> parse(Reader reader)
    {
        return new HashSet<String>();
    }

    /**
     * Create a parameter map for the given template and one optional parameter.
     * 
     * @param template template name
     * @param parameter parameter name, ignored if null
     * @param value value of the parameter
     * @return new parameter map
     */
    protected Map<String, String> getParamsFor(String template, String parameter, String value)
    {
        return params(kv("xpage", template), kv(parameter, value));
    }

    /**
     * Create a parameter map containing one parameter with the value set to the test string.
     * 
     * @param parameter parameter name
     * @return new parameter map
     */
    protected Map<String, String> getTestParams(String parameter)
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
    protected Map<String, String> getTestParams(String parameter, String value, String testedParameter)
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
    protected void checkForErrorTrace(String url) throws IOException
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
    protected void createPage(String space, String page, String title, String content)
    {
        // create
        String url = createUrl("save", space, page, params(kv("title", title),
                                                           kv("content", content),
                                                           kv("action_save", "Save+%26+View")));
        AbstractEscapingTest.getUrlContent(url);
        // schedule for deletion
        deleteAfterwards(space, page);
    }

    /**
     * Schedule a page for deletion in {@link #tearDown()}.
     * 
     * @param space space name
     * @param page page name
     */
    protected void deleteAfterwards(String space, String page)
    {
        this.toDeleteURLs.add(createUrl("delete", space, page, params(kv("confirm", "1"))));
    }

    /**
     * Create a parameter map from the given list of key-value pairs. Properly URL-escapes values.
     * 
     * @param keysAndValues list of key-value pairs
     * @return parameter map
     * @see AbstractManualTest#kv(String, String)
     * @see AbstractManualTest#test(String)
     */
    protected static Map<String, String> params(String[]... keysAndValues)
    {
        Map<String, String> params = new HashMap<String, String>();
        for (String[] kv : keysAndValues) {
            if (kv != null && kv.length > 0 && kv[0] != null) {
                String key = kv[0];
                String value = "";
                if (kv.length > 1 && kv[1] != null) {
                    value = escapeUrl(kv[1]);
                }
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * Create one key-value pair with the given key name and value.
     * 
     * @param key key name
     * @param value unescaped value
     * @return a key-value pair
     * @see AbstractManualTest#params(String[][])
     * @see AbstractManualTest#test(String)
     */
    protected static String[] kv(String key, String value)
    {
        return new String[] {key, value};
    }

    /**
     * Create one key-value pair with the given key name and value set to the test string.
     * 
     * @param key key name
     * @return a key-value-pair
     * @see AbstractManualTest#params(String[][])
     * @see AbstractManualTest#kv(String, String)
     */
    protected static String[] test(String key)
    {
        return kv(key, XMLEscapingValidator.getTestString());
    }

    /**
     * Clean up.
     */
    @After
    public void tearDown()
    {
        // delete all created pages
        for (String url : this.toDeleteURLs) {
            AbstractEscapingTest.getUrlContent(url);
        }
    }
}
