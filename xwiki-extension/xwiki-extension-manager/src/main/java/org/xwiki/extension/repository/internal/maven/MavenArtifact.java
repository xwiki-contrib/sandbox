package org.xwiki.extension.repository.internal.maven;

import org.xwiki.extension.repository.Artifact;
import org.xwiki.extension.repository.ArtifactType;
import org.xwiki.extension.repository.Repository;

public class MavenArtifact implements Artifact
{
    private org.apache.maven.artifact.Artifact artifact;

    private ArtifactType type;

    private MavenRepository repository;

    public MavenArtifact(org.apache.maven.artifact.Artifact artifact, ArtifactType type, MavenRepository repository)
    {
        this.artifact = artifact;
        this.type = type;
        this.repository = repository;
    }

    public String getName()
    {
        return this.artifact.getId();
    }

    public String getVersion()
    {
        return this.artifact.getVersion();
    }

    public ArtifactType getType()
    {
        return type;
    }

    public Repository getRepository()
    {
        return repository;
    }
}
