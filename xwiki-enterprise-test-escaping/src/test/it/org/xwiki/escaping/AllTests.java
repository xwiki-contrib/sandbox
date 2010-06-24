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
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

import org.xwiki.test.XWikiExecutor;

/**
 * Parent test suite that runs all functional escaping tests. Starts XWiki server before other tests
 * and stops it afterwards.
 * 
 * TODO
 * 2. implement some manual test (copy requiring 2 parameters is a good example)
 * 3. also test applications
 * 4. check that the fixed templates are fixed
 * 5. add over-escaping test
 * 6. add a way to specify which action to use with some parameters
 * 7. test for escaping of action
 * 8. add variable names that are bound to request parameters to the parsing regex (might need to use another URL)
 * 9. add a multilanguage test (switch to multilanguage, test language escaping everywhere, switch back)
 * 
 * @version $Id$
 * @since 2.5
 */
@RunWith(ClasspathSuite.class)
public class AllTests
{
    /** Executes XWiki server. */
    private static XWikiExecutor executor;

    /**
     * Start XWiki server.
     * 
     * @throws Exception on errors
     */
    @BeforeClass
    public static void init() throws Exception
    {
        AllTests.executor = new XWikiExecutor(0);
        AllTests.executor.start();
    }

    /**
     * Stop XWiki server.
     * 
     * @throws Exception on errors
     */
    @AfterClass
    public static void shutdown() throws Exception
    {
        AllTests.executor.stop();
    }
}
