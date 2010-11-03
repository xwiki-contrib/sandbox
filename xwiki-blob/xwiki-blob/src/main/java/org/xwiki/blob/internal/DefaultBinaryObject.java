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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.xwiki.blob.BinaryObject;
import org.xwiki.blob.FastStorageItem;
import org.xwiki.blob.StorageItem;


/**
 * Filesystem based BinaryObject.
 *
 * @version $Id$
 * @since 2.6M1
 */
public class DefaultBinaryObject implements BinaryObject
{
    /** This holds the unsaved version, all write actions go to this item. */
    private final FastStorageItem writeStore;

    /** This holds the saved version, all read actions got to this item. */
    private final FastStorageItem readStore;

    /**
     * This holds the data in it's persistent state.
     * To save resources, it is only loaded on first load and only saved when {@link #save()} is called.
     */
    private final StorageItem persistentStore;

    /** A simple which causes concurrent read or write actions to block. */
    private DualItemLock lock = new DualItemLock();

    /**
     * Constructor with underlying StorageItems specified.
     *
     * @param writeStore the storage for the scratch pad which will not be readable until save.
     * @param readStore the storage for content which has been saved and now may be read.
     * @param persistentStore the storage for content which should last.
     * @throws IOException if initialization fails.
     */
    public DefaultBinaryObject(final FastStorageItem writeStore,
                               final FastStorageItem readStore,
                               final StorageItem persistentStore) throws IOException
    {
        this.writeStore = writeStore;
        this.readStore = readStore;
        this.persistentStore = persistentStore;
        this.writeStore.init(UUID.randomUUID());
        this.readStore.init(UUID.randomUUID());
        this.persistentStore.init(UUID.randomUUID());
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
        final DualItemLock dil = this.lock;
        dil.lock(DualItemLock.Action.ADD);
        // Content is always added to temporary storage.
        return new RunOnCloseOutputStream(this.writeStore.write(), new Runnable() {

            private DualItemLock lock = dil;

            public void run()
            {
                this.lock.unlock(DualItemLock.Action.ADD);
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
        this.lock.lock(DualItemLock.Action.CLEAR);

        this.writeStore.clear();

        this.lock.unlock(DualItemLock.Action.CLEAR);
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#save()
     */
    public UUID save() throws IOException
    {
        this.lock.lock(DualItemLock.Action.SAVE);

        this.persistentStore.clear();
        DefaultBinaryObject.copy(this.writeStore, this.persistentStore);
        this.readStore.clear();
        DefaultBinaryObject.copy(this.persistentStore, this.readStore);

        this.lock.unlock(DualItemLock.Action.SAVE);

        return this.persistentStore.getKey();
    }

    /**
     * Copy content of one StorageItem to another.
     *
     * @param from copy content out of this item.
     * @param to copy content into this item.
     * @throws IOException if the copy operation fails.
     */
    private static void copy(final StorageItem from, final StorageItem to) throws IOException
    {
        final OutputStream os = to.write();
        final InputStream is = from.read();
        IOUtils.copy(is, os);
        is.close();
        os.close();
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#load(UUID)
     */
    public void load(final UUID key) throws IOException
    {
        this.lock.lock(DualItemLock.Action.LOAD);
        this.persistentStore.init(key);
        this.readStore.clear();
        DefaultBinaryObject.copy(this.persistentStore, this.readStore);
        this.lock.unlock(DualItemLock.Action.LOAD);
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#getContent()
     */
    public InputStream getContent() throws IOException
    {
        final DualItemLock dil = this.lock;
        dil.lock(DualItemLock.Action.GET);
        // All reads come from readStore. Anything unsaved is not available.
        return new RunOnCloseInputStream(this.readStore.read(), new Runnable() {

            private DualItemLock lock = dil;

            public void run()
            {
                this.lock.unlock(DualItemLock.Action.GET);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObject#size()
     */
    public long size()
    {
        this.lock.lock(DualItemLock.Action.SIZE);
        try {
            return this.readStore.size();
        } finally {
            this.lock.unlock(DualItemLock.Action.SIZE);
        }
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
     * another thread and the first thread will then try to get another stream.
     * This lock will make that request block.
     */
    private static class DualItemLock
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
            LOAD(false, true),

            /** Get the size of the read store. */
            SIZE(false, true);

            /** True if this action uses the temporary file. */
            private final boolean locksWriteStore;

            /** True if this action uses the persistent file. */
            private final boolean locksReadStore;

            /**
             * Private Constructor.
             * Used internally by Action.
             *
             * @param locksWriteStore true if the action uses the temporary file.
             * @param locksReadStore true if this action uses the persistent file.
             */
            private Action(final boolean locksWriteStore, final boolean locksReadStore)
            {
                this.locksWriteStore = locksWriteStore;
                this.locksReadStore = locksReadStore;
            }

            /** @return true if this action uses the temporary file. */
            public boolean locksWriteStore()
            {
                return this.locksWriteStore;
            }

            /** @return true if this action uses the persistent file. */
            public boolean locksReadStore()
            {
                return this.locksReadStore;
            }
        }

        /** True if one of the current actions is using the temporary file. */
        private boolean writeStoreLocked;

        /** True if one of the current actions is using the persistent file. */
        private boolean readStoreLocked;

        /**
         * Will block if a request is make on a resource which is being held.
         *
         * @param action the action which we are locking for (used to give more helpfull exception messages)
         */
        public synchronized void lock(Action action)
        {
            try {
                while ((action.locksWriteStore() && this.writeStoreLocked)
                       || (action.locksReadStore() && this.readStoreLocked))
                {
                    this.wait();
                }
                if (action.locksWriteStore()) {
                    writeStoreLocked = true;
                }
                if (action.locksReadStore()) {
                    readStoreLocked = true;
                }
            } catch (InterruptedException e) {
                // this is not an expected condition.
                throw new RuntimeException("The thread was interrupted while waiting to aquire the lock.");
            }
        }

        /**
         * Unlock the resources.
         * Called when the streams are closed. Whatever resources were locked by the given action are unlocked
         * and any threads waiting are notified.
         *
         * @param action the action which the lock was set for.
         */
        public synchronized void unlock(Action action)
        {
            if (action.locksWriteStore()) {
                writeStoreLocked = false;
            }
            if (action.locksReadStore()) {
                readStoreLocked = false;
            }
            this.notifyAll();
        }
    }
}
