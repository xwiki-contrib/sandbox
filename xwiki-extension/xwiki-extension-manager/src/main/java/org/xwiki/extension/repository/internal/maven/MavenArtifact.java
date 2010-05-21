package org.xwiki.extension.repository.internal.maven;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.xwiki.extension.repository.Artifact;
import org.xwiki.extension.repository.ArtifactId;
import org.xwiki.extension.repository.ArtifactType;
import org.xwiki.extension.repository.LocalRepository;

public class MavenArtifact implements Artifact
{
    private ArtifactId artifactId;

    private ArtifactType type;

    private org.apache.maven.artifact.Artifact artifact;

    private MavenRepository repository;

    public MavenArtifact(ArtifactId artifactId, org.apache.maven.artifact.Artifact artifact, ArtifactType type,
        MavenRepository repository)
    {
        this.artifact = artifact;
        this.type = type;
        this.repository = repository;
    }

    public ArtifactId getId()
    {
        return this.artifactId;
    }

    public ArtifactType getType()
    {
        return this.type;
    }

    public List<Artifact> getDependencies()
    {
        // TODO
        return Collections.emptyList();
    }

    public void download(LocalRepository localRepository, boolean dependencies)
    {
        File file = localRepository.getFile(this.artifactId);

        download(file);

        for (Artifact artifact : getDependencies()) {
            artifact.download(localRepository, dependencies);
        }
    }

    private void download(File file)
    {
        // TODO
    }
}
