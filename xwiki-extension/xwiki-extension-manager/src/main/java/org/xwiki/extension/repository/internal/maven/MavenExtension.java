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
