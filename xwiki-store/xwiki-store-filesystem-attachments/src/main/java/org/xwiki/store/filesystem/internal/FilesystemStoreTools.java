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
package org.xwiki.store.filesystem.internal;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.List;

import com.xpn.xwiki.doc.XWikiAttachment;
import org.xwiki.component.annotation.ComponentRole;


/**
 * Tools for getting files to store data in the filesystem.
 * This should be replaced by a module which provides a secure extension of java.io.File.
 *
 * @version $Id$
 * @since TODO
 */
@ComponentRole
public interface FilesystemStoreTools
{
    /** The name of the directory in the work directory where the hirearchy will be stored. */
    String STORAGE_DIR_NAME = "storage";

    /**
     * The name of the directory where document information is stored.
     * This must have a URL illegal character in it,
     * otherwise it will be confused if/when nested spaces are implemented.
     */
    String DOCUMENT_DIR_NAME = "~this";

    /** The directory within each document's directory where the document's attachments are stored. */
    String ATTACHMENT_DIR_NAME = "attachments";

    /**
     * This stores the attachment metadata for each revision of the attachment in XML format.
     * @see #metaFileForAttachment(XWikiAttachment)
     */
    String ATTACH_ARCHIVE_META_FILENAME = "~METADATA.xml";

    /**
     * When a file is being saved, the original will be moved to the same name with this after it.
     * If the save operation fails then this file will be moved back to the regular position to come as
     * close as possible to ACID transaction handling.
     */
    String BACKUP_FILE_SUFFIX = "~bak";

    /**
     * When a file is being deleted, it will be renamed with this at the end of the filename in the
     * transaction. If the transaction succeeds then the temp file will be deleted, if it fails then the
     * temp file will be renamed back to the original filename.
     */
    String TEMP_FILE_SUFFIX = "~tmp";

    /**
     * This must precede the version of a file. It has to be URL invalid so that it cannot collide with
     * the name of another file. Also no other key can start with ~v because the name of the version
     * might be anything. If the prefix was "~b" and a version was made called "ak" then it would collide
     * with the BACKUP_FILE_SUFFIX.
     */
    String FILE_VERSION_PREFIX = "~v";

    /**
     * Get a backup file which for a given storage file.
     * This file name will never collide with any other file gotten through this interface.
     *
     * @param storageFile the file to get a backup file for.
     * @return a backup file with a name based on the name of the given file.
     */
    File getBackupFile(final File storageFile);

    /**
     * Get a temporary file which for a given storage file.
     * This file name will never collide with any other file gotten through this interface.
     *
     * @param storageFile the file to get a temporary file for.
     * @return a temporary file with a name based on the name of the given file.
     */
    File getTempFile(final File storageFile);

    /**
     * Get the meta file for the attachment.
     * The meta file contains information about each version of the attachment such as who saved it.
     * This will be a file named ~METADATA.xml which will reside in the dame directory as the content
     * file gotten by fileForAttachment().
     *
     * @param attachment the attachment to get the metadata for.
     * @return a File which corrisponds to this attachment for holding the attachment's metadata history.
     */
    File metaFileForAttachment(final XWikiAttachment attachment);

    /**
     * Get a File for loading or storing this attachment's content.
     * This file is derived from the name of the document which the attachment resides in and the
     * attachment filename. The file will be placed in the storage area in a directory structure
     * called <storage dir>/<wiki>/<space>/<document name>/~this/attachments/<attachment file name>/
     * So an attachment called file.txt in a document called Sandbox.Test in a the main wiki ("xwiki")
     * would go in the following file:
     * <storage dir>/xwiki/Sandbox/Test/~this/attachments/file.txt/file.txt
     *
     * @param attachment the attachment to get a file for content storage.
     * @return a file to store the content of the given attachment.
     */
    File fileForAttachment(final XWikiAttachment attachment);

    /**
     * Get a file corrisponding to this version of this attachment.
     * This file's path is derived from the name of the document with space and wiki.
     * The file path is the same as the path for fileForAttachment() but the name
     * has a version number added.
     * If the file has one or more dots ('.') in it then the version number is inserted before
     * the last dot. Otherwise it is appended to the end. Version numbers always have "~v" prepended
     * to prevent collision.
     * version 1.1 of an attachment called file.txt will be stored as file~v1.1.txt
     * version 1.2 of an attachment called noExtension will be stored as noExtension~v1.2
     *
     * @param attachment the attachment to get the version of.
     * @param versionName the name of the version for example "1.1" or "1.2".
     * @return a File for this version of this attachment which is guaranteed not to collide with any
     *         file gotten through this or any other function in FilesystemStoreTools.
     */
    File fileForAttachmentVersion(final XWikiAttachment attachment,
                                  final String versionName);

    /**
     * Get a {@link java.util.concurrent.locks.ReadWriteLock} which is unique to the given file.
     * This method will always return the same lock for the path on the filesystem even if the 
     * {@link java.io.File} object is different.
     *
     * @param toLock the file to get a lock for.
     * @return a lock for the given file.
     */
    ReadWriteLock getLockForFile(final File toLock);

    /**
     * Get a {@link java.util.concurrent.locks.ReadWriteLock} which when locked, will lock on
     * each of a list of files.
     *
     * @param toLock the list of files to get a lock for.
     * @return a lock for the given files.
     */
    ReadWriteLock getLockForFiles(final List<File> toLock);
}
