package org.xwiki.extension.repository;

import java.util.List;

public interface Artifact
{
    ArtifactId getId();

    ArtifactType getType();

    List<Artifact> getDependencies();

    void download(LocalRepository localRepository, boolean dependencies);
}
