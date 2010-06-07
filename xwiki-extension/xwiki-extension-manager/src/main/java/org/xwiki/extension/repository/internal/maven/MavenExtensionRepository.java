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
package org.xwiki.extension.repository.internal.maven;

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.internal.maven.configuration.MavenConfiguration;

public class MavenExtensionRepository implements ExtensionRepository
{
    private ExtensionRepositoryId repositoryId;

    private ArtifactRepository repository;

    private MavenConfiguration mavenConfiguration;

    private MavenComponentManager mavenComponentManager;

    private ProjectBuilder projectBuilder;

    private RepositorySystem repositorySystem;

    public MavenExtensionRepository(ExtensionRepositoryId repositoryId, ArtifactRepository repository,
        MavenConfiguration mavenConfiguration, MavenComponentManager mavenComponentManager)
        throws ComponentLookupException
    {
        this.repositoryId = repositoryId;
        this.repository = repository;

        this.mavenConfiguration = mavenConfiguration;

        this.mavenComponentManager = mavenComponentManager;

        this.projectBuilder = this.mavenComponentManager.getPlexus().lookup(ProjectBuilder.class);
        this.repositorySystem = this.mavenComponentManager.getPlexus().lookup(RepositorySystem.class);
    }

    public ArtifactRepository getRepository()
    {
        return repository;
    }

    public ExtensionRepositoryId getId()
    {
        return this.repositoryId;
    }

    public List<Extension> getExtensions(int nb, int offset)
    {
        // TODO
        return Collections.emptyList();
    }

    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        String groupId = extensionId.getName().substring(0, extensionId.getName().indexOf(':'));
        String artifactId = extensionId.getName().substring(groupId.length() + 1);

        org.apache.maven.artifact.Artifact pomArtifact =
            this.repositorySystem.createProjectArtifact(groupId, artifactId, extensionId.getVersion());

        ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();

        projectBuildingRequest.setRemoteRepositories(Collections.singletonList(this.repository));
        try {
            projectBuildingRequest.setLocalRepository(this.mavenConfiguration.getLocalRepository());
        } catch (InvalidRepositoryException e) {
            throw new ResolveException("Failed to get local maven repository", e);
        }
        // projectBuildingRequest.setRepositoryCache(repositoryCache);
        projectBuildingRequest.setResolveDependencies(false);
        projectBuildingRequest.setOffline(false);
        projectBuildingRequest.setForceUpdate(false);
        // projectBuildingRequest.setTransferListener(transferListener);

        ProjectBuildingResult result;
        try {
            result = this.projectBuilder.build(pomArtifact, projectBuildingRequest);
        } catch (ProjectBuildingException e) {
            throw new ResolveException("Failed to resolve extension [" + extensionId + "]", e);
        }

        return new MavenExtension(extensionId, result.getProject(), this, this.mavenComponentManager);
    }

    public boolean exists(ExtensionId extensionId)
    {
        // TODO
        return false;
    }
}
