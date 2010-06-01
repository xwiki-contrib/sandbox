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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.LocalExtensionRepository;

/**
 * TODO: decide local repository format (probably maven-like)
 */
@Component
public class DefaultLocalExtensionRepository implements LocalExtensionRepository, Initializable
{
    private ExtensionRepositoryId repositoryId;

    private File rootFolder;

    public void initialize() throws InitializationException
    {
        // TODO: resolve local repository path/uri based on configuration

        this.repositoryId = new ExtensionRepositoryId("local", "xwiki", uri);
        this.rootFolder = new File(uri);
    }

    public File getRootFolder()
    {
        return this.rootFolder;
    }

    // Repository

    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        return getLocalExtension(extensionId);
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

    public LocalExtension getLocalExtension(ExtensionId extensionId) throws ResolveException
    {
        return getLocalExtension(extensionId.getName(), extensionId.getVersion());
    }

    private LocalExtension getLocalExtension(String name, String version)
    {
        // TODO: search the extension descriptor in the local filesystem
        return null;
    }

    private LocalExtension createExtension(Extension extension, boolean dependency)
    {
        // TODO: create a local extension descriptor and export it in a file in the local repository

        return null;
    }

    public List<Extension> getExtensions(int nb, int offset)
    {
        return (List) getLocalExtensions(nb, offset);
    }

    public void installExtension(Extension extension, boolean dependency) throws InstallException
    {
        LocalExtension localExtension = getLocalExtension(extension.getName(), extension.getVersion());

        if (localExtension == null) {
            localExtension = createExtension(extension, dependency);

            extension.download(localExtension.getFile());
        }
    }

    public void uninstallExtension(LocalExtension extension) throws UninstallException
    {
        // TODO: delete artifact file and descriptor
    }
}
