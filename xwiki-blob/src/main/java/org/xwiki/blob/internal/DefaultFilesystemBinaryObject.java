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
 *
 */

package org.xwiki.blob.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import org.xwiki.blob.BinaryObject;

/**
 * Filesystem based BinaryObject.
 *
 * @version $Id$
 * @since 2.6M1
 */
public class DefaultFilesystemBinaryObject implements BinaryObject
{
    /** The directory where the file backing this BinaryObject is stored. */
    private final File storageDirectory;

    /** True if the files created by this BinaryObject should be deleted on JVM exit. */
    private final boolean deleteOnExit;

    private FilesystemStorageItem temporaryFile;

    private FilesystemStorageItem persistentFile;

    /**
     * True if clear() has been called since the last save.
     * Used to decide whether the content of the scratchpad should become the persistant content or if the
     * content of the scratchpad needs to be copied on top of the existing persistent content.
     * Initialized to true because the BinaryObject starts off clear.
     */
    private boolean wasCleared = true;

    /** A simple lock providing Exceptions rather than undefined behavior if this class is used concurrently. */
    private TwoFileIOLock lock = new TwoFileIOLock();

    /**
     * The Constructor.
     *
     * @param storageDirectory the directory where the File backing this BinaryObject will be stored.
     * @param temporary true if this file should be deleted on JVM exit.
     */
    public DefaultFilesystemBinaryObject(final File storageDirectory, final boolean temporary)
    {
        this.storageDirectory = storageDirectory;
        this.deleteOnExit = temporary;
        this.temporaryFile = new FilesystemStorageItem(storageDirectory, temporary);
        this.persistentFile = new FilesystemStorageItem(storageDirectory, temporary);
    }

    /*-------------- BinaryObject Methods --------------*/

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#addContent(InputStream)
     */
    public void addContent(final InputStream content) throws IOException
    {
        OutputStream os = this.addContent();
        IOUtils.copy(content, os);
        os.close();
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#addContent()
     */
    public OutputStream addContent() throws IOException
    {
        final TwoFileIOLock ffl = this.lock;
        ffl.lock(TwoFileIOLock.Action.ADD);
        // Content is always added to temporary storage.
        return new RunOnCloseOutputStream(this.temporaryFile.write(true), new Runnable() {

            private TwoFileIOLock lock = ffl;

            public void run()
            {
                this.lock.unlock(TwoFileIOLock.Action.ADD);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#clear()
     */
    public void clear() throws IOException
    {
        this.lock.lock(TwoFileIOLock.Action.CLEAR);

        this.temporaryFile.clear();
        this.wasCleared = true;

        this.lock.unlock(TwoFileIOLock.Action.CLEAR);
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#save()
     */
    public UUID save() throws IOException
    {
        this.lock.lock(TwoFileIOLock.Action.SAVE);

        /*
         * One of 2 things is happening. Either the user wants us to make the content of the scratchpad final, 
         * or the user wants us to append the content of the scratchpad to the content of the persistent storage.
         */
        if (this.wasCleared)
        {
            // Case 1, make this into the final storage. Simply swap the temp into the persistent position.
            this.persistentFile = this.temporaryFile;
            this.temporaryFile = new FilesystemStorageItem(this.storageDirectory, this.deleteOnExit);
            // Since we have just saved, wasCleared (since last save) is now false.
            this.wasCleared = false;
        } else {
            // Case 2, Create a new file, copy the content of the persistent then the temporary file to the
            //         new file, then delete the temporary file and let the old persistent file set in case it
            //         might be loaded again.
            
            final FilesystemStorageItem newPersist =
                new FilesystemStorageItem(this.storageDirectory, this.deleteOnExit);

            final OutputStream os = newPersist.write(false);
            final InputStream is1 = this.persistentFile.read();
            IOUtils.copy(is1, os);
            is1.close();
            final InputStream is2 = this.temporaryFile.read();
            IOUtils.copy(is2, os);
            is2.close();
            os.close();
            this.persistentFile = newPersist;
            this.temporaryFile.clear();
        }

        this.lock.unlock(TwoFileIOLock.Action.SAVE);

        return this.persistentFile.getKey();
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#load(UUID)
     */
    public void load(final UUID key) throws IOException
    {
        this.lock.lock(TwoFileIOLock.Action.LOAD);
        this.persistentFile = new FilesystemStorageItem(this.storageDirectory, this.deleteOnExit, key);
        this.lock.unlock(TwoFileIOLock.Action.LOAD);
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#getContent()
     */
    public InputStream getContent() throws IOException
    {
        final TwoFileIOLock ffl = this.lock;
        ffl.lock(TwoFileIOLock.Action.GET);
        // All reads come from persistentFile. Anything unsaved is not available.
        return new RunOnCloseInputStream(this.persistentFile.read(), new Runnable() {

            private TwoFileIOLock lock = ffl;

            public void run()
            {
                this.lock.unlock(TwoFileIOLock.Action.GET);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#getContent(OutputStream)
     */
    public void getContent(final OutputStream writeTo) throws IOException
    {
        final InputStream is = this.getContent();
        IOUtils.copy(is, writeTo);
        is.close();
    }

    /*-------------- Internal Classes --------------*/

    /**
     * A simple lock which can handle two files, one which is the authority to be read from and one is the
     * scratch pad to be written to.
     * This class differs from the classes in java.util.concurrent.locks because it will lock out the thread
     * who aquired the lock. There is a good chance that a thread might get an OutputStream and hand it off to
     * another thread and the first thread will try to get another stream. This lock will make that request block.
     */
    private static class TwoFileIOLock
    {
        /** Enumerates all actions which can be done on a NonConcurrentFileBinaryObject. */
        public static enum Action
        {
            /** Action representing getting of content from the file. */
            GET(false, true),

            /** Action representing appending content into the file. */
            ADD(true, false),

            /** Action representing clearing the file of content. */
            CLEAR(true, false),

            /** Action which represents saving of a binary object. */
            SAVE(true, true),

            /** Action which represents loading of a binary object. */
            LOAD(false, true);

            /** True if this action uses the temporary file. */
            private final boolean locksTemporaryFile;

            /** True if this action uses the persistent file. */
            private final boolean locksPersistentFile;

            /**
             * Private Constructor.
             * Used internally by Action.
             *
             * @param locksTemporaryFile true if the action uses the temporary file.
             * @param locksPersistentFile true if this action uses the persistent file.
             */
            private Action(final boolean locksTemporaryFile, final boolean locksPersistentFile)
            {
                this.locksTemporaryFile = locksTemporaryFile;
                this.locksPersistentFile = locksPersistentFile;
            }

            /** @return true if this action uses the temporary file. */
            public boolean locksTemporaryFile()
            {
                return this.locksTemporaryFile;
            }

            /** @return true if this action uses the persistent file. */
            public boolean locksPersistentFile()
            {
                return this.locksPersistentFile;
            }
        }

        private boolean temporaryFileLocked;

        private boolean persistentFileLocked;

        /**
         * Will block if a request is make on a resource which is being held.
         *
         * @param action the action which we are locking for (used to give more helpfull exception messages)
         * @throws IOException if the lock is already being held.
         */
        public synchronized void lock(Action action) throws IOException
        {
            try {
                while ((action.locksTemporaryFile() && this.temporaryFileLocked)
                       || (action.locksPersistentFile() && this.persistentFileLocked)) 
                {
                    this.wait();
                }
                if (action.locksTemporaryFile()) {
                    temporaryFileLocked = true;
                }
                if (action.locksPersistentFile()) {
                    persistentFileLocked = true;
                }
            } catch (InterruptedException e) {
                // this is not an expected condition.
                throw new RuntimeException("The thread was interrupted while waiting to aquire the lock.");
            }
        }

        /** Called when the streams are closed. */
        public synchronized void unlock(Action action)
        {
            if (action.locksTemporaryFile()) {
                temporaryFileLocked = false;
            }
            if (action.locksPersistentFile()) {
                persistentFileLocked = false;
            }
            this.notify();
        }
    }

    private static class FilesystemStorageItem
    {
        /** The names of all files which back BinaryObjects of this class will begin with this. */
        private static final String FILE_NAME_PREFIX = "BinaryObj_";

        private final UUID key;

        private final File file;

        public FilesystemStorageItem(final File storageDirectory, final boolean deleteOnExit)
        {
            this(storageDirectory, deleteOnExit, UUID.randomUUID());
        }

        public FilesystemStorageItem(final File storageDirectory, final boolean deleteOnExit, final UUID key)
        {
            this.key = key;
            this.file = this.getFile(storageDirectory, key);
            if (deleteOnExit) {
                this.file.deleteOnExit();
            }
        }

        public InputStream read() throws FileNotFoundException
        {
            if (this.file.exists()) {
                return new FileInputStream(this.file);
            } else {
                return new ByteArrayInputStream(new byte[0]);
            }
        }

        public OutputStream write(final boolean append) throws FileNotFoundException
        {
            return new FileOutputStream(this.file, append);
        }

        public void clear()
        {
            if (this.file.exists()) {
                this.file.delete();
            }
        }

        public UUID getKey()
        {
            return this.key;
        }

        /**
         * @param storageDirectory the directory to store the files in.
         * @param storageKey the UUID to get the correct file.
         * @return the file.
         */
        private static File getFile(final File storageDirectory, final UUID storageKey)
        {
            return new File(storageDirectory, FILE_NAME_PREFIX + storageKey.toString());
        }
    }
}
