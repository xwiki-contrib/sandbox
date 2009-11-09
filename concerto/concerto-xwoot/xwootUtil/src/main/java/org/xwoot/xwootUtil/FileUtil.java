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
package org.xwoot.xwootUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.nio.channels.FileChannel;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility class for file operatios.
 * 
 * @version $Id$
 */
public final class FileUtil
{
    /** Buffer size for buffered file operations. */
    public static final int BUFFER = 2048;

    /** Default filename encoding to used. */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /** Directory name where to store tests data. */
    public static final String TESTS_DIRECTORY_NAME = "xwootTests";

    /** Commonly used separator. */
    public static final String COMMA_SPACE_SEPARATOR = ", ";

    /** Disable utility class instantiation. */
    private FileUtil()
    {
        // void
    }

    /**
     * Copy a file from one location to another.
     * <p>
     * If the destination file exists, it will be overridden.
     * 
     * @param sourceFile the file to copy from.
     * @param destinationFile the file to copy to.
     * @throws IOException if the sourceFilePath is not found or other IO problems occur.
     */
    public static void copyFile(File sourceFile, File destinationFile) throws IOException
    {
        // check destination.
        FileUtil.checkDirectoryPath(destinationFile.getParentFile());

        // do the actual copy
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;
        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destinationChannel = new FileOutputStream(destinationFile).getChannel();

            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (sourceChannel != null) {
                    sourceChannel.close();
                }
                if (destinationChannel != null) {
                    destinationChannel.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * Convenience method to copy a file from one location to another.
     * 
     * @param sourceFilePath the location of the file to copy from.
     * @param destinationFilePath the location of the file to copy to.
     * @throws IOException if the sourceFilePath is not found or other IO problems occur.
     * @see #copyFile(File, File)
     */
    public static void copyFile(String sourceFilePath, String destinationFilePath) throws IOException
    {
        FileUtil.copyFile(new File(sourceFilePath), new File(destinationFilePath));
    }

    /**
     * Copies the first level readable files of one directory to another.
     * 
     * @param sourceDirectoryPath the location of the directory to copy from.
     * @param destinationDirectoryPath the location of the directory to copy to.
     * @throws IOException if problems occur while copying the contents.
     * @throws NullPointerException if at least one of the parameters are null.
     * @see #copyFile(String, String)
     */
    public static void copyFiles(String sourceDirectoryPath, String destinationDirectoryPath) throws IOException
    {
        if (sourceDirectoryPath == null || destinationDirectoryPath == null) {
            throw new NullPointerException("Null values provided as parameters.");
        }

        File sourceDir = new File(sourceDirectoryPath);

        for (File file : sourceDir.listFiles()) {
            if (file.isFile() && file.canRead()) {
                FileUtil.copyFile(file.toString(), destinationDirectoryPath + File.separator + file.getName());
            }
        }
    }

    /**
     * Copies data from the InputStream to the OutputStream using {@link #BUFFER} bytes at a time.
     * <p>
     * NOTE: Both streams are <b>not</b> automatically closed, so the user will have to take care of that.
     * 
     * @param in the source Stream.
     * @param out the destination Stream.
     * @throws IOException if transfer problems occur.
     */
    public static void copyInputStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[BUFFER];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     * 
     * @param directory the directory to delete.
     */
    public static void deleteDirectory(File directory)
    {
        if (directory == null) {
            throw new NullPointerException("A null value was provided instead of a File object.");
        }

        if (directory.exists()) {
            String[] children = directory.list();

            for (String element : children) {
                File f = new File(directory, element);
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }

            directory.delete();
        }
    }

    /**
     * Convenience method.
     * 
     * @param directoryPath the path of the directory to delete.
     * @see #deleteDirectory(File)
     */
    public static void deleteDirectory(String directoryPath)
    {
        if (directoryPath == null || directoryPath.length() == 0) {
            throw new InvalidParameterException("An empty or null value was provided for directory path.");
        }

        deleteDirectory(new File(directoryPath));
    }

    /**
     * Move one file from a location to another. Can also be used to rename files.
     * <p>
     * The behavior is that renaming the file is first tried. If that fails, the file will be copied to the destination
     * and the source will be deleted.
     * 
     * @param sourceFile the file to move.
     * @param destinationFile the file to move to.
     * @return a new {@link File} instance pointing to the new location.
     * @throws IOException if the destination file's parent directory is not usable. A {@link FileNotFoundException} is
     *             thrown if the source file does not exist.
     * @throws IllegalArgumentException if at least one of the source or the destination file is a directory.
     * @throws NullPointerException if one of the parameters are null.
     * @see #checkDirectoryPath(File)
     */
    public static File moveFile(File sourceFile, File destinationFile) throws IOException, IllegalArgumentException,
        NullPointerException
    {
        if (sourceFile == null || destinationFile == null) {
            throw new NullPointerException("Null values were provided: " + sourceFile + COMMA_SPACE_SEPARATOR
                + destinationFile);
        }

        if (sourceFile.isDirectory() || destinationFile.isDirectory()) {
            throw new IllegalArgumentException(
                "Both the source and the destination need to point to files and not directories. (" + sourceFile
                    + COMMA_SPACE_SEPARATOR + destinationFile + ")");
        }

        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + sourceFile);
        }

        File result = null;

        FileUtil.checkDirectoryPath(destinationFile.getParent());

        if (!sourceFile.renameTo(destinationFile)) {
            FileUtil.copyFile(sourceFile, destinationFile);

            sourceFile.delete();
        }

        result = new File(destinationFile.getPath());

        return result;
    }

    /**
     * Convenience method to move/rename a file.
     * 
     * @param sourceFilePath the location of the file to move.
     * @param destinationFilePath the location of the file to move to.
     * @return a new {@link File} instance pointing to the new location.
     * @throws IOException if the destination file's parent directory is not usable. A {@link FileNotFoundException} is
     *             thrown if the source file does not exist.
     * @throws IllegalArgumentException if at least one of the source or the destination file is a directory.
     * @throws NullPointerException if one of the parameters are null.
     * @see #checkDirectoryPath(File)
     * @see #moveFile(File, File)
     */
    public static File moveFile(String sourceFilePath, String destinationFilePath) throws IOException,
        IllegalArgumentException, NullPointerException
    {
        if (sourceFilePath == null || destinationFilePath == null) {
            throw new NullPointerException("Null values provided: " + sourceFilePath + COMMA_SPACE_SEPARATOR
                + destinationFilePath);
        }

        return FileUtil.moveFile(new File(sourceFilePath), new File(destinationFilePath));
    }

    /**
     * @param pageId the pageId that needs to be encoded.
     * @return the Base64 encoded filename corresponding to the provided pageId using the {@link #DEFAULT_ENCODING}.
     * @throws UnsupportedEncodingException if the {@link #DEFAULT_ENCODING} is not supported.
     */
    public static String getEncodedFileName(String pageId) throws UnsupportedEncodingException
    {
        return new String(Base64.encodeBase64(pageId.getBytes(DEFAULT_ENCODING)), DEFAULT_ENCODING);
    }

    /**
     * @param filename the filename that needs to be decoded.
     * @return the Base64 decoded filename using the {@link #DEFAULT_ENCODING}.
     * @throws UnsupportedEncodingException if the {@link #DEFAULT_ENCODING} is not supported.
     */
    public static String getDecodedFileName(String filename) throws UnsupportedEncodingException
    {
        return new String(Base64.decodeBase64(filename.getBytes(DEFAULT_ENCODING)), DEFAULT_ENCODING);
    }

    /**
     * Normalize a string by replacing accents with ASCII equivalents and removing and non-ASCII characters.
     * 
     * @param string the string to normalize.
     * @return the normalized string.
     * @see #removeAccents(String)
     */
    public static String normalizeName(String string)
    {
        String temp = FileUtil.removeAccents(string);

        return temp.replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * Flattens accentuated characters to their basic ASCII form.
     * 
     * @param string the string to remove accents from.
     * @return a copy of the provided string, having it's accents removed.
     */
    public static String removeAccents(String string)
    {
        /*
         * @TODO: switch to {@link java.text.Normalizer} when switching to JDK1.6 for better coverage. Example: text =
         * Normalizer.normalize(text, Normalizer.Form.NFD);
         */

        String sSemAcento = string + "";
        // sSemAcento = sSemAcento.replaceAll("[áàâãäă]", "a");
        sSemAcento = sSemAcento.replaceAll("[\u00E1\u00E0\u00E2\u00E3\u00E4\u0103]", "a");
        // sSemAcento = sSemAcento.replaceAll("[ÁÀÂÃÄ]", "A");
        sSemAcento = sSemAcento.replaceAll("[\u00C1\u00C0\u00C2\u00C3\u00C4]", "A");
        // sSemAcento = sSemAcento.replaceAll("[éèêë]", "e");
        sSemAcento = sSemAcento.replaceAll("[\u00E9\u00E8\u00EA\u00EB]", "e");
        // sSemAcento = sSemAcento.replaceAll("[ÉÈÊË]", "E");
        sSemAcento = sSemAcento.replaceAll("[\u00C9\u00C8\u00CA\u00CB]", "E");
        // sSemAcento = sSemAcento.replaceAll("[íìîï]", "i");
        sSemAcento = sSemAcento.replaceAll("[\u00ED\u00EC\u00EE\u00EF]", "i");
        // sSemAcento = sSemAcento.replaceAll("[ÍÌÎÏ]", "I");
        sSemAcento = sSemAcento.replaceAll("[\u00CD\u00CC\u00CE\u00CF]", "I");
        // sSemAcento = sSemAcento.replaceAll("[óòôõö]", "o");
        sSemAcento = sSemAcento.replaceAll("[\u00F3\u00F2\u00F4\u00F5\u00F6]", "o");
        // sSemAcento = sSemAcento.replaceAll("[ÓÒÔÕÖ]", "O");
        sSemAcento = sSemAcento.replaceAll("[\u00D3\u00D2\u00D4\u00D5\u00D6]", "O");
        // sSemAcento = sSemAcento.replaceAll("[úùûü]", "u");
        sSemAcento = sSemAcento.replaceAll("[\u00FA\u00F9\u00FB\u00FC]", "u");
        // sSemAcento = sSemAcento.replaceAll("[ÚÙÛÜ]", "U");
        sSemAcento = sSemAcento.replaceAll("[\u00DA\u00D9\u00DB\u00DC]", "U");
        // sSemAcento = sSemAcento.replaceAll("ç", "c");
        sSemAcento = sSemAcento.replaceAll("\u00E7", "c");
        // sSemAcento = sSemAcento.replaceAll("Ç", "C");
        sSemAcento = sSemAcento.replaceAll("\u00C7", "C");
        // sSemAcento = sSemAcento.replaceAll("ñ", "n");
        sSemAcento = sSemAcento.replaceAll("\u00F1", "n");
        // sSemAcento = sSemAcento.replaceAll("Ñ", "N");
        sSemAcento = sSemAcento.replaceAll("\u00D1", "N");
        // sSemAcento = sSemAcento.replaceAll("[ş]", "s");
        sSemAcento = sSemAcento.replaceAll("[\u015F]", "s");
        // sSemAcento = sSemAcento.replaceAll("[Ş]", "S");
        sSemAcento = sSemAcento.replaceAll("[\u015E]", "S");
        // sSemAcento = sSemAcento.replaceAll("[ţ]", "t");
        sSemAcento = sSemAcento.replaceAll("[\u0163]", "t");
        // sSemAcento = sSemAcento.replaceAll("[Ţ]", "T");
        sSemAcento = sSemAcento.replaceAll("[\u0162]", "T");

        return sSemAcento;
    }

    /**
     * Zips a directory's first level files and saves the resulting file in the temporary directory. The file will be
     * named "&lt;directory_name&gt;.zip".
     * 
     * @param directoryPath the directory to zip.
     * @return the actual location of the resulting temporary zip file.
     * @throws IOException if problems occur.
     * @see ZipOutputStream
     */
    public static String zipDirectory(String directoryPath) throws IOException
    {
        File tempFile = File.createTempFile(new File(directoryPath).getName(), ".zip");

        zipDirectory(directoryPath, tempFile.toString());

        return tempFile.toString();
    }

    /**
     * Compresses a directory's first level files using the ZIP format.
     * 
     * @param dirPath the path of the directory to zip.
     * @param resultFilePath the destination zip file.
     * @throws IOException if problems occur with file operations.
     * @see ZipOutputStream
     */
    public static void zipDirectory(String dirPath, String resultFilePath) throws IOException
    {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        zipFiles(files, resultFilePath);
    }

    /**
     * Zip an array of files. Each file in the array must reflect a file on disk. If a file represents a directory on
     * disk, it will be skipped.
     * 
     * @param files the files to be zipped.
     * @param zippedFileDestinationPath the location of the zip file that will result.
     * @throws IOException if problems occur.
     */
    public static void zipFiles(File[] files, String zippedFileDestinationPath) throws IOException
    {
        if (files == null || zippedFileDestinationPath == null) {
            throw new NullPointerException("Null paramenters. filePaths: " + files + " zippedFileDestinationPath: "
                + zippedFileDestinationPath);
        }

        FileUtil.checkDirectoryPath(new File(zippedFileDestinationPath).getParent());

        if (files.length < 1) {
            return;
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(zippedFileDestinationPath);
            bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(fos);

            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(Deflater.BEST_COMPRESSION);

            for (File aFile : files) {
                zipFiletoZipOutputStream(aFile.getParentFile(), aFile.getName(), zos);
            }

        } catch (Exception e) {
            throw new IOException("Failed to zip files: (" + e.getClass() + ") " + e.getMessage());
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                throw new IOException("Problem closing streams. Reason: " + e.getMessage());
            }
        }
    }

    /**
     * Zips a file as a {@link ZipEntry} in a ZipOutputStream.
     * 
     * @param dir the directory that contains the file to zip.
     * @param fileName the name of the file to zip.
     * @param zos the ZipOutputStream to write the ZipEntry to.
     * @return true if the file has been zipped and written successfully; false if the combination dir/fileName is a
     *         directory instead of a file.
     * @throws IOException if problems occur.
     */
    public static boolean zipFiletoZipOutputStream(File dir, String fileName, ZipOutputStream zos) throws IOException
    {
        if (dir == null || fileName == null || fileName.length() == 0 || zos == null) {
            throw new NullPointerException("Null or empty values not allowed.");
        }

        // skip directories.
        File fileToZip = new File(dir, fileName);
        if (fileToZip.isDirectory()) {
            return false;
        }

        byte[] data = new byte[FileUtil.BUFFER];

        FileInputStream fis = new FileInputStream(fileToZip);
        BufferedInputStream buffer = new BufferedInputStream(fis, FileUtil.BUFFER);
        ZipEntry entry = new ZipEntry(FileUtil.normalizeName(fileName));

        zos.putNextEntry(entry);

        int count;

        while ((count = buffer.read(data, 0, FileUtil.BUFFER)) != -1) {
            zos.write(data, 0, count);
        }

        zos.closeEntry();
        buffer.close();

        return true;
    }

    /**
     * Convenience method.
     * 
     * @param zippedFilePath the location of the zip file.
     * @param destinationDirPath the directory where to extract the zip file. If it does not exist, it will be created.
     * @return a list of extracted file names.
     * @throws IOException if the destinationDirPath is not usable or other I/O or zip problems occur.
     * @see #unzipInDirectory(ZipFile, String)
     * @see ZipFile
     * @see #checkDirectoryPath(String)
     */
    public static List<String> unzipInDirectory(String zippedFilePath, String destinationDirPath) throws IOException
    {
        if (!new File(zippedFilePath).exists()) {
            throw new FileNotFoundException(zippedFilePath + " does not exist.");
        }

        FileUtil.checkDirectoryPath(destinationDirPath);

        return FileUtil.unzipInDirectory(new ZipFile(zippedFilePath), destinationDirPath);
    }

    /**
     * Extracts a zip file file to a directory.
     * <p>
     * Note: The zipFile object will be closed when this method returns.
     * 
     * @param zipFile a valid ZipFile object.
     * @param destinationDirPath the directory where to extract the zip file. If it does not exist, it will be created.
     * @return a list of extracted file names.
     * @throws IOException if the destinationDirPath is not usable or other I/O or zip problems occur.
     * @see ZipFile
     * @see ZipFile#close()
     * @see #checkDirectoryPath(String)
     */
    public static List<String> unzipInDirectory(ZipFile zipFile, String destinationDirPath) throws IOException
    {
        FileUtil.checkDirectoryPath(destinationDirPath);

        List<String> result = new ArrayList<String>();

        InputStream currentZipEntryInputStream = null;
        BufferedOutputStream bosToFile = null;

        try {
            Enumeration< ? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String currentDestinationFilePath = destinationDirPath + File.separator + entry.getName();

                currentZipEntryInputStream = zipFile.getInputStream(entry);
                bosToFile = new BufferedOutputStream(new FileOutputStream(currentDestinationFilePath));

                try {
                    FileUtil.copyInputStream(currentZipEntryInputStream, bosToFile);
                } catch (IOException ioe) {
                    throw new IOException("Error unzipping entry " + entry.getName()
                        + ". Check disk space or write access.");
                }

                currentZipEntryInputStream.close();
                bosToFile.close();

                result.add(entry.getName());
            }
        } finally {
            try {
                if (currentZipEntryInputStream != null) {
                    currentZipEntryInputStream.close();
                }

                if (bosToFile != null) {
                    bosToFile.close();
                }

                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception e) {
                throw new IOException("Unable to close zip file " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Checks if the given directory path exists and if it's a valid and writable directory. If it doesn't exist, it is
     * created.
     * 
     * @param directoryPath the path to check.
     * @throws IOException if the directory did not exist and can not be created. It is also thrown if the path is valid
     *             but not writable.
     * @throws InvalidParameterException if the given path is valid but it points to a file instead of pointing to a
     *             directory.
     * @throws NullPointerException if the directoryPath is null.
     */
    public static void checkDirectoryPath(String directoryPath) throws IOException, InvalidParameterException
    {
        if (directoryPath == null) {
            throw new NullPointerException("The provided directoryPath is null.");
        }

        File directory = new File(directoryPath);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Can't create directory: " + directoryPath);
            }
        } else if (!directory.isDirectory()) {
            throw new InvalidParameterException(directoryPath + " -- is not a directory");
        } else if (!directory.canWrite()) {
            throw new IOException(directoryPath + " -- isn't writable");
        }
    }

    /**
     * Convenience method.
     * 
     * @param directory the directory to check.
     * @throws IOException if the directory did not exist and can not be created. It is also thrown if the path is valid
     *             but not writable.
     * @throws InvalidParameterException if the given path is valid but it points to a file instead of pointing to a
     *             directory.
     * @see #checkDirectoryPath(String)
     */
    public static void checkDirectoryPath(File directory) throws IOException, InvalidParameterException
    {
        if (directory == null) {
            throw new NullPointerException("The provided directory is null.");
        }

        FileUtil.checkDirectoryPath(directory.toString());
    }

    /**
     * @return the operating-system-independent temporary directory.
     */
    public static String getSystemTemporaryDirectory()
    {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * @return the working directory for tests.
     */
    public static String getTestsWorkingDirectoryPath()
    {
        return getSystemTemporaryDirectory() + File.separator + TESTS_DIRECTORY_NAME;
    }

    /**
     * @param moduleName the name of the module.
     * @return the working directory for tests for the specified module.
     */
    public static String getTestsWorkingDirectoryPathForModule(String moduleName)
    {
        if (moduleName == null || moduleName.length() == 0) {
            throw new InvalidParameterException("Module name must not be null or empty.");
        }

        return getTestsWorkingDirectoryPath() + File.separator + moduleName;
    }
}
