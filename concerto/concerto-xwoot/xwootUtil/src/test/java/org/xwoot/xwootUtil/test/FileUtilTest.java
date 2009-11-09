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

import org.junit.Test;
import org.xwoot.xwootUtil.FileUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Random;

import junit.framework.Assert;

/**
 * Tests for the utility class FileUtil.
 * 
 * @version $Id:$
 */
public class FileUtilTest extends AbstractXwootUtilTestBase
{
    /** New line char. */
    public static final String NEW_LINE = System.getProperty("line.separator");

    /** Test file name. */
    private static final String FILE_NAME = "tempFile";

    /** Test dir name. */
    private static final String DIR_NAME = "tempDir";

    /** Content to use for tests as source. */
    private static final String SOURCE_CONTENT = "first line\nsecond line\nthird line\n";

    /** Name of source file. */
    private static final String SOURCE_FILE_NAME = "source";

    /** Name of destination file. */
    private static final String DESTINATION_FILE_NAME = "destination";

    /** Test string containing characters with accents. */
    // private static final String ACCENTUATED_STRING
    // = "áàâãäăÁÀÂÃÄéèêëÉÈÊËíìîïÍÌÎÏóòôõöÓÒÔÕÖúùûüÚÙÛÜçÇñÑşŞţŢ";
    private static final String ACCENTUATED_STRING =
        "\u00E1\u00E0\u00E2\u00E3\u00E4\u0103\u00C1\u00C0\u00C2"
            + "\u00C3\u00C4\u00E9\u00E8\u00EA\u00EB\u00C9\u00C8\u00CA\u00CB\u00ED\u00EC\u00EE\u00EF"
            + "\u00CD\u00CC\u00CE\u00CF\u00F3\u00F2\u00F4\u00F5\u00F6\u00D3\u00D2\u00D4\u00D5\u00D6"
            + "\u00FA\u00F9\u00FB\u00FC\u00DA\u00D9\u00DB\u00DC\u00E7\u00C7\u00F1\u00D1\u015F\u015E\u0163\u0162";

    /** Test string containing the accentuatedString and some extra non-ASCII chars. */
    // private static final String DIRTY_STRING = "قذر" + ACCENTUATED_STRING + "أشياء";
    private static final String DIRTY_STRING =
        "\u0642\u0630\u0631" + ACCENTUATED_STRING + "\u0623\u0634\u064A\u0627\u0621";

    /** The clean version of the accentuatedString containing only ASCII characters. */
    private static final String CLEAN_STRING = "aaaaaaAAAAAeeeeEEEEiiiiIIIIoooooOOOOOuuuuUUUUcCnNsStT";

    /**
     * Exhaustively test the getEncodedFileName and getDecodedFileName methods by generating random strings.
     * <p>
     * Result: The result of decoding a previously encoded string must be equal with the original string.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testBrutForceFileName() throws Exception
    {
        for (int i = 0; i < 50000; i++) {
            Random r = new Random();
            Random r2 = new Random();
            byte[] tab = new byte[r2.nextInt(100)];
            r.nextBytes(tab);

            String filename = new String(tab);
            String encodedFilename = FileUtil.getEncodedFileName(filename);
            String decodedFileName = FileUtil.getDecodedFileName(encodedFilename);

            Assert.assertTrue(Arrays.equals(filename.getBytes(), decodedFileName.getBytes()));
        }
    }

    /**
     * Test decoding an encoded string and checking it with the original.
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testFileName() throws Exception
    {
        String filename = "Test file name";
        String encodedFilename = FileUtil.getEncodedFileName(filename);
        String decodedFileName = FileUtil.getDecodedFileName(encodedFilename);
        Assert.assertEquals(filename, decodedFileName);
    }

    /**
     * Test copying a file from a location to another.
     * <p>
     * Result: The two files will be identical.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCopyFile() throws Exception
    {
        String sourceFileName = SOURCE_FILE_NAME;
        String destinationFileName = DESTINATION_FILE_NAME;

        File sourceFile = new File(this.workingDir, sourceFileName);
        File destinationFile = new File(this.workingDir, destinationFileName);

        FileOutputStream fos = new FileOutputStream(sourceFile);

        fos.write(SOURCE_CONTENT.getBytes());
        fos.close();

        FileUtil.copyFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());

        BufferedReader br = new BufferedReader(new FileReader(destinationFile));

        String destinationFileContent = "";
        String line = null;
        while ((line = br.readLine()) != null) {
            destinationFileContent += line + NEW_LINE;
        }

        Assert.assertEquals(SOURCE_CONTENT, destinationFileContent);
    }

    /**
     * Test for the copyInputStream method.
     * <p>
     * Result: the content of the output stream is equal to the source content.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCopyInputStream() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(SOURCE_CONTENT.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        FileUtil.copyInputStream(bais, baos);

        baos.flush();
        baos.close();

        Assert.assertEquals(SOURCE_CONTENT, baos.toString());
    }

    /**
     * Test the deleteDirectory method.
     * <p>
     * Result: A directory's content will be recursively deleted.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testDeleteDirectory() throws Exception
    {
        File directoryToDelete = new File(this.workingDir, DIR_NAME);

        // create empty dir
        directoryToDelete.mkdir();
        Assert.assertTrue(directoryToDelete.exists() && directoryToDelete.isDirectory());

        // delete empty dir.
        FileUtil.deleteDirectory(directoryToDelete);
        Assert.assertFalse(directoryToDelete.exists());

        // create non-empty directory with files and non-empty subdirs.
        directoryToDelete.mkdir();
        int numberOfFilesOrDirs = 10;
        // create dir with files and subdirs containing subdir-files.
        for (int i = 0; i < numberOfFilesOrDirs; i++) {
            File tempFile = new File(directoryToDelete, FILE_NAME + i);
            tempFile.createNewFile();

            File tempSubDir = new File(directoryToDelete, DIR_NAME + i);
            tempSubDir.mkdir();

            File tempSubDirFile = new File(tempSubDir, FILE_NAME + i);
            tempSubDirFile.createNewFile();
        }

        // dir now contains numberOfFilesOrDirs files and numberOfFilesOrDirs subdirs.
        Assert.assertEquals(numberOfFilesOrDirs * 2, directoryToDelete.list().length);

        // recursively delete non-empty dir.
        FileUtil.deleteDirectory(directoryToDelete);
        Assert.assertFalse(directoryToDelete.exists());

        // delete non-existing directory.
        FileUtil.deleteDirectory(directoryToDelete);
        Assert.assertFalse(directoryToDelete.exists());
    }

    /**
     * Test the removeAccents and nomalizeName metods.
     * <p>
     * Result: the resulting as described in {@link FileUtil#removeAccents(String)} and
     * {@link FileUtil#normalizeName(String)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCleanFileName() throws Exception
    {
        Assert.assertEquals(CLEAN_STRING, FileUtil.removeAccents(ACCENTUATED_STRING));
        Assert.assertEquals(CLEAN_STRING, FileUtil.normalizeName(DIRTY_STRING));
    }

    /**
     * Test the zipDirectory and the unzipDirectory methods.
     * <p>
     * Result: after zipping a directory, the unzipped content of the resulting zip file will be identical to the
     * original directory's content.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testZipUnzipDirectory() throws Exception
    {
        File directoryToZip = new File(this.workingDir, DIR_NAME);

        // create directory with files.
        directoryToZip.mkdir();
        int numberOfFiles = 10;
        for (int i = 0; i < numberOfFiles; i++) {
            File tempFile = new File(directoryToZip, FILE_NAME + i);
            tempFile.createNewFile();
        }

        // check the directory's content.
        Assert.assertEquals(numberOfFiles, directoryToZip.list().length);

        // zip the directory and check that the file was created and it's not emtpy.
        File zippedFile = new File(FileUtil.zipDirectory(directoryToZip.getPath()));
        Assert.assertTrue(zippedFile.length() > 0);

        // check if the output directory does not already exist.
        File directoryToUnzipTo = new File(this.workingDir, DIR_NAME + "Unzipped");
        Assert.assertFalse(directoryToUnzipTo.exists());

        // unzip the file and check if the output dir was created.
        FileUtil.unzipInDirectory(zippedFile.getPath(), directoryToUnzipTo.getPath());
        Assert.assertTrue(directoryToUnzipTo.exists());

        // check if the zip was propperly deflated in the output directory.
        Assert.assertEquals(numberOfFiles, directoryToUnzipTo.list().length);
        
        // clear the temp zip file.
        zippedFile.delete();
    }

    /**
     * Test the checkDirectoryPath method.
     * <p>
     * Restul: as described by {@link FileUtil#checkDirectoryPath(String)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCheckDirectoryPath() throws Exception
    {
        File testDir = new File(this.workingDir, DIR_NAME);

        // the directory does not yet exist.
        Assert.assertFalse(testDir.exists());

        // so it will be created.
        FileUtil.checkDirectoryPath(testDir.getPath());
        Assert.assertTrue(testDir.exists());

        // delete the dir
        FileUtil.deleteDirectory(testDir);
        Assert.assertFalse(testDir.exists());

        // set write permission on root dir to false;
        File testDirRoot = new File(this.workingDir);
        testDirRoot.setReadOnly();

        // Check if the test is run by a normal user. If it is run by a power user, skip this part.
        if (!testDirRoot.canWrite()) {
            IOException mustHappen = null;
            try {
                FileUtil.checkDirectoryPath(testDir.getPath());
            } catch (IOException e) {
                mustHappen = e;
            }

            // if all went well, the dir must have not been able to be created and an exception should have been thrown.
            Assert.assertFalse(mustHappen == null && testDir.exists());
        }

        // reset the root directory to writable and create the test dir but make it not writable.
        testDirRoot.delete();
        testDirRoot = new File(this.workingDir);
        testDir.mkdirs();
        testDir.setReadOnly();

        // Check if the test is run by a normal user. If it is run by a power user, skip this part.
        if (!testDir.canWrite()) {
            // check the direcotry and throw an exception because it is exists but it's not usable.
            IOException mustHappenToo = null;
            try {
                FileUtil.checkDirectoryPath(testDir.getPath());
            } catch (IOException e) {
                mustHappenToo = e;
            }
            Assert.assertNotNull(mustHappenToo);
        }

        // create a file and pass it as argument, instead of a directory.
        File testFile = new File(this.workingDir, FILE_NAME);
        testFile.createNewFile();

        // Check and throw an InvalidParameterException exception.
        InvalidParameterException fileInsteadOfDirectory = null;
        try {
            FileUtil.checkDirectoryPath(testFile.getPath());
        } catch (InvalidParameterException e) {
            fileInsteadOfDirectory = e;
        }
        Assert.assertNotNull(fileInsteadOfDirectory);
    }

    /**
     * Test copying a file from a location to another.
     * <p>
     * Result: The two files will be identical.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testMoveFile() throws Exception
    {
        String sourceFileName = SOURCE_FILE_NAME;
        String destinationFileName = DESTINATION_FILE_NAME;

        File sourceFile = new File(this.workingDir, sourceFileName);
        File destinationFile = new File(this.workingDir, destinationFileName);

        FileOutputStream fos = new FileOutputStream(sourceFile);

        fos.write(SOURCE_CONTENT.getBytes());
        fos.close();

        FileUtil.moveFile(sourceFile, destinationFile);

        BufferedReader br = new BufferedReader(new FileReader(destinationFile));

        String destinationFileContent = "";
        String line = null;
        while ((line = br.readLine()) != null) {
            destinationFileContent += line + NEW_LINE;
        }

        Assert.assertEquals(SOURCE_CONTENT, destinationFileContent);
    }
}
