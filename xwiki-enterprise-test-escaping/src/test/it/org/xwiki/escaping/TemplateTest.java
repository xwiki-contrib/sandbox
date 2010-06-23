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
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.xwiki.escaping.framework.AbstractEscapingTest;
import org.xwiki.escaping.framework.EscapingException;
import org.xwiki.escaping.suite.ArchiveSuite;
import org.xwiki.escaping.suite.ArchiveSuite.ArchivePathGetter;


/**
 * Runs the automatically generated escaping tests for all velocity templates found in XWiki enterprise WAR file.
 * 
 * @version $Id$
 * @since 2.5
 */
@RunWith(ArchiveSuite.class)
public class TemplateTest extends AbstractEscapingTest
{
    /** Static part of the test URL. */
    private static final String URL_START = "http://127.0.0.1:8080/xwiki/bin/view/";

    /**
     * Get the path to the archive from system properties defined in the maven build configuration.
     * 
     * @return local path to the WAR archive to use
     */
    @ArchivePathGetter
    public static String getArchivePath()
    {
        return System.getProperty("localRepository") + "/" + System.getProperty("pathToXWikiWar");
    }

    /**
     * Create new TemplateTest
     */
    public TemplateTest()
    {
        super(Pattern.compile(".*\\.vm"));
    }

    @Test
    public void testEscaping() throws EscapingException
    {
        String content = getUrlContent(createUrl("Main", "", null, null));
        Assert.assertNotNull("Response is null", content);
        Assert.assertTrue("Not logged in", content.contains("Log-out"));
        System.out.println(name + ": " + userInput);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does some approximate regex matching to find used parameters and other
     * common user-controlled things like user name.</p>
     */
    @Override
    protected Set<String> parse(Reader reader)
    {
        Set<String> input = new HashSet<String>();
        BufferedReader data = new BufferedReader(reader);
        Pattern pattern = Pattern.compile("\\$\\{?request\\.get\\((?:\"|')(\\w+)(?:\"|')\\)|"
                                        + "\\$\\{?request\\.getParameter\\((?:\"|')(\\w+)(?:\"|')\\)|"
                                        + "\\$\\{?request\\.(\\w+)[^(]|"
                                        + "\\b(editor)\\b|"
                                        + "\\b(xredirect)\\b|"
                                        + "\\.(fullName|name)\\b");
        try {
            String line;
            while ((line = data.readLine()) != null) {
                Matcher match = pattern.matcher(line);
                while (match.find()) {
                    for (int i = 1; i <= match.groupCount(); i++) {
                        input.add(match.group(i));
                    }
                }
            }
        } catch (IOException exception) {
            // ignore, use what was already found
        }
        return input;
    }

    /**
     * Create a target URL from given parameters, adding the template name. URL-escapes everything.
     * 
     * @param space space name to use, "Main" is used if null
     * @param page page name to use, "WebHome" is used if null
     * @param parameter parameter name to add, omitted if null or empty string
     * @param value parameter value, empty string is used if null
     * @return the resulting absolute URL
     */
    private String createUrl(String space, String page, String parameter, String value)
    {
        String url = URL_START + escapeUrl(space) + "/" + escapeUrl(page) + "?xpage=" + escapeUrl(name);
        if (parameter != null && !parameter.equals("")) {
            url += "&" + escapeUrl(parameter) + "=" + (value == null ? "" : escapeUrl(value));
        }
        return url;
    }
}

