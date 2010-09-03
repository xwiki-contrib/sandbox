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
package org.xwiki.extension.repository.internal.aether;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionType;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.internal.plexus.PlexusComponentManager;

public class AetherExtension implements Extension
{
    private PlexusComponentManager mavenComponentManager;

    private AetherExtensionRepository repository;
    
    private ArtifactDescriptorResult artifactDescriptorResult;

    private ExtensionId artifactId;

    private ExtensionType extensionType;

    private List<ExtensionDependency> dependencies;

    private List<ExtensionId> suggested;

    public AetherExtension(ExtensionId artifactId, ArtifactDescriptorResult artifactDescriptorResult,
        AetherExtensionRepository repository, PlexusComponentManager mavenComponentManager)
        throws ComponentLookupException
    {
        this.mavenComponentManager = mavenComponentManager;

        this.repository = repository;
        
        this.artifactId = artifactId;
        this.artifactDescriptorResult = artifactDescriptorResult;

        if (artifactDescriptorResult.getArtifact().getExtension().equals("jar")) {
            this.extensionType = ExtensionType.JAR;
        } else if (artifactDescriptorResult.getArtifact().getExtension().equals("xar")) {
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
        return null;// return this.project.getDescription();
    }

    public String getWebSite()
    {
        return null;// return this.project.getUrl();
    }

    public ExtensionType getType()
    {
        return this.extensionType;
    }

    public List<ExtensionDependency> getDependencies()
    {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<ExtensionDependency>();

            for (Dependency aetherDependency : this.artifactDescriptorResult.getDependencies()) {
                if (!aetherDependency.isOptional()
                    && (aetherDependency.getScope().equals("compile") || aetherDependency.getScope().equals("runtime") || aetherDependency
                        .getScope().equals("provided"))) {
                    this.dependencies.add(new AetherExtensionDependency(new ExtensionId(aetherDependency.getArtifact()
                        .getGroupId() + ":" + aetherDependency.getArtifact().getArtifactId(), aetherDependency
                        .getArtifact().getVersion())));
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

            for (Dependency mavenDependency : this.artifactDescriptorResult.getDependencies()) {
                if (mavenDependency.isOptional()) {
                    this.suggested.add(new ExtensionId(mavenDependency.getArtifact().getGroupId() + ":"
                        + mavenDependency.getArtifact().getArtifactId(), mavenDependency.getArtifact().getVersion()));
                }
            }
        }

        return this.suggested;
    }

    public void download(File file) throws ExtensionException
    {
        // TODO
    }

    public ExtensionRepository getRepository()
    {
        return this.repository;
    }
}
