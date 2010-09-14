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
package org.xwiki.extension.internal;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.install.ExtensionInstaller;
import org.xwiki.extension.install.ExtensionInstallerException;
import org.xwiki.extension.install.ExtensionInstallerManager;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.ExtensionRepositorySource;
import org.xwiki.extension.repository.LocalExtensionRepository;

/**
 * TODO: cut installation process in steps (create and validate an install plan, install, etc.)
 */
@Component
public class DefaultExtensionManager extends AbstractLogEnabled implements ExtensionManager, Initializable
{
    @Requirement
    private ExtensionRepositoryManager repositoryManager;

    @Requirement(role = ExtensionRepositorySource.class)
    private List<ExtensionRepositorySource> repositoriesSources;

    @Requirement
    private CoreExtensionRepository coreExtensionRepository;

    @Requirement
    private LocalExtensionRepository localExtensionRepository;

    @Requirement
    private ComponentManager componentManager;

    @Requirement
    private VersionManager versionManager;

    @Requirement
    private ExtensionInstallerManager extensionInstallerManager;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        // Load extension repositories
        for (ExtensionRepositorySource repositoriesSource : this.repositoriesSources) {
            for (ExtensionRepositoryId repositoryId : repositoriesSource.getExtensionRepositories()) {
                try {
                    this.repositoryManager.addRepository(repositoryId);
                } catch (ExtensionRepositoryException e) {
                    getLogger().error("Failed to add repository [" + repositoryId + "]", e);
                }
            }
        }

        // Load extensions from local repository
        List<LocalExtension> localExtensions = this.localExtensionRepository.getLocalExtensions();
        for (LocalExtension localExtension : localExtensions) {
            try {
                // TODO: validate dependencies
                installExtension(localExtension);
            } catch (Exception e) {
                getLogger().error("Failed to install local extension [" + localExtension + "]", e);
            }
        }
    }

    public int coundAvailableExtensions()
    {
        // TODO
        return 0;
    }

    public int coundInstalledExtensions()
    {
        // TODO
        return 0;
    }

    public List<Extension> getAvailableExtensions(int nb, int offset)
    {
        // TODO
        return null;
    }

    public List< ? extends Extension> getInstalledExtensions(int nb, int offset)
    {
        return this.localExtensionRepository.getExtensions(nb, offset);
    }

    public LocalExtension installExtension(ExtensionId extensionId) throws InstallException
    {
        if (this.coreExtensionRepository.exists(extensionId.getId())) {
            throw new InstallException("[" + extensionId.getId() + "]: core extension");
        }

        LocalExtension localExtension = this.localExtensionRepository.getLocalExtension(extensionId.getId());

        if (localExtension != null) {
            int diff = this.versionManager.compareVersions(extensionId.getVersion(), localExtension.getVersion());

            if (diff == 0) {
                throw new InstallException("[" + extensionId.getId() + "]: already installed");
            } else if (diff < 0) {
                throw new InstallException("[" + extensionId.getId() + "]: a more recent version is already installed");
            }
        }

        return installExtension(extensionId, false);

    }

    private LocalExtension installExtension(ExtensionId extensionId, boolean dependency) throws InstallException
    {
        // Resolve extension
        Extension remoteExtension;
        try {
            remoteExtension = this.repositoryManager.resolve(extensionId);
        } catch (ResolveException e) {
            throw new InstallException("Failed to resolve extension [" + extensionId + "]", e);
        }

        if (remoteExtension == null) {
            throw new InstallException("Failed to resolve extension [" + extensionId + "]");
        }

        try {
            return installExtension(remoteExtension, dependency);
        } catch (Exception e) {
            throw new InstallException("Failed to install extension", e);
        }
    }

    private LocalExtension installExtension(Extension remoteExtension, boolean dependency)
        throws ComponentLookupException, InstallException, ExtensionInstallerException
    {
        for (ExtensionDependency dependencyDependency : remoteExtension.getDependencies()) {
            ExtensionId dependencyId = new ExtensionId(dependencyDependency.getId(), dependencyDependency.getVersion());

            if (!this.coreExtensionRepository.exists(dependencyId.getId())
                && !this.localExtensionRepository.exists(dependencyId)) {
                installExtension(dependencyId, true);
            }
        }

        // Store extension in local repository
        LocalExtension localExtension = this.localExtensionRepository.installExtension(remoteExtension, dependency);

        this.extensionInstallerManager.install(localExtension);

        return localExtension;
    }

    private LocalExtension installExtension(LocalExtension localExtension) throws ComponentLookupException,
        ExtensionInstallerException
    {
        // Load extension
        ExtensionInstaller extensionInstaller =
            this.componentManager.lookup(ExtensionInstaller.class, localExtension.getType().toString().toLowerCase());

        extensionInstaller.install(localExtension);

        return localExtension;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.ExtensionManager#uninstallExtension(java.lang.String)
     */
    public void uninstallExtension(String name) throws UninstallException
    {
        LocalExtension localExtension = this.localExtensionRepository.getLocalExtension(name);

        if (localExtension == null) {
            throw new UninstallException("[" + name + "]: extension is not installed");
        }

        try {
            uninstallExtension(localExtension);
        } catch (Exception e) {
            throw new UninstallException("Failed to uninstall extension", e);
        }
    }

    public void uninstallExtension(LocalExtension localExtension) throws ComponentLookupException,
        ExtensionInstallerException, UninstallException
    {
        // Unload extension
        ExtensionInstaller extensionInstaller =
            this.componentManager.lookup(ExtensionInstaller.class, localExtension.getType().toString().toLowerCase());

        extensionInstaller.install(localExtension);

        // Remove from local repository
        this.localExtensionRepository.uninstallExtension(localExtension);
    }

    public Extension resolveExtension(ExtensionId extensionId) throws ResolveException
    {
        Extension extension = null;

        extension = this.coreExtensionRepository.getCoreExtension(extensionId.getId());

        if (extension == null) {
            extension = this.localExtensionRepository.getLocalExtension(extensionId.getId());

            if (extension == null) {
                extension = this.repositoryManager.resolve(extensionId);
            }
        }

        return extension;
    }
}
