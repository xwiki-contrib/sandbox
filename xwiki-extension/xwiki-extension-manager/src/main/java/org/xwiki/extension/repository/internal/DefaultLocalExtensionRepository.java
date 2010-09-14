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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
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
public class DefaultLocalExtensionRepository extends AbstractLogEnabled implements LocalExtensionRepository,
    Initializable
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
        this.rootFolder = this.configuration.getLocalRepository();

        this.repositoryId = new ExtensionRepositoryId("local", "xwiki", this.rootFolder.toURI());

        loadExtensions();
    }

    // TODO: define real extension descriptor instead of looking at files names
    private void loadExtensions()
    {
        File rootFolder = getRootFolder();

        if (rootFolder.exists()) {
            for (File child : rootFolder.listFiles()) {
                if (!child.isDirectory()) {
                    String fileName = child.getName();

                    int versionIndex = fileName.lastIndexOf('-');
                    int dotIndex = fileName.lastIndexOf('.');

                    if (versionIndex != -1 && dotIndex != -1) {
                        String name = fileName.substring(0, versionIndex);
                        String version = fileName.substring(versionIndex + 1, dotIndex);
                        String type = fileName.substring(dotIndex + 1, fileName.length());

                        LocalExtension existingExtension = this.extensions.get(name);

                        if (existingExtension == null
                            || this.versionManager.compareVersions(existingExtension.getVersion(), version) < 0) {
                            LocalExtension localExtension = new DefaultLocalExtension(this, name, version, type);

                            this.extensions.put(name, localExtension);
                        }
                    } else {
                        getLogger().warn("Invalid file name [" + child + "]");
                    }
                }
            }
        }
    }

    public File getRootFolder()
    {
        return this.rootFolder;
    }

    // Repository

    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        LocalExtension localExtension = getLocalExtension(extensionId.getId());

        if (localExtension == null
            || (extensionId.getVersion() != null && !localExtension.getVersion().equals(extensionId.getVersion()))) {
            throw new ResolveException("Can't find extension [" + extensionId + "]");
        }

        return localExtension;
    }

    public boolean exists(ExtensionId extensionId)
    {
        LocalExtension localExtension = getLocalExtension(extensionId.getId());

        if (localExtension == null
            || (extensionId.getVersion() != null && !localExtension.getVersion().equals(extensionId.getVersion()))) {
            return false;
        }

        return true;
    }

    public ExtensionRepositoryId getId()
    {
        return this.repositoryId;
    }

    // LocalRepository

    public List<LocalExtension> getLocalExtensions()
    {
        return new ArrayList<LocalExtension>(this.extensions.values());
    }

    public LocalExtension getLocalExtension(String id)
    {
        LocalExtension extension = this.extensions.get(id);

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
        return getLocalExtensions().subList(offset, offset + nb);
    }

    public LocalExtension installExtension(Extension extension, boolean dependency) throws InstallException
    {
        LocalExtension localExtension = getLocalExtension(extension.getId());

        if (localExtension == null || !extension.getVersion().equals(localExtension.getVersion())) {
            localExtension = createExtension(extension, dependency);

            try {
                extension.download(localExtension.getFile());
                this.extensions.put(localExtension.getId(), localExtension);
            } catch (ExtensionException e) {
                // TODO: clean

                throw new InstallException("Failed to download extension [" + extension + "]", e);
            }
        }

        return localExtension;
    }

    public void uninstallExtension(LocalExtension extension) throws UninstallException
    {
        extension.getFile().delete();

        LocalExtension existingExtension = getLocalExtension(extension.getId());

        if (existingExtension == extension) {
            this.extensions.remove(extension.getId());
        }
    }
}
