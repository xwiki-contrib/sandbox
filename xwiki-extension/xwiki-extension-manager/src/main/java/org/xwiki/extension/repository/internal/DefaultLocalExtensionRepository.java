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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.internal.VersionManager;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.LocalExtensionRepository;

/**
 * <ul>
 * <li>TODO: decide local repository format (probably maven-like)</li>
 * <li>TODO: make it threadsafe bulletproof</li>
 * </ul>
 */
@Component
public class DefaultLocalExtensionRepository implements LocalExtensionRepository, Initializable
{
    @Requirement
    private ExtensionManagerConfiguration configuration;

    @Requirement
    private VersionManager versionManager;

    private ExtensionRepositoryId repositoryId;

    private File rootFolder;

    private Map<String, LocalExtension> extensions = new ConcurrentHashMap<String, LocalExtension>();

    public void initialize() throws InitializationException
    {
        try {
            this.rootFolder = this.configuration.getLocalRepository();
        } catch (IOException e) {
            new InitializationException("Failed to access local repository", e);
        }

        this.repositoryId = new ExtensionRepositoryId("local", "xwiki", this.rootFolder.toURI());

        // TODO: load local extensions from repository
    }

    public File getRootFolder()
    {
        return this.rootFolder;
    }

    // Repository

    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        LocalExtension localExtension = getLocalExtension(extensionId.getName());

        if (localExtension == null) {
            throw new ResolveException("Can't find extension [" + extensionId + "]");
        }

        return localExtension;
    }

    public boolean exists(ExtensionId extensionId)
    {
        // TODO
        return false;
    }

    public ExtensionRepositoryId getId()
    {
        return this.repositoryId;
    }

    // LocalRepository

    public List<LocalExtension> getLocalExtensions(int nb, int offset)
    {
        // TODO
        return Collections.emptyList();
    }

    public LocalExtension getLocalExtension(String name)
    {
        LocalExtension extension = this.extensions.get(name);

        return extension != null ? extension : null;
    }

    private LocalExtension createExtension(Extension extension, boolean dependency)
    {
        DefaultLocalExtension localExtension = new DefaultLocalExtension(this, extension);

        localExtension.setDependency(dependency);

        return localExtension;
    }

    public int countExtensions()
    {
        return this.extensions.size();
    }

    public List< ? extends LocalExtension> getExtensions(int nb, int offset)
    {
        return getLocalExtensions(nb, offset);
    }

    public LocalExtension installExtension(Extension extension, boolean dependency) throws InstallException
    {
        LocalExtension localExtension = getLocalExtension(extension.getName());

        if (localExtension == null
            || this.versionManager.compareVersions(extension.getVersion(), localExtension.getVersion()) != 0) {
            localExtension = createExtension(extension, dependency);

            try {
                extension.download(localExtension.getFile());
            } catch (ExtensionException e) {
                // TODO: clean

                throw new InstallException("Failed to download extension [" + extension + "]", e);
            }
        }

        return localExtension;
    }

    public void uninstallExtension(LocalExtension extension) throws UninstallException
    {
        // TODO: delete artifact file and descriptor
    }
}
