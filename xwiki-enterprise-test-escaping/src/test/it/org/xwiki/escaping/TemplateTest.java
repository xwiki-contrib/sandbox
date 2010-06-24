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

import java.util.HashMap;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.escaping.framework.AbstractVelocityEscapingTest;
import org.xwiki.escaping.framework.XMLEscapingValidator;
import org.xwiki.escaping.suite.ArchiveSuite;
import org.xwiki.escaping.suite.ArchiveSuite.ArchivePathGetter;


/**
 * Runs automatically generated escaping tests for all velocity templates found in XWiki Enterprise WAR file.
 * <p>
 * The following configuration properties are supported (set in maven):
 * <ul>
 * <li>localRepository: Path to maven repository, where XWiki files can be found</li>
 * <li>pathToXWikiWar: Used to read all templates</li>
 * </ul></p>
 * 
 * @version $Id$
 * @since 2.5
 */
@RunWith(ArchiveSuite.class)
public class TemplateTest extends AbstractVelocityEscapingTest
{
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
     * Create new TemplateTest for all *.vm files.
     */
    public TemplateTest()
    {
        super(Pattern.compile(".*\\.vm"));
    }

    @Test
    public void testSpaceEscaping()
    {
        // space name
        String url = createUrl(XMLEscapingValidator.getTestString(), null, null, "");
        checkUnderEscaping(url, "space name");
    }

    @Test
    public void testPageEscaping()
    {
        // page name
        String url = createUrl("Main", XMLEscapingValidator.getTestString(), null, "");
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
     * Create a target URL from given parameters, adding the template name.
     * 
     * @param space space name to use, "Main" is used if null
     * @param page page name to use, "WebHome" is used if null
     * @param parameter parameter name to add, omitted if null or empty string
     * @param value parameter value, empty string is used if null
     * @return the resulting absolute URL
     * @see #createUrl(String, String, String, java.util.Map)
     */
    protected String createUrl(String space, String page, String parameter, String value)
    {
        String template = name.replaceAll("^.+/", "");
        String skin = "default";
        if (name.startsWith("skins")) {
            skin = name.replaceFirst("^\\w+/", "").replaceAll("/.+$", "");
        }
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("skin", skin);
        if ("xpart".equals(parameter)) {
            // xpart=something must be tested differently
            parameters.put("xpage", template).replaceAll("\\.\\w+$", "");
        } else {
            // this variant initializes some commonly used variables
            parameters.put("xpage", "xpart");
            parameters.put("vm", template);
        }
        if (parameter != null) {
            parameters.put(parameter, value);
        }
        return createUrl(null, space, page, parameters);
    }
}

