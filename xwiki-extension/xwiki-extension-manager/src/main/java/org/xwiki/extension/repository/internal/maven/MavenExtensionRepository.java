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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.WagonManager;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.internal.maven.configuration.MavenConfiguration;

@ComponentRole
public class MavenExtensionRepository implements ExtensionRepository
{
    @Requirement
    private MavenConfiguration mavenConfiguration;
    
    private ExtensionRepositoryId repositoryId;

    private MavenComponentManager mavenComponentManager;

    private ProjectBuilder projectBuilder;

    private RepositorySystem repositorySystem;

    private org.apache.maven.artifact.repository.ArtifactRepository repository;

    public MavenExtensionRepository(ExtensionRepositoryId repositoryId,
        org.apache.maven.artifact.repository.ArtifactRepository repository, MavenComponentManager mavenComponentManager)
    {
        this.repositoryId = repositoryId;
        this.mavenComponentManager = mavenComponentManager;
        this.repository = repository;

        this.projectBuilder = this.mavenComponentManager.getPlexus().lookup(ProjectBuilder.class);
    }

    public ExtensionRepositoryId getId()
    {
        return this.repositoryId;
    }

    public List<Extension> getExtensions(int nb, int offset)
    {
        // TODO
        return null;
    }

    public Extension resolve(ExtensionId extensionId)
    {
        Extension artifact = null;

        // TODO: parse actifactId id to get group and artifact ids

        org.apache.maven.artifact.Artifact pomArtifact =
            this.repositorySystem.createProjectArtifact(groupId, artifactId, extensionId.getVersion());

        ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();

        projectBuildingRequest.setRemoteRepositories(Collections.singletonList(this.repository));
        projectBuildingRequest.setLocalRepository(this.mavenConfiguration.getLocalRepository());
        projectBuildingRequest.setRepositoryCache(repositoryCache);
        projectBuildingRequest.setResolveDependencies(false);
        projectBuildingRequest.setOffline(false);
        projectBuildingRequest.setForceUpdate(false);
        projectBuildingRequest.setTransferListener(transferListener);

        ProjectBuildingResult result = this.projectBuilder.build(pomArtifact, projectBuildingRequest);

        return new MavenExtension(extensionId, result.getProject(), this, this.mavenComponentManager);
    }
    
    public boolean exists(ExtensionId extensionId)
    {
        // TODO
        return false;
    }
}
