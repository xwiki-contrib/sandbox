package org.xwiki.extension.repository;

public interface RepositoryManager
{
    void addRepository(RepositoryId repositoryId);

    void removeRepository(RepositoryId repositoryId);

    Artifact findArtifact(ArtifactId actifactId);
}
