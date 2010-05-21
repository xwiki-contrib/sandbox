package org.xwiki.extension.repository;

import java.io.File;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface Repository
{
    RepositoryId getId();

    Artifact findArtifact(ArtifactId actifactId);
}
