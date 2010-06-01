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

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.xwiki.extension.Extension;
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

    public MavenExtension(ExtensionId artifactId, MavenProject project, MavenExtensionRepository repository,
        MavenComponentManager mavenComponentManager)
    {
        this.project = project;
        this.repository = repository;
        this.mavenComponentManager = mavenComponentManager;
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
        // TODO Auto-generated method stub
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
        List<ExtensionId> dependencies = new ArrayList<ExtensionId>();

        for (Dependency mavenDependency : this.project.getDependencies()) {
            dependencies.add(new ExtensionId(mavenDependency.getGroupId() + ":" + mavenDependency.getArtifactId(),
                mavenDependency.getVersion()));
        }

        return dependencies;
    }

    public void download(File file)
    {
        // TODO
    }

    public ExtensionRepository getRepository()
    {
        return this.repository;
    }
}
