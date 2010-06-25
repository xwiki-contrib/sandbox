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
 * Runs automatically generated escaping tests for all XWiki documents found in XWiki Enterprise XAR file.
 * <p>
 * The following configuration properties are supported (set in maven):
 * <ul>
 * <li>localRepository: Path to maven repository, where XWiki files can be found</li>
 * <li>pathToXWikiXar: Used to read all documents</li>
 * </ul></p>
 * 
 * @version $Id$
 * @since 2.5
 */
@RunWith(ArchiveSuite.class)
public class ApplicationTest extends AbstractVelocityEscapingTest
{
    /**
     * Get the path to the archive from system properties defined in the maven build configuration.
     * 
     * @return local path to the XAR archive to use
     */
    @ArchivePathGetter
    public static String getArchivePath()
    {
        return System.getProperty("localRepository") + "/" + System.getProperty("pathToXWikiXar");
    }

    /**
     * Create new ApplicationTest for all *.xml files in subdirectories.
     */
    public ApplicationTest()
    {
        super(Pattern.compile(".+/.+\\.xml"));
    }

    @Test
    public void testParametersInColibri()
    {
        // NOTE: results in other skins seem to be the same
        testParameterEscaping("colibri");
    }

    /**
     * Run the tests for all parameters for given skin.
     * 
     * @param skin skin to use
     */
    private void testParameterEscaping(String skin)
    {
        String space = name.replaceAll("/.+$", "");
        String page = name.replaceAll("^.+/", "").replaceAll("\\..+$", "");

        // all found parameters
        // TODO need to also consider parameters from page header templates bound to variables
        for (String parameter : userInput) {
            String url = createUrl(space, page, parameter, XMLEscapingValidator.getTestString(), skin);
            checkUnderEscaping(url, "\"" + parameter + "\"");
        }
    }

    /**
     * Convenience method to create the target URL with one parameter.
     * 
     * @param space space name to use
     * @param page page name to use
     * @param parameter parameter name to add, omitted if null or empty string
     * @param value parameter value, empty string is used if null
     * @param skin skin name to use
     * @return the resulting absolute URL
     * @see #createUrl(String, String, String, java.util.Map)
     */
    protected String createUrl(String space, String page, String parameter, String value, String skin)
    {
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (skin != null) {
            parameters.put("skin", skin);
        }
        if (parameter != null) {
            parameters.put(parameter, value);
        }
        return createUrl(null, space, page, parameters);
    }
}

