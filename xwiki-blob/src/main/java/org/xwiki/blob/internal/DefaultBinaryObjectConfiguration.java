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
package org.xwiki.blob.internal;

import java.io.File;

import org.xwiki.blob.BinaryObjectConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Get configuration for blob storage.
 *
 * @version $Id$
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class DefaultBinaryObjectConfiguration implements BinaryObjectConfiguration
{
    /** The place where we go to get the configuration. */
    @Requirement
    private ConfigurationSource configSource;

    /**
     * {@inheritDoc}
     *
     * @see BinaryObjectConfiguration#getStorageDirectory()
     */
    public File getStorageDirectory()
    {
        return new File(configSource.getProperty("blob.file.storageDirectory",
                                                 System.getProperty("java.io.tmpdir")));
    }

    /**
     * {@inheritDoc}
     *
     * @see BinaryObjectConfiguration#getCachingStorageHint()
     */
    public String getCachingStorageHint()
    {
        return configSource.getProperty("blob.cachingStorageHint", "default");
    }
}
