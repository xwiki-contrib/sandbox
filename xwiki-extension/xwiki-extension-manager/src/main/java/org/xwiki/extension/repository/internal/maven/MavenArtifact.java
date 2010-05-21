package org.xwiki.extension.repository.internal.maven;

import org.xwiki.extension.repository.Artifact;

public class MavenArtifact implements Artifact
{
    org.apache.maven.artifact.Artifact artifact;

    public MavenArtifact(org.apache.maven.artifact.Artifact artifact)
    {
        this.artifact = artifact;
    }

    public String getName()
    {
        return this.artifact.getId();
    }

    public String getVersion()
    {
        return this.artifact.getVersion();
    }
}
