package org.xwiki.store.filesystem.internal;

import java.io.File;

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
    final String STORAGE_DIR_NAME = "storage";

    /**
     * The name of the directory where document information is stored.
     * This must have a URL illegal character in it,
     * otherwise it will be confused if/when nested spaces are implemented.
     */
    final String DOCUMENT_DIR_NAME = "~this";

    /** The directory within each document's directory where the document's attachments are stored. */
    final String ATTACHMENT_DIR_NAME = "attachments";

    /**
     * When a file is being saved, the original will be moved to the same name with this after it.
     * If the save operation fails then this file will be moved back to the regular position to come as
     * close as possible to ACID transaction handling.
     */
    final String BACKUP_FILE_SUFFIX = "~";

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
}
