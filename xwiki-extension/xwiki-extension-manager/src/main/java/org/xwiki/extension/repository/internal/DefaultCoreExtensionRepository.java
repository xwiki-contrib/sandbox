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
package org.xwiki.extension.repository.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;

import com.google.common.base.Predicates;

@Component
public class DefaultCoreExtensionRepository extends AbstractLogEnabled implements CoreExtensionRepository,
    Initializable
{
    public static final String COMPONENT_OVERRIDE_LIST = "META-INF/pom.xml";

    private ExtensionRepositoryId repositoryId;

    protected Map<String, CoreExtension> extensions = new ConcurrentHashMap<String, CoreExtension>();

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        this.repositoryId = new ExtensionRepositoryId("core", "xwiki-core", null);

        loadExtensions();
    }

    private void loadExtensions()
    {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setScanners(new ResourcesScanner());
        configurationBuilder.setUrls(ClasspathHelper.getUrlsForCurrentClasspath());

        Reflections reflections = new Reflections(configurationBuilder);

        Set<String> descriptors = reflections.getResources(Predicates.equalTo("pom.xml"));

        for (String descriptor : descriptors) {
            URL descriptorUrl = getClass().getClassLoader().getResource(descriptor);

            // TODO: extract jar URL from descriptorUrl

            InputStream descriptorStream = getClass().getClassLoader().getResourceAsStream(descriptor);
            try {
                CoreExtension coreExtension = new DefaultCoreExtension(this, descriptorUrl, descriptorStream);

                this.extensions.put(coreExtension.getName(), coreExtension);
            } catch (Exception e) {
                getLogger().error("Failed to parse descriptor [" + descriptorUrl + "]", e);
            } finally {
                try {
                    descriptorStream.close();
                } catch (IOException e) {
                    // Should not happen
                    getLogger().error("Failed to close descriptor stream [" + descriptorUrl + "]", e);
                }
            }
        }
    }

    // Repository

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.ExtensionRepository#resolve(org.xwiki.extension.ExtensionId)
     */
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        return getCoreExtension(extensionId.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.ExtensionRepository#exists(org.xwiki.extension.ExtensionId)
     */
    public boolean exists(ExtensionId extensionId)
    {
        return exists(extensionId.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.CoreExtensionRepository#exists(java.lang.String)
     */
    public boolean exists(String name)
    {
        return this.extensions.containsKey(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.ExtensionRepository#getId()
     */
    public ExtensionRepositoryId getId()
    {
        return this.repositoryId;
    }

    // LocalRepository

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.ExtensionRepository#countExtensions()
     */
    public int countExtensions()
    {
        return this.extensions.size();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.CoreExtensionRepository#getCoreExtensions(int, int)
     */
    public List<CoreExtension> getCoreExtensions(int nb, int offset)
    {
        return new ArrayList<CoreExtension>(this.extensions.values()).subList(offset, offset + nb);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.CoreExtensionRepository#getCoreExtension(java.lang.String)
     */
    public CoreExtension getCoreExtension(String name) throws ResolveException
    {
        return this.extensions.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.repository.ExtensionRepository#getExtensions(int, int)
     */
    public List< ? extends CoreExtension> getExtensions(int nb, int offset)
    {
        return getCoreExtensions(nb, offset);
    }
}
