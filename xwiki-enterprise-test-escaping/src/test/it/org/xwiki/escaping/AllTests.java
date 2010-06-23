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
 * Parent test suite that runs all functional escaping tests.
 * We start and stop XWiki here.
 * 
 * TODO
 * 3. add some basic escaping checks
 * 4. implement file excludes
 * 5. implement some manual test (copy requiring 2 parameters is a good example)
 * 6. add more sophisticated escaping tests -> parsing
 * 7. add over-escaping test
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
     */
    @BeforeClass
    public static void init() throws Exception
    {
        AllTests.executor = new XWikiExecutor(0);
        AllTests.executor.start();
    }

    @AfterClass
    public static void shutdown() throws Exception
    {
        AllTests.executor.stop();
    }
}
