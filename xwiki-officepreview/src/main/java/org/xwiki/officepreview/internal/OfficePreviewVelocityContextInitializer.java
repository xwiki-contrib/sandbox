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
package org.xwiki.officepreview.internal;

import org.apache.velocity.VelocityContext;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.officepreview.OfficePreviewVelocityBridge;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.velocity.VelocityContextInitializer;

/**
 * Initializes velocity contexts with a reference to {@link OfficePreviewVelocityBridge}.
 * 
 * @version $Id$
 * @since 2.2M1
 */
@Component("officepreview")
public class OfficePreviewVelocityContextInitializer extends AbstractLogEnabled implements Initializable,
    VelocityContextInitializer
{
    /**
     * Key used to access the corresponding {@link OfficePreviewVelocityBridge} instance from velocity scripts.
     */
    private static final String VELOCITY_CONTEXT_KEY = "officepreview";

    /**
     * Used to construct {@link OfficePreviewVelocityBridge} instances.
     */
    @Requirement
    private ComponentManager componentManager;

    /**
     * Used to initialize the previews cache.
     */
    @Requirement
    private CacheManager cacheManager;

    /**
     * Cache of office file previews.
     */
    private Cache<XDOM> cache;

    /**
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {
        CacheConfiguration config = new CacheConfiguration();
        LRUEvictionConfiguration lec = new LRUEvictionConfiguration();
        // TODO: Make this configurable.
        lec.setMaxEntries(10);
        config.put(LRUEvictionConfiguration.CONFIGURATIONID, lec);
        try {
            cache = cacheManager.createNewCache(config);
        } catch (CacheException ex) {
            throw new InitializationException("Error while initializing previews cache.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(VelocityContext context)
    {
        try {
            context.put(VELOCITY_CONTEXT_KEY, new OfficePreviewVelocityBridge(componentManager, cache, getLogger()));
        } catch (Exception ex) {
            getLogger().error("Could not initialize office-preview support.", ex);
        }
    }
}
