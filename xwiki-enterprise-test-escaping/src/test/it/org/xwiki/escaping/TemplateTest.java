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
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.xwiki.escaping.framework.AbstractEscapingTest;
import org.xwiki.escaping.framework.EscapingError;
import org.xwiki.escaping.framework.XMLEscapingValidator;
import org.xwiki.escaping.suite.ArchiveSuite;
import org.xwiki.escaping.suite.ArchiveSuite.ArchivePathGetter;
import org.xwiki.validator.ValidationError;


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
     * Create new TemplateTest.
     */
    public TemplateTest()
    {
        super(Pattern.compile(".*\\.vm"));
    }

    @Test
    public void testSpaceEscaping()
    {
        // space name
        String url = createUrl(XMLEscapingValidator.getTestString(), null, null, null);
        checkUnderEscaping(url, "space name");
    }

    @Test
    public void testPageEscaping()
    {
        // page name
        String url = createUrl("Main", XMLEscapingValidator.getTestString(), null, null);
        checkUnderEscaping(url, "page name");
    }

    @Test
    public void testParameterEscaping()
    {
        // all found parameters
        for (String parameter : userInput) {
            String url = createUrl("Main", null, parameter, XMLEscapingValidator.getTestString());
            checkUnderEscaping(url, "\"" + parameter + "\"");
        }
    }

    /**
     * Check for unescaped data in the given {@code content}.
     * 
     * @param url URL used in the test
     * @param description description of the test
     */
    private void checkUnderEscaping(String url, String description)
    {
        InputStream content = getUrlContent(url);
        String where = "  Template: " + name + "\n  URL: " + url;
        Assert.assertNotNull("Response is null\n" + where, content);
        XMLEscapingValidator validator = new XMLEscapingValidator();
        validator.setDocument(content);
        List<ValidationError> errors;
        try {
            errors = validator.validate();
        } catch (EscapingError error) {
            // most probably false positive, generate an error instead of failing the test
            throw new RuntimeException(EscapingError.formatMessage(error.getMessage(), name, url, null));
        }
        if (!errors.isEmpty()) {
            throw new EscapingError("Escaping test failed.", name, url, errors);
        }
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
        // TODO match if user name, space name or action is used
        Set<String> input = new HashSet<String>();
        BufferedReader data = new BufferedReader(reader);
        Pattern pattern = Pattern.compile("\\$\\{?request\\.get\\((?:\"|')(\\w+)(?:\"|')\\)|"
                                        + "\\$\\{?request\\.getParameter\\((?:\"|')(\\w+)(?:\"|')\\)|"
                                        + "\\$\\{?request\\.(\\w+)[^(a-zA-Z_0-9]|"
                                        + "\\b(editor)\\b|"
                                        + "\\b(viewer)\\b|"
                                        + "\\b(section)\\b|"
                                        + "\\b(template)\\b|"
                                        + "\\b(xredirect)\\b|"
                                        + "\\b(x-maximized)\\b|"
                                        + "\\b(xnotification)\\b|"
                                        + "\\b(classname)\\b|"
                                        + "\\b(comment)\\b|"
                                        + "\\b(rev1)\\b|"
                                        + "\\b(rev2)\\b|"
                                        + "\\b(sourcedoc)\\b|"
                                        + "\\b(targetdoc)\\b|"
                                        + "\\b(srid)\\b|"
                                        + "\\b(language)\\b");
        try {
            String line;
            while ((line = data.readLine()) != null) {
                Matcher match = pattern.matcher(line);
                while (match.find()) {
                    for (int i = 1; i < match.groupCount(); i++) {
                        String parameter = match.group(i);
                        if (parameter != null && !parameter.matches("\\s*")) {
                            input.add(parameter);
                        }
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
        String template = name.replaceAll("^.+/", "").replaceAll("\\.\\w+$", "");
        if (space == null) {
            space = "Main";
        }
        if (page == null) {
            page = "WebHome";
        }
        String url = URL_START + escapeUrl(space) + "/" + escapeUrl(page) + "?xpage=" + escapeUrl(template);
        if (parameter != null && !parameter.equals("")) {
            url += "&" + escapeUrl(parameter) + "=" + (value == null ? "" : escapeUrl(value));
        }
        return url;
    }
}

