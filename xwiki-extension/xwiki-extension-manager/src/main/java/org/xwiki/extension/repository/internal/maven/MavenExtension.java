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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.WagonManager;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionType;
import org.xwiki.extension.repository.ExtensionRepository;

public class MavenExtension implements Extension
{
    private MavenProject project;

    private ExtensionId artifactId;

    private ExtensionType extensionType;

    private MavenExtensionRepository repository;

    private MavenComponentManager mavenComponentManager;

    private RepositorySystem repositorySystem;

    private List<ExtensionId> dependencies;

    private List<ExtensionId> suggested;

    public MavenExtension(ExtensionId artifactId, MavenProject project, MavenExtensionRepository repository,
        MavenComponentManager mavenComponentManager) throws ComponentLookupException
    {
        this.artifactId = artifactId;
        this.project = project;
        this.repository = repository;

        this.mavenComponentManager = mavenComponentManager;

        this.repositorySystem = this.mavenComponentManager.getPlexus().lookup(RepositorySystem.class);

        if (project.getPackaging().equals("jar")) {
            this.extensionType = ExtensionType.JAR;
        } else if (project.getPackaging().equals("xar")) {
            this.extensionType = ExtensionType.PAGES;
        } else {
            this.extensionType = ExtensionType.UNKNOWN;
        }
    }

    public String getName()
    {
        return this.artifactId.getName();
    }

    public String getVersion()
    {
        return this.artifactId.getVersion();
    }

    public String getAuthor()
    {
        // TODO
        return null;
    }

    public String getDescription()
    {
        return this.project.getDescription();
    }

    public String getWebSite()
    {
        return this.project.getUrl();
    }

    public ExtensionType getType()
    {
        return this.extensionType;
    }

    public List<ExtensionId> getDependencies()
    {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<ExtensionId>();

            for (Dependency mavenDependency : this.project.getDependencies()) {
                if (!mavenDependency.isOptional()
                    && (mavenDependency.getScope().equals("compile") || mavenDependency.getScope().equals("runtime") || mavenDependency
                        .getScope().equals("provided"))) {
                    this.dependencies.add(new ExtensionId(mavenDependency.getGroupId() + ":"
                        + mavenDependency.getArtifactId(), mavenDependency.getVersion()));
                }
            }
        }

        return this.dependencies;
    }

    // IDEA
    public List<ExtensionId> getSuggestedExtensions()
    {
        if (this.suggested == null) {
            this.suggested = new ArrayList<ExtensionId>();

            for (Dependency mavenDependency : this.project.getDependencies()) {
                if (mavenDependency.isOptional()) {
                    this.suggested.add(new ExtensionId(mavenDependency.getGroupId() + ":"
                        + mavenDependency.getArtifactId(), mavenDependency.getVersion()));
                }
            }
        }

        return this.suggested;
    }

    public void download(File file) throws ExtensionException
    {
        try {
            WagonManager wagonManager = this.mavenComponentManager.getPlexus().lookup(WagonManager.class);

            Artifact fileArtifact =
                this.repositorySystem.createArtifact(this.project.getGroupId(), this.project.getArtifactId(),
                    this.project.getVersion(), this.project.getPackaging());

            wagonManager.getRemoteFile(this.repository.getRepository(), file, this.repository.getRepository().pathOf(
                fileArtifact), null/* downloadMonitor */, null/* checksumPolicy */, true);
        } catch (Exception e) {
            throw new ExtensionException("Failed to download extension", e);
        }
    }

    public ExtensionRepository getRepository()
    {
        return this.repository;
    }
}
