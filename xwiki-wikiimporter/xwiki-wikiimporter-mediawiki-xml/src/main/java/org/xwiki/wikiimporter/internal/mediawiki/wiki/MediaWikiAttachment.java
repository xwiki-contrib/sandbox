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
package org.xwiki.wikiimporter.internal.mediawiki.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.xwiki.wikiimporter.internal.importer.WikiImporterLogger;
import org.xwiki.wikiimporter.wiki.AbstractAttachment;

/**
 * This class represents MediaWiki Attachment
 * 
 * @version $Id$
 */
public class MediaWikiAttachment extends AbstractAttachment
{
    private String directory;

    private String excludeDirList;

    private WikiImporterLogger logger;

    public MediaWikiAttachment(String directory, String fileName, String excludeDirList, WikiImporterLogger logger)
    {
        this.directory = directory;
        this.fileName = fileName;
        this.excludeDirList = excludeDirList;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.Attachment#getContent()
     */
    public byte[] getContent()
    {
        // TODO Auto-generated method stub
        this.attachmentAsFile = fetchAttachment(directory, fileName, excludeDirsAsList(excludeDirList));
        if (attachmentAsFile != null && attachmentAsFile.exists()) {
            try {
                InputStream is = new FileInputStream(attachmentAsFile);

                // Get the size of the file
                long length = attachmentAsFile.length();

                if (length > Integer.MAX_VALUE) {
                    logger.error(fileName + " is too large to import.", true);
                }

                // Create the byte array to hold the data
                byte[] bytes = new byte[(int) length];

                // Read in the bytes
                int offset = 0;
                int numRead = 0;
                while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                    offset += numRead;
                }

                // Ensure all the bytes have been read in
                if (offset < bytes.length) {
                    logger.error("Attachment : Unable to read the complete file" + fileName, true);
                }

                // Close the input stream and return bytes
                is.close();
                return bytes;

            } catch (Exception e) {
                // TODO Handle Exceptions
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * Fetches the file by recursively searching for the given filename in the given parent directory. Excluding the
     * list of exclude directories.
     * 
     * @param directory The parent directory from which the file is to be fetched..
     * @param fileName the name of the file to be fetched.
     * @param excludeDirectories list of directories to be excluded while searching.
     * @return the File handle of the file if its found.
     */
    private File fetchAttachment(final String directory, final String fileName, final List<String> excludeDirectories)
    {

        class MediaWikiAttachmentFilter implements IOFileFilter
        {

            public boolean accept(File file)
            {
                String name = file.getName();
                return name.equals(fileName);
            }

            public boolean accept(File file, String string)
            {
                String name = file.getName();
                return name.equals(fileName);
            }
        }

        class MediaWikiDirectoryFilter implements IOFileFilter
        {

            public boolean accept(File file)
            {
                if (file.isDirectory() && !excludeDirectories.contains(file.getName())) {

                    return true;
                }

                return false;
            }

            public boolean accept(File file, String string)
            {
                if (file.isDirectory()) {
                    return true;
                }

                return false;
            }
        }

        MediaWikiAttachmentFilter fileFilter = new MediaWikiAttachmentFilter();
        MediaWikiDirectoryFilter dirFilter = new MediaWikiDirectoryFilter();
        File imageDirectory = new File(directory);
        Collection<File> fileList = FileUtils.listFiles(imageDirectory, fileFilter, dirFilter);

        if (fileList.size() == 1) {
            for (File file : fileList) {
                return file;
            }
        }

        return null;

    }

    private List<String> excludeDirsAsList(String list)
    {

        List<String> excludeDirList = new ArrayList<String>();

        if (StringUtils.isNotBlank(list)) {
            Pattern comma = Pattern.compile("[^,]+");
            Matcher matcher = comma.matcher(list);

            while (matcher.find()) {
                excludeDirList.add(matcher.group());
            }
        }

        return excludeDirList;
    }
}
