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
import java.util.Random;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.xwiki.escaping.framework.AbstractEscapingTest;
import org.xwiki.escaping.framework.UserInput;
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
    public void testBla()
    {
        System.out.println(name + ": bla");
    }

    @Test
    public void testFailRandomly()
    {
        if (new Random().nextInt(10) == 7) {
            Assert.fail("buh");
        }
    }

    /**
     * 
     * 
     * @param reader
     * @return
     */
    @Override
    protected UserInput parse(Reader reader)
    {
        return new UserInput();
    }
}

