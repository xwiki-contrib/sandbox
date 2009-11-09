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

package org.xwiki.xoo.xwikilib;

import java.io.File;

/**
 * Keeps track of file system storage for XOO.
 * 
 * @version $Id$
 * @since 1.0
 */
public class XOOFileStorage
{
    private String sXOODirName = "xoo";

    /**
     * Top-level temporary working directory.
     */
    private File tempDir;

    /**
     * Output file (html).
     */
    private File outputFile = null;

    /**
     * Some characters are not allowed as file names under the windows platform. Following regular expression is used to
     * match such characters.
     */
    public static final String INVALID_FILE_NAME_CHARS = "[/\\\\:\\*\\?\"<>|.]";

    /**
     * Default constructor.
     * 
     * @param tempDirName name of the temporary files directory.
     */
    public XOOFileStorage(String tempDirName, String outputFileName)
    {
        File xooDir = new File(getDefaultTmpDir(), sXOODirName);
        if (!xooDir.exists())
            xooDir.mkdir();
        tempDir = new File(getDefaultXOOTmpDir(), tempDirName.replaceAll(INVALID_FILE_NAME_CHARS, "-"));
        tempDir.mkdir();
        outputFile = new File(tempDir, outputFileName + ".html");
    }

    /**
     * Cleans up the allocated file storage.
     */
    public void cleanUp()
    {
        File[] outputFiles = tempDir.listFiles();
        for (File file : outputFiles) {
            file.delete();
        }
        tempDir.delete();
    }

    /**
     * @return the top level temporary directory.
     */
    public File getTempDir()
    {
        return tempDir;
    }

    /**
     * @return the main html output file for the conversion.
     */
    public File getOutputFile()
    {
        return outputFile;
    }

    private String getDefaultTmpDir()
    {
        return System.getProperty("java.io.tmpdir");
    }

    private String getDefaultXOOTmpDir()
    {
        return System.getProperty("java.io.tmpdir") + File.separator + sXOODirName;
    }
}
