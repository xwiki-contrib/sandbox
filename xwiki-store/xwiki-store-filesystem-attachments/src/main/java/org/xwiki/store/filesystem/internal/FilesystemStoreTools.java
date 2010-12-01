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
     * When a file is being saved, the original will be moved to the same name with this after it.
     * If the save operation fails then this file will be moved back to the regular position to come as
     * close as possible to ACID transaction handling.
     */
    String BACKUP_FILE_SUFFIX = "~";

    /**
     * Get a temporary file which for a given storage file.
     *
     * @param storageFile the perminant file to get a
     * @return a temporary file with a name based on the name of the given perminent file.
     */
    File getBackupFile(final File storageFile);

    /**
     * Get a File for loading or storing this attachment.
     *
     * @param attachment the attachment to get a file for content storage.
     * @return a file to store the content of the given attachment.
     */
    File fileForAttachment(final XWikiAttachment attachment);

    /**
     * Get a {@link java.util.concurrent.locks.ReadWriteLock} which is unique to the given file.
     * This method will always return the same lock for the path on the filesystem even if the 
     * {@link java.io.File} object is different.
     *
     * @param toLock the file to get a lock for.
     * @return a lock for the given file.
     */
    ReadWriteLock getLockForFile(final File toLock);
}
