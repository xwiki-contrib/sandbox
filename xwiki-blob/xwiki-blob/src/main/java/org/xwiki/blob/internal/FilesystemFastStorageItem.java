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
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.xwiki.blob.BinaryObjectConfiguration;
import org.xwiki.blob.FastStorageItem;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;


/**
 * Filesystem based implementation of FastStorageItem.
 *
 * @version $Id$
 * @since 2.6M1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class FilesystemFastStorageItem implements FastStorageItem
{
    /** The names of all files which back BinaryObjects of this class will begin with this. */
    private static final String FILE_NAME_PREFIX = "BinaryObj_";

    /** Get the configuration so we can get the right directory to put files in. */
    @Requirement
    private BinaryObjectConfiguration config;

    /** The key to allow the same item to be recovered later. */
    private UUID key;

    /** The file which backs this item. */
    private File file;

    /**
     * Constructor with storage location defined.
     * Useful primarily for testing.
     *
     * @param storageDirectory the directory where the file backing this
     *                         FilesystemFastStorageItem will be located.
     */
    public FilesystemFastStorageItem(final File storageDirectory)
    {
        this.config = new DefaultBinaryObjectConfiguration() {
            public File store = storageDirectory;

            public File getStorageDirectory()
            {
                return this.store;
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.FastStorageItem#init()
     */
    public void init(final UUID key)
    {
        this.file = FilesystemFastStorageItem.getFile(this.config.getStorageDirectory(), key);
        this.key = key;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.FastStorageItem#read()
     */
    public InputStream read() throws IOException
    {
        if (this.file.exists()) {
            return new FileInputStream(this.file);
        } else {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.FastStorageItem#write()
     */
    public OutputStream write() throws IOException
    {
        if (this.file == null) {
            throw new IOException("Cannot write to uninitialized StorageItem.");
        }
        return new FileOutputStream(this.file, true);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.FastStorageItem#clear()
     */
    public void clear()
    {
        if (this.file.exists()) {
            this.file.delete();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.FastStorageItem#getKey()
     */
    public UUID getKey()
    {
        return this.key;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.FastStorageItem#size()
     */
    public long size()
    {
        return this.file.length();
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
