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
package org.xwoot.xwootUtil.test;

import org.junit.After;
import org.junit.Before;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Common behavior for xwootUtil tests.
 * <p>
 * Just add tests to subclasses.
 * 
 * @version $Id$
 */
public class AbstractXwootUtilTestBase
{
    /** Working directory for tests. */
    protected final String workingDir = FileUtil.getTestsWorkingDirectoryPathForModule("xwootUtil");

    /**
     * Initializes the working directory.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void initWorkingDir() throws Exception
    {
        FileUtil.checkDirectoryPath(this.workingDir);
    }

    /**
     * Clears the tests directory.
     * 
     * @throws Exception if problems occur.
     */
    @After
    public void clearWorkingDir() throws Exception
    {
        FileUtil.deleteDirectory(this.workingDir);
    }
}
