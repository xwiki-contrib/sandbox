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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.WeakHashMap;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;

/**
 * Default tools for getting files to store data in the filesystem.
 * This should be replaced by a module which provides a secure extension of java.io.File.
 *
 * @version $Id$
 * @since TODO
 */
@Component
public class DefaultFilesystemStoreTools implements FilesystemStoreTools, Initializable
{
    /** The name of the directory in the work directory where the hirearchy will be stored. */
    private static final String STORAGE_DIR_NAME = "storage";

    /**
     * The name of the directory where document information is stored.
     * This must have a URL illegal character in it,
     * otherwise it will be confused if/when nested spaces are implemented.
     */
    private static final String DOCUMENT_DIR_NAME = "~this";

    /** The directory within each document's directory where the document's attachments are stored. */
    private static final String ATTACHMENT_DIR_NAME = "attachments";

    /** The directory within each document's directory for attachments which have been deleted. */
    private static final String DELETED_ATTACHMENT_DIR_NAME = "deleted-attachments";

    /**
     * When a file is being saved, the original will be moved to the same name with this after it.
     * If the save operation fails then this file will be moved back to the regular position to come as
     * close as possible to ACID transaction handling.
     */
    private static final String BACKUP_FILE_SUFFIX = "~bak";

    /**
     * When a file is being deleted, it will be renamed with this at the end of the filename in the
     * transaction. If the transaction succeeds then the temp file will be deleted, if it fails then the
     * temp file will be renamed back to the original filename.
     */
    private static final String TEMP_FILE_SUFFIX = "~tmp";

    /** Serializer used for obtaining a safe file path from a document reference. */
    @Requirement("path")
    private EntityReferenceSerializer<String> pathSerializer;

    /**
     * We need to get the XWiki object in order to get the work directory.
     */
    @Requirement
    private Execution exec;

    /** This is the directory where all of the attachments will stored. */
    private File storageDir;

    /** A map which holds locks by the file path so that the same lock is used for the same file. */
    private final Map<String, WeakReference<ReadWriteLock>> fileLockMap =
        new WeakHashMap<String, WeakReference<ReadWriteLock>>();

    /**
     * Testing Constructor.
     *
     * @param pathSerializer an EntityReferenceSerializer for generating file paths.
     * @param storageDir the directory to store the content in.
     */
    public DefaultFilesystemStoreTools(final EntityReferenceSerializer<String> pathSerializer,
                                       final File storageDir)
    {
        this.pathSerializer = pathSerializer;
        this.storageDir = storageDir;
    }

    /** {@inheritDoc} */
    public void initialize()
    {
        final XWikiContext context = ((XWikiContext) this.exec.getContext().getProperty("xwikicontext"));
        final File workDir = context.getWiki().getWorkDirectory(context);
        this.storageDir = new File(workDir, STORAGE_DIR_NAME);

        deleteEmptyDirs(this.storageDir);
    }

    /**
     * Delete all empty directories under the given directory.
     * A directory which contains only empty directories is also considered an empty ditectory.
     *
     * @param location a directory to delete.
     * @return true if the directory existed, was empty and was deleted.
     */
    private static boolean deleteEmptyDirs(final File location)
    {
        if (location != null && location.exists() && location.isDirectory()) {
            final File[] dirs = location.listFiles();
            boolean empty = true;
            for (int i = 0; i < dirs.length; i++) {
                if (!deleteEmptyDirs(dirs[i])) {
                    empty = false;
                }
            }
            if (empty) {
                location.delete();
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see FilesystemStoreTools#getBackupFile(File)
     */
    public File getBackupFile(final File storageFile)
    {
        return new File(storageFile.getAbsolutePath() + BACKUP_FILE_SUFFIX);
    }

    /**
     * {@inheritDoc}
     *
     * @see FilesystemStoreTools#getTempFile(File)
     */
    public File getTempFile(final File storageFile)
    {
        return new File(storageFile.getAbsolutePath() + TEMP_FILE_SUFFIX);
    }

    /**
     * {@inheritDoc}
     *
     * @see FilesystemStoreTools#getDeletedAttachmentFileProvider(XWikiAttachment, Date)
     */
    public AttachmentFileProvider getDeletedAttachmentFileProvider(final XWikiAttachment attachment,
                                                                   final Date deleteDate)
    {
        return new DefaultDeletedAttachmentFileProvider(
            this.getDeletedAttachmentDir(attachment, deleteDate), attachment.getFilename());
    }

    /**
     * {@inheritDoc}
     *
     * @see FilesystemStoreTools#getAttachmentFileProvider(XWikiAttachment)
     */
    public AttachmentFileProvider getAttachmentFileProvider(final XWikiAttachment attachment)
    {
        return new DefaultAttachmentFileProvider(this.getAttachmentDir(attachment),
                                                 attachment.getFilename());
    }

    /**
     * Get the directory for storing files for an attachment.
     * This will look like storage/xwiki/Main/WebHome/~this/attachments/file.name/
     *
     * @param attachment the attachment to get the directory for.
     * @return a File representing the directory. Note: The directory may not exist.
     */
    private File getAttachmentDir(final XWikiAttachment attachment)
    {
        final XWikiDocument doc = attachment.getDoc();
        if (doc == null) {
            throw new NullPointerException("Could not store attachment because it is not "
                                           + "associated with a document.");
        }
        final File docDir = getDocumentDir(doc.getDocumentReference(),
                                           this.storageDir,
                                           this.pathSerializer);
        final File attachmentsDir = new File(docDir, ATTACHMENT_DIR_NAME);
        return new File(attachmentsDir, GenericFileUtils.getURLEncoded(attachment.getFilename()));
    }

    /**
     * Get a directory for storing the contentes of a deleted attachment.
     * The format is <document name>/~this/deleted-attachments/<attachment name>-<delete date>/
     * <delete date> is expressed in "unix time" so it might look like:
     * WebHome/~this/deleted-attachments/file.txt-0123456789/
     *
     * @param attachment the attachment to get the file for.
     * @param deleteDate the date the attachment was deleted.
     * @return a directory which will be repeatable only with the same inputs.
     */
    private File getDeletedAttachmentDir(final XWikiAttachment attachment,
                                         final Date deleteDate)
    {
        final XWikiDocument doc = attachment.getDoc();
        if (doc == null) {
            throw new NullPointerException("Could not store deleted attachment because "
                                           + "it is not associated with any document.");
        }
        final File docDir = getDocumentDir(doc.getDocumentReference(),
                                           this.storageDir,
                                           this.pathSerializer);
        final File deletedAttachmentsDir = new File(docDir, DELETED_ATTACHMENT_DIR_NAME);
        final String fileName = attachment.getFilename() + "-" + deleteDate.getTime();
        return new File(deletedAttachmentsDir, GenericFileUtils.getURLEncoded(fileName));
    }

    /**
     * {@inheritDoc}
     *
     * @see FilesystemStoreTools#getLockForFile(File)
     */
    public synchronized ReadWriteLock getLockForFile(final File toLock)
    {
        final String path = toLock.getAbsolutePath();
        WeakReference<ReadWriteLock> lock = this.fileLockMap.get(path);
        ReadWriteLock strongLock = null;
        if (lock != null) {
            strongLock = lock.get();
        }
        if (strongLock == null) {
            strongLock = new ReentrantReadWriteLock() {
                /**
                 * A strong reference on the string to make sure that the
                 * mere existence of the lock will keep it in the map.
                 */
                private final String lockMapReference = path;
            };
            this.fileLockMap.put(path, new WeakReference<ReadWriteLock>(strongLock));
        }
        return strongLock;
    }

    /**
     * {@inheritDoc}
     *
     * @see FilesystemStoreTools#getLockForFiles(List)
     */
    public synchronized ReadWriteLock getLockForFiles(final List<File> toLock)
    {
        final List<ReadWriteLock> locks = new ArrayList<ReadWriteLock>(toLock.size());
        for (File file : toLock) {
            locks.add(this.getLockForFile(file));
        }
        return new CompositeReadWriteLock(locks);
    }

    /**
     * Get the directory associated with this document.
     * This is a path obtained from the owner document reference, where each reference segment
     * (wiki, spaces, document name) contributes to the final path.
     * For a document called xwiki:Main.WebHome, the directory will be:
     * <code>(storageDir)/xwiki/Main/WebHome/~this/</code>
     * 
     * @param docRef the DocumentReference for the document to get the directory for.
     * @param storageDir the directory to place the directory hirearcy for attachments in.
     * @param pathSerializer an EntityReferenceSerializer which will make a directory path from an
     *                       an EntityReference.
     * @return a file path corresponding to the attachment location; each segment in the path is
     *         URL-encoded in order to be safe.
     */
    private static File getDocumentDir(final DocumentReference docRef,
                                       final File storageDir,
                                       final EntityReferenceSerializer<String> pathSerializer)
    {
        final File path = new File(storageDir, pathSerializer.serialize(docRef));
        return new File(path, DOCUMENT_DIR_NAME);
    }

    /**
     * A ReadWriteLock made up of many ReadWriteLocks.
     * To acquire this lock means acquiring all of it's component locks.
     */
    private static class CompositeReadWriteLock implements ReadWriteLock
    {
        /** The composite lock made of all the read locks. */
        private final Lock readLock;

        /** The composite lock made of all the write locks. */
        private final Lock writeLock;

        /**
         * The Constructor.
         *
         * @param members the locks to make this composite lock out of.
         */
        public CompositeReadWriteLock(final List<ReadWriteLock> members)
        {
            final List<Lock> readLocks = new ArrayList<Lock>();
            final List<Lock> writeLocks = new ArrayList<Lock>();
            for (ReadWriteLock member : members) {
                readLocks.add(member.readLock());
                writeLocks.add(member.writeLock());
            }
            this.readLock = new CompositeLock(readLocks);
            this.writeLock = new CompositeLock(writeLocks);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.ReadWriteLock#readLock()
         */
        public Lock readLock()
        {
            return this.readLock;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.ReadWriteLock#writeLock()
         */
        public Lock writeLock()
        {
            return this.writeLock;
        }
    }

    /**
     * A Lock made up of a number of other Locks.
     * Acquiring this lock means acquiring all of it's component locks.
     */
    private static class CompositeLock implements Lock
    {
        /** Exception to throw when a function is called which has not been written. */
        private static final String NOT_IMPLEMENTED = "Function not implemented.";

        /** The locks which make up this composite lock. */
        private final List<Lock> locks = new ArrayList<Lock>();

        /**
         * The Constructor.
         *
         * @param locks the locks which should make up this composite lock.
         */
        public CompositeLock(final List<Lock> locks)
        {
            this.locks.addAll(locks);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.Lock#lock()
         */
        public void lock()
        {
            final List<Lock> toLock = new ArrayList(this.locks);
            try {
                // Get all of the locks which we can.
                int i = 0;
                while (i < toLock.size()) {
                    if (toLock.get(i).tryLock()) {
                        toLock.remove(i);
                    } else {
                        i++;
                    }
                }
                // Force the locks which we haven't locked yet.
                for (Lock stillWaiting : toLock) {
                    stillWaiting.lock();
                }
            } catch (RuntimeException e) {
                this.unlock();
                throw e;
            }
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#lockInterruptibly()
         */
        public void lockInterruptibly()
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#newCondition()
         */
        public Condition newCondition()
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#tryLock()
         */
        public boolean tryLock()
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#tryLock(long, TimeUnit)
         */
        public boolean tryLock(final long time, final TimeUnit unit)
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.Lock#unlock()
         */
        public void unlock()
        {
            for (Lock lock : this.locks) {
                try {
                    lock.unlock();
                } catch (RuntimeException e) {
                    // We probably don't own this lock so we just move on.
                }
            }
        }
    }
}
