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

import java.io.IOException;

import org.xwiki.blob.BinaryObject;
import org.xwiki.blob.BinaryObjectConfiguration;
import org.xwiki.blob.FastStorageItem;
import org.xwiki.blob.StorageItem;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentLookupException;


/**
 * Filesystem based BinaryObject.
 *
 * @version $Id$
 * @since 2.6M1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class DefaultBinaryObjectProvider extends AbstractLogEnabled
{
    /** Get the componentManager so that new StorageItems can be loaded on demand. */
    @Requirement
    private ComponentManager componentManager;

    /** Allow us to get the role hint for getting FastStorageItems to be used as a cache. */
    @Requirement
    private BinaryObjectConfiguration config;

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.BinaryObjectProvider#get()
     */
    public void get()
    {
        this.get("default");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.blob.BinaryObjectProvider#get(String)
     */
    public BinaryObject get(final String storageItemRoleHint)
    {
        FastStorageItem readStore;
        FastStorageItem writeStore;
        try {
            readStore = this.componentManager.lookup(FastStorageItem.class, this.config.getCachingStorageHint());
            writeStore = this.componentManager.lookup(FastStorageItem.class, this.config.getCachingStorageHint());
        } catch (ComponentLookupException e) {
            this.getLogger().warn("Could not load FastStorageItem with hint: ["
                                  + this.config.getCachingStorageHint() + "] falling back on default.");
            try {
                // Try loading the default item.
                readStore = this.componentManager.lookup(FastStorageItem.class);
                writeStore = this.componentManager.lookup(FastStorageItem.class);
            } catch (ComponentLookupException ee) {
                // Bad day...
                throw new RuntimeException("Failed to lookup default FastStorageItem.", ee);
            }
        }

        final StorageItem persistentStore;
        try {
            persistentStore = this.componentManager.lookup(StorageItem.class, storageItemRoleHint);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to load StorageItem with hint: " + storageItemRoleHint, e);
        }

        try {
            return new DefaultBinaryObject(writeStore, readStore, persistentStore);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize the StorageItems", e);
            // TODO handle this better.
        }
    }
}
